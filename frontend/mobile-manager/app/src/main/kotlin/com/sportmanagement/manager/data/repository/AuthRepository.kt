package com.sportmanagement.manager.data.repository

import com.sportmanagement.manager.data.local.SessionManager
import com.sportmanagement.manager.data.remote.api.AuthApiService
import com.sportmanagement.manager.data.remote.dto.LoginRequest
import com.sportmanagement.manager.data.remote.dto.UserDto

class AuthRepository(
    private val authApiService: AuthApiService,
    private val sessionManager: SessionManager
) {

    suspend fun login(username: String, password: String): Result<UserDto> {
        return try {
            val response = authApiService.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                if (body?.success == true && data != null) {
                    sessionManager.saveSession(
                        token = data.token,
                        refreshToken = data.refreshToken,
                        userId = data.user.personId,
                        name = data.user.name ?: username,
                        role = data.user.role,
                        avatarUrl = data.user.avatarUrl
                    )
                    Result.success(data.user)
                } else {
                    Result.failure(Exception(body?.message ?: "Đăng nhập thất bại"))
                }
            } else {
                Result.failure(Exception("Tên đăng nhập hoặc mật khẩu không đúng"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối đến máy chủ. Kiểm tra lại kết nối mạng."))
        }
    }

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()
    fun getManagerName(): String? = sessionManager.getUserName()
    fun getAvatarUrl(): String? = sessionManager.getAvatarUrl()
    fun getUserId(): Int = sessionManager.getUserId()
    fun updateName(name: String) = sessionManager.updateName(name)
    fun logout() = sessionManager.clearSession()
}
