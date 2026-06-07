package com.sportmanagement.manager.data.remote.api

import com.sportmanagement.manager.data.remote.dto.ActionResponse
import com.sportmanagement.manager.data.remote.dto.BlockedSlotsResponse
import com.sportmanagement.manager.data.remote.dto.CourtsResponse
import com.sportmanagement.manager.data.remote.dto.CreateBlockedSlotRequest
import com.sportmanagement.manager.data.remote.dto.CreateCourtRequest
import com.sportmanagement.manager.data.remote.dto.CreateFieldRequest
import com.sportmanagement.manager.data.remote.dto.FieldListResponse
import com.sportmanagement.manager.data.remote.dto.FieldResponse
import com.sportmanagement.manager.data.remote.dto.PoliciesResponse
import com.sportmanagement.manager.data.remote.dto.ServicesResponse
import com.sportmanagement.manager.data.remote.dto.UpdateFieldStatusRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface FieldApiService {

    @GET("api/manager/fields")
    suspend fun getFields(): Response<FieldListResponse>

    @GET("api/manager/fields/{id}")
    suspend fun getField(@Path("id") id: Int): Response<FieldResponse>

    @POST("api/manager/fields")
    suspend fun createField(@Body request: CreateFieldRequest): Response<FieldResponse>

    @PUT("api/manager/fields/{id}")
    suspend fun updateField(
        @Path("id") id: Int,
        @Body request: CreateFieldRequest
    ): Response<FieldResponse>

    @PUT("api/manager/fields/{id}/status")
    suspend fun updateFieldStatus(
        @Path("id") id: Int,
        @Body request: UpdateFieldStatusRequest
    ): Response<ActionResponse>

    @DELETE("api/manager/fields/{id}")
    suspend fun deleteField(@Path("id") id: Int): Response<ActionResponse>

    // Courts
    @GET("api/manager/fields/{id}/courts")
    suspend fun getCourts(@Path("id") id: Int): Response<CourtsResponse>

    @POST("api/manager/fields/{id}/courts")
    suspend fun createCourt(
        @Path("id") id: Int,
        @Body request: CreateCourtRequest
    ): Response<CourtsResponse>

    // Services
    @GET("api/manager/fields/{id}/services")
    suspend fun getServices(@Path("id") id: Int): Response<ServicesResponse>

    // Policies
    @GET("api/manager/fields/{id}/policies")
    suspend fun getPolicies(@Path("id") id: Int): Response<PoliciesResponse>

    // Blocked slots
    @GET("api/manager/fields/{id}/blocked-slots")
    suspend fun getBlockedSlots(@Path("id") id: Int): Response<BlockedSlotsResponse>

    @POST("api/manager/fields/{id}/blocked-slots")
    suspend fun createBlockedSlot(
        @Path("id") id: Int,
        @Body request: CreateBlockedSlotRequest
    ): Response<ActionResponse>

    @DELETE("api/manager/fields/{id}/blocked-slots/{slotId}")
    suspend fun deleteBlockedSlot(
        @Path("id") id: Int,
        @Path("slotId") slotId: Int
    ): Response<ActionResponse>
}
