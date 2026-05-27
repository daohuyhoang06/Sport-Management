package com.sportmanagement.manager.domain.model

enum class PitchStatus(val label: String, val isBadge: String) {
    ACTIVE("Đang trống", "ĐANG TRỐNG"),
    BOOKED("Đang được đặt", "ĐANG ĐẶT"),
    MAINTENANCE("Đang bảo trì", "BẢO TRÌ"),
    LOCKED("Tạm khóa", "TẠM KHÓA")
}

enum class CourtStatus(val label: String) {
    ACTIVE("Hoạt động"),
    INACTIVE("Tạm ngưng")
}

enum class BlockType(val label: String) {
    MAINTENANCE("Bảo trì"),
    EVENT("Sự kiện"),
    OTHER("Khác")
}

data class Pitch(
    val id: String,
    val name: String,
    val location: String,
    val pricePerHour: Long,
    val imageUrl: String,
    val status: PitchStatus,
    val rating: Float = 4.5f,
    val bookingCount: Int = 0
)

data class Court(
    val id: String,
    val fieldId: String,
    val courtCode: String,
    val courtName: String,
    val status: CourtStatus,
    val sortOrder: Int
)

data class FieldService(
    val id: String,
    val fieldId: String,
    val serviceName: String,
    val isFree: Boolean,
    val price: Long = 0L
)

data class FieldPolicy(
    val id: String,
    val fieldId: String,
    val policyType: String,
    val title: String,
    val content: String
)

data class BlockedSlot(
    val id: String,
    val fieldId: String,
    val courtId: String?,
    val blockDate: String,
    val startTime: String,
    val endTime: String,
    val reason: String,
    val blockType: BlockType
)

data class FieldScheduleConfig(
    val openTime: String,
    val closeTime: String,
    val slotMinutes: Int,
    val slotPrice: Long,
    val pendingHoldMinutes: Int
)

data class PitchDetail(
    val id: String,
    val name: String,
    val sportType: String,
    val location: String,
    val latitude: Double?,
    val longitude: Double?,
    val phone: String,
    val status: PitchStatus,
    val avatarImageUrl: String,
    val cardImageUrl: String,
    val galleryUrls: List<String>,
    val rating: Float,
    val bookingCount: Int,
    val scheduleConfig: FieldScheduleConfig,
    val courts: List<Court>,
    val services: List<FieldService>,
    val policies: List<FieldPolicy>,
    val blockedSlots: List<BlockedSlot>
)
