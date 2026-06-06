package com.sportmanagement.manager.data.repository

import com.sportmanagement.manager.data.remote.api.FieldApiService
import com.sportmanagement.manager.data.remote.dto.BlockedSlotDto
import com.sportmanagement.manager.data.remote.dto.CreateBlockedSlotRequest
import com.sportmanagement.manager.data.remote.dto.FieldCourtDto
import com.sportmanagement.manager.data.remote.dto.FieldDto
import com.sportmanagement.manager.data.remote.dto.FieldPolicyDto
import com.sportmanagement.manager.data.remote.dto.FieldServiceDto
import com.sportmanagement.manager.data.remote.dto.UpdateFieldStatusRequest

class FieldRepository(private val api: FieldApiService) {

    suspend fun getFields(): Result<List<FieldDto>> = safeCall {
        val response = api.getFields()
        if (response.isSuccessful) {
            Result.success(response.body()?.data ?: emptyList())
        } else {
            Result.failure(Exception("Lỗi tải danh sách sân (${response.code()})"))
        }
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

    suspend fun getServices(fieldId: Int): Result<List<FieldServiceDto>> = safeCall {
        val response = api.getServices(fieldId)
        if (response.isSuccessful) {
            Result.success(response.body()?.data ?: emptyList())
        } else {
            Result.failure(Exception("Lỗi tải dịch vụ"))
        }
    }

    suspend fun getPolicies(fieldId: Int): Result<List<FieldPolicyDto>> = safeCall {
        val response = api.getPolicies(fieldId)
        if (response.isSuccessful) {
            Result.success(response.body()?.data ?: emptyList())
        } else {
            Result.failure(Exception("Lỗi tải chính sách"))
        }
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
