package com.sportmanagement.user.domain.repository

import com.sportmanagement.user.domain.model.BookingScheduleData
import com.sportmanagement.user.domain.model.HomeSearchFilterOptions
import com.sportmanagement.user.domain.model.SportCategory
import com.sportmanagement.user.domain.model.UserField
import com.sportmanagement.user.domain.model.UserProfile
import com.sportmanagement.user.domain.model.UserStat

interface UserRepository {
    fun isLoggedIn(): Boolean = false

    fun getCachedProfile(): UserProfile? = null

    fun getCachedHomeFields(
        latitude: Double? = null,
        longitude: Double? = null
    ): List<UserField> = emptyList()

    fun getSavedUserLocation(): Pair<Double, Double>? = null

    fun saveUserLocation(latitude: Double, longitude: Double) = Unit

    fun getRecentFieldSearches(): List<String> = emptyList()

    fun saveRecentFieldSearch(query: String) = Unit

    fun getPreferredSportTypeKeys(): Set<String> = emptySet()

    fun savePreferredSportTypeKeys(sportTypeKeys: Set<String>) = Unit

    suspend fun getHomeFields(
        latitude: Double? = null,
        longitude: Double? = null
    ): List<UserField>

    suspend fun getHomeFieldsPage(
        page: Int,
        limit: Int,
        latitude: Double? = null,
        longitude: Double? = null
    ): List<UserField> {
        val all = getHomeFields(latitude, longitude)
        val safeLimit = limit.coerceAtLeast(1)
        val start = ((page - 1).coerceAtLeast(0)) * safeLimit
        if (start >= all.size) return emptyList()
        val end = (start + safeLimit).coerceAtMost(all.size)
        return all.subList(start, end)
    }

    suspend fun getSportCategories(): List<SportCategory>
    suspend fun getMapCategories(): List<String>
    suspend fun getNearbyFields(
        latitude: Double? = null,
        longitude: Double? = null
    ): List<UserField>

    suspend fun getNearbyFieldsPage(
        page: Int,
        limit: Int,
        latitude: Double? = null,
        longitude: Double? = null
    ): List<UserField> {
        val all = getNearbyFields(latitude, longitude)
        val safeLimit = limit.coerceAtLeast(1)
        val start = ((page - 1).coerceAtLeast(0)) * safeLimit
        if (start >= all.size) return emptyList()
        val end = (start + safeLimit).coerceAtMost(all.size)
        return all.subList(start, end)
    }

    suspend fun getFavoriteFields(): List<UserField>

    suspend fun login(
        identifier: String,
        password: String
    ): UserProfile {
        throw UnsupportedOperationException("Login not implemented")
    }

    suspend fun loginWithGoogle(idToken: String): UserProfile {
        throw UnsupportedOperationException("Google login not implemented")
    }

    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        phone: String? = null,
        birthday: String? = null,
        address: String? = null,
        favoriteSportTypeKeys: Set<String> = emptySet()
    ): UserProfile {
        throw UnsupportedOperationException("Register not implemented")
    }

    suspend fun updateProfile(profile: UserProfile): UserProfile {
        return profile
    }

    fun logout() = Unit

    suspend fun searchFieldsPage(
        keyword: String? = null,
        address: String? = null,
        sportType: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        radiusKm: Double? = null,
        sortBy: String? = null,
        page: Int,
        limit: Int
    ): List<UserField> {
        val normalizedKeyword = keyword.orEmpty().trim()
        val normalizedAddress = address.orEmpty().trim()
        val normalizedSport = sportType.orEmpty().trim()
        val all = getNearbyFields(latitude, longitude)
        val filtered = all.filter { field ->
            val byKeyword = normalizedKeyword.isBlank() ||
                field.name.contains(normalizedKeyword, ignoreCase = true) ||
                field.location.contains(normalizedKeyword, ignoreCase = true)
            val byAddress = normalizedAddress.isBlank() ||
                field.location.contains(normalizedAddress, ignoreCase = true) ||
                field.province.contains(normalizedAddress, ignoreCase = true) ||
                field.district.contains(normalizedAddress, ignoreCase = true)
            val bySport = normalizedSport.isBlank() ||
                field.sportIconType.name.equals(normalizedSport, ignoreCase = true)
            byKeyword && byAddress && bySport
        }
        val safeLimit = limit.coerceAtLeast(1)
        val start = ((page - 1).coerceAtLeast(0)) * safeLimit
        if (start >= filtered.size) return emptyList()
        return filtered.subList(start, (start + safeLimit).coerceAtMost(filtered.size))
    }
    suspend fun getProfile(): UserProfile
    suspend fun getStats(): List<UserStat>
    suspend fun getBookingSchedule(): BookingScheduleData
    suspend fun getFieldGrid(fieldId: Int, date: String): BookingScheduleData
    suspend fun getHomeSearchFilterOptions(): HomeSearchFilterOptions
}
