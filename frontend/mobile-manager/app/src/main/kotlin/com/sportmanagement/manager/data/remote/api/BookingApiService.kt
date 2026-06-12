package com.sportmanagement.manager.data.remote.api

import com.sportmanagement.manager.data.remote.dto.BookingActionResponse
import com.sportmanagement.manager.data.remote.dto.BookingCancelRequest
import com.sportmanagement.manager.data.remote.dto.BookingDto
import com.sportmanagement.manager.data.remote.dto.BookingHistoryResponse
import com.sportmanagement.manager.data.remote.dto.BookingRejectRequest
import com.sportmanagement.manager.data.remote.dto.BookingRescheduleRequest
import com.sportmanagement.manager.data.remote.dto.BookingRescheduleResponse
import com.sportmanagement.manager.data.remote.dto.CreateBookingRequest
import com.sportmanagement.manager.data.remote.dto.CreateBookingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface BookingApiService {

    @POST("api/manager/bookings")
    suspend fun createBooking(@Body request: CreateBookingRequest): Response<CreateBookingResponse>

    @GET("api/manager/bookings")
    suspend fun getBookings(
        @Query("status") status: String? = null,
        @Query("field_id") fieldId: Int? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<List<BookingDto>>

    @GET("api/manager/bookings/{id}")
    suspend fun getBooking(@Path("id") id: Int): Response<BookingDto>

    @PUT("api/manager/bookings/{id}/approve")
    suspend fun approveBooking(@Path("id") id: Int): Response<BookingActionResponse>

    @PUT("api/manager/bookings/{id}/reject")
    suspend fun rejectBooking(
        @Path("id") id: Int,
        @Body request: BookingRejectRequest
    ): Response<BookingActionResponse>

    @PUT("api/manager/bookings/{id}/cancel")
    suspend fun cancelBooking(
        @Path("id") id: Int,
        @Body request: BookingCancelRequest
    ): Response<BookingActionResponse>

    @PUT("api/manager/bookings/{id}/complete")
    suspend fun completeBooking(@Path("id") id: Int): Response<BookingActionResponse>

    @PUT("api/manager/bookings/{id}/reschedule")
    suspend fun rescheduleBooking(
        @Path("id") id: Int,
        @Body request: BookingRescheduleRequest
    ): Response<BookingRescheduleResponse>

    @GET("api/manager/bookings/{id}/history")
    suspend fun getBookingHistory(@Path("id") id: Int): Response<BookingHistoryResponse>
}
