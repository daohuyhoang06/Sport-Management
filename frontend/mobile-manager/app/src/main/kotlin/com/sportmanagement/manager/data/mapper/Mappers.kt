package com.sportmanagement.manager.data.mapper

import com.sportmanagement.manager.data.remote.dto.BlockedSlotDto
import com.sportmanagement.manager.data.remote.dto.BookingDto
import com.sportmanagement.manager.data.remote.dto.BookingHistoryDto
import com.sportmanagement.manager.data.remote.dto.ChatListDto
import com.sportmanagement.manager.data.remote.dto.ChatMessageDto
import com.sportmanagement.manager.data.remote.dto.FieldCourtDto
import com.sportmanagement.manager.data.remote.dto.FieldDto
import com.sportmanagement.manager.data.remote.dto.FieldPolicyDto
import com.sportmanagement.manager.data.remote.dto.FieldServiceDto
import com.sportmanagement.manager.data.remote.NetworkClient
import com.sportmanagement.manager.domain.model.BlockType
import com.sportmanagement.manager.domain.model.BlockedSlot
import com.sportmanagement.manager.domain.model.BookingCustomer
import com.sportmanagement.manager.domain.model.BookingHistoryEvent
import com.sportmanagement.manager.domain.model.BookingItem
import com.sportmanagement.manager.domain.model.BookingStatus
import com.sportmanagement.manager.domain.model.ChatMessage
import com.sportmanagement.manager.domain.model.ConversationItem
import com.sportmanagement.manager.domain.model.MessageStatus
import com.sportmanagement.manager.domain.model.Court
import com.sportmanagement.manager.domain.model.CourtStatus
import com.sportmanagement.manager.domain.model.FieldPolicy
import com.sportmanagement.manager.domain.model.FieldScheduleConfig
import com.sportmanagement.manager.domain.model.FieldService
import com.sportmanagement.manager.domain.model.Pitch
import com.sportmanagement.manager.domain.model.PitchDetail
import com.sportmanagement.manager.domain.model.PitchStatus
import com.sportmanagement.manager.domain.model.ServiceCategory
import com.sportmanagement.manager.domain.model.ServiceDetailItem
import com.sportmanagement.manager.domain.model.ServiceItemStatus
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// ── Field ──────────────────────────────────────────────────────────────────────

fun FieldDto.toPitch(): Pitch = Pitch(
    id = fieldId.toString(),
    name = fieldName,
    location = location ?: "",
    pricePerHour = slotPrice?.toLong() ?: 0L,
    imageUrl = resolveMediaUrl(cardImageUrl) ?: resolveMediaUrl(avatarImageUrl) ?: "",
    status = when (status.lowercase()) {
        "active"      -> PitchStatus.ACTIVE
        "maintenance" -> PitchStatus.MAINTENANCE
        "inactive"    -> PitchStatus.LOCKED
        else          -> PitchStatus.ACTIVE
    },
    rating = 0f,
    bookingCount = 0
)

fun FieldCourtDto.toCourt(): Court = Court(
    id = courtId.toString(),
    fieldId = fieldId.toString(),
    courtCode = courtCode,
    courtName = courtName,
    status = if (status == "active") CourtStatus.ACTIVE else CourtStatus.INACTIVE,
    sortOrder = sortOrder
)

fun FieldDto.toPitchDetail(
    courts: List<Court> = emptyList(),
    services: List<FieldService> = emptyList(),
    policies: List<FieldPolicy> = emptyList()
): PitchDetail = PitchDetail(
    id = fieldId.toString(),
    name = fieldName,
    sportType = sportName ?: "",
    location = location ?: "",
    latitude = null,
    longitude = null,
    phone = phone ?: "",
    status = when (status.lowercase()) {
        "active"      -> PitchStatus.ACTIVE
        "maintenance" -> PitchStatus.MAINTENANCE
        else          -> PitchStatus.LOCKED
    },
    avatarImageUrl = resolveMediaUrl(avatarImageUrl) ?: resolveMediaUrl(cardImageUrl) ?: "",
    cardImageUrl = resolveMediaUrl(cardImageUrl) ?: resolveMediaUrl(avatarImageUrl) ?: "",
    galleryUrls = listOfNotNull(resolveMediaUrl(avatarImageUrl), resolveMediaUrl(cardImageUrl)).distinct(),
    rating = 0f,
    bookingCount = 0,
    scheduleConfig = FieldScheduleConfig(
        openTime = openTime ?: "06:00",
        closeTime = closeTime ?: "22:00",
        slotMinutes = slotMinutes ?: 60,
        slotPrice = slotPrice?.toLong() ?: 0L,
        pendingHoldMinutes = 15
    ),
    courts = courts,
    services = services,
    policies = policies,
    blockedSlots = emptyList()
)

