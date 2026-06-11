package com.sportmanagement.manager.data.repository

import com.sportmanagement.manager.data.remote.api.ProfileApiService
import com.sportmanagement.manager.data.remote.dto.ChangePasswordRequest
import com.sportmanagement.manager.data.remote.dto.ProfileDto
import com.sportmanagement.manager.data.remote.dto.UpdateProfileRequest

class ProfileRepository(private val api: ProfileApiService) {

    suspend fun getProfile(): Result<ProfileDto> = safeCall {
        val r = api.getProfile()
        if (r.isSuccessful) {
            val data = r.body()?.data
            if (data != null) Result.success(data)
            else Result.failure(Exception("Không có dữ liệu hồ sơ"))
        } else {
            Result.failure(Exception("Lỗi tải hồ sơ (${r.code()})"))
        }
    }

    suspend fun updateProfile(personName: String?, email: String?, phone: String?): Result<ProfileDto> = safeCall {
        val r = api.updateProfile(UpdateProfileRequest(personName, email, phone))
        if (r.isSuccessful) {
            val data = r.body()?.data
            if (data != null) Result.success(data)
            else Result.failure(Exception("Cập nhật hồ sơ thất bại"))
        } else {
            Result.failure(Exception("Cập nhật hồ sơ thất bại (${r.code()})"))
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = safeCall {
        val r = api.changePassword(ChangePasswordRequest(currentPassword, newPassword))
        if (r.isSuccessful && r.body()?.success == true) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(r.body()?.message ?: "Đổi mật khẩu thất bại"))
        }
    }

    private suspend fun <T> safeCall(block: suspend () -> Result<T>): Result<T> =
        try { block() } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối đến máy chủ"))
        }
}
