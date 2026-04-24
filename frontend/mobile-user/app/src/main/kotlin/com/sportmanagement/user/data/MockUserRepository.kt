package com.sportmanagement.user.data

import com.sportmanagement.user.ui.model.SportCategory
import com.sportmanagement.user.ui.model.SportIconType
import com.sportmanagement.user.ui.model.UserField
import com.sportmanagement.user.ui.model.UserProfile
import com.sportmanagement.user.ui.model.UserStat
import com.sportmanagement.user.ui.model.VenueCardType

class MockUserRepository : UserRepository {

    override fun getSportCategories(): List<SportCategory> = listOf(
        SportCategory("Bóng đá", SportIconType.FOOTBALL),
        SportCategory("Bóng chuyền", SportIconType.VOLLEYBALL),
        SportCategory("Pickleball", SportIconType.PICKLEBALL),
        SportCategory("Cầu lông", SportIconType.BADMINTON),
        SportCategory("Tennis", SportIconType.TENNIS)
    )

    override fun getHomeFields(): List<UserField> = listOf(
        UserField(
            name = "Sân Bóng Dịch Vọng",
            location = "Số 123 Dịch Vọng Hậu, Cầu Giấy, Hà Nội",
            price = "300.000đ/h",
            rating = "5.0",
            distance = "0.5 km",
            hours = "06:00 - 22:00",
            isProLeague = true,
            cardType = VenueCardType.LARGE_IMAGE
        ),
        UserField(
            name = "Sân Bóng Trung Hòa",
            location = "Số 45 Trung Hòa, Cầu Giấy, Hà Nội",
            price = "350.000đ/h",
            rating = "4.5",
            distance = "1.2 km",
            hours = "06:00 - 22:00",
            cardType = VenueCardType.LARGE_IMAGE
        ),
        UserField(
            name = "Sân Bóng Mỹ Đình",
            location = "Số 89 Phạm Hùng, Nam Từ Liêm, Hà Nội",
            price = "280.000đ/h",
            rating = "5.0",
            distance = "2.0 km",
            hours = "06:00 - 22:00",
            cardType = VenueCardType.LARGE_IMAGE
        ),
        UserField(
            name = "Sân Bóng Nghĩa Tân",
            location = "Số 67 Nghĩa Tân, Cầu Giấy, Hà Nội",
            price = "320.000đ/h",
            rating = "4.8",
            distance = "0.8 km",
            hours = "06:00 - 22:00",
            cardType = VenueCardType.LARGE_IMAGE
        ),
        UserField(
            name = "Sân Bóng Duy Tân",
            location = "Số 156 Duy Tân, Cầu Giấy, Hà Nội",
            price = "400.000đ/h",
            rating = "4.7",
            distance = "1.5 km",
            hours = "06:00 - 22:00",
            isProLeague = true,
            cardType = VenueCardType.LARGE_IMAGE
        ),
        UserField(
            name = "Sân Bóng Yên Hòa",
            location = "Số 234 Trần Kim Xuyến, Cầu Giấy, Hà Nội",
            price = "290.000đ/h",
            rating = "4.6",
            distance = "1.0 km",
            hours = "06:00 - 22:00",
            cardType = VenueCardType.LARGE_IMAGE
        ),
        UserField(
            name = "Sân Bóng Mai Dịch",
            location = "Số 78 Phạm Văn Đồng, Cầu Giấy, Hà Nội",
            price = "380.000đ/h",
            rating = "4.0",
            distance = "1.8 km",
            hours = "06:00 - 22:00",
            cardType = VenueCardType.LARGE_IMAGE
        ),
        UserField(
            name = "Sân Bóng Xuân Thủy",
            location = "Số 92 Xuân Thủy, Cầu Giấy, Hà Nội",
            price = "420.000đ/h",
            rating = "5.0",
            distance = "0.3 km",
            hours = "06:00 - 22:00",
            cardType = VenueCardType.LARGE_IMAGE
        )
    )

    override fun getMapCategories(): List<String> =
        listOf("Bóng đá", "Bóng chuyền", "Pickleball", "Cầu lông", "Tennis")

    override fun getNearbyFields(): List<UserField> =
        listOf(
            UserField("Sân Bóng Dịch Vọng", "Cầu Giấy, Hà Nội", "300.000đ/h", "5.0"),
            UserField("Sân Bóng Trung Hòa", "Cầu Giấy, Hà Nội", "350.000đ/h", "4.5"),
            UserField("Sân Bóng Mỹ Đình", "Nam Từ Liêm, Hà Nội", "280.000đ/h", "5.0")
        )

    override fun getFavoriteFields(): List<UserField> =
        listOf(
            UserField("Sân Bóng Dịch Vọng", "Cầu Giấy, Hà Nội", "300.000đ/h", "5.0"),
            UserField("Sân Bóng Duy Tân", "Cầu Giấy, Hà Nội", "400.000đ/h", "4.7")
        )

    override fun getProfile(): UserProfile =
        UserProfile(
            name = "Nguyễn Văn An",
            email = "user1@gmail.com",
            phone = "0907890123",
            membership = "Vàng"
        )

    override fun getStats(): List<UserStat> =
        listOf(
            UserStat("12", "Lần đặt"),
            UserStat("4.8", "Điểm uy tín")
        )
}
