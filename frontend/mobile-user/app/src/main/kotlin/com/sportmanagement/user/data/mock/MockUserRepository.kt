package com.sportmanagement.user.data.mock

import com.sportmanagement.user.domain.model.BookingScheduleData
import com.sportmanagement.user.domain.model.BookingSubCourt
import com.sportmanagement.user.domain.model.BookingTimeGridData
import com.sportmanagement.user.domain.model.BookingTimeRange
import com.sportmanagement.user.domain.model.HomeSearchFilterOptions
import com.sportmanagement.user.domain.model.HomeSearchProvinceOption
import com.sportmanagement.user.domain.model.SportCategory
import com.sportmanagement.user.domain.model.SportIconType
import com.sportmanagement.user.domain.model.UserField
import com.sportmanagement.user.domain.model.UserProfile
import com.sportmanagement.user.domain.model.UserStat
import com.sportmanagement.user.domain.model.VenueCardType
import com.sportmanagement.user.domain.repository.UserRepository

class   MockUserRepository : UserRepository {

    override suspend fun getSportCategories(): List<SportCategory> = listOf(
        SportCategory("Bóng đá", SportIconType.FOOTBALL),
        SportCategory("Bóng chuyền", SportIconType.VOLLEYBALL),
        SportCategory("Pickleball", SportIconType.PICKLEBALL),
        SportCategory("Cầu lông", SportIconType.BADMINTON),
        SportCategory("Tennis", SportIconType.TENNIS)
    )

    override suspend fun getHomeFields(
        latitude: Double?,
        longitude: Double?
    ): List<UserField> =
        getNearbyFields(null, null).mapIndexed { index, field ->
            val ratingScore = field.rating.toDoubleOrNull() ?: 0.0
            val distanceKm = homeDistanceKmByIndex(index)
            UserField(
                name = field.name,
                location = field.location,
                price = if (field.price == "Liên hệ") defaultPriceBySport(field.sportIconType) else field.price,
                rating = field.rating,
                sportIconType = field.sportIconType,
                latitude = field.latitude,
                longitude = field.longitude,
                distance = formatDistanceKm(distanceKm),
                hours = homeHoursBySport(field.sportIconType),
                isProLeague = ratingScore >= 4.7 || index % 9 == 0,
                tags = homeTagsBySport(field.sportIconType),
                availability = if (index % 4 == 0) "Còn sân tối nay" else "",
                cardType = VenueCardType.LARGE_IMAGE,
                region = field.region,
                province = field.province,
                district = field.district,
                distanceKm = distanceKm
            )
        }

    override suspend fun getMapCategories(): List<String> =
        listOf("Bóng đá", "Bóng chuyền", "Pickleball", "Cầu lông", "Tennis")

