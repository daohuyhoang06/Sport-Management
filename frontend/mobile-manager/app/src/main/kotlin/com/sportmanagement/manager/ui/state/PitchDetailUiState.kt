package com.sportmanagement.manager.ui.state

import com.sportmanagement.manager.domain.model.BlockType
import com.sportmanagement.manager.domain.model.BlockedSlot
import com.sportmanagement.manager.domain.model.Court
import com.sportmanagement.manager.domain.model.CourtStatus
import com.sportmanagement.manager.domain.model.FieldPolicy
import com.sportmanagement.manager.domain.model.FieldScheduleConfig
import com.sportmanagement.manager.domain.model.FieldService
import com.sportmanagement.manager.domain.model.PitchDetail
import com.sportmanagement.manager.domain.model.PitchStatus

enum class PitchDetailTab(val label: String) {
    OVERVIEW("Tổng quan"),
    COURTS("Sân con"),
    SCHEDULE("Lịch & Giá"),
    SERVICES("Dịch vụ"),
    POLICIES("Chính sách"),
    BOOKING_HISTORY("Lịch sử đặt")
}

data class PitchDetailUiState(
    val pitchDetail: PitchDetail = mockPitchDetail(),
    val selectedTab: PitchDetailTab = PitchDetailTab.OVERVIEW,
    val showAddCourtDialog: Boolean = false,
    val showAddServiceDialog: Boolean = false,
    val showAddPolicyDialog: Boolean = false,
    val showBlockSlotDialog: Boolean = false,
    val showEditScheduleDialog: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,

    // Court form
    val newCourtCode: String = "",
    val newCourtName: String = "",

    // Service form
    val newServiceName: String = "",
    val newServiceIsFree: Boolean = true,
    val newServicePrice: String = "",

    // Policy form
    val newPolicyType: String = "payment",
    val newPolicyTitle: String = "",
    val newPolicyContent: String = "",

    // Block slot form
    val newBlockDate: String = "",
    val newBlockStart: String = "",
    val newBlockEnd: String = "",
    val newBlockReason: String = "",
    val newBlockType: BlockType = BlockType.MAINTENANCE,

    // Schedule edit form
    val editOpenTime: String = "",
    val editCloseTime: String = "",
    val editSlotMinutes: Int = 60,
    val editSlotPrice: String = "",
    val editPendingHold: Int = 15
)

fun mockPitchDetail() = PitchDetail(
    id = "pitch_001",
    name = "Sân bóng Mỹ Đình A",
    sportType = "Bóng đá",
    location = "Số 141 Đường Lê Đức Thọ, Nam Từ Liêm, Hà Nội",
    latitude = 21.0285,
    longitude = 105.7840,
    phone = "024 3765 8888",
    status = PitchStatus.ACTIVE,
    avatarImageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA_Vi9Xi_oX6bTETw0833W5-7pZs9tNEsO_f-nD9MI1RMjBrrsDBchRiEUV9J-go8Cnc47FXzzoWKBTrOKD87rlUsaqSSkKy6-_ucCtjcOqbnb8J47EvvWR2YYcT7BfcnxyIDTsbbAZ4kQBgSJ_fydZsim6Yb3y_QTbWYrdcyCez6d-vzeG0S-Z_R_7NabV3sKNueuSUdbCLnRvrme",
    cardImageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA_Vi9Xi_oX6bTETw0833W5-7pZs9tNEsO_f-nD9MI1RMjBrrsDBchRiEUV9J-go8Cnc47FXzzoWKBTrOKD87rlUsaqSSkKy6-_ucCtjcOqbnb8J47EvvWR2YYcT7BfcnxyIDTsbbAZ4kQBgSJ_fydZsim6Yb3y_QTbWYrdcyCez6d-vzeG0S-Z_R_7NabV3sKNueuSUdbCLnRvrme",
    galleryUrls = listOf(
        "https://lh3.googleusercontent.com/aida-public/AB6AXuDyNTazlfrAd9-bjGc44ndIQ0kBAN4P8SX6jiWt1kp1lpF99h4RbT7-d-jjULIonpdLNFBfao_GK9RPeDlhuwS5vjdp_kRySMFKUR4nCE_qM2Pqx4Y3OBj80g_D4Hy-tFvRragT91kHpj-gwMOM-jUc74274VtGKINAhCHlmqjTh8aTPUQyoBCOS7mbwv5KoCkF86wxYVqLl82vjXE8YyXpn-BgqhOmCGLMYoAsX2MNGDbAZiQQbAqfHb5zrDBKPFXIcSQS42MOf_fK",
        "https://lh3.googleusercontent.com/aida-public/AB6AXuDqSz0OShmApq_057DXZpM1fkV8AOCMpF4vbsPSIP5wbRdwUNJB9sIqph_QdgwxxSTl1NqfJLzThKTFqs7YlhK9Ra31rjZ_Ircaj96pRJWHT6pwbLDrH81RCMwlLDXzgXuFdz-bKAiD75IlIT_HI0LLD7mj4hlGmOVQwKshH3MterQBZcc7D7PXq1RdPsoxEkevPcsHqF6_dv6XGQLvEOD1fO2k9x2PIvokwE9LYiXllT8nwai3cg_r4Dt0FjvrMqguN2W-Bc4W8CUL"
    ),
    rating = 5.0f,
    bookingCount = 24,
    scheduleConfig = FieldScheduleConfig(
        openTime = "06:00",
        closeTime = "22:00",
        slotMinutes = 60,
        slotPrice = 450_000L,
        pendingHoldMinutes = 15
    ),
    courts = listOf(
        Court("c1", "pitch_001", "S1", "Sân 1", CourtStatus.ACTIVE, 1),
        Court("c2", "pitch_001", "S2", "Sân 2", CourtStatus.ACTIVE, 2),
        Court("c3", "pitch_001", "S3", "Sân 3", CourtStatus.INACTIVE, 3)
    ),
    services = listOf(
        FieldService("sv1", "pitch_001", "Cho thuê giày", false, 30_000L),
        FieldService("sv2", "pitch_001", "Nước uống", true, 0L),
        FieldService("sv3", "pitch_001", "Gửi xe miễn phí", true, 0L),
        FieldService("sv4", "pitch_001", "Đèn chiếu sáng", false, 50_000L)
    ),
    policies = listOf(
        FieldPolicy("p1", "pitch_001", "payment", "Thanh toán", "Thanh toán trước khi sử dụng sân. Chấp nhận tiền mặt và chuyển khoản ngân hàng."),
        FieldPolicy("p2", "pitch_001", "cancellation", "Hủy đặt sân", "Hủy trước 2 giờ được hoàn tiền 100%. Hủy trong vòng 2 giờ không được hoàn tiền."),
        FieldPolicy("p3", "pitch_001", "rules", "Nội quy sân", "Không mang đồ ăn uống vào sân. Mặc đồ thể thao phù hợp khi thi đấu.")
    ),
    blockedSlots = listOf(
        BlockedSlot("bs1", "pitch_001", null, "2026-05-25", "12:00", "14:00", "Bảo trì thiết bị chiếu sáng", BlockType.MAINTENANCE),
        BlockedSlot("bs2", "pitch_001", "c1", "2026-05-26", "08:00", "12:00", "Giải đấu nội bộ", BlockType.EVENT)
    )
)
