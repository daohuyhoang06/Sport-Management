package com.sportmanagement.user.data

import com.sportmanagement.user.ui.model.UserField
import com.sportmanagement.user.ui.model.UserProfile
import com.sportmanagement.user.ui.model.UserStat

class MockUserRepository : UserRepository {
    override fun getHomeFields(): List<UserField> =
        listOf(
            UserField("Arena Alpha", "Quận 1", "250.000đ/h", "4.8"),
            UserField("Green Pitch", "Quận 7", "220.000đ/h", "4.6"),
            UserField("Sunlight Field", "Thủ Đức", "280.000đ/h", "4.9")
        )

    override fun getMapCategories(): List<String> =
        listOf("Sân bóng đá", "Sân tennis", "Sân cầu lông")

    override fun getNearbyFields(): List<UserField> =
        listOf(
            UserField("CLB Pickleball H.A.S", "Nam Từ Liêm", "280.000đ/h", "4.9"),
            UserField("Arena Alpha", "Quận 1", "250.000đ/h", "4.8"),
            UserField("Green Pitch", "Quận 7", "220.000đ/h", "4.6")
        )

    override fun getFavoriteFields(): List<UserField> =
        listOf(
            UserField("Arena Alpha", "Quận 1", "250.000đ/h", "4.8"),
            UserField("Night Pro", "Thủ Đức", "300.000đ/h", "4.7"),
            UserField("Sunlight Field", "Gò Vấp", "230.000đ/h", "4.6")
        )

    override fun getProfile(): UserProfile =
        UserProfile(
            name = "Nguyễn Văn A",
            email = "user@sport.local",
            phone = "09xx xxx xxx",
            membership = "Vàng"
        )

    override fun getStats(): List<UserStat> =
        listOf(
            UserStat("12", "Lần đặt"),
            UserStat("4.8", "Điểm uy tín")
        )
}
