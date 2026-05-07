package com.sportmanagement.user.domain.model

data class UserField(
    val name: String,
    val location: String,
    val price: String,
    val rating: String,
    val sportIconType: SportIconType = SportIconType.FOOTBALL,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val distance: String = "0.8 miles away",
    val hours: String = "00:00 - 24:00",
    val imageUrl: String = "",
    val isProLeague: Boolean = false,
    val tags: List<String> = emptyList(),
    val availability: String = "",
    val cardType: VenueCardType = VenueCardType.LARGE_IMAGE
)

enum class VenueCardType {
    LARGE_IMAGE,
    SMALL_HORIZONTAL,
    SMALL_HORIZONTAL_NO_IMAGE
}

data class SportCategory(
    val name: String,
    val iconType: SportIconType
)

enum class SportIconType {
    FOOTBALL, TENNIS, BADMINTON, VOLLEYBALL, PICKLEBALL
}

data class UserBooking(
    val fieldName: String,
    val dateTime: String,
    val status: String
)

data class UserProfile(
    val name: String,
    val email: String,
    val phone: String,
    val membership: String
)

data class UserStat(
    val value: String,
    val label: String
)