fun FieldServiceDto.toFieldService(): FieldService = FieldService(
    id = id.toString(),
    fieldId = fieldId.toString(),
    serviceName = serviceName,
    isFree = when (isFree) {
        is Boolean -> isFree
        is Number  -> isFree.toInt() != 0
        is String  -> isFree == "1" || isFree.equals("true", ignoreCase = true)
        else       -> false
    },
    price = when (price) {
        is Number -> price.toLong()
        is String -> price.toDoubleOrNull()?.toLong() ?: 0L
        else      -> 0L
    }
)

fun FieldPolicyDto.toFieldPolicy(): FieldPolicy = FieldPolicy(
    id = id.toString(),
    fieldId = fieldId.toString(),
    policyType = policyType,
    title = title,
    content = content
)

fun FieldServiceDto.toServiceDetailItem(): ServiceDetailItem {
    val resolvedFree = when (isFree) {
        is Boolean -> isFree
        is Number  -> isFree.toInt() != 0
        is String  -> isFree == "1" || isFree.equals("true", ignoreCase = true)
        else       -> false
    }
    val resolvedPrice = when (price) {
        is Number -> price.toLong()
        is String -> price.toDoubleOrNull()?.toLong() ?: 0L
        else      -> 0L
    }
    return ServiceDetailItem(
    id = id.toString(),
    name = serviceName,
    category = ServiceCategory.OTHER,
    price = resolvedPrice,
    stock = if (resolvedFree) -1 else 0,
    maxStock = -1,
    status = ServiceItemStatus.AVAILABLE,
    isActive = true,
    description = description ?: "",
    soldCount = 0,
    revenue = 0L,
    stockTransactions = emptyList()
)
}

// ── Chat ───────────────────────────────────────────────────────────────────────

fun ChatListDto.toConversationItem(): ConversationItem = ConversationItem(
    id = chatId.toString(),
    customerName = customerName ?: "Khách hàng",
    customerPhone = customerPhone ?: "",
    customerAvatarUrl = customerAvatar,
    isOnline = false,
    lastMessage = lastMessage ?: "",
    lastMessageTime = formatChatTime(lastMessageTime),
    unreadCount = unreadCount ?: 0,
    totalBookings = 0
)

fun ChatMessageDto.toChatMessage(currentUserId: Int): ChatMessage = ChatMessage(
    id = messageId.toString(),
    content = content ?: "",
    isFromManager = senderId == currentUserId,
    timestamp = formatChatTime(sentAt),
    rawTimestamp = sentAt,
    isRead = isRead ?: true,
    status = MessageStatus.SENT
)

private fun formatChatTime(raw: String?): String {
    if (raw == null) return ""
    val date = parseIso(raw) ?: return raw
    val now = java.util.Date()
    val diffMs = now.time - date.time
    val diffDays = diffMs / (1000 * 60 * 60 * 24)
    return if (diffDays < 1) chatTimeFmt.format(date) else chatDateTimeFmt.format(date)
}

fun BlockedSlotDto.toBlockedSlot(): BlockedSlot = BlockedSlot(
    id = slotId.toString(),
    fieldId = fieldId.toString(),
    courtId = courtId?.toString(),
    blockDate = blockDate,
    startTime = startTime.take(5),  // "12:00:00" → "12:00"
    endTime = endTime.take(5),
    reason = reason ?: "",
    blockType = when (blockType.lowercase()) {
        "event"       -> BlockType.EVENT
        "other"       -> BlockType.OTHER
        else          -> BlockType.MAINTENANCE
    }
)

fun BookingHistoryDto.toHistoryEvent(): BookingHistoryEvent = BookingHistoryEvent(
    timestamp = formatChatTime(createdAt),
    action = action,
    note = note ?: "",
    author = author ?: "system"
)

// ── Booking ────────────────────────────────────────────────────────────────────

private val utc = TimeZone.getTimeZone("UTC")
private val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).also { it.timeZone = utc }
private val isoFmtAlt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).also { it.timeZone = utc }
private val bookingIsoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
private val bookingIsoFmtAlt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
private val bookingLocalIsoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
private val bookingLocalIsoFmtAlt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
private val bookingSpaceFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
private val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))
private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
private val chatTimeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
private val chatDateTimeFmt = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
private val dayFmt = SimpleDateFormat("EEEE", Locale("vi", "VN"))

