package com.tixku.app.data

import com.tixku.app.data.remote.ConfirmBookingRequestDto
import com.tixku.app.data.remote.CreatePaymentRequestDto
import com.tixku.app.data.remote.LockRequestDto
import com.tixku.app.data.remote.TixkuApi
import com.tixku.app.data.remote.toDomain
import com.tixku.app.data.remote.toShow
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.time.Instant
import java.time.format.DateTimeParseException

/**
 * REST implementation that talks to the Tixku backend via Retrofit.
 * Maps network DTOs to domain models and translates HTTP errors (e.g. 409 on a
 * seat conflict) into the same semantics the UI already expects.
 */
class RemoteDataSource(private val api: TixkuApi) : TixkuDataSource {

    override suspend fun cities(): List<City> = api.listCities().map { it.toDomain() }

    override suspend fun searchMovies(cityId: String, query: String): List<Movie> =
        api.searchMovies(cityId = cityId, query = query.ifBlank { null })
            .content.map { it.toDomain() }

    override suspend fun getMovie(movieId: String): Movie? =
        runCatching { api.getMovie(movieId).toDomain() }.getOrNull()

    override suspend fun getShowsForMovie(movieId: String, cityId: String): List<CinemaShows> =
        api.getShowsForMovie(movieId, cityId).map { dto ->
            CinemaShows(
                cinema = dto.cinema.toDomain(),
                shows = dto.shows.map { it.toDomain() },
            )
        }

    override suspend fun getShow(showId: String): ShowDetail? {
        val dto = runCatching { api.getShow(showId) }.getOrNull() ?: return null
        val show = dto.toShow()
        val movie = dto.movie?.toDomain() ?: getMovie(dto.movieId) ?: return null
        // Cinema may be embedded; otherwise we cannot resolve it from this endpoint alone.
        val cinema = dto.cinema?.toDomain() ?: return null
        return ShowDetail(show, movie, cinema)
    }

    override suspend fun getSeatMap(showId: String, heldByMe: Set<String>): List<Seat> {
        val seats = api.getSeatMap(showId).map { it.toDomain() }
        // Seats the current user is holding locally render as LOCKED (their selection).
        return seats.map { if (it.id in heldByMe) it.copy(status = SeatStatus.LOCKED) else it }
    }

    override suspend fun lockSeats(showId: String, seatIds: Set<String>): SeatLock? {
        return try {
            val res = api.lockSeats(showId, LockRequestDto(seatIds.toList()))
            SeatLock(
                lockId = res.lockId,
                showId = res.showId,
                seatIds = res.seatIds.toSet(),
                expiresAtEpochMs = res.expiresAt.toEpochMs(),
            )
        } catch (e: HttpException) {
            if (e.code() == 409) null else throw e // 409 = seat taken
        }
    }

    override suspend fun releaseLock(showId: String, lock: SeatLock) {
        runCatching { api.releaseLock(showId, lock.lockId) }
    }

    override suspend fun createPayment(lock: SeatLock, amount: Int, method: String): PaymentResult {
        val created = api.createPayment(CreatePaymentRequestDto(lock.lockId, amount, method))
        var payment = created
        // Poll until the payment reaches a terminal state (simulating gateway webhook).
        var attempts = 0
        while (payment.status.equals("PENDING", true) && attempts < 10) {
            delay(800)
            payment = api.getPayment(created.id)
            attempts++
        }
        return PaymentResult(
            paymentId = payment.id,
            status = when (payment.status.uppercase()) {
                "SUCCESS" -> PaymentStatus.SUCCESS
                "FAILED" -> PaymentStatus.FAILED
                else -> PaymentStatus.PENDING
            },
            amount = payment.amount,
        )
    }

    override suspend fun confirmBooking(showId: String, lock: SeatLock, payment: PaymentResult): Booking? {
        if (payment.status != PaymentStatus.SUCCESS) {
            releaseLock(showId, lock)
            return null
        }
        return try {
            api.confirmBooking(ConfirmBookingRequestDto(lock.lockId, payment.paymentId)).toDomain()
        } catch (e: HttpException) {
            if (e.code() == 402 || e.code() == 409) null else throw e
        }
    }

    override suspend fun listBookings(): List<Booking> = api.listBookings().map { it.toDomain() }

    override suspend fun getBooking(bookingId: String): Booking? =
        runCatching { api.getBooking(bookingId).toDomain() }.getOrNull()

    override suspend fun cancelBooking(bookingId: String): Booking? =
        runCatching { api.cancelBooking(bookingId).toDomain() }.getOrNull()

    private fun String.toEpochMs(): Long = try {
        Instant.parse(this).toEpochMilli()
    } catch (e: DateTimeParseException) {
        System.currentTimeMillis() + 5 * 60 * 1000L
    }
}
