package com.sportmanagement.manager.data.repository

import com.sportmanagement.manager.data.remote.api.DashboardApiService
import com.sportmanagement.manager.data.remote.dto.DashboardStatsDto
import com.sportmanagement.manager.data.remote.dto.MonthlyRevenueDto
import com.sportmanagement.manager.data.remote.dto.RevenueTrendPointDto

class DashboardRepository(private val api: DashboardApiService) {

    suspend fun getStats(): Result<DashboardStatsDto> = safeCall {
        val response = api.getStats()
        if (response.isSuccessful) {
            Result.success(response.body() ?: DashboardStatsDto())
        } else {
            Result.failure(Exception("Lỗi tải dashboard (${response.code()})"))
        }
    }

    suspend fun getMonthlyRevenue(year: Int? = null): Result<List<MonthlyRevenueDto>> = safeCall {
        val response = api.getMonthlyRevenue(year)
        if (response.isSuccessful) {
            Result.success(response.body() ?: emptyList())
        } else {
            Result.failure(Exception("Lỗi tải doanh thu"))
        }
    }

    suspend fun getRevenueTrend(period: String): Result<List<RevenueTrendPointDto>> = safeCall {
        val response = api.getRevenueTrend(period)
        if (response.isSuccessful) {
            Result.success(response.body() ?: emptyList())
        } else {
            Result.failure(Exception("Lỗi tải xu hướng doanh thu"))
        }
    }

    private suspend fun <T> safeCall(block: suspend () -> Result<T>): Result<T> {
        return try {
            block()
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối đến máy chủ"))
        }
    }
}