private fun parseIso(raw: String?): java.util.Date? {
    if (raw == null) return null
    return runCatching { isoFmt.parse(raw) }.getOrNull()
        ?: runCatching { isoFmtAlt.parse(raw) }.getOrNull()
        ?: runCatching {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .also { it.timeZone = utc }
                .parse(raw)
        }.getOrNull()
}

private fun durationMinutes(start: String?, end: String?): Int {
    val s = parseBookingDateTime(start) ?: return 0
    val e = parseBookingDateTime(end) ?: return 0
    return ((e.time - s.time) / 60_000).toInt().coerceAtLeast(0)
}

private fun parseBookingDateTime(raw: String?): java.util.Date? {
    if (raw == null) return null
    val normalized = raw.trim()
    if (normalized.isBlank()) return null

    // Booking timestamps represent local wall-clock times from the backend DB,
    // so we intentionally parse them as local time instead of UTC.
    return runCatching { bookingIsoFmt.parse(normalized) }.getOrNull()
        ?: runCatching { bookingIsoFmtAlt.parse(normalized) }.getOrNull()
        ?: runCatching { bookingLocalIsoFmt.parse(normalized) }.getOrNull()
        ?: runCatching { bookingLocalIsoFmtAlt.parse(normalized) }.getOrNull()
        ?: runCatching { bookingSpaceFmt.parse(normalized) }.getOrNull()
}

private fun resolveMediaUrl(value: String?): String? {
    val trimmed = value?.trim().orEmpty()
    if (trimmed.isBlank() || trimmed.equals("null", ignoreCase = true)) return null
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
    val base = NetworkClient.BASE_URL.trimEnd('/')
    return "$base${if (trimmed.startsWith("/")) trimmed else "/$trimmed"}"
}

private fun normalizePaymentMethodLabel(value: String?): String {
    return when (value?.trim()?.lowercase()) {
        null, "" -> ""
        "momo" -> "MoMo"
        "bank_transfer" -> "Chuyển khoản"
        "cash" -> "Tiền mặt"
        "zalopay" -> "ZaloPay"
        "vnpay" -> "VNPay"
        "credit_card" -> "Thẻ tín dụng"
        else -> value.trim()
    }
}

fun BookingDto.toBookingItem(): BookingItem {
    val startDate = parseBookingDateTime(startTime)
    val endDate = parseBookingDateTime(endTime)
    val createdAtDate = parseBookingDateTime(createdAt) ?: parseIso(createdAt)
    val bookingPaymentStatus = paymentStatus.orEmpty()
    val bookingPaymentMethod = normalizePaymentMethodLabel(paymentMethod)
        .ifBlank { if (status.equals("confirmed", ignoreCase = true) && managerCreated != 1) "MoMo" else "" }

    val customer = BookingCustomer(
        id = customerId?.toString() ?: "0",
        name = customerName ?: "Khách hàng",
        phone = customerPhone ?: "",
        email = customerEmail ?: "",
        avatarUrl = null,
        totalBookings = 0,
        totalSpend = 0L,
        memberSince = ""
    )

    val bookingStatus = when (status?.lowercase()) {
        "pending"   -> BookingStatus.PENDING
        "confirmed" -> BookingStatus.CONFIRMED
        "completed" -> BookingStatus.COMPLETED
        "cancelled", "rejected" -> BookingStatus.CANCELLED
        else -> BookingStatus.PENDING
    }

    return BookingItem(
        id = bookingId.toString(),
        pitchName = fieldName ?: "",
        courtCode = courtCode ?: courtId?.toString() ?: "",
        courtName = courtName ?: "",
        customer = customer,
        date = startDate?.let { dateFmt.format(it) } ?: "",
        dayOfWeek = startDate?.let { dayFmt.format(it) }
            ?.replaceFirstChar { it.uppercaseChar() } ?: "",
        startTime = startDate?.let { timeFmt.format(it) } ?: "",
        endTime = endDate?.let { timeFmt.format(it) } ?: "",
        createdAtTime = createdAtDate?.let { timeFmt.format(it) } ?: "",
        durationMinutes = durationMinutes(startTime, endTime),
        pricePerHour = price?.toLong() ?: 0L,
        totalPrice = price?.toLong() ?: 0L,
        depositPaid = 0L,
        status = bookingStatus,
        paymentStatus = bookingPaymentStatus,
        isPaid = bookingPaymentStatus.equals("completed", ignoreCase = true) ||
            bookingStatus == BookingStatus.COMPLETED ||
            (bookingStatus == BookingStatus.CONFIRMED && managerCreated != 1),
        paymentMethod = bookingPaymentMethod,
        notes = note ?: "",
        isManagerCreated = managerCreated == 1,
        fieldId = fieldId,
        courtId = courtId
    )
}
