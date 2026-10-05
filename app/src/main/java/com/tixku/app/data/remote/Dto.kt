package com.tixku.app.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Objects matching the Tixku REST API (see docs/openapi.yaml).
 * These are intentionally separate from the domain models in data/Models.kt so
 * the wire format can evolve independently of the UI layer.
 */

@JsonClass(generateAdapter = true)
data class CityDto(val id: String, val name: String)

@JsonClass(generateAdapter = true)
data class MovieDto(
    val id: String,
    val title: String,
    val genres: List<String>,
    val language: String,
    val durationMinutes: Int,
    val rating: Double,
    val synopsis: String,
    val cast: List<String>,
    val posterUrl: String? = null,
    val posterColor: String, // hex ARGB, e.g. "FFE53935"
    val certification: String,
)

@JsonClass(generateAdapter = true)
data class MoviePageDto(
    val content: List<MovieDto>,
    val page: Int = 0,
    val size: Int = 20,
    val totalElements: Int = 0,
)

@JsonClass(generateAdapter = true)
data class CinemaDto(
    val id: String,
    val name: String,
    val cityId: String,
    val address: String,
)

@JsonClass(generateAdapter = true)
data class ShowDto(
    val id: String,
    val movieId: String,
    val cinemaId: String,
    val startTime: String,
    val format: String, // TWO_D | THREE_D | IMAX
    val price: Int,
)

@JsonClass(generateAdapter = true)
data class ShowDetailDto(
    val id: String,
    val movieId: String,
    val cinemaId: String,
    val startTime: String,
    val format: String,
    val price: Int,
    val movie: MovieDto? = null,
    val cinema: CinemaDto? = null,
)

@JsonClass(generateAdapter = true)
data class CinemaShowsDto(
    val cinema: CinemaDto,
    val shows: List<ShowDto>,
)

@JsonClass(generateAdapter = true)
data class SeatDto(
    val id: String,
    val row: String,
    val number: Int,
    val status: String, // AVAILABLE | LOCKED | BOOKED
)

@JsonClass(generateAdapter = true)
data class LockRequestDto(val seatIds: List<String>)

@JsonClass(generateAdapter = true)
data class LockResponseDto(
    val lockId: String,
    val showId: String,
    val seatIds: List<String>,
    val expiresAt: String,
)

@JsonClass(generateAdapter = true)
data class CreatePaymentRequestDto(
    val lockId: String,
    val amount: Int,
    val method: String,
)

@JsonClass(generateAdapter = true)
data class PaymentDto(
    val id: String,
    val status: String, // PENDING | SUCCESS | FAILED
    val amount: Int,
    val redirectUrl: String? = null,
)

@JsonClass(generateAdapter = true)
data class ConfirmBookingRequestDto(
    val lockId: String,
    val paymentId: String,
)

@JsonClass(generateAdapter = true)
data class BookingDto(
    val id: String,
    val movieTitle: String,
    val cinemaName: String,
    val showTime: String,
    val format: String,
    val seatIds: List<String>,
    val totalPrice: Int,
    val createdAt: String,
    val status: String, // CONFIRMED | CANCELLED
    val qrPayload: String,
)

@JsonClass(generateAdapter = true)
data class ErrorDto(val code: String, val message: String)