    override suspend fun getNearbyFields(
        latitude: Double?,
        longitude: Double?
    ): List<UserField> =
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
            ),
            UserField(
                name = "Sân bóng Phú Thọ Arena",
                location = "Lý Thường Kiệt, Quận 11, TP Hồ Chí Minh",
                price = "360.000đ/h",
                rating = "4.6",
                sportIconType = SportIconType.FOOTBALL,
                latitude = 10.7646,
                longitude = 106.6641
            ),
            UserField(
                name = "Sân Pickleball Thủ Đức Hub",
                location = "Xa lộ Hà Nội, Thủ Đức, TP Hồ Chí Minh",
                price = "320.000đ/h",
                rating = "4.7",
                sportIconType = SportIconType.PICKLEBALL,
                latitude = 10.8516,
                longitude = 106.7713
            ),
            UserField(
                name = "Sân Tennis Sơn Trà",
                location = "Võ Nguyên Giáp, Sơn Trà, Đà Nẵng",
                price = "410.000đ/h",
                rating = "4.5",
                sportIconType = SportIconType.TENNIS,
                latitude = 16.0700,
                longitude = 108.2430
            ),
            UserField(
                name = "Nhà thi đấu Hải Châu",
                location = "Phan Đăng Lưu, Hải Châu, Đà Nẵng",
                price = "230.000đ/h",
                rating = "4.4",
                sportIconType = SportIconType.VOLLEYBALL,
                latitude = 16.0471,
                longitude = 108.2068
            ),
            UserField(
                name = "Sân cầu lông Lê Chân Center",
                location = "Tô Hiệu, Lê Chân, Hải Phòng",
                price = "190.000đ/h",
                rating = "4.3",
                sportIconType = SportIconType.BADMINTON,
                latitude = 20.8449,
                longitude = 106.6881
            ),
            UserField(
                name = "Sân bóng Hồng Bàng Sports Park",
                location = "Hùng Vương, Hồng Bàng, Hải Phòng",
                price = "330.000đ/h",
                rating = "4.4",
                sportIconType = SportIconType.FOOTBALL,
                latitude = 20.8648,
                longitude = 106.6834
            ),
            UserField(
                name = "Sân Tennis Ninh Kiều Riverside",
                location = "Hai Bà Trưng, Ninh Kiều, Cần Thơ",
                price = "350.000đ/h",
                rating = "4.5",
                sportIconType = SportIconType.TENNIS,
                latitude = 10.0342,
                longitude = 105.7882
            ),
            UserField(
                name = "Sân bóng Cái Răng Stadium",
                location = "Nam Kỳ Khởi Nghĩa, Cái Răng, Cần Thơ",
                price = "300.000đ/h",
                rating = "4.2",
                sportIconType = SportIconType.FOOTBALL,
                latitude = 10.0018,
                longitude = 105.7507
            )
        ).map(::enrichFieldLocation)

    override suspend fun getFavoriteFields(): List<UserField> =
        listOf(
            UserField(name = "Sân Bóng Dịch Vọng", location = "Cầu Giấy, Hà Nội", price = "300.000đ/h", rating = "5.0"),
            UserField(name = "Sân Bóng Duy Tân", location = "Cầu Giấy, Hà Nội", price = "400.000đ/h", rating = "4.7")
        )

    override suspend fun setFavoriteField(fieldId: Int, isFavorite: Boolean): List<UserField> =
        getFavoriteFields()

    override suspend fun getProfile(): UserProfile =
        UserProfile(
            name = "Nguyễn Văn An",
            email = "user1@gmail.com",
            phone = "0907890123",
            membership = "Vàng"
        )

    override suspend fun getStats(): List<UserStat> =
        listOf(
            UserStat("12", "Lần đặt"),
            UserStat("4.8", "Điểm uy tín")
        )

    override suspend fun getBookingSchedule(): BookingScheduleData {
        return BookingScheduleData(
            selectedDate = "25/04/2026",
            grid = BookingTimeGridData(
                openTime = "06:00",
                closeTime = "22:00",
                gridStepMinutes = 30,
                minBookingMinutes = 60,
                courts = listOf(
                    BookingSubCourt("court-1", "Sân 1"),
                    BookingSubCourt("court-2", "Sân 2"),
                    BookingSubCourt("court-3", "Sân 3")
                ),
                bookedSlots = listOf(
                    BookingTimeRange("court-1", "17:00", "18:00"),
                    BookingTimeRange("court-2", "17:00", "18:00"),
                    BookingTimeRange("court-3", "18:00", "18:30")
                ),
                blockedSlots = listOf(
                    BookingTimeRange("court-1", "19:00", "20:00"),
                    BookingTimeRange("court-2", "11:00", "14:00"),
                    BookingTimeRange("court-3", "06:00", "14:00")
                )
            ),
            pricePerHour = 150_000,
            estimatedPrice = "150.000đ"
        )
    }

    override suspend fun getFieldGrid(fieldId: Int, date: String): BookingScheduleData {
        return getBookingSchedule().copy(selectedDate = date)
    }

    override suspend fun getHomeSearchFilterOptions(): HomeSearchFilterOptions {
        val provinceOptions = getNearbyFields(null, null)
            .filter { it.province.isNotBlank() }
            .groupBy { it.province }
            .mapNotNull { (_, fields) ->
                val sample = fields.firstOrNull() ?: return@mapNotNull null
                HomeSearchProvinceOption(
                    regionName = sample.region,
                    provinceName = sample.province,
                    districtNames = fields.map { it.district }.filter { it.isNotBlank() }.distinct().sorted()
                )
            }
            .sortedWith(
                compareBy<HomeSearchProvinceOption> {
                    LARGE_PROVINCE_ORDER.indexOf(it.provinceName).let { index ->
                        if (index >= 0) index else Int.MAX_VALUE
                    }
                }.thenBy { it.provinceName }
            )

        return HomeSearchFilterOptions(
            sports = getSportCategories(),
            provinces = provinceOptions,
            radiusOptionsKm = listOf(3, 5, 10, 20, 30)
        )
    }

    private fun homeDistanceKmByIndex(index: Int): Double {
        val distances = listOf(0.4, 0.8, 1.2, 2.5, 4.0, 6.5, 9.0, 14.0, 22.0)
        return distances[index % distances.size]
    }

    private fun formatDistanceKm(distanceKm: Double): String {
        return if (distanceKm >= 10) {
            "${distanceKm.toInt()} km"
        } else {
            String.format("%.1f km", distanceKm)
        }
    }

    private fun enrichFieldLocation(field: UserField): UserField {
        val segments = field.location.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val province = segments.lastOrNull().orEmpty()
        val district = segments.getOrNull(segments.lastIndex - 1).orEmpty()
        return field.copy(
            province = province,
            district = district,
            region = regionForProvince(province)
        )
    }

    private fun homeHoursBySport(type: SportIconType): String {
        return when (type) {
            SportIconType.FOOTBALL -> "06:00 - 23:00"
            SportIconType.VOLLEYBALL -> "06:00 - 22:30"
            SportIconType.PICKLEBALL -> "05:30 - 22:00"
            SportIconType.BADMINTON -> "05:00 - 23:00"
            SportIconType.TENNIS -> "06:00 - 22:00"
        }
    }

    private fun defaultPriceBySport(type: SportIconType): String {
        return when (type) {
            SportIconType.FOOTBALL -> "320.000đ/h"
            SportIconType.VOLLEYBALL -> "220.000đ/h"
            SportIconType.PICKLEBALL -> "280.000đ/h"
            SportIconType.BADMINTON -> "180.000đ/h"
            SportIconType.TENNIS -> "380.000đ/h"
        }
    }

    private fun homeTagsBySport(type: SportIconType): List<String> {
        return when (type) {
            SportIconType.FOOTBALL -> listOf("7 người", "Cỏ nhân tạo")
            SportIconType.VOLLEYBALL -> listOf("Trong nhà", "Sàn gỗ")
            SportIconType.PICKLEBALL -> listOf("Indoor", "Đèn LED")
            SportIconType.BADMINTON -> listOf("Tiêu chuẩn", "Điều hòa")
            SportIconType.TENNIS -> listOf("Hard court", "Huấn luyện")
        }
    }

    private fun regionForProvince(province: String): String {
        return when (province) {
            "Hà Nội", "Hải Phòng" -> "Miền Bắc"
            "Đà Nẵng" -> "Miền Trung"
            "TP Hồ Chí Minh", "Cần Thơ" -> "Miền Nam"
            else -> "Toàn quốc"
        }
    }

    companion object {
        private val LARGE_PROVINCE_ORDER = listOf(
            "Hà Nội",
            "TP Hồ Chí Minh",
            "Đà Nẵng",
            "Hải Phòng",
            "Cần Thơ"
        )
    }
}

