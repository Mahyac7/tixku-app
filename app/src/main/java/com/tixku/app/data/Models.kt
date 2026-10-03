package com.tixku.app.data

/**
 * Domain models for Tixku. These mirror the "Key Entities" from the BookMyShow
 * system design: User, Movie, Cinema, Screen, Seat, Show, Booking, Payment.
 */

data class City(
    val id: String,
    val name: String,
)

enum class ShowFormat { TWO_D, THREE_D, IMAX }

data class Movie(
    val id: String,
    val title: String,
    val genres: List<String>,
    val language: String,
    val durationMinutes: Int,
    val rating: Double,
    val synopsis: String,
    val cast: List<String>,
    // A pleasant gradient placeholder color (ARGB) used in place of a poster image.
    val posterColor: Long,
    val certification: String, // e.g. "R13+", "SU", "D17+"
)

data class Cinema(
    val id: String,
    val name: String,
    val cityId: String,
    val address: String,
)

/**
 * A scheduled screening of a movie at a cinema.
 */
data class Show(
    val id: String,
    val movieId: String,
    val cinemaId: String,
    val startTime: String, // e.g. "13:00"
    val format: ShowFormat,
    val price: Int, // price per seat in IDR
)

enum class SeatStatus { AVAILABLE, LOCKED, BOOKED }

data class Seat(
    val id: String,     // e.g. "A1"
    val row: Char,
    val number: Int,
    val status: SeatStatus,
)

data class Booking(
    val id: String,
    val movieTitle: String,
    val cinemaName: String,
    val showTime: String,
    val format: ShowFormat,
    val seatIds: List<String>,
    val totalPrice: Int,
    val createdAtEpochMs: Long,
    val status: BookingStatus,
    val qrPayload: String,
)

enum class BookingStatus { CONFIRMED, CANCELLED }

fun ShowFormat.label(): String = when (this) {
    ShowFormat.TWO_D -> "2D"
    ShowFormat.THREE_D -> "3D"
    ShowFormat.IMAX -> "IMAX"
}
