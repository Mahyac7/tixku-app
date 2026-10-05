package com.tixku.app.data.remote

import com.tixku.app.data.Booking
import com.tixku.app.data.BookingStatus
import com.tixku.app.data.Cinema
import com.tixku.app.data.City
import com.tixku.app.data.Movie
import com.tixku.app.data.Seat
import com.tixku.app.data.SeatStatus
import com.tixku.app.data.Show
import com.tixku.app.data.ShowFormat

/** Converters between network DTOs and domain models. */

fun CityDto.toDomain() = City(id = id, name = name)

fun MovieDto.toDomain() = Movie(
    id = id,
    title = title,
    genres = genres,
    language = language,
    durationMinutes = durationMinutes,
    rating = rating,
    synopsis = synopsis,
    cast = cast,
    posterColor = posterColor.toColorLong(),
    certification = certification,
)

fun CinemaDto.toDomain() = Cinema(id = id, name = name, cityId = cityId, address = address)

fun String.toShowFormat(): ShowFormat = when (uppercase()) {
    "THREE_D", "3D" -> ShowFormat.THREE_D
    "IMAX" -> ShowFormat.IMAX
    else -> ShowFormat.TWO_D
}

fun ShowFormat.toWire(): String = when (this) {
    ShowFormat.TWO_D -> "TWO_D"
    ShowFormat.THREE_D -> "THREE_D"
    ShowFormat.IMAX -> "IMAX"
}

fun ShowDto.toDomain() = Show(
    id = id,
    movieId = movieId,
    cinemaId = cinemaId,
    startTime = startTime,
    format = format.toShowFormat(),
    price = price,
)

fun ShowDetailDto.toShow() = Show(
    id = id,
    movieId = movieId,
    cinemaId = cinemaId,
    startTime = startTime,
    format = format.toShowFormat(),
    price = price,
)

fun SeatDto.toDomain() = Seat(
    id = id,
    row = row.firstOrNull() ?: 'A',
    number = number,
    status = when (status.uppercase()) {
        "BOOKED" -> SeatStatus.BOOKED
        "LOCKED" -> SeatStatus.LOCKED
        else -> SeatStatus.AVAILABLE
    },
)

fun BookingDto.toDomain() = Booking(
    id = id,
    movieTitle = movieTitle,
    cinemaName = cinemaName,
    showTime = showTime,
    format = format.toShowFormat(),
    seatIds = seatIds,
    totalPrice = totalPrice,
    createdAtEpochMs = System.currentTimeMillis(),
    status = if (status.equals("CANCELLED", true)) BookingStatus.CANCELLED else BookingStatus.CONFIRMED,
    qrPayload = qrPayload,
)

/** Parses a hex ARGB string like "FFE53935" into a Long color value. */
private fun String.toColorLong(): Long = try {
    removePrefix("#").removePrefix("0x").toLong(16)
} catch (e: NumberFormatException) {
    0xFFE53935
}
