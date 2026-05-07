package com.sportmanagement.user.data.mock

import com.sportmanagement.user.domain.model.BookingScheduleData
import com.sportmanagement.user.domain.model.CourtRow
import com.sportmanagement.user.domain.model.SlotStatus
import com.sportmanagement.user.domain.model.SportCategory
import com.sportmanagement.user.domain.model.SportIconType
import com.sportmanagement.user.domain.model.TimeSlot
import com.sportmanagement.user.domain.model.UserField
import com.sportmanagement.user.domain.model.UserProfile
import com.sportmanagement.user.domain.model.UserStat
import com.sportmanagement.user.domain.model.VenueCardType
import com.sportmanagement.user.domain.repository.UserRepository

class   MockUserRepository : UserRepository {

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
            UserField(
                name = "Sân vận động Quốc gia Mỹ Đình",
                location = "Lê Đức Thọ, Nam Từ Liêm, Hà Nội",
                price = "Liên hệ",
                rating = "4.8",
                sportIconType = SportIconType.FOOTBALL,
                latitude = 21.0227,
                longitude = 105.7630
            ),
            UserField(
                name = "Sân vận động Hàng Đẫy",
                location = "Trịnh Hoài Đức, Đống Đa, Hà Nội",
                price = "Liên hệ",
                rating = "4.6",
                sportIconType = SportIconType.FOOTBALL,
                latitude = 21.0286,
                longitude = 105.8356
            ),
            UserField(
                name = "Cung thể thao Quần Ngựa",
                location = "Văn Cao, Ba Đình, Hà Nội",
                price = "Liên hệ",
                rating = "4.5",
                sportIconType = SportIconType.BADMINTON,
                latitude = 21.0394,
                longitude = 105.8208
            ),
            UserField(
                name = "Cung điền kinh Hà Nội",
                location = "Khu liên hợp Mỹ Đình, Nam Từ Liêm, Hà Nội",
                price = "Liên hệ",
                rating = "4.4",
                sportIconType = SportIconType.VOLLEYBALL,
                latitude = 21.0208,
                longitude = 105.7657
            ),
            UserField(
                name = "Sân bóng C500 Học viện An Ninh",
                location = "Trần Phú, Hà Đông, Hà Nội",
                price = "320.000đ/h",
                rating = "4.7",
                sportIconType = SportIconType.FOOTBALL,
                latitude = 21.0466,
                longitude = 105.7868
            ),
            UserField(
                name = "Sân bóng Minh Kiệt",
                location = "Cầu Giấy, Hà Nội",
                price = "300.000đ/h",
                rating = "4.5",
                sportIconType = SportIconType.FOOTBALL,
                latitude = 21.0368,
                longitude = 105.8215
            ),
            UserField(
                name = "Sân Tennis Hanoi Club",
                location = "Yên Phụ, Tây Hồ, Hà Nội",
                price = "450.000đ/h",
                rating = "4.8",
                sportIconType = SportIconType.TENNIS,
                latitude = 21.0501,
                longitude = 105.8384
            ),
            UserField(
                name = "Rooftop Pickleball Trần Duy Hưng",
                location = "Trần Duy Hưng, Cầu Giấy, Hà Nội",
                price = "280.000đ/h",
                rating = "4.6",
                sportIconType = SportIconType.PICKLEBALL,
                latitude = 21.0096,
                longitude = 105.8019
            ),
            UserField(
                name = "Sân Pickleball 68 Nguyễn Hoàng",
                location = "Nguyễn Hoàng, Nam Từ Liêm, Hà Nội",
                price = "260.000đ/h",
                rating = "4.5",
                sportIconType = SportIconType.PICKLEBALL,
                latitude = 21.0347,
                longitude = 105.7798
            ),
            UserField(
                name = "Sân Cầu lông Trịnh Hoài Đức",
                location = "Trịnh Hoài Đức, Đống Đa, Hà Nội",
                price = "180.000đ/h",
                rating = "4.4",
                sportIconType = SportIconType.BADMINTON,
                latitude = 21.0281,
                longitude = 105.8332
            ),
            UserField(
                name = "Sân Cầu lông Bách Khoa",
                location = "Đại Cồ Việt, Hai Bà Trưng, Hà Nội",
                price = "170.000đ/h",
                rating = "4.3",
                sportIconType = SportIconType.BADMINTON,
                latitude = 21.0043,
                longitude = 105.8448
            ),
            UserField(
                name = "Sân bóng chuyền Nhà thi đấu Cầu Giấy",
                location = "Trần Quý Kiên, Cầu Giấy, Hà Nội",
                price = "220.000đ/h",
                rating = "4.4",
                sportIconType = SportIconType.VOLLEYBALL,
                latitude = 21.0340,
                longitude = 105.7889
            ),
            UserField(
                name = "Sân bóng chuyền Thanh Xuân",
                location = "Lê Văn Lương, Thanh Xuân, Hà Nội",
                price = "210.000đ/h",
                rating = "4.2",
                sportIconType = SportIconType.VOLLEYBALL,
                latitude = 21.0046,
                longitude = 105.8046
            ),
            UserField(
                name = "Sân Tennis Mỹ Đình",
                location = "Mỹ Đình, Nam Từ Liêm, Hà Nội",
                price = "380.000đ/h",
                rating = "4.5",
                sportIconType = SportIconType.TENNIS,
                latitude = 21.0288,
                longitude = 105.7688
            ),
            UserField(
                name = "Sân Pickleball Cầu Giấy Center",
                location = "Duy Tân, Cầu Giấy, Hà Nội",
                price = "290.000đ/h",
                rating = "4.7",
                sportIconType = SportIconType.PICKLEBALL,
                latitude = 21.0345,
                longitude = 105.7851
            ),
            UserField(
                name = "Sân Pickleball Xuân Thủy Arena",
                location = "Xuân Thủy, Cầu Giấy, Hà Nội",
                price = "270.000đ/h",
                rating = "4.6",
                sportIconType = SportIconType.PICKLEBALL,
                latitude = 21.0377,
                longitude = 105.7825
            ),
            UserField(
                name = "Sân Tennis Trung Hòa",
                location = "Trung Hòa, Cầu Giấy, Hà Nội",
                price = "360.000đ/h",
                rating = "4.4",
                sportIconType = SportIconType.TENNIS,
                latitude = 21.0119,
                longitude = 105.8036
            ),
            UserField(
                name = "Sân Tennis Ciputra",
                location = "Phú Thượng, Tây Hồ, Hà Nội",
                price = "420.000đ/h",
                rating = "4.6",
                sportIconType = SportIconType.TENNIS,
                latitude = 21.0736,
                longitude = 105.8058
            ),
            UserField(
                name = "Sân Cầu lông Thanh Xuân Sports Hub",
                location = "Nguyễn Trãi, Thanh Xuân, Hà Nội",
                price = "190.000đ/h",
                rating = "4.4",
                sportIconType = SportIconType.BADMINTON,
                latitude = 21.0002,
                longitude = 105.8148
            ),
            UserField(
                name = "Sân Cầu lông Hai Bà Trưng Arena",
                location = "Lò Đúc, Hai Bà Trưng, Hà Nội",
                price = "200.000đ/h",
                rating = "4.5",
                sportIconType = SportIconType.BADMINTON,
                latitude = 21.0126,
                longitude = 105.8609
            ),
            UserField(
                name = "Sân bóng Linh Đàm",
                location = "Hoàng Liệt, Hoàng Mai, Hà Nội",
                price = "320.000đ/h",
                rating = "4.3",
                sportIconType = SportIconType.FOOTBALL,
                latitude = 20.9688,
                longitude = 105.8245
            ),
            UserField(
                name = "Sân bóng Gamuda Yên Sở",
                location = "Yên Sở, Hoàng Mai, Hà Nội",
                price = "340.000đ/h",
                rating = "4.5",
                sportIconType = SportIconType.FOOTBALL,
                latitude = 20.9801,
                longitude = 105.8704
            ),
            UserField(
                name = "Sân bóng Long Biên Riverside",
                location = "Ngọc Lâm, Long Biên, Hà Nội",
                price = "310.000đ/h",
                rating = "4.2",
                sportIconType = SportIconType.FOOTBALL,
                latitude = 21.0475,
                longitude = 105.8808
            ),
            UserField(
                name = "Sân bóng chuyền Long Biên",
                location = "Bồ Đề, Long Biên, Hà Nội",
                price = "220.000đ/h",
                rating = "4.2",
                sportIconType = SportIconType.VOLLEYBALL,
                latitude = 21.0431,
                longitude = 105.8893
            ),
            UserField(
                name = "Sân bóng chuyền Tây Hồ Club",
                location = "Âu Cơ, Tây Hồ, Hà Nội",
                price = "230.000đ/h",
                rating = "4.3",
                sportIconType = SportIconType.VOLLEYBALL,
                latitude = 21.0593,
                longitude = 105.8298
            ),
            UserField(
                name = "Sân Pickleball Hồ Tây",
                location = "Quảng An, Tây Hồ, Hà Nội",
                price = "300.000đ/h",
                rating = "4.8",
                sportIconType = SportIconType.PICKLEBALL,
                latitude = 21.0548,
                longitude = 105.8235
            ),
            UserField(
                name = "Sân Pickleball Vành Đai 3",
                location = "Mễ Trì, Nam Từ Liêm, Hà Nội",
                price = "280.000đ/h",
                rating = "4.5",
                sportIconType = SportIconType.PICKLEBALL,
                latitude = 21.0127,
                longitude = 105.7779
            ),
            UserField(
                name = "Sân Tennis Giảng Võ",
                location = "Giảng Võ, Ba Đình, Hà Nội",
                price = "390.000đ/h",
                rating = "4.4",
                sportIconType = SportIconType.TENNIS,
                latitude = 21.0287,
                longitude = 105.8224
            ),
            UserField(
                name = "Sân Cầu lông Văn Quán",
                location = "Văn Quán, Hà Đông, Hà Nội",
                price = "175.000đ/h",
                rating = "4.3",
                sportIconType = SportIconType.BADMINTON,
                latitude = 20.9838,
                longitude = 105.7951
            ),
            UserField(
                name = "Sân bóng Hà Đông Premier",
                location = "Nguyễn Trãi, Hà Đông, Hà Nội",
                price = "300.000đ/h",
                rating = "4.1",
                sportIconType = SportIconType.FOOTBALL,
                latitude = 20.9714,
                longitude = 105.7888
            ),
            UserField(
                name = "Sân bóng chuyền Hà Đông",
                location = "Mỗ Lao, Hà Đông, Hà Nội",
                price = "210.000đ/h",
                rating = "4.2",
                sportIconType = SportIconType.VOLLEYBALL,
                latitude = 20.9859,
                longitude = 105.7817
            ),
            UserField(
                name = "Sân Cầu lông Từ Liêm Pro",
                location = "Phú Diễn, Bắc Từ Liêm, Hà Nội",
                price = "185.000đ/h",
                rating = "4.5",
                sportIconType = SportIconType.BADMINTON,
                latitude = 21.0461,
                longitude = 105.7614
            ),
            UserField(
                name = "Sân bóng Bắc Từ Liêm Arena",
                location = "Cầu Diễn, Bắc Từ Liêm, Hà Nội",
                price = "315.000đ/h",
                rating = "4.4",
                sportIconType = SportIconType.FOOTBALL,
                latitude = 21.0417,
                longitude = 105.7558
            ),
            UserField(
                name = "Sân Tennis Bắc Từ Liêm",
                location = "Đức Thắng, Bắc Từ Liêm, Hà Nội",
                price = "340.000đ/h",
                rating = "4.3",
                sportIconType = SportIconType.TENNIS,
                latitude = 21.0718,
                longitude = 105.7713
            ),
            UserField(
                name = "Sân Pickleball Bách Khoa",
                location = "Tạ Quang Bửu, Hai Bà Trưng, Hà Nội",
                price = "275.000đ/h",
                rating = "4.4",
                sportIconType = SportIconType.PICKLEBALL,
                latitude = 21.0053,
                longitude = 105.8463
            )
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

