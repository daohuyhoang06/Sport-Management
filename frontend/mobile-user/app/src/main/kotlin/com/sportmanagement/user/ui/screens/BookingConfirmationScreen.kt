package com.sportmanagement.user.ui.screens

import android.widget.Toast
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.R
import com.sportmanagement.user.domain.model.BookingConfirmationData
import com.sportmanagement.user.ui.AppNavigationBarEffect
import com.sportmanagement.user.ui.components.booking.BookingConfirmationBottomBar
import com.sportmanagement.user.ui.components.booking.BookingConfirmationHeader
import com.sportmanagement.user.ui.components.booking.BookingNoticeCard
import com.sportmanagement.user.ui.components.booking.ConfirmFieldLabel
import com.sportmanagement.user.ui.components.booking.ConfirmNoteField
import com.sportmanagement.user.ui.components.booking.ConfirmPhoneField
import com.sportmanagement.user.ui.components.booking.ConfirmReadonlyField
import com.sportmanagement.user.ui.components.booking.ConfirmationInfoCard
import com.sportmanagement.user.ui.components.booking.InfoLine
import com.sportmanagement.user.ui.components.booking.bookingInfoLabelStyle
import com.sportmanagement.user.ui.components.booking.bookingInfoValueStyle
import com.sportmanagement.user.ui.components.booking.formatConfirmationCurrencyVnd
import com.sportmanagement.user.ui.components.booking.formatConfirmationDurationCompact

@Composable
fun BookingConfirmationScreen(
    confirmationData: BookingConfirmationData,
    userName: String,
    userPhone: String,
    isLoggedIn: Boolean,
    findOpponentDraft: FindOpponentDraft? = null,
    onBackClick: () -> Unit,
    onConfirmPaymentClick: (name: String, phone: String, note: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var note by rememberSaveable { mutableStateOf("") }
    var editableUserName by rememberSaveable(userName) { mutableStateOf(userName) }
    var editableUserPhone by rememberSaveable(userPhone) { mutableStateOf(userPhone) }
    val bottomBarColor = MaterialTheme.colorScheme.primary

    AppNavigationBarEffect(
        navigationBarColor = bottomBarColor,
        useDarkIcons = bottomBarColor.luminance() > 0.5f
    )

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.primary,
        bottomBar = {
            BookingConfirmationBottomBar(
                onConfirmPaymentClick = {
                    if (!isLoggedIn) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.booking_confirm_login_required),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        onConfirmPaymentClick(
                            editableUserName.trim(),
                            editableUserPhone.trim(),
                            note.trim()
                        )
                    }
                }
            )
        }
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
                val clubName = confirmationData.fieldName.ifBlank {
                    confirmationData.ranges.firstOrNull()?.courtName
                        ?: stringResource(R.string.booking_confirm_default_club_name)
                }
                val fieldAddress = confirmationData.fieldAddress.ifBlank {
                    stringResource(R.string.booking_confirm_default_address)
                }
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
                        value = fieldAddress
                    )
                }
            }

            item {
                ConfirmationInfoCard(
                    title = stringResource(R.string.booking_confirm_booking_info_title),
                    icon = Icons.Default.ReceiptLong
                ) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.Top) {
                        Text(
                            text = stringResource(R.string.booking_confirm_date),
                            style = bookingInfoLabelStyle(),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = confirmationData.selectedDate,
                            style = bookingInfoValueStyle(),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    confirmationData.ranges.forEach { range ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "- ${range.courtName}:",
                                    style = bookingInfoValueStyle().copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${range.startTimeLabel} - ${range.endTimeLabel}",
                                    style = bookingInfoValueStyle(),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formatConfirmationCurrencyVnd(range.price),
                                style = bookingInfoValueStyle(),
                                color = MaterialTheme.colorScheme.secondary,
                                textDecoration = TextDecoration.Underline
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Row(verticalAlignment = androidx.compose.ui.Alignment.Top) {
                        Text(
                            text = stringResource(R.string.booking_confirm_total_hours),
                            style = bookingInfoLabelStyle(),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formatConfirmationDurationCompact(confirmationData.totalMinutes),
                            style = bookingInfoValueStyle(),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = androidx.compose.ui.Alignment.Top) {
                        Text(
                            text = stringResource(R.string.booking_confirm_total_price),
                            style = bookingInfoLabelStyle(),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formatConfirmationCurrencyVnd(confirmationData.totalPrice),
                            style = bookingInfoValueStyle(),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            if (findOpponentDraft != null) {
                item {
                    ConfirmationInfoCard(
                        title = "Hình thức đặt sân",
                        icon = Icons.Default.Map
                    ) {
                        InfoLine(
                            label = "Hình thức",
                            value = "Tìm đối thủ"
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(10.dp)) }
            }

            item {
                ConfirmFieldLabel(text = stringResource(R.string.booking_confirm_name_label))
                ConfirmReadonlyField(
                    value = editableUserName,
                    onValueChange = { editableUserName = it },
                    placeholder = "Nhập tên của bạn"
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                ConfirmFieldLabel(text = stringResource(R.string.booking_confirm_phone_label))
                ConfirmPhoneField(
                    value = editableUserPhone,
                    onValueChange = { editableUserPhone = it },
                    placeholder = "Nhập số điện thoại"
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                ConfirmFieldLabel(text = stringResource(R.string.booking_confirm_note_label))
                ConfirmNoteField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = stringResource(R.string.booking_confirm_note_placeholder)
                )
            }
            item { Spacer(modifier = Modifier.height(10.dp)) }

            item {
                BookingNoticeCard()
            }
        }
    }
}
