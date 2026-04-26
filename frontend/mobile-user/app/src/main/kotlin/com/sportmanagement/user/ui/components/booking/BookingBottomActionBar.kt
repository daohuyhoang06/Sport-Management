package com.sportmanagement.user.ui.components.booking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.R
import com.sportmanagement.user.domain.model.BookingSelectionSummary
import com.sportmanagement.user.ui.theme.AppCtaAmber
import com.sportmanagement.user.ui.theme.AppCtaCornerRadius
import com.sportmanagement.user.ui.theme.AppCtaWideHeight
import com.sportmanagement.user.ui.theme.AppCtaWideWidthFraction
import com.sportmanagement.user.ui.theme.AppOnCtaAmber
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BookingBottomActionBar(
    sliderValue: Float,
    onSliderChange: (Float) -> Unit,
    summary: BookingSelectionSummary?,
    showSelectedRange: Boolean,
    onToggleSelectedRange: () -> Unit,
    hasSelection: Boolean,
    onNextClick: () -> Unit,
    onRequireSelection: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 4.dp
            ) {
                Slider(
                    value = sliderValue,
                    onValueChange = onSliderChange,
                    valueRange = 4f..68f,
                    modifier = Modifier
                        .width(170.dp)
                        .padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }
        }

        if (summary != null) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    TextButton(
                        onClick = onToggleSelectedRange,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(
                            imageVector = if (showSelectedRange) {
                                Icons.Default.KeyboardArrowDown
                            } else {
                                Icons.Default.KeyboardArrowUp
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(
                                if (showSelectedRange) {
                                    R.string.booking_hide_selected_range
                                } else {
                                    R.string.booking_show_selected_range
                                }
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    if (showSelectedRange) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            summary.selectedRanges.forEach { selectedRange ->
                                Text(
                                    text = stringResource(
                                        R.string.booking_selected_range_format,
                                        selectedRange.courtName,
                                        selectedRange.startTimeLabel,
                                        selectedRange.endTimeLabel
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(
                                R.string.booking_total_hours_format,
                                formatBookingDurationCompact(summary.totalMinutes)
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(
                                R.string.booking_total_price_format,
                                formatBookingCurrencyVnd(summary.totalPrice)
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    BookingNextButton(
                        hasSelection = hasSelection,
                        onNextClick = onNextClick,
                        onRequireSelection = onRequireSelection
                    )
                }
            }
        } else {
            BookingNextButton(
                hasSelection = hasSelection,
                onNextClick = onNextClick,
                onRequireSelection = onRequireSelection,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun BookingNextButton(
    hasSelection: Boolean,
    onNextClick: () -> Unit,
    onRequireSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {
            if (hasSelection) onNextClick() else onRequireSelection()
        },
        shape = RoundedCornerShape(AppCtaCornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppCtaAmber,
            contentColor = AppOnCtaAmber
        ),
        modifier = modifier
            .fillMaxWidth(AppCtaWideWidthFraction)
            .height(AppCtaWideHeight)
    ) {
        Text(
            text = stringResource(R.string.booking_next),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatBookingDurationCompact(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return "%dh%02d".format(hours, minutes)
}

private fun formatBookingCurrencyVnd(amount: Int): String {
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    return "${formatter.format(amount)} \u20AB"
}

