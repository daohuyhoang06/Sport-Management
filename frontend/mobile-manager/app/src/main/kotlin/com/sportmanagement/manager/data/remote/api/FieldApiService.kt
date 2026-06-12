package com.sportmanagement.manager.data.remote.api

import com.sportmanagement.manager.data.remote.dto.ActionResponse
import com.sportmanagement.manager.data.remote.dto.BlockedSlotsResponse
import com.sportmanagement.manager.data.remote.dto.CourtsResponse
import com.sportmanagement.manager.data.remote.dto.CreateBlockedSlotRequest
import com.sportmanagement.manager.data.remote.dto.CreateCourtRequest
import com.sportmanagement.manager.data.remote.dto.CreatePolicyRequest
import com.sportmanagement.manager.data.remote.dto.CreateServiceRequest
import com.sportmanagement.manager.data.remote.dto.UpdateBasicInfoRequest
import com.sportmanagement.manager.data.remote.dto.UpdateCourtRequest
import com.sportmanagement.manager.data.remote.dto.CreateFieldRequest
import com.sportmanagement.manager.data.remote.dto.FieldListResponse
import com.sportmanagement.manager.data.remote.dto.FieldReviewDto
import com.sportmanagement.manager.data.remote.dto.FieldResponse
import com.sportmanagement.manager.data.remote.dto.FieldReviewStatsResponse
import com.sportmanagement.manager.data.remote.dto.FieldStatsResponse
import com.sportmanagement.manager.data.remote.dto.PoliciesResponse
import com.sportmanagement.manager.data.remote.dto.ServicesResponse
import com.sportmanagement.manager.data.remote.dto.UpdateFieldStatusRequest
import com.sportmanagement.manager.data.remote.dto.UploadImageResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import com.sportmanagement.manager.data.remote.dto.AvailabilityResponse
import retrofit2.http.Query
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface FieldApiService {

    @Multipart
    @POST("api/manager/upload/field-image")
    suspend fun uploadFieldImage(@Part image: MultipartBody.Part): Response<UploadImageResponse>

    @GET("api/manager/fields")
    suspend fun getFields(): Response<FieldListResponse>

    @GET("api/manager/fields/{id}")
    suspend fun getField(@Path("id") id: Int): Response<FieldResponse>

    @GET("api/manager/fields/{id}/stats")
    suspend fun getFieldStats(@Path("id") id: Int): Response<FieldStatsResponse>

    @GET("api/user/reviews/stats/{fieldId}")
    suspend fun getFieldReviewStats(@Path("fieldId") fieldId: Int): Response<FieldReviewStatsResponse>

    @GET("api/user/reviews")
    suspend fun getFieldReviews(@Query("field_id") fieldId: Int): Response<List<FieldReviewDto>>

    @POST("api/manager/fields")
    suspend fun createField(@Body request: CreateFieldRequest): Response<FieldResponse>

    @PUT("api/manager/fields/{id}")
    suspend fun updateField(
        @Path("id") id: Int,
        @Body request: CreateFieldRequest
    ): Response<FieldResponse>

    @PATCH("api/manager/fields/{id}")
    suspend fun patchField(
        @Path("id") id: Int,
        @Body request: UpdateBasicInfoRequest
    ): Response<ActionResponse>

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

    @GET("api/manager/fields/{fieldId}/courts/{courtId}/availability")
    suspend fun getCourtAvailability(
        @Path("fieldId") fieldId: Int,
        @Path("courtId") courtId: Int,
        @Query("date") date: String
    ): Response<AvailabilityResponse>

    @POST("api/manager/fields/{id}/courts")
    suspend fun createCourt(
        @Path("id") id: Int,
        @Body request: CreateCourtRequest
    ): Response<ActionResponse>

    @PUT("api/manager/fields/{id}/courts/{courtId}")
    suspend fun updateCourt(
        @Path("id") id: Int,
        @Path("courtId") courtId: Int,
        @Body request: UpdateCourtRequest
    ): Response<ActionResponse>

    @DELETE("api/manager/fields/{id}/courts/{courtId}")
    suspend fun deleteCourt(
        @Path("id") id: Int,
        @Path("courtId") courtId: Int
    ): Response<ActionResponse>

    // Services
    @GET("api/manager/fields/{id}/services")
    suspend fun getServices(@Path("id") id: Int): Response<ServicesResponse>

    @POST("api/manager/fields/{id}/services")
    suspend fun createService(
        @Path("id") id: Int,
        @Body request: CreateServiceRequest
    ): Response<ActionResponse>

    @DELETE("api/manager/fields/{id}/services/{serviceId}")
    suspend fun deleteService(
        @Path("id") id: Int,
        @Path("serviceId") serviceId: Int
    ): Response<ActionResponse>

    // Policies
    @GET("api/manager/fields/{id}/policies")
    suspend fun getPolicies(@Path("id") id: Int): Response<PoliciesResponse>

    @POST("api/manager/fields/{id}/policies")
    suspend fun createPolicy(
        @Path("id") id: Int,
        @Body request: CreatePolicyRequest
    ): Response<ActionResponse>

    @DELETE("api/manager/fields/{id}/policies/{policyId}")
    suspend fun deletePolicy(
        @Path("id") id: Int,
        @Path("policyId") policyId: Int
    ): Response<ActionResponse>

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
