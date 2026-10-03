package com.tixku.app.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Single in-memory repository that stands in for the backend microservices from
 * the system design (Search, Movie, Theatre, Show, Booking, Payment).
 *
 * The seat-locking logic simulates the Redis-based temporary lock with a TTL
 * that is central to solving the "concurrency + seat locking + consistency"
 * challenge: a seat can only be held by one booking flow at a time, and the
 * lock expires if the user does not complete payment in time.
 */
object TixkuRepository {

    private const val SEAT_LOCK_TTL_MS = 5 * 60 * 1000L // 5 minutes
    private const val ROWS = 6
    private const val COLS = 10

    // showId -> (seatId -> status). Represents persisted BOOKED seats.
    private val bookedSeats = mutableMapOf<String, MutableSet<String>>()

    // showId -> (seatId -> lockExpiryEpochMs). Represents the Redis seat locks.
    private val seatLocks = mutableMapOf<String, MutableMap<String, Long>>()

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    // ---- Search Service ----
    fun searchMovies(cityId: String, query: String): List<Movie> {
        val cinemaIds = MockData.cinemas.filter { it.cityId == cityId }.map { it.id }.toSet()
        val movieIdsInCity = MockData.shows
            .filter { it.cinemaId in cinemaIds }
            .map { it.movieId }
            .toSet()
        return MockData.movies
            .filter { it.id in movieIdsInCity }
            .filter { m ->
                query.isBlank() ||
                    m.title.contains(query, ignoreCase = true) ||
                    m.genres.any { it.contains(query, ignoreCase = true) } ||
                    m.language.contains(query, ignoreCase = true)
            }
    }

    // ---- Movie Service ----
    fun getMovie(movieId: String): Movie? = MockData.movies.firstOrNull { it.id == movieId }

    // ---- Theatre + Show Service ----
    fun getShowsForMovie(movieId: String, cityId: String): Map<Cinema, List<Show>> {
        val cinemasInCity = MockData.cinemas.filter { it.cityId == cityId }
        return cinemasInCity.associateWith { cinema ->
            MockData.shows.filter { it.movieId == movieId && it.cinemaId == cinema.id }
                .sortedBy { it.startTime }
        }.filterValues { it.isNotEmpty() }
    }

    fun getShow(showId: String): Show? = MockData.shows.firstOrNull { it.id == showId }

    fun getCinema(cinemaId: String): Cinema? = MockData.cinemas.firstOrNull { it.id == cinemaId }

    // ---- Booking Service: seat map with live lock/booked status ----
    fun getSeatMap(showId: String, heldByMe: Set<String> = emptySet()): List<Seat> {
        purgeExpiredLocks(showId)
        val booked = bookedSeats[showId] ?: emptySet<String>()
        val locked = seatLocks[showId]?.keys ?: emptySet<String>()
        return buildList {
            for (r in 0 until ROWS) {
                val row = ('A' + r)
                for (n in 1..COLS) {
                    val id = "$row$n"
                    val status = when {
                        id in booked -> SeatStatus.BOOKED
                        id in heldByMe -> SeatStatus.LOCKED
                        id in locked -> SeatStatus.LOCKED
                        else -> SeatStatus.AVAILABLE
                    }
                    add(Seat(id = id, row = row, number = n, status = status))
                }
            }
        }
    }

    /**
     * Attempts to atomically acquire locks on the requested seats for the current
     * booking flow. Returns true only if ALL requested seats were free; otherwise
     * no seat is locked. This is the single-writer guarantee from the design.
     */
    fun lockSeats(showId: String, seatIds: Set<String>): Boolean {
        purgeExpiredLocks(showId)
        val booked = bookedSeats[showId] ?: emptySet<String>()
        val locks = seatLocks.getOrPut(showId) { mutableMapOf() }
        val now = System.currentTimeMillis()
        // Reject if any seat is already booked or locked by someone else.
        val anyTaken = seatIds.any { it in booked || locks.containsKey(it) }
        if (anyTaken) return false
        seatIds.forEach { locks[it] = now + SEAT_LOCK_TTL_MS }
        return true
    }

    fun releaseSeats(showId: String, seatIds: Set<String>) {
        seatLocks[showId]?.let { locks -> seatIds.forEach { locks.remove(it) } }
    }

    // ---- Payment Service + confirmation ----
    fun confirmBooking(
        showId: String,
        seatIds: Set<String>,
        paymentSuccess: Boolean,
    ): Booking? {
        if (!paymentSuccess) {
            releaseSeats(showId, seatIds)
            return null
        }
        val show = getShow(showId) ?: return null
        val movie = getMovie(show.movieId) ?: return null
        val cinema = getCinema(show.cinemaId) ?: return null

        // Promote locked seats to permanently booked.
        val booked = bookedSeats.getOrPut(showId) { mutableSetOf() }
        booked.addAll(seatIds)
        releaseSeats(showId, seatIds)

        val id = "TIX-" + UUID.randomUUID().toString().take(8).uppercase()
        val booking = Booking(
            id = id,
            movieTitle = movie.title,
            cinemaName = cinema.name,
            showTime = show.startTime,
            format = show.format,
            seatIds = seatIds.sorted(),
            totalPrice = show.price * seatIds.size,
            createdAtEpochMs = System.currentTimeMillis(),
            status = BookingStatus.CONFIRMED,
            qrPayload = "TIXKU|$id|${movie.title}|${cinema.name}|${show.startTime}|${seatIds.sorted().joinToString(",")}",
        )
        _bookings.value = listOf(booking) + _bookings.value
        return booking
    }

    fun cancelBooking(bookingId: String) {
        _bookings.value = _bookings.value.map {
            if (it.id == bookingId) it.copy(status = BookingStatus.CANCELLED) else it
        }
        // Free up the seats so they can be rebooked.
        val b = _bookings.value.firstOrNull { it.id == bookingId } ?: return
        val show = MockData.shows.firstOrNull {
            getMovie(it.movieId)?.title == b.movieTitle && it.startTime == b.showTime
        }
        if (show != null) bookedSeats[show.id]?.removeAll(b.seatIds.toSet())
    }

    private fun purgeExpiredLocks(showId: String) {
        val now = System.currentTimeMillis()
        seatLocks[showId]?.entries?.removeAll { it.value < now }
    }
}
