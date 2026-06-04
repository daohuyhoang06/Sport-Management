package com.sportmanagement.user.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.sportmanagement.user.BuildConfig
import com.sportmanagement.user.R
import com.sportmanagement.user.data.remote.api.CreateBookingRequest
import com.sportmanagement.user.data.remote.api.MomoPaymentApi
import com.sportmanagement.user.data.remote.api.MomoPaymentResponse
import com.sportmanagement.user.data.remote.api.UserApi
import com.sportmanagement.user.domain.model.BookingConfirmationData
import com.sportmanagement.user.ui.AppNavigationBarEffect
import com.sportmanagement.user.ui.components.booking.formatConfirmationCurrencyVnd
import com.sportmanagement.user.ui.share.FieldShareLink
import com.sportmanagement.user.ui.share.FieldShareLink.MomoPaymentReturn
import com.sportmanagement.user.ui.theme.AppBadgeCornerRadius
import com.sportmanagement.user.ui.theme.AppCardCornerRadius
import com.sportmanagement.user.ui.theme.AppCtaAmber
import com.sportmanagement.user.ui.theme.AppCtaCornerRadius
import com.sportmanagement.user.ui.theme.AppCtaWideHeight
import com.sportmanagement.user.ui.theme.AppHeaderGradientEnd
import com.sportmanagement.user.ui.theme.AppHeaderGradientStart
import com.sportmanagement.user.ui.theme.AppOnCtaAmber
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private sealed interface PaymentCreateUiState {
    data object Idle : PaymentCreateUiState
    data object Loading : PaymentCreateUiState
    data class Ready(val response: MomoPaymentResponse) : PaymentCreateUiState
    data class Error(val message: String) : PaymentCreateUiState
}

private enum class PaymentReturnStatus {
    Idle,
    Pending,
    Success,
    Failed
}

