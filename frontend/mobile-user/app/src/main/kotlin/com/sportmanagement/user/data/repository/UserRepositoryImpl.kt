package com.sportmanagement.user.data.repository

import com.sportmanagement.user.data.remote.api.UserApi
import com.sportmanagement.user.domain.model.BookingScheduleData
import com.sportmanagement.user.domain.model.SportCategory
import com.sportmanagement.user.domain.model.UserField
import com.sportmanagement.user.domain.model.UserProfile
import com.sportmanagement.user.domain.model.UserStat
import com.sportmanagement.user.domain.repository.UserRepository

/**
 * Production implementation of [UserRepository] backed by [UserApi].
 * TODO: Wire up with actual API when backend integration is ready.
 */
class UserRepositoryImpl(
    private val api: UserApi
) : UserRepository {

    override fun getHomeFields(): List<UserField> {
        // TODO: Replace with actual API call (suspend + coroutine)
        return emptyList()
    }

    override fun getSportCategories(): List<SportCategory> {
        return emptyList()
    }

    override fun getMapCategories(): List<String> {
        return emptyList()
    }

    override fun getNearbyFields(): List<UserField> {
        return emptyList()
    }

    override fun getFavoriteFields(): List<UserField> {
        return emptyList()
    }

    override fun getProfile(): UserProfile {
        return UserProfile(
            name = "Người dùng",
            email = "user@example.com",
            phone = "0123456789",
            membership = "Thành viên Đồng"
        )
    }

    override fun getStats(): List<UserStat> {
        return emptyList()
    }

    override fun getBookingSchedule(): BookingScheduleData {
        return BookingScheduleData(
            selectedDate = "",
            timeHeaders = emptyList(),
            courts = emptyList()
        )
    }
}
