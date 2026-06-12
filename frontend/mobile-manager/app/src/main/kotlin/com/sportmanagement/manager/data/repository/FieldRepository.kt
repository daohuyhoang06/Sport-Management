package com.sportmanagement.manager.data.repository

import com.sportmanagement.manager.data.remote.api.FieldApiService
import com.sportmanagement.manager.data.remote.dto.BlockedSlotDto
import com.sportmanagement.manager.data.remote.dto.CreateBlockedSlotRequest
import com.sportmanagement.manager.data.remote.dto.CreateCourtRequest
import com.sportmanagement.manager.data.remote.dto.CreatePolicyRequest
import com.sportmanagement.manager.data.remote.dto.CreateServiceRequest
import com.sportmanagement.manager.data.remote.dto.BookedRangeDto
import com.sportmanagement.manager.data.remote.dto.FieldCourtDto
import com.sportmanagement.manager.data.remote.dto.UpdateBasicInfoRequest
import com.sportmanagement.manager.data.remote.dto.UpdateCourtRequest
import com.sportmanagement.manager.data.remote.dto.FieldDto
import com.sportmanagement.manager.data.remote.dto.FieldPolicyDto
import com.sportmanagement.manager.data.remote.dto.FieldServiceDto
import com.sportmanagement.manager.data.remote.dto.UpdateFieldStatusRequest
import okhttp3.MultipartBody

class FieldRepository(private val api: FieldApiService) {

    suspend fun uploadFieldImage(imagePart: MultipartBody.Part): Result<String> = safeCall {
        val response = api.uploadFieldImage(imagePart)
        if (response.isSuccessful) {
            val url = response.body()?.url
                ?: return@safeCall Result.failure(Exception("Upload ảnh thất bại: không nhận được URL"))
            Result.success(url)
        } else {
            Result.failure(Exception("Upload ảnh thất bại (${response.code()})"))
        }
    }

    suspend fun createField(request: com.sportmanagement.manager.data.remote.dto.CreateFieldRequest): Result<FieldDto> = safeCall {
        val response = api.createField(request)
        if (response.isSuccessful) {
            val data = response.body()?.data
                ?: return@safeCall Result.failure(Exception("Tạo sân thất bại: không có dữ liệu trả về"))
            Result.success(data)
        } else {
            Result.failure(Exception("Tạo sân thất bại (${response.code()})"))
        }
    }

    suspend fun getFields(): Result<List<FieldDto>> = safeCall {
        val response = api.getFields()
        if (response.isSuccessful) {
            Result.success(response.body()?.data ?: emptyList())
        } else {
            Result.failure(Exception("Lỗi tải danh sách sân (${response.code()})"))
        }
    }

    suspend fun updateBasicInfo(id: Int, request: UpdateBasicInfoRequest): Result<Unit> = safeCall {
        val response = api.patchField(id, request)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Cập nhật thông tin thất bại (${response.code()})"))
    }

    suspend fun updateFieldStatus(id: Int, status: String): Result<Unit> = safeCall {
        val response = api.updateFieldStatus(id, UpdateFieldStatusRequest(status))
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Cập nhật trạng thái thất bại"))
    }

    suspend fun deleteField(id: Int): Result<Unit> = safeCall {
        val response = api.deleteField(id)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Xóa sân thất bại"))
    }

    suspend fun getCourts(fieldId: Int): Result<List<FieldCourtDto>> = safeCall {
        val response = api.getCourts(fieldId)
        if (response.isSuccessful) {
            Result.success(response.body()?.data ?: emptyList())
        } else {
            Result.failure(Exception("Lỗi tải sân con"))
        }
    }

    suspend fun getCourtAvailability(fieldId: Int, courtId: Int, date: String): Result<List<BookedRangeDto>> = safeCall {
        val response = api.getCourtAvailability(fieldId, courtId, date)
        if (response.isSuccessful) {
            Result.success(response.body()?.data ?: emptyList())
        } else {
            Result.failure(Exception("Lỗi tải lịch đặt sân"))
        }
    }

    suspend fun createCourt(fieldId: Int, request: CreateCourtRequest): Result<Unit> = safeCall {
        val response = api.createCourt(fieldId, request)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Tạo sân con thất bại (${response.code()})"))
    }

    suspend fun updateCourt(fieldId: Int, courtId: Int, request: UpdateCourtRequest): Result<Unit> = safeCall {
        val response = api.updateCourt(fieldId, courtId, request)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Cập nhật sân con thất bại (${response.code()})"))
    }

    suspend fun deleteCourt(fieldId: Int, courtId: Int): Result<Unit> = safeCall {
        val response = api.deleteCourt(fieldId, courtId)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Xóa sân con thất bại (${response.code()})"))
    }

    suspend fun getServices(fieldId: Int): Result<List<FieldServiceDto>> = safeCall {
        val response = api.getServices(fieldId)
        if (response.isSuccessful) {
            Result.success(response.body()?.data ?: emptyList())
        } else {
            Result.failure(Exception("Lỗi tải dịch vụ"))
        }
    }

    suspend fun createService(fieldId: Int, request: CreateServiceRequest): Result<Unit> = safeCall {
        val response = api.createService(fieldId, request)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Tạo dịch vụ thất bại (${response.code()})"))
    }

    suspend fun deleteService(fieldId: Int, serviceId: Int): Result<Unit> = safeCall {
        val response = api.deleteService(fieldId, serviceId)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Xóa dịch vụ thất bại (${response.code()})"))
    }

    suspend fun getPolicies(fieldId: Int): Result<List<FieldPolicyDto>> = safeCall {
        val response = api.getPolicies(fieldId)
        if (response.isSuccessful) {
            Result.success(response.body()?.data ?: emptyList())
        } else {
            Result.failure(Exception("Lỗi tải chính sách"))
        }
    }

    suspend fun createPolicy(fieldId: Int, request: CreatePolicyRequest): Result<Unit> = safeCall {
        val response = api.createPolicy(fieldId, request)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Tạo chính sách thất bại (${response.code()})"))
    }

    suspend fun deletePolicy(fieldId: Int, policyId: Int): Result<Unit> = safeCall {
        val response = api.deletePolicy(fieldId, policyId)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Xóa chính sách thất bại (${response.code()})"))
    }

    suspend fun getBlockedSlots(fieldId: Int): Result<List<BlockedSlotDto>> = safeCall {
        val response = api.getBlockedSlots(fieldId)
        if (response.isSuccessful) Result.success(response.body()?.data ?: emptyList())
        else Result.failure(Exception("Lỗi tải blocked slots"))
    }

    suspend fun createBlockedSlot(fieldId: Int, request: CreateBlockedSlotRequest): Result<Unit> = safeCall {
        val response = api.createBlockedSlot(fieldId, request)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Tạo blocked slot thất bại"))
    }

    suspend fun deleteBlockedSlot(fieldId: Int, slotId: Int): Result<Unit> = safeCall {
        val response = api.deleteBlockedSlot(fieldId, slotId)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Xóa blocked slot thất bại"))
    }

    private suspend fun <T> safeCall(block: suspend () -> Result<T>): Result<T> {
        return try {
            block()
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối đến máy chủ"))
        }
    }
}
