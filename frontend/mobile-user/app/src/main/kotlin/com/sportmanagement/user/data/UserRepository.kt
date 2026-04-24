package com.sportmanagement.user.data

import com.sportmanagement.user.ui.model.SportCategory
import com.sportmanagement.user.ui.model.UserField
import com.sportmanagement.user.ui.model.UserProfile
import com.sportmanagement.user.ui.model.UserStat

interface UserRepository {
    fun getHomeFields(): List<UserField>
    fun getSportCategories(): List<SportCategory>
    fun getMapCategories(): List<String>
    fun getNearbyFields(): List<UserField>
    fun getFavoriteFields(): List<UserField>
    fun getProfile(): UserProfile
    fun getStats(): List<UserStat>
}
