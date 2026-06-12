package com.sportmanagement.manager.data.remote.api

import com.sportmanagement.manager.data.remote.dto.DashboardStatsDto
import com.sportmanagement.manager.data.remote.dto.MonthlyRevenueDto
import com.sportmanagement.manager.data.remote.dto.RevenueTrendPointDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DashboardApiService {

    @GET("api/manager/dashboard/stats")
    suspend fun getStats(): Response<DashboardStatsDto>

    @GET("api/manager/dashboard/monthly-revenue")
    suspend fun getMonthlyRevenue(
        @Query("year") year: Int? = null
    ): Response<List<MonthlyRevenueDto>>

    @GET("api/manager/dashboard/revenue-trend")
    suspend fun getRevenueTrend(
        @Query("period") period: String
    ): Response<List<RevenueTrendPointDto>>
}
