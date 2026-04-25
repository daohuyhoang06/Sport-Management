package com.sportmanagement.user.data.remote.api

import com.sportmanagement.user.data.remote.dto.UserFieldDto
import com.sportmanagement.user.data.remote.dto.UserProfileDto
import com.sportmanagement.user.data.remote.dto.SportCategoryDto
import com.sportmanagement.user.data.remote.dto.UserStatDto

/**
 * API interface for user-related network calls.
 * TODO: Replace with actual Retrofit/Ktor interface when backend integration is ready.
 */
interface UserApi {
    suspend fun getHomeFields(): List<UserFieldDto>
    suspend fun getSportCategories(): List<SportCategoryDto>
    suspend fun getMapCategories(): List<String>
    suspend fun getNearbyFields(): List<UserFieldDto>
    suspend fun getFavoriteFields(): List<UserFieldDto>
    suspend fun getProfile(): UserProfileDto
    suspend fun getStats(): List<UserStatDto>
}
