package com.sportmanagement.user.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WarningAmber
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sportmanagement.user.R
import com.sportmanagement.user.data.remote.api.MomoPaymentApi
import com.sportmanagement.user.data.remote.api.MomoPaymentResponse
import com.sportmanagement.user.domain.model.BookingConfirmationData
import com.sportmanagement.user.ui.components.booking.formatConfirmationCurrencyVnd
import com.sportmanagement.user.ui.theme.AppBadgeCornerRadius
import com.sportmanagement.user.ui.theme.AppCardCornerRadius
import com.sportmanagement.user.ui.theme.AppAccentCitrus
import com.sportmanagement.user.ui.theme.AppCtaAmber
import com.sportmanagement.user.ui.theme.AppCtaCornerRadius
import com.sportmanagement.user.ui.theme.AppCtaWideHeight
import com.sportmanagement.user.ui.theme.AppHeaderGradientEnd
import com.sportmanagement.user.ui.theme.AppHeaderGradientStart
import com.sportmanagement.user.ui.theme.AppOnCtaAmber
import kotlinx.coroutines.delay

private data class PaymentReceiverAccount(
    val accountName: String,
    val accountNumber: String,
    val bankName: String
)

private sealed interface PaymentScreenUiState {
    data object Loading : PaymentScreenUiState
    data class Ready(val response: MomoPaymentResponse) : PaymentScreenUiState
    data class Error(val message: String) : PaymentScreenUiState
}

