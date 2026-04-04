package com.sportmanagement.user.ui.model

data class UserField(
    val name: String,
    val location: String,
    val price: String,
    val rating: String
)

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
