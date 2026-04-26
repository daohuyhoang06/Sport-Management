package com.sportmanagement.user.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.R
import com.sportmanagement.user.domain.model.BookingConfirmationData
import com.sportmanagement.user.ui.components.booking.BookingConfirmationBottomBar
import com.sportmanagement.user.ui.components.booking.BookingConfirmationHeader
import com.sportmanagement.user.ui.components.booking.BookingNoticeCard
import com.sportmanagement.user.ui.components.booking.BookingOfferSummaryCard
import com.sportmanagement.user.ui.components.booking.ConfirmFieldLabel
import com.sportmanagement.user.ui.components.booking.ConfirmPhoneField
import com.sportmanagement.user.ui.components.booking.ConfirmReadonlyField
import com.sportmanagement.user.ui.components.booking.ConfirmationInfoCard
import com.sportmanagement.user.ui.components.booking.InfoLine
import com.sportmanagement.user.ui.components.booking.formatConfirmationCurrencyVnd
import com.sportmanagement.user.ui.components.booking.formatConfirmationDurationCompact

@Composable
fun BookingConfirmationScreen(
    confirmationData: BookingConfirmationData,
    userName: String,
    userPhone: String,
    onBackClick: () -> Unit,
    onConfirmPaymentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var note by rememberSaveable { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.primary,
        bottomBar = { BookingConfirmationBottomBar(onConfirmPaymentClick = onConfirmPaymentClick) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                BookingConfirmationHeader(onBackClick = onBackClick)
            }

            item {
                val clubName = confirmationData.ranges.firstOrNull()?.courtName
                    ?: stringResource(R.string.booking_confirm_default_club_name)
                ConfirmationInfoCard(
                    title = stringResource(R.string.booking_confirm_field_info_title),
                    icon = Icons.Default.Map
                ) {
                    InfoLine(
                        label = stringResource(R.string.booking_confirm_club_name),
                        value = clubName
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    InfoLine(
                        label = stringResource(R.string.booking_confirm_address),
                        value = stringResource(R.string.booking_confirm_default_address)
                    )
                }
            }

            item {
                ConfirmationInfoCard(
                    title = stringResource(R.string.booking_confirm_booking_info_title),
                    icon = Icons.Default.ReceiptLong
                ) {
                    Text(
                        text = "${stringResource(R.string.booking_confirm_date)} ${confirmationData.selectedDate}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    confirmationData.ranges.forEach { range ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "- ${range.courtName}: ${range.startTimeLabel} - ${range.endTimeLabel}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formatConfirmationCurrencyVnd(range.price),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                textDecoration = TextDecoration.Underline
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(
                        text = "${stringResource(R.string.booking_confirm_total_hours)} ${formatConfirmationDurationCompact(confirmationData.totalMinutes)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${stringResource(R.string.booking_confirm_total_price)} ${formatConfirmationCurrencyVnd(confirmationData.totalPrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            item {
                BookingOfferSummaryCard(totalPrice = confirmationData.totalPrice)
            }

            item {
                ConfirmFieldLabel(text = stringResource(R.string.booking_confirm_name_label))
                ConfirmReadonlyField(value = userName)
            }

            item {
                ConfirmFieldLabel(text = stringResource(R.string.booking_confirm_phone_label))
                ConfirmPhoneField(value = userPhone)
            }

            item {
                ConfirmFieldLabel(text = stringResource(R.string.booking_confirm_note_label))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(120.dp),
                    textStyle = MaterialTheme.typography.titleMedium,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.booking_confirm_note_placeholder),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            item {
                BookingNoticeCard()
            }
        }
    }
}
