package com.sportmanagement.user.data.remote.mapper

import com.sportmanagement.user.data.remote.dto.SportCategoryDto
import com.sportmanagement.user.data.remote.dto.UserFieldDto
import com.sportmanagement.user.data.remote.dto.UserProfileDto
import com.sportmanagement.user.data.remote.dto.UserStatDto
import com.sportmanagement.user.domain.model.SportCategory
import com.sportmanagement.user.domain.model.SportIconType
import com.sportmanagement.user.domain.model.UserField
import com.sportmanagement.user.domain.model.UserProfile
import com.sportmanagement.user.domain.model.UserStat
import com.sportmanagement.user.domain.model.VenueCardType

/**
 * Maps DTOs from the remote layer to domain models.
 */
object UserMapper {

    fun UserFieldDto.toDomain(): UserField = UserField(
        fieldId = fieldId ?: 0,
        name = name,
        location = location,
        price = price,
        rating = rating,
        sportIconType = sportIconType?.toSportIconType() ?: SportIconType.FOOTBALL,
        latitude = latitude,
        longitude = longitude,
        distanceKm = distanceKm,
        distance = distance ?: "",
        hours = hours ?: "00:00 - 24:00",
        imageUrl = imageUrl ?: "",
        isProLeague = isProLeague ?: false,
        tags = tags ?: emptyList(),
        availability = availability ?: "",
        cardType = cardType?.toVenueCardType() ?: VenueCardType.LARGE_IMAGE,
        region = region ?: "",
        province = province ?: "",
        district = district ?: ""
    )

    fun SportCategoryDto.toDomain(): SportCategory = SportCategory(
        name = name,
        iconType = iconType.toSportIconType()
    )

    fun UserProfileDto.toDomain(): UserProfile = UserProfile(
        name = name,
        email = email,
        phone = phone,
        membership = membership
    )

    fun UserStatDto.toDomain(): UserStat = UserStat(
        value = value,
        label = label
    )

    private fun String.toVenueCardType(): VenueCardType = when (this.uppercase()) {
        "LARGE_IMAGE" -> VenueCardType.LARGE_IMAGE
        "SMALL_HORIZONTAL" -> VenueCardType.SMALL_HORIZONTAL
        "SMALL_HORIZONTAL_NO_IMAGE" -> VenueCardType.SMALL_HORIZONTAL_NO_IMAGE
        else -> VenueCardType.LARGE_IMAGE
    }

    private fun String.toSportIconType(): SportIconType = when (this.uppercase()) {
        "FOOTBALL" -> SportIconType.FOOTBALL
        "TENNIS" -> SportIconType.TENNIS
        "BADMINTON" -> SportIconType.BADMINTON
        "VOLLEYBALL" -> SportIconType.VOLLEYBALL
        "PICKLEBALL" -> SportIconType.PICKLEBALL
        else -> SportIconType.FOOTBALL
    }
}