    override fun getBookingSchedule(): BookingScheduleData {
        val headers = (0..48).map { index ->
            val hour = index / 2
            val minute = if (index % 2 == 0) "00" else "30"
            "$hour:$minute"
        }

        return BookingScheduleData(
            selectedDate = "25/04/2026",
            timeHeaders = headers,
            courts = listOf(
                CourtRow(
                    courtName = "Sân 1",
                    slots = headers.mapIndexed { index, label ->
                        val status = when (index) {
                            in 34..37 -> SlotStatus.BOOKED
                            in 38..41 -> SlotStatus.LOCKED
                            else -> SlotStatus.AVAILABLE
                        }
                        TimeSlot(timeLabel = label, status = status)
                    }
                ),
                CourtRow(
                    courtName = "Sân 2",
                    slots = headers.mapIndexed { index, label ->
                        val status = when (index) {
                            in 34..35 -> SlotStatus.BOOKED
                            in 10..15 -> SlotStatus.LOCKED
                            else -> SlotStatus.AVAILABLE
                        }
                        TimeSlot(timeLabel = label, status = status)
                    }
                ),
                CourtRow(
                    courtName = "Sân 3",
                    slots = headers.mapIndexed { index, label ->
                        val status = when (index) {
                            36 -> SlotStatus.BOOKED
                            in 0..15 -> SlotStatus.LOCKED
                            else -> SlotStatus.AVAILABLE
                        }
                        TimeSlot(timeLabel = label, status = status)
                    }
                )
            ),
            selectedCourtName = "Sân 1",
            selectedStartTime = "15:30",
            selectedEndTime = "18:30",
            durationMinutes = 180,
            selectedSlotCount = 3,
            estimatedPrice = "450.000đ"
        )
    }
}
