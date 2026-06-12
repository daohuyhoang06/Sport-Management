package com.sportmanagement.manager.ui.state

import com.sportmanagement.manager.domain.model.BlockType
import com.sportmanagement.manager.domain.model.FieldScheduleConfig
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
    val isLoading: Boolean = false,
    val error: String? = null,
    val pitchDetail: PitchDetail = emptyPitchDetail(),
    val selectedTab: PitchDetailTab = PitchDetailTab.OVERVIEW,

    val showEditBasicInfoDialog: Boolean = false,
    val showAddCourtDialog: Boolean = false,
    val showAddServiceDialog: Boolean = false,
    val showAddPolicyDialog: Boolean = false,
    val showBlockSlotDialog: Boolean = false,
    val showEditScheduleDialog: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,

    // Basic info edit form
    val editFieldName: String = "",
    val editLocation: String = "",
    val editPhone: String = "",

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

fun emptyPitchDetail() = PitchDetail(
    id = "",
    name = "",
    sportType = "",
    location = "",
    latitude = null,
    longitude = null,
    phone = "",
    status = PitchStatus.ACTIVE,
    avatarImageUrl = "",
    cardImageUrl = "",
    galleryUrls = emptyList(),
    rating = 0f,
    bookingCount = 0,
    scheduleConfig = FieldScheduleConfig(
        openTime = "",
        closeTime = "",
        slotMinutes = 60,
        slotPrice = 0L,
        pendingHoldMinutes = 15
    ),
    courts = emptyList(),
    services = emptyList(),
    policies = emptyList(),
    blockedSlots = emptyList()
)
