package com.tixku.app.data

import java.util.UUID

/**
 * Offline implementation backed by in-memory seed data (MockData). Stands in for
 * the backend when BuildConfig.USE_REMOTE_API is false. Implements the same
 * atomic seat-locking + TTL semantics as the real Booking Service.
 */
class LocalDataSource : TixkuDataSource {

    private val seatLockTtlMs = 5 * 60 * 1000L
    private val rows = 6
    private val cols = 10

    private val bookedSeats = mutableMapOf<String, MutableSet<String>>()
    // showId -> (seatId -> expiry)
    private val seatLocks = mutableMapOf<String, MutableMap<String, Long>>()
    // lockId -> SeatLock
    private val activeLocks = mutableMapOf<String, SeatLock>()
    private val bookingsStore = mutableListOf<Booking>()

    override suspend fun cities(): List<City> = MockData.cities

    override suspend fun searchMovies(cityId: String, query: String): List<Movie> {
        val cinemaIds = MockData.cinemas.filter { it.cityId == cityId }.map { it.id }.toSet()
        val movieIds = MockData.shows.filter { it.cinemaId in cinemaIds }.map { it.movieId }.toSet()
        return MockData.movies
            .filter { it.id in movieIds }
            .filter { m ->
                query.isBlank() ||
                    m.title.contains(query, true) ||
                    m.genres.any { it.contains(query, true) } ||
                    m.language.contains(query, true)
            }
    }

    override suspend fun getMovie(movieId: String): Movie? =
        MockData.movies.firstOrNull { it.id == movieId }

    override suspend fun getShowsForMovie(movieId: String, cityId: String): List<CinemaShows> {
        return MockData.cinemas
            .filter { it.cityId == cityId }
            .map { cinema ->
                CinemaShows(
                    cinema = cinema,
                    shows = MockData.shows
                        .filter { it.movieId == movieId && it.cinemaId == cinema.id }
                        .sortedBy { it.startTime },
                )
            }
            .filter { it.shows.isNotEmpty() }
    }

    override suspend fun getShow(showId: String): ShowDetail? {
        val show = MockData.shows.firstOrNull { it.id == showId } ?: return null
        val movie = getMovie(show.movieId) ?: return null
        val cinema = MockData.cinemas.firstOrNull { it.id == show.cinemaId } ?: return null
        return ShowDetail(show, movie, cinema)
    }

    override suspend fun getSeatMap(showId: String, heldByMe: Set<String>): List<Seat> {
        purgeExpired(showId)
        val booked = bookedSeats[showId] ?: emptySet<String>()
        val locked = seatLocks[showId]?.keys ?: emptySet<String>()
        return buildList {
            for (r in 0 until rows) {
                val row = 'A' + r
                for (n in 1..cols) {
                    val id = "$row$n"
                    val status = when {
                        id in booked -> SeatStatus.BOOKED
                        id in heldByMe -> SeatStatus.LOCKED
                        id in locked -> SeatStatus.LOCKED
                        else -> SeatStatus.AVAILABLE
                    }
                    add(Seat(id, row, n, status))
                }
            }
        }
    }

    override suspend fun lockSeats(showId: String, seatIds: Set<String>): SeatLock? {
        purgeExpired(showId)
        val booked = bookedSeats[showId] ?: emptySet<String>()
        val locks = seatLocks.getOrPut(showId) { mutableMapOf() }
        if (seatIds.any { it in booked || locks.containsKey(it) }) return null
        val expiry = System.currentTimeMillis() + seatLockTtlMs
        seatIds.forEach { locks[it] = expiry }
        val lock = SeatLock(
            lockId = "lock_" + UUID.randomUUID().toString().take(6),
            showId = showId,
            seatIds = seatIds,
            expiresAtEpochMs = expiry,
        )
        activeLocks[lock.lockId] = lock
        return lock
    }

    override suspend fun releaseLock(showId: String, lock: SeatLock) {
        seatLocks[showId]?.let { l -> lock.seatIds.forEach { l.remove(it) } }
        activeLocks.remove(lock.lockId)
    }

    override suspend fun createPayment(lock: SeatLock, amount: Int, method: String): PaymentResult {
        // Local simulation always succeeds.
        return PaymentResult(
            paymentId = "pay_" + UUID.randomUUID().toString().take(6),
            status = PaymentStatus.SUCCESS,
            amount = amount,
        )
    }

    override suspend fun confirmBooking(showId: String, lock: SeatLock, payment: PaymentResult): Booking? {
        if (payment.status != PaymentStatus.SUCCESS) {
            releaseLock(showId, lock)
            return null
        }
        // Lock must still be valid (not expired).
        if (activeLocks[lock.lockId] == null || lock.expiresAtEpochMs < System.currentTimeMillis()) {
            return null
        }
        val detail = getShow(showId) ?: return null
        val booked = bookedSeats.getOrPut(showId) { mutableSetOf() }
        booked.addAll(lock.seatIds)
        releaseLock(showId, lock)

        val id = "TIX-" + UUID.randomUUID().toString().take(8).uppercase()
        val booking = Booking(
            id = id,
            movieTitle = detail.movie.title,
            cinemaName = detail.cinema.name,
            showTime = detail.show.startTime,
            format = detail.show.format,
            seatIds = lock.seatIds.sorted(),
            totalPrice = detail.show.price * lock.seatIds.size,
            createdAtEpochMs = System.currentTimeMillis(),
            status = BookingStatus.CONFIRMED,
            qrPayload = "TIXKU|$id|${detail.movie.title}|${detail.cinema.name}|${detail.show.startTime}|${lock.seatIds.sorted().joinToString(",")}",
        )
        bookingsStore.add(0, booking)
        return booking
    }

    override suspend fun listBookings(): List<Booking> = bookingsStore.toList()

    override suspend fun getBooking(bookingId: String): Booking? =
        bookingsStore.firstOrNull { it.id == bookingId }

    override suspend fun cancelBooking(bookingId: String): Booking? {
        val idx = bookingsStore.indexOfFirst { it.id == bookingId }
        if (idx < 0) return null
        val cancelled = bookingsStore[idx].copy(status = BookingStatus.CANCELLED)
        bookingsStore[idx] = cancelled
        // Free the seats.
        val show = MockData.shows.firstOrNull {
            getMovieTitle(it.movieId) == cancelled.movieTitle && it.startTime == cancelled.showTime
        }
        if (show != null) bookedSeats[show.id]?.removeAll(cancelled.seatIds.toSet())
        return cancelled
    }

    private fun getMovieTitle(movieId: String) = MockData.movies.firstOrNull { it.id == movieId }?.title

    private fun purgeExpired(showId: String) {
        val now = System.currentTimeMillis()
        seatLocks[showId]?.entries?.removeAll { it.value < now }
        activeLocks.entries.removeAll { it.value.expiresAtEpochMs < now }
    }
}
