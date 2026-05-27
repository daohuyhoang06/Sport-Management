package com.sportmanagement.manager.ui.state

import com.sportmanagement.manager.domain.model.Pitch
import com.sportmanagement.manager.domain.model.PitchStatus

data class PitchesUiState(
    val pitches: List<Pitch> = listOf(
        Pitch(
            id = "pitch_001",
            name = "Sân bóng Mỹ Đình A",
            location = "Nam Từ Liêm, Hà Nội",
            pricePerHour = 450_000L,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA_Vi9Xi_oX6bTETw0833W5-7pZs9tNEsO_f-nD9MI1RMjBrrsDBchRiEUV9J-go8Cnc47FXzzoWKBTrOKD87rlUsaqSSkKy6-_ucCtjcOqbnb8J47EvvWR2YYcT7BfcnxyIDTsbbAZ4kQBgSJ_fydZsim6Yb3y_QTbWYrdcyCez6d-vzeG0S-Z_R_7NabV3sKNueuSUdbCLnRvrme",
            status = PitchStatus.ACTIVE,
            rating = 5.0f,
            bookingCount = 24
        ),
        Pitch(
            id = "pitch_002",
            name = "Sân bóng Mỹ Đình B",
            location = "Nam Từ Liêm, Hà Nội",
            pricePerHour = 400_000L,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDyNTazlfrAd9-bjGc44ndIQ0kBAN4P8SX6jiWt1kp1lpF99h4RbT7-d-jjULIonpdLNFBfao_GK9RPeDlhuwS5vjdp_kRySMFKUR4nCE_qM2Pqx4Y3OBj80g_D4Hy-tFvRragT91kHpj-gwMOM-jUc74274VtGKINAhCHlmqjTh8aTPUQyoBCOS7mbwv5KoCkF86wxYVqLl82vjXE8YyXpn-BgqhOmCGLMYoAsX2MNGDbAZiQQbAqfHb5zrDBKPFXIcSQS42MOf_fK",
            status = PitchStatus.BOOKED,
            rating = 4.5f,
            bookingCount = 18
        ),
        Pitch(
            id = "pitch_003",
            name = "Nhà thi đấu Cầu Giấy",
            location = "Cầu Giấy, Hà Nội",
            pricePerHour = 600_000L,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDqSz0OShmApq_057DXZpM1fkV8AOCMpF4vbsPSIP5wbRdwUNJB9sIqph_QdgwxxSTl1NqfJLzThKTFqs7YlhK9Ra31rjZ_Ircaj96pRJWHT6pwbLDrH81RCMwlLDXzgXuFdz-bKAiD75IlIT_HI0LLD7mj4hlGmOVQwKshH3MterQBZcc7D7PXq1RdPsoxEkevPcsHqF6_dv6XGQLvEOD1fO2k9x2PIvokwE9LYiXllT8nwai3cg_r4Dt0FjvrMqguN2W-Bc4W8CUL",
            status = PitchStatus.ACTIVE,
            rating = 4.8f,
            bookingCount = 32
        ),
        Pitch(
            id = "pitch_004",
            name = "Sân bóng chuyền Đống Đa",
            location = "Đống Đa, Hà Nội",
            pricePerHour = 300_000L,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA_Vi9Xi_oX6bTETw0833W5-7pZs9tNEsO_f-nD9MI1RMjBrrsDBchRiEUV9J-go8Cnc47FXzzoWKBTrOKD87rlUsaqSSkKy6-_ucCtjcOqbnb8J47EvvWR2YYcT7BfcnxyIDTsbbAZ4kQBgSJ_fydZsim6Yb3y_QTbWYrdcyCez6d-vzeG0S-Z_R_7NabV3sKNueuSUdbCLnRvrme",
            status = PitchStatus.MAINTENANCE,
            rating = 4.2f,
            bookingCount = 10
        )
    ),
    val searchQuery: String = "",
    val showFilterDialog: Boolean = false,
    val filterStatus: PitchStatus? = null,
    val filterSportType: String? = null,
    val filterMaxPrice: Long? = null
) {
    val filteredPitches: List<Pitch>
        get() = pitches.filter { pitch ->
            val matchSearch = pitch.name.contains(searchQuery, ignoreCase = true) ||
                pitch.location.contains(searchQuery, ignoreCase = true)
            val matchStatus = filterStatus == null || pitch.status == filterStatus
            val matchPrice = filterMaxPrice == null || pitch.pricePerHour <= filterMaxPrice
            matchSearch && matchStatus && matchPrice
        }

    val hasActiveFilter: Boolean
        get() = filterStatus != null || filterSportType != null || filterMaxPrice != null
}