@Composable
fun BookingPaymentScreen(
    confirmationData: BookingConfirmationData,
    userName: String,
    userPhone: String,
    onBackClick: () -> Unit,
    onConfirmBookingClick: () -> Unit,
    onReturnHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var retryKey by rememberSaveable { mutableStateOf(0) }
    var bookingInfoExpanded by rememberSaveable { mutableStateOf(true) }
    var remainingPendingSeconds by rememberSaveable { mutableStateOf(5 * 60) }
    var paymentProofImageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var state by remember { mutableStateOf<PaymentScreenUiState>(PaymentScreenUiState.Loading) }
    val pickProofImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        paymentProofImageUri = uri?.toString()
    }

    val receiverAccount = remember {
        PaymentReceiverAccount(
            accountName = "LE THI MAI PHUONG",
            accountNumber = "0904926388",
            bankName = "Ví MoMo Sandbox"
        )
    }

    val sandboxAmount = remember(confirmationData.totalPrice) { confirmationData.totalPrice.coerceAtLeast(1_000) }
    val sandboxOrderInfo = remember(confirmationData.selectedDate) {
        "Thanh toan lich dat san ${confirmationData.selectedDate}"
    }
    val lowerBackgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.36f)

    LaunchedEffect(sandboxAmount, sandboxOrderInfo, retryKey) {
        state = PaymentScreenUiState.Loading
        state = try {
            PaymentScreenUiState.Ready(
                MomoPaymentApi.createDemoPayment(
                    amount = sandboxAmount,
                    orderInfo = sandboxOrderInfo
                )
            )
        } catch (error: Exception) {
            PaymentScreenUiState.Error(
                error.message ?: context.getString(R.string.payment_sandbox_create_error)
            )
        }
    }

    val currentState = state
    val paymentResponse = (currentState as? PaymentScreenUiState.Ready)?.response
    val paymentUrl = paymentResponse?.payUrl ?: paymentResponse?.deeplink ?: paymentResponse?.qrCodeUrl
    val fallbackQrUrl = remember {
        "https://quickchart.io/qr?text=${Uri.encode("MOMO_SANDBOX_TEST")}&size=320"
    }
    val qrDisplayUrl = resolveQrDisplayUrl(paymentResponse, paymentUrl) ?: fallbackQrUrl
    val isPendingExpired = remainingPendingSeconds <= 0

    LaunchedEffect(Unit) {
        while (remainingPendingSeconds > 0) {
            delay(1000)
            remainingPendingSeconds -= 1
        }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = lowerBackgroundColor,
        bottomBar = {
            if (isPendingExpired) {
                PaymentExpiredBottomActions(
                    onViewCancelledClick = {},
                    onReturnHomeClick = onReturnHomeClick,
                    backgroundColor = lowerBackgroundColor
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(lowerBackgroundColor)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Button(
                        onClick = onConfirmBookingClick,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .height(AppCtaWideHeight),
                        shape = RoundedCornerShape(AppCtaCornerRadius),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppCtaAmber,
                            contentColor = AppOnCtaAmber
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.payment_confirm_booking),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
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
                if (isPendingExpired) {
                    PaymentExpiredContent(
                        modifier = Modifier.fillMaxSize()
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
                                            icon = Icons.Default.PersonOutline,
                                            label = stringResource(R.string.payment_customer_name_label),
                                            value = userName
                                        )
                                        PaymentInfoItem(
                                            icon = Icons.Default.PhoneIphone,
                                            label = stringResource(R.string.payment_customer_phone_label),
                                            value = userPhone
                                        )
                                        PaymentInfoItem(
                                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                            label = stringResource(R.string.payment_order_code_label),
                                            value = paymentResponse?.orderId ?: stringResource(R.string.payment_order_code_pending)
                                        )
                                        PaymentInfoItem(
                                            icon = Icons.Default.CalendarToday,
                                            label = stringResource(R.string.payment_booking_detail_label),
                                            value = bookingDetail,
                                            allowMultiline = true
                                        )
                                        PaymentInfoItem(
                                            icon = Icons.Default.CreditCard,
                                            label = stringResource(R.string.payment_total_amount_label),
                                            value = formatConfirmationCurrencyVnd(confirmationData.totalPrice),
                                            emphasize = true
                                        )
                                        PaymentInfoItem(
                                            icon = Icons.Default.CreditCard,
                                            label = stringResource(R.string.payment_due_amount_label),
                                            value = formatConfirmationCurrencyVnd(confirmationData.totalPrice),
                                            emphasize = true
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
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
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                        val qrSize = if (maxWidth < 360.dp) 96.dp else 110.dp

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(end = 6.dp)
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.payment_bank_info_title),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                PaymentBankAccountSection(
                                                    account = receiverAccount,
                                                    onCopyAccount = {
                                                        clipboardManager.setText(AnnotatedString(receiverAccount.accountNumber))
                                                        Toast.makeText(
                                                            context,
                                                            context.getString(R.string.payment_copy_success),
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                )
                                            }
                                            PaymentQrPanel(
                                                state = currentState,
                                                qrDisplayUrl = qrDisplayUrl,
                                                size = qrSize
                                            )
                                        }
                                    }

                                    when (currentState) {
                                        PaymentScreenUiState.Loading -> Text(
                                            text = stringResource(R.string.payment_sandbox_creating),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        is PaymentScreenUiState.Error -> {
                                            Text(
                                                text = currentState.message,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                            TextButton(onClick = { retryKey++ }) {
                                                Text(text = stringResource(R.string.payment_retry_button))
                                            }
                                        }

                                        is PaymentScreenUiState.Ready -> Unit
                                    }
                                }
                            }
                        }

                        item {
                            PaymentPendingProofSection(
                                amount = confirmationData.totalPrice,
                                remainingSeconds = remainingPendingSeconds,
                                proofImageUri = paymentProofImageUri,
                                onPickProof = { pickProofImageLauncher.launch("image/*") }
                            )
                        }
                    }
                }
            }
        }
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
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp)
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
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun PaymentInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    emphasize: Boolean = false,
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
                fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (allowMultiline) 5 else 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PaymentBankAccountSection(
    account: PaymentReceiverAccount,
    onCopyAccount: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PaymentBankLine(
            label = stringResource(R.string.payment_bank_name_label),
            value = account.accountName,
            allowWrap = true
        )
        PaymentBankLine(
            label = stringResource(R.string.payment_bank_number_label),
            value = account.accountNumber,
            onCopy = onCopyAccount,
            allowWrap = true
        )
        PaymentBankLine(
            label = stringResource(R.string.payment_bank_label),
            value = account.bankName,
            allowWrap = true
        )
    }
}