@Composable
fun BookingPaymentScreen(
    confirmationData: BookingConfirmationData,
    userName: String,
    userPhone: String,
    bookingNote: String,
    incomingMomoPaymentReturn: MomoPaymentReturn?,
    onMomoPaymentReturnConsumed: () -> Unit,
    onBackClick: () -> Unit,
    onViewInvoiceClick: (BookingInfo) -> Unit,
    onViewCancelledBookingClick: () -> Unit,
    onReturnHomeClick: () -> Unit,
    onPaymentConfirmed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userApi = remember { UserApi() }
    var bookingInfoExpanded by rememberSaveable { mutableStateOf(true) }
    var createPaymentNonce by rememberSaveable { mutableIntStateOf(0) }
    var createState by remember { mutableStateOf<PaymentCreateUiState>(PaymentCreateUiState.Idle) }
    var paymentStatus by rememberSaveable { mutableStateOf(PaymentReturnStatus.Idle) }
    var paymentMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var reopenedPayUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var currentOrderId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingBookingIds by rememberSaveable { mutableStateOf(emptyList<Int>()) }
    var remainingHoldSeconds by rememberSaveable { mutableIntStateOf(6 * 60) }
    var isHoldExpired by rememberSaveable { mutableStateOf(false) }

    val totalAmount = remember(confirmationData.totalPrice) {
        confirmationData.totalPrice.coerceAtLeast(1_000)
    }
    val paidAmount = if (paymentStatus == PaymentReturnStatus.Success) totalAmount else 0
    val dueAmount = (totalAmount - paidAmount).coerceAtLeast(0)
    val lowerBackgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.36f)
    AppNavigationBarEffect(
        navigationBarColor = lowerBackgroundColor,
        useDarkIcons = lowerBackgroundColor.luminance() > 0.5f
    )
    val invoiceInfo = remember(
        confirmationData,
        userName,
        userPhone,
        currentOrderId,
        pendingBookingIds,
        paymentMessage
    ) {
        val fieldName = confirmationData.fieldName.ifBlank {
            confirmationData.ranges.firstOrNull()?.courtName
                ?: context.getString(R.string.payment_field_unknown)
        }
        val fieldAddress = confirmationData.fieldAddress.ifBlank {
            context.getString(R.string.payment_field_unknown)
        }
        val timeRange = confirmationData.ranges.joinToString("\n") { range ->
            "${range.courtName}: ${range.startTimeLabel} - ${range.endTimeLabel}"
        }
        val bookingCode = pendingBookingIds.firstOrNull()?.let { "#B$it" }
            ?: currentOrderId
            ?: context.getString(R.string.payment_order_code_pending)

        BookingInfo(
            fieldName = fieldName,
            timeRange = timeRange.ifBlank { confirmationData.selectedDate },
            dateLabel = confirmationData.selectedDate,
            bookingCode = bookingCode,
            statusLabel = context.getString(R.string.payment_status_success),
            statusCode = "paid",
            address = fieldAddress,
            paymentMethod = context.getString(R.string.payment_method_momo_title),
            totalAmount = formatConfirmationCurrencyVnd(totalAmount),
            transactionId = "",
            orderId = currentOrderId ?: "",
            checkInCode = "",
            shareUrl = "",
            customerName = userName.ifBlank { context.getString(R.string.payment_customer_name_placeholder) },
            customerPhone = userPhone.ifBlank { context.getString(R.string.payment_customer_phone_placeholder) },
            ownerPhone = "",
            ownerNote = paymentMessage ?: context.getString(R.string.payment_message_momo_confirmed),
            fieldId = confirmationData.fieldId,
            bookingId = pendingBookingIds.firstOrNull(),
            notificationId = null
        )
    }

    LaunchedEffect(createPaymentNonce) {
        if (createPaymentNonce == 0) return@LaunchedEffect

        createState = PaymentCreateUiState.Loading
        val orderInfo = context.getString(R.string.payment_order_info_format, confirmationData.selectedDate)
        runCatching {
            val authToken = loadAuthToken(context)
                ?: error(context.getString(R.string.payment_auth_required_error))

            val bookingIds = if (pendingBookingIds.isNotEmpty()) {
                pendingBookingIds
            } else {
                val fieldId = confirmationData.fieldId
                    ?: error(context.getString(R.string.payment_booking_missing_field_error))
                val createRequests = confirmationData.ranges.map { range ->
                    CreateBookingRequest(
                        fieldId = fieldId,
                        courtId = range.courtId.toIntOrNull(),
                        startTime = buildBookingDateTimeIsoUtc(
                            dateText = confirmationData.selectedDate,
                            timeText = range.startTimeLabel
                        ),
                        endTime = buildBookingDateTimeIsoUtc(
                            dateText = confirmationData.selectedDate,
                            timeText = range.endTimeLabel
                        ),
                        price = range.price,
                        note = null,
                        customerName = null,
                        customerPhone = null
                    )
                }
                val batchResult = userApi.createBookings(
                    token = authToken,
                    fieldId = fieldId,
                    requests = createRequests,
                    note = bookingNote,
                    customerName = userName,
                    customerPhone = userPhone
                )
                val createdIds = batchResult.bookings.map { it.bookingId }.filter { it > 0 }
                if (createdIds.isEmpty() || createdIds.size != createRequests.size) {
                    error(context.getString(R.string.payment_booking_create_error))
                }
                pendingBookingIds = createdIds
                if (batchResult.pendingHoldSeconds > 0) {
                    remainingHoldSeconds = batchResult.pendingHoldSeconds
                }
                createdIds
            }

            MomoPaymentApi.createPayment(
                token = authToken,
                bookingIds = bookingIds,
                orderInfo = orderInfo,
                redirectUrl = FieldShareLink.momoReturnLink()
            )
        }.onSuccess { response ->
            createState = PaymentCreateUiState.Ready(response)
            currentOrderId = response.orderId
            paymentStatus = PaymentReturnStatus.Pending
            paymentMessage = context.getString(R.string.payment_message_waiting_for_momo)
            val nextUrl = response.deeplink ?: response.payUrl ?: response.qrCodeUrl
            reopenedPayUrl = nextUrl
            if (nextUrl.isNullOrBlank()) {
                createState = PaymentCreateUiState.Error(
                    context.getString(R.string.payment_open_link_error)
                )
                paymentStatus = PaymentReturnStatus.Failed
                paymentMessage = context.getString(R.string.payment_open_link_error)
            } else {
                openPaymentUrl(context, nextUrl) {
                    createState = PaymentCreateUiState.Error(
                        context.getString(R.string.payment_open_link_fail)
                    )
                    paymentStatus = PaymentReturnStatus.Failed
                    paymentMessage = context.getString(R.string.payment_open_link_fail)
                }
            }
        }.onFailure { error ->
            createState = PaymentCreateUiState.Error(
                error.message ?: context.getString(R.string.payment_sandbox_create_error)
            )
            paymentStatus = PaymentReturnStatus.Failed
            paymentMessage = error.message ?: context.getString(R.string.payment_sandbox_create_error)
        }
    }

    LaunchedEffect(incomingMomoPaymentReturn) {
        val callback = incomingMomoPaymentReturn ?: return@LaunchedEffect
        val sandboxAutoComplete = BuildConfig.PAYMENT_SANDBOX_AUTO_COMPLETE
        if (currentOrderId == null || currentOrderId == callback.orderId) {
            currentOrderId = callback.orderId ?: currentOrderId
            paymentStatus = when (callback.resultCode) {
                0 -> PaymentReturnStatus.Success
                null -> if (sandboxAutoComplete) PaymentReturnStatus.Success else PaymentReturnStatus.Pending
                else -> PaymentReturnStatus.Failed
            }.let { status ->
                if (sandboxAutoComplete && status != PaymentReturnStatus.Success) {
                    PaymentReturnStatus.Success
                } else {
                    status
                }
            }
            paymentMessage = if (sandboxAutoComplete) {
                context.getString(R.string.payment_message_momo_confirmed)
            } else {
                callback.message
            }
        }

        val authToken = loadAuthToken(context)
        val orderId = callback.orderId
        val requestId = callback.requestId
        val resultCode = when {
            sandboxAutoComplete -> 0
            else -> callback.resultCode
        }
        if (
            authToken != null &&
            !orderId.isNullOrBlank() &&
            resultCode != null
        ) {
            runCatching {
                MomoPaymentApi.confirmClientPaymentResult(
                    token = authToken,
                    orderId = orderId,
                    requestId = requestId,
                    resultCode = resultCode,
                    message = callback.message
                )
            }.onSuccess {
                onPaymentConfirmed()
            }
        }
        onMomoPaymentReturnConsumed()
    }

    LaunchedEffect(paymentStatus, currentOrderId) {
        val orderId = currentOrderId ?: return@LaunchedEffect
        if (paymentStatus != PaymentReturnStatus.Pending) return@LaunchedEffect

        repeat(10) {
            delay(2_000)
            val status = runCatching { MomoPaymentApi.getPaymentByOrderId(orderId) }.getOrNull()
                ?: return@repeat
            when (status.paymentStatus?.lowercase()) {
                "completed" -> {
                    paymentStatus = PaymentReturnStatus.Success
                    paymentMessage = context.getString(R.string.payment_message_momo_confirmed)
                    onPaymentConfirmed()
                    return@LaunchedEffect
                }
                "failed" -> {
                    paymentStatus = PaymentReturnStatus.Failed
                    paymentMessage = status.failureReason ?: context.getString(R.string.payment_message_momo_failed)
                    return@LaunchedEffect
                }
            }
        }
    }

    LaunchedEffect(paymentStatus, currentOrderId) {
        if (!BuildConfig.PAYMENT_SANDBOX_AUTO_COMPLETE) return@LaunchedEffect
        if (paymentStatus != PaymentReturnStatus.Pending) return@LaunchedEffect

        delay(4_000)
        if (paymentStatus != PaymentReturnStatus.Pending) return@LaunchedEffect

        paymentStatus = PaymentReturnStatus.Success
        paymentMessage = context.getString(R.string.payment_message_momo_confirmed)
        val authToken = loadAuthToken(context)
        val orderId = currentOrderId ?: return@LaunchedEffect
        if (authToken != null) {
            runCatching {
                MomoPaymentApi.confirmClientPaymentResult(
                    token = authToken,
                    orderId = orderId,
                    requestId = null,
                    resultCode = 0,
                    message = context.getString(R.string.payment_message_momo_confirmed)
                )
            }.onSuccess {
                onPaymentConfirmed()
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            if (isHoldExpired || paymentStatus == PaymentReturnStatus.Success) continue
            if (remainingHoldSeconds > 0) {
                remainingHoldSeconds -= 1
            }
            if (remainingHoldSeconds <= 0 && paymentStatus != PaymentReturnStatus.Success) {
                isHoldExpired = true
                paymentStatus = PaymentReturnStatus.Failed
                paymentMessage = context.getString(R.string.payment_pending_expired_message)
            }
        }
    }

    val showExpiredScreen = isHoldExpired && paymentStatus != PaymentReturnStatus.Success

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = lowerBackgroundColor,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(lowerBackgroundColor)
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (showExpiredScreen) {
                        Button(
                            onClick = onViewCancelledBookingClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(AppCtaWideHeight),
                            shape = RoundedCornerShape(AppCtaCornerRadius),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.payment_view_cancelled_booking),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Button(
                            onClick = onReturnHomeClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(AppCtaWideHeight),
                            shape = RoundedCornerShape(AppCtaCornerRadius),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppCtaAmber,
                                contentColor = AppOnCtaAmber
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.payment_back_home),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                when {
                                    paymentStatus == PaymentReturnStatus.Success -> onViewInvoiceClick(invoiceInfo)
                                    paymentStatus == PaymentReturnStatus.Pending && !reopenedPayUrl.isNullOrBlank() -> {
                                        openPaymentUrl(context, reopenedPayUrl!!) {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.payment_open_link_fail),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    createState !is PaymentCreateUiState.Loading -> {
                                        paymentMessage = null
                                        createPaymentNonce += 1
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(AppCtaWideHeight),
                            shape = RoundedCornerShape(AppCtaCornerRadius),
                            enabled = createState !is PaymentCreateUiState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (paymentStatus == PaymentReturnStatus.Success) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    AppCtaAmber
                                },
                                contentColor = if (paymentStatus == PaymentReturnStatus.Success) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    AppOnCtaAmber
                                }
                            )
                        ) {
                            if (createState is PaymentCreateUiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.size(10.dp))
                            }
                            Text(
                                text = when {
                                    paymentStatus == PaymentReturnStatus.Success -> stringResource(R.string.payment_view_invoice)
                                    else -> stringResource(R.string.payment_pay_only)
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (paymentStatus == PaymentReturnStatus.Success || paymentStatus == PaymentReturnStatus.Failed) {
                            TextButton(
                                onClick = onReturnHomeClick,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(text = stringResource(R.string.payment_return_home))
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PaymentHeader(onBackClick = onBackClick)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(lowerBackgroundColor)
            ) {
                if (showExpiredScreen) {
                    PaymentExpiredSection(
                        message = stringResource(R.string.payment_pending_expired_message)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 10.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                                    .animateContentSize(),
                                shape = RoundedCornerShape(AppCardCornerRadius),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                                ),
                                tonalElevation = 2.dp,
                                shadowElevation = 3.dp
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = stringResource(R.string.payment_booking_info_title),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        IconButton(onClick = { bookingInfoExpanded = !bookingInfoExpanded }) {
                                            Icon(
                                                imageVector = if (bookingInfoExpanded) {
                                                    Icons.Default.ExpandLess
                                                } else {
                                                    Icons.Default.ExpandMore
                                                },
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (bookingInfoExpanded) {
                                        val fieldName = confirmationData.fieldName.ifBlank {
                                            stringResource(R.string.payment_field_unknown)
                                        }
                                        val fieldAddress = confirmationData.fieldAddress.ifBlank {
                                            stringResource(R.string.payment_field_unknown)
                                        }
                                        val bookingDetail = buildString {
                                            append(confirmationData.selectedDate)
                                            confirmationData.ranges.forEach { range ->
                                                append("\n- ")
                                                append(range.courtName)
                                                append(": ")
                                                append(range.startTimeLabel)
                                                append(" - ")
                                                append(range.endTimeLabel)
                                            }
                                        }

                                        PaymentInfoItem(
                                            icon = Icons.Default.Map,
                                            label = stringResource(R.string.payment_field_label),
                                            value = fieldName
                                        )
                                        PaymentInfoItem(
                                            icon = Icons.Default.Map,
                                            label = stringResource(R.string.payment_field_address_label),
                                            value = fieldAddress,
                                            allowMultiline = true
                                        )
                                        PaymentInfoItem(
                                            icon = Icons.Default.PersonOutline,
                                            label = stringResource(R.string.payment_customer_name_label),
                                            value = userName.ifBlank { stringResource(R.string.payment_customer_name_placeholder) }
                                        )
                                        PaymentInfoItem(
                                            icon = Icons.Default.PhoneIphone,
                                            label = stringResource(R.string.payment_customer_phone_label),
                                            value = userPhone.ifBlank { stringResource(R.string.payment_customer_phone_placeholder) }
                                        )
                                        PaymentInfoItem(
                                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                            label = stringResource(R.string.payment_order_code_label),
                                            value = currentOrderId ?: stringResource(R.string.payment_order_code_pending)
                                        )
                                        PaymentInfoItem(
                                            icon = Icons.Default.CalendarToday,
                                            label = stringResource(R.string.payment_booking_detail_label),
                                            value = bookingDetail,
                                            allowMultiline = true
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            PaymentMethodSummarySection(
                                totalAmount = totalAmount,
                                paidAmount = paidAmount,
                                dueAmount = dueAmount
                            )
                        }
                        if (paymentStatus != PaymentReturnStatus.Success) {
                            item {
                                PaymentHoldSlotSection(
                                    remainingSeconds = remainingHoldSeconds.coerceAtLeast(0)
                                )
                            }
                        }

                        if (paymentStatus != PaymentReturnStatus.Idle || createState is PaymentCreateUiState.Error) {
                            item {
                                PaymentStatusSection(
                                    paymentStatus = paymentStatus,
                                    createState = createState,
                                    paymentMessage = paymentMessage
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentHoldSlotSection(
    remainingSeconds: Int
) {
    val minutes = (remainingSeconds / 60).coerceAtLeast(0)
    val seconds = (remainingSeconds % 60).coerceAtLeast(0)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.payment_pending_instruction_line_2),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.payment_pending_time_format, minutes, seconds),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PaymentExpiredSection(
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(56.dp))
        Box(
            modifier = Modifier.size(196.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(150.dp),
                shape = CircleShape,
                color = Color(0xFF2D557A)
            ) {}
            Surface(
                modifier = Modifier.size(116.dp),
                shape = CircleShape,
                color = Color(0xFFE8F0F8)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = Color(0xFF2D557A),
                        modifier = Modifier.size(52.dp)
                    )
                }
            }
            Surface(
                modifier = Modifier
                    .size(84.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-4).dp, y = (-2).dp),
                shape = CircleShape,
                color = Color(0xFFE25C6C)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PaymentHeader(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(AppHeaderGradientStart, AppHeaderGradientEnd)
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.booking_back_content_description),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text(
                text = stringResource(R.string.payment_title),
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Default),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun PaymentInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    allowMultiline: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    RoundedCornerShape(AppBadgeCornerRadius)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (allowMultiline) 5 else 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PaymentMethodSummarySection(
    totalAmount: Int,
    paidAmount: Int,
    dueAmount: Int
) {
    var selectedMethod by rememberSaveable { mutableStateOf("momo") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(AppCardCornerRadius),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
            ),
            tonalElevation = 2.dp,
            shadowElevation = 3.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.payment_method_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )

                PaymentMethodRow(
                    title = stringResource(R.string.payment_method_momo_title),
                    description = stringResource(R.string.payment_method_momo_desc),
                    selected = selectedMethod == "momo",
                    customSvgRes = R.raw.momo_logo_app,
                    iconContainerSize = 42.dp,
                    iconPadding = 2.dp,
                    showIconBorder = false,
                    onClick = { selectedMethod = "momo" }
                )
                PaymentMethodRow(
                    title = stringResource(R.string.payment_method_zalopay_title),
                    description = stringResource(R.string.payment_method_zalopay_desc),
                    selected = selectedMethod == "zalopay",
                    accentColor = Color(0xFF0068FF),
                    badgeText = "ZP",
                    customSvgRes = R.raw.zalopay_logo,
                    emphasizeTitle = true,
                    iconContainerSize = 42.dp,
                    iconPadding = 1.dp,
                    showIconBorder = false,
                    onClick = { selectedMethod = "zalopay" }
                )
                PaymentMethodRow(
                    title = stringResource(R.string.payment_method_bank_title),
                    description = stringResource(R.string.payment_method_bank_desc),
                    selected = selectedMethod == "bank",
                    accentColor = Color(0xFF2563EB),
                    badgeText = "NH",
                    customDrawableRes = R.drawable.bank,
                    customLogoTint = Color(0xFF143D8F),
                    iconContainerSize = 42.dp,
                    iconPadding = 5.dp,
                    showIconBorder = true,
                    iconBorderWidth = 1.dp,
                    iconBorderAlpha = 0.18f,
                    onClick = { selectedMethod = "bank" }
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(AppCardCornerRadius),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
            ),
            tonalElevation = 2.dp,
            shadowElevation = 3.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.payment_summary_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )

                PaymentAmountRow(
                    label = stringResource(R.string.payment_total_price_label),
                    value = formatConfirmationCurrencyVnd(totalAmount),
                    compactValue = true
                )
                PaymentAmountRow(
                    label = stringResource(R.string.payment_paid_amount_label),
                    value = formatConfirmationCurrencyVnd(paidAmount)
                )
                DashedAmountSeparator()
                PaymentAmountRow(
                    label = stringResource(R.string.payment_due_amount_plain_label),
                    value = formatConfirmationCurrencyVnd(dueAmount),
                    emphasize = true
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodRow(
    title: String,
    description: String,
    selected: Boolean,
    accentColor: Color = Color(0xFFA50064),
    badgeText: String = "mo",
    customSvgRes: Int? = null,
    customDrawableRes: Int? = null,
    customLogoTint: Color? = null,
    emphasizeTitle: Boolean = false,
    iconContainerSize: Dp = 36.dp,
    iconPadding: Dp = 4.dp,
    showIconBorder: Boolean = true,
    iconBorderWidth: Dp = 1.dp,
    iconBorderAlpha: Float = 0.45f,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AppCardCornerRadius),
        color = Color.White,
        border = BorderStroke(
            width = if (selected) 1.4.dp else 1.dp,
            color = if (selected) accentColor.copy(alpha = 0.85f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(iconContainerSize),
                shape = RoundedCornerShape(12.dp),
                color = if (customSvgRes != null || customDrawableRes != null) Color.White else accentColor,
                border = if (customSvgRes != null || customDrawableRes != null) {
                    if (showIconBorder) BorderStroke(iconBorderWidth, accentColor.copy(alpha = iconBorderAlpha)) else null
                } else {
                    null
                }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (customSvgRes != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(customSvgRes)
                                .decoderFactory(SvgDecoder.Factory())
                                .crossfade(true)
                                .build(),
                            contentDescription = title,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(iconPadding),
                            contentScale = ContentScale.Fit,
                            colorFilter = customLogoTint?.let { ColorFilter.tint(it) }
                        )
                    } else if (customDrawableRes != null) {
                        Image(
                            painter = painterResource(id = customDrawableRes),
                            contentDescription = title,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(iconPadding),
                            contentScale = ContentScale.Fit,
                            colorFilter = customLogoTint?.let { ColorFilter.tint(it) }
                        )
                    } else {
                        Text(
                            text = badgeText,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = if (emphasizeTitle) 17.sp else 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (selected) {
                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = CircleShape,
                    color = accentColor
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DashedAmountSeparator() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = Color(0xFFB8C0CC),
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
        )
    }
}

@Composable
private fun PaymentAmountRow(
    label: String,
    value: String,
    emphasize: Boolean = false,
    compactValue: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (emphasize) Color(0xFFA50064) else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Normal
        )
        Text(
            text = value,
            style = if (emphasize) {
                MaterialTheme.typography.headlineSmall
            } else if (compactValue) {
                MaterialTheme.typography.bodyLarge
            } else {
                MaterialTheme.typography.titleSmall
            },
            color = if (emphasize) Color(0xFFA50064) else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (emphasize) FontWeight.ExtraBold else FontWeight.SemiBold
        )
    }
}

@Composable
private fun PaymentStatusSection(
    paymentStatus: PaymentReturnStatus,
    createState: PaymentCreateUiState,
    paymentMessage: String?
) {
    val statusLabel = when {
        createState is PaymentCreateUiState.Loading -> stringResource(R.string.payment_status_creating_transaction)
        paymentStatus == PaymentReturnStatus.Pending -> stringResource(R.string.payment_status_waiting)
        paymentStatus == PaymentReturnStatus.Success -> stringResource(R.string.payment_status_success)
        else -> stringResource(R.string.payment_status_failed)
    }
    val icon = when {
        createState is PaymentCreateUiState.Loading || paymentStatus == PaymentReturnStatus.Pending ->
            Icons.Default.HourglassTop
        paymentStatus == PaymentReturnStatus.Success -> Icons.Default.CheckCircle
        else -> Icons.Default.ErrorOutline
    }
    val tint = when {
        createState is PaymentCreateUiState.Loading || paymentStatus == PaymentReturnStatus.Pending ->
            Color(0xFFD97706)
        paymentStatus == PaymentReturnStatus.Success -> Color(0xFF15803D)
        else -> Color(0xFFDC2626)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(AppCardCornerRadius),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.25f)),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(tint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (createState is PaymentCreateUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = tint
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                if (!paymentMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = paymentMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun loadAuthToken(context: android.content.Context): String? {
    return context
        .applicationContext
        .getSharedPreferences("user_repository_cache", android.content.Context.MODE_PRIVATE)
        .getString("auth_token", null)
        ?.takeIf { it.isNotBlank() }
}

private fun buildBookingDateTimeIsoUtc(
    dateText: String,
    timeText: String
): String {
    val patterns = listOf("yyyy-MM-dd HH:mm", "dd/MM/yyyy HH:mm")
    val sourceTimeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")

    for (pattern in patterns) {
        val parser = SimpleDateFormat(pattern, Locale.getDefault()).apply {
            isLenient = false
            timeZone = sourceTimeZone
        }
        val parsed = runCatching { parser.parse("$dateText $timeText") }.getOrNull()
        if (parsed != null) {
            return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(parsed)
        }
    }

    error("Invalid booking datetime: $dateText $timeText")
}

private fun openPaymentUrl(
    context: android.content.Context,
    url: String,
    onFailure: () -> Unit
) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: Exception) {
        onFailure()
    }
}
