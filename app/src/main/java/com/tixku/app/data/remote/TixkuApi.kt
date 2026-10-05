package com.tixku.app.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface mirroring the Tixku REST API (docs/openapi.yaml).
 * Each group of endpoints corresponds to a backend microservice.
 */
interface TixkuApi {

    // ---- Cities ----
    @GET("cities")
    suspend fun listCities(): List<CityDto>

    // ---- Search Service ----
    @GET("search/movies")
    suspend fun searchMovies(
        @Query("cityId") cityId: String,
        @Query("q") query: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): MoviePageDto

    // ---- Movie Service ----
    @GET("movies/{movieId}")
    suspend fun getMovie(@Path("movieId") movieId: String): MovieDto

    // ---- Theatre + Show Service ----
    @GET("movies/{movieId}/shows")
    suspend fun getShowsForMovie(
        @Path("movieId") movieId: String,
        @Query("cityId") cityId: String,
    ): List<CinemaShowsDto>

    @GET("shows/{showId}")
    suspend fun getShow(@Path("showId") showId: String): ShowDetailDto

    @GET("shows/{showId}/seats")
    suspend fun getSeatMap(@Path("showId") showId: String): List<SeatDto>

    // ---- Booking Service (seat locking) ----
    @POST("shows/{showId}/locks")
    suspend fun lockSeats(
        @Path("showId") showId: String,
        @Body body: LockRequestDto,
    ): LockResponseDto

    @DELETE("shows/{showId}/locks/{lockId}")
    suspend fun releaseLock(
        @Path("showId") showId: String,
        @Path("lockId") lockId: String,
    )

    @GET("bookings")
    suspend fun listBookings(): List<BookingDto>

    @GET("bookings/{bookingId}")
    suspend fun getBooking(@Path("bookingId") bookingId: String): BookingDto

    @POST("bookings")
    suspend fun confirmBooking(@Body body: ConfirmBookingRequestDto): BookingDto

    @POST("bookings/{bookingId}/cancel")
    suspend fun cancelBooking(@Path("bookingId") bookingId: String): BookingDto

    // ---- Payment Service ----
    @POST("payments")
    suspend fun createPayment(@Body body: CreatePaymentRequestDto): PaymentDto

    @GET("payments/{paymentId}")
    suspend fun getPayment(@Path("paymentId") paymentId: String): PaymentDto
}
