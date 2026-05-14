package com.sportmanagement.user.domain.repository

import com.sportmanagement.user.domain.model.BookingScheduleData
import com.sportmanagement.user.domain.model.HomeSearchFilterOptions
import com.sportmanagement.user.domain.model.SportCategory
import com.sportmanagement.user.domain.model.UserField
import com.sportmanagement.user.domain.model.UserProfile
import com.sportmanagement.user.domain.model.UserStat

interface UserRepository {
    fun getHomeFields(): List<UserField>
    fun getSportCategories(): List<SportCategory>
    fun getMapCategories(): List<String>
    fun getNearbyFields(): List<UserField>
    fun getFavoriteFields(): List<UserField>
    fun getProfile(): UserProfile
    fun getStats(): List<UserStat>
    fun getBookingSchedule(): BookingScheduleData
    fun getHomeSearchFilterOptions(): HomeSearchFilterOptions
}
