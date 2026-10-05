package com.tixku.app.data

import com.tixku.app.BuildConfig
import com.tixku.app.data.remote.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Facade the UI talks to. It delegates to either [RemoteDataSource] (REST backend)
 * or [LocalDataSource] (offline mock) based on BuildConfig.USE_REMOTE_API, and
 * caches the bookings list in a StateFlow the booking screens observe.
 *
 * This mirrors the system design: screens are backend-agnostic, and swapping the
 * data source is the only change needed to go from mock to the real API Gateway.
 */
object TixkuRepository {

    private val source: TixkuDataSource =
        if (BuildConfig.USE_REMOTE_API) RemoteDataSource(NetworkModule.api)
        else LocalDataSource()

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    suspend fun cities(): List<City> = source.cities()

    suspend fun searchMovies(cityId: String, query: String): List<Movie> =
        source.searchMovies(cityId, query)

    suspend fun getMovie(movieId: String): Movie? = source.getMovie(movieId)

    suspend fun getShowsForMovie(movieId: String, cityId: String): List<CinemaShows> =
        source.getShowsForMovie(movieId, cityId)

    suspend fun getShow(showId: String): ShowDetail? = source.getShow(showId)

    suspend fun getSeatMap(showId: String, heldByMe: Set<String> = emptySet()): List<Seat> =
        source.getSeatMap(showId, heldByMe)

    suspend fun lockSeats(showId: String, seatIds: Set<String>): SeatLock? =
        source.lockSeats(showId, seatIds)

    suspend fun releaseLock(showId: String, lock: SeatLock) = source.releaseLock(showId, lock)

    suspend fun createPayment(lock: SeatLock, amount: Int, method: String): PaymentResult =
        source.createPayment(lock, amount, method)

    /** Runs the full payment + confirm flow and refreshes the cached bookings. */
    suspend fun confirmBooking(showId: String, lock: SeatLock, payment: PaymentResult): Booking? {
        val booking = source.confirmBooking(showId, lock, payment)
        if (booking != null) refreshBookings()
        return booking
    }

    suspend fun refreshBookings() {
        _bookings.value = source.listBookings()
    }

    suspend fun getBooking(bookingId: String): Booking? =
        bookings.value.firstOrNull { it.id == bookingId } ?: source.getBooking(bookingId)

    suspend fun cancelBooking(bookingId: String): Booking? {
        val result = source.cancelBooking(bookingId)
        refreshBookings()
        return result
    }
}