@Composable
private fun PaymentBankLine(
    label: String,
    value: String,
    onCopy: (() -> Unit)? = null,
    allowWrap: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(88.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = if (allowWrap) 2 else 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (onCopy != null) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = stringResource(R.string.payment_copy_content_description),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(top = 1.dp)
                    .size(18.dp)
                    .clickable(onClick = onCopy)
            )
        }
    }
}

@Composable
private fun PaymentQrPanel(
    state: PaymentScreenUiState,
    qrDisplayUrl: String?,
    size: Dp = 110.dp
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .size(size)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(AppCardCornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        val shouldRenderImage = !qrDisplayUrl.isNullOrBlank()

        if (shouldRenderImage) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(qrDisplayUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.payment_qr_content_description),
                modifier = Modifier
                    .size(size - 8.dp)
                    .background(Color.White, RoundedCornerShape(AppBadgeCornerRadius))
            )
        } else {
            when (state) {
                PaymentScreenUiState.Loading -> CircularProgressIndicator(strokeWidth = 2.dp)
                is PaymentScreenUiState.Error -> Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                is PaymentScreenUiState.Ready -> Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun resolveQrDisplayUrl(
    response: MomoPaymentResponse?,
    paymentUrl: String?
): String? {
    val directQrUrl = response?.qrCodeUrl
    if (!directQrUrl.isNullOrBlank() && (directQrUrl.startsWith("http://") || directQrUrl.startsWith("https://"))) {
        return directQrUrl
    }

    val qrText = when {
        !directQrUrl.isNullOrBlank() -> directQrUrl
        !paymentUrl.isNullOrBlank() -> paymentUrl
        else -> null
    }

    if (qrText.isNullOrBlank()) {
        return null
    }

    val encoded = Uri.encode(qrText)
    return "https://quickchart.io/qr?text=$encoded&size=320"
}

@Composable
private fun PaymentPendingProofSection(
    amount: Int,
    remainingSeconds: Int,
    proofImageUri: String?,
    onPickProof: () -> Unit
) {
    val amountText = formatConfirmationCurrencyVnd(amount)
    val transferInstruction = buildAnnotatedString {
        append(stringResource(R.string.payment_transfer_notice_prefix).trimEnd())
        append(" ")
        withStyle(
            SpanStyle(
                color = AppAccentCitrus,
                fontWeight = FontWeight.Bold
            )
        ) {
            append(amountText)
        }
        append(" ")
        append(stringResource(R.string.payment_transfer_notice_suffix).trimStart())
    }

    val minutes = (remainingSeconds.coerceAtLeast(0)) / 60
    val seconds = (remainingSeconds.coerceAtLeast(0)) % 60
    val pendingTime = stringResource(R.string.payment_pending_time_format, minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(AppCardCornerRadius),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = AppAccentCitrus,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = transferInstruction,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Text(
            text = stringResource(R.string.payment_pending_instruction_line_1),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.payment_pending_instruction_line_2),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = pendingTime,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Surface(
            shape = RoundedCornerShape(AppCardCornerRadius),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clickable(onClick = onPickProof)
        ) {
            if (proofImageUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(proofImageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.payment_upload_proof_content_description),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.payment_upload_proof_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentExpiredContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.BottomEnd
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(180.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(88.dp)
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color(0xFFE25A67),
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = stringResource(R.string.payment_pending_expired_message),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PaymentExpiredBottomActions(
    onViewCancelledClick: () -> Unit,
    onReturnHomeClick: () -> Unit,
    backgroundColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onViewCancelledClick,
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
    }
}
