package com.sportmanagement.user.data.remote.dto

/**
 * Data Transfer Objects representing API responses.
 * These mirror the backend JSON structure and are mapped to domain models via [UserMapper].
 */

data class UserFieldDto(
    val fieldId: Int? = null,
    val name: String,
    val location: String,
    val price: String,
    val rating: String,
    val sportIconType: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val distanceKm: Double? = null,
    val distance: String? = null,
    val hours: String? = null,
    val imageUrl: String? = null,
    val isProLeague: Boolean? = null,
    val tags: List<String>? = null,
    val availability: String? = null,
    val cardType: String? = null,
    val region: String? = null,
    val province: String? = null,
    val district: String? = null
)

data class SportCategoryDto(
    val name: String,
    val iconType: String
)

data class UserProfileDto(
    val name: String,
    val email: String,
    val phone: String,
    val membership: String
)

data class UserStatDto(
    val value: String,
    val label: String
)
