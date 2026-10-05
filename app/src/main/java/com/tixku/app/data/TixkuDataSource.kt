package com.tixku.app.data

/**
 * Abstraction over the data layer so the UI is agnostic to whether data comes
 * from the REST backend (RemoteDataSource) or local seed data (LocalDataSource).
 * All methods are suspend because the remote implementation performs network IO.
 */
interface TixkuDataSource {

    suspend fun cities(): List<City>

    suspend fun searchMovies(cityId: String, query: String): List<Movie>

    suspend fun getMovie(movieId: String): Movie?

    suspend fun getShowsForMovie(movieId: String, cityId: String): List<CinemaShows>

    suspend fun getShow(showId: String): ShowDetail?

    suspend fun getSeatMap(showId: String, heldByMe: Set<String> = emptySet()): List<Seat>

    /** Atomically locks the requested seats. Returns a lock on success, null on conflict. */
    suspend fun lockSeats(showId: String, seatIds: Set<String>): SeatLock?

    suspend fun releaseLock(showId: String, lock: SeatLock)

    /** Creates a payment intent for a lock. */
    suspend fun createPayment(lock: SeatLock, amount: Int, method: String): PaymentResult

    /** Confirms the booking after a successful payment. Returns null if payment failed. */
    suspend fun confirmBooking(showId: String, lock: SeatLock, payment: PaymentResult): Booking?

    suspend fun listBookings(): List<Booking>

    suspend fun getBooking(bookingId: String): Booking?

    suspend fun cancelBooking(bookingId: String): Booking?
}

/** Convenience aggregate returned by getShowsForMovie. */
data class CinemaShows(val cinema: Cinema, val shows: List<Show>)

/** Show plus its related movie and cinema. */
data class ShowDetail(val show: Show, val movie: Movie, val cinema: Cinema)

/** A held seat lock with its id and expiry (TTL). */
data class SeatLock(
    val lockId: String,
    val showId: String,
    val seatIds: Set<String>,
    val expiresAtEpochMs: Long,
)

enum class PaymentStatus { PENDING, SUCCESS, FAILED }

data class PaymentResult(
    val paymentId: String,
    val status: PaymentStatus,
    val amount: Int,
)
