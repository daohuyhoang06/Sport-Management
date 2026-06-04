package com.sportmanagement.user.ui.components.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import com.sportmanagement.user.R
import com.sportmanagement.user.ui.theme.AppCompactCornerRadius
import com.sportmanagement.user.ui.theme.AppCtaAmber
import com.sportmanagement.user.ui.theme.AppCtaCornerRadius
import com.sportmanagement.user.ui.theme.AppCtaWideHeight
import com.sportmanagement.user.ui.theme.AppBadgeCornerRadius
import com.sportmanagement.user.ui.theme.AppCardCornerRadius
import com.sportmanagement.user.ui.theme.AppFieldHorizontalPadding
import com.sportmanagement.user.ui.theme.AppFieldVerticalPadding
import com.sportmanagement.user.ui.theme.AppOnCtaAmber
import com.sportmanagement.user.ui.theme.AppScreenHorizontalPadding
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BookingConfirmationBottomBar(onConfirmPaymentClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.primary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Button(
                onClick = onConfirmPaymentClick,
                shape = RoundedCornerShape(AppCtaCornerRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppCtaAmber,
                    contentColor = AppOnCtaAmber
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppCtaWideHeight)
            ) {
                Text(
                    text = stringResource(R.string.booking_confirm_and_pay),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun BookingConfirmationHeader(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.booking_back_content_description),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    text = stringResource(R.string.booking_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Default),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun ConfirmationInfoCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(AppCardCornerRadius),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun InfoLine(label: String, value: String) {
    Text(
        text = "$label $value",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onPrimary
    )
}

@Composable
fun BookingOfferSummaryCard(totalPrice: Int) {
    Surface(
        shape = RoundedCornerShape(AppCardCornerRadius),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.booking_confirm_offer),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.booking_confirm_choose_offer),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        textDecoration = TextDecoration.Underline
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.secondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.booking_confirm_amount_due),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = formatConfirmationCurrencyVnd(totalPrice),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}

@Composable
fun ConfirmFieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onPrimary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

@Composable
fun ConfirmReadonlyField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    val inputCardShape = RoundedCornerShape(AppCompactCornerRadius)
    Surface(
        shape = inputCardShape,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppScreenHorizontalPadding)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, inputCardShape)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppFieldHorizontalPadding, vertical = 2.dp),
            textStyle = MaterialTheme.typography.titleMedium,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            singleLine = true,
            trailingIcon = {
                if (value.isNotBlank()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.booking_confirm_clear_input)
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
fun ConfirmPhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    val inputCardShape = RoundedCornerShape(AppCompactCornerRadius)
    Surface(
        shape = inputCardShape,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppScreenHorizontalPadding)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, inputCardShape)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppFieldHorizontalPadding, vertical = 2.dp),
            textStyle = MaterialTheme.typography.titleMedium,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            trailingIcon = {
                if (value.isNotBlank()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.booking_confirm_clear_input)
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
fun BookingNoticeCard() {
    Surface(
        shape = RoundedCornerShape(AppCardCornerRadius),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(AppBadgeCornerRadius)
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.booking_confirm_notice_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            ConfirmNoticeText(stringResource(R.string.booking_confirm_notice_short))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.booking_confirm_notice_terms),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun ConfirmNoteField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    val noteCorner = RoundedCornerShape(AppCompactCornerRadius)
    Surface(
        shape = noteCorner,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppScreenHorizontalPadding)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, noteCorner)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
private fun ConfirmNoticeText(text: String) {
    Text(
        text = "\u2022 $text",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

fun formatConfirmationCurrencyVnd(amount: Int): String {
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    return "${formatter.format(amount)} \u20AB"
}

fun formatConfirmationDurationCompact(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return "%dh%02d".format(hours, minutes)
}

