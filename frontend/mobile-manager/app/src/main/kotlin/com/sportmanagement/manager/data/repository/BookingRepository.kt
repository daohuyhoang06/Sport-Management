package com.sportmanagement.manager.data.repository

import com.sportmanagement.manager.data.remote.api.BookingApiService
import com.sportmanagement.manager.data.remote.dto.BookingCancelRequest
import com.sportmanagement.manager.data.remote.dto.BookingDto
import com.sportmanagement.manager.data.remote.dto.BookingHistoryDto
import com.sportmanagement.manager.data.remote.dto.BookingRejectRequest
import com.sportmanagement.manager.data.remote.dto.CreateBookingRequest

class BookingRepository(private val api: BookingApiService) {

    suspend fun getBookings(
        status: String? = null,
        fieldId: Int? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Result<List<BookingDto>> = safeCall {
        val response = api.getBookings(status = status, fieldId = fieldId, startDate = startDate, endDate = endDate)
        if (response.isSuccessful) {
            Result.success(response.body() ?: emptyList())
        } else {
            Result.failure(Exception("Lỗi tải danh sách đặt sân (${response.code()})"))
        }
    }

    suspend fun approveBooking(id: Int): Result<Unit> = safeCall {
        val response = api.approveBooking(id)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Xác nhận booking thất bại"))
    }

    suspend fun rejectBooking(id: Int, reason: String? = null): Result<Unit> = safeCall {
        val response = api.rejectBooking(id, BookingRejectRequest(reason))
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Từ chối booking thất bại"))
    }

    suspend fun cancelBooking(id: Int, reason: String? = null): Result<Unit> = safeCall {
        val response = api.cancelBooking(id, BookingCancelRequest(reason))
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Hủy booking thất bại"))
    }

    suspend fun completeBooking(id: Int): Result<Unit> = safeCall {
        val response = api.completeBooking(id)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Hoàn thành booking thất bại"))
    }

    suspend fun createBooking(request: CreateBookingRequest): Result<BookingDto> = safeCall {
        val response = api.createBooking(request)
        if (response.isSuccessful) {
            val data = response.body()?.data
            if (data != null) Result.success(data)
            else Result.failure(Exception("Tạo booking thất bại"))
        } else {
            Result.failure(Exception("Tạo booking thất bại (${response.code()})"))
        }
    }

    suspend fun getBookingHistory(id: Int): Result<List<BookingHistoryDto>> = safeCall {
        val response = api.getBookingHistory(id)
        if (response.isSuccessful) Result.success(response.body()?.data ?: emptyList())
        else Result.failure(Exception("Lỗi tải lịch sử booking"))
    }

    private suspend fun <T> safeCall(block: suspend () -> Result<T>): Result<T> {
        return try {
            block()
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối đến máy chủ"))
        }
    }
}
