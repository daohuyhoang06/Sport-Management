package com.sportmanagement.user.ui.components.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.R
import com.sportmanagement.user.domain.model.SlotStatus

@Composable
fun BookingHeaderSection(
    selectedDateText: String,
    onBackClick: () -> Unit,
    onDateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.booking_back_content_description),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text(
                text = stringResource(R.string.booking_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                onClick = onDateClick,
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedDateText,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = stringResource(R.string.booking_calendar_content_description),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BookingLegendItem(SlotStatus.AVAILABLE, stringResource(R.string.booking_status_available))
            BookingLegendItem(SlotStatus.BOOKED, stringResource(R.string.booking_status_booked))
            BookingLegendItem(SlotStatus.LOCKED, stringResource(R.string.booking_status_locked))
        }

        Text(
            text = stringResource(R.string.booking_view_field_and_price),
            modifier = Modifier.padding(top = 14.dp),
            style = MaterialTheme.typography.titleMedium,
            textDecoration = TextDecoration.Underline,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun BookingLegendItem(status: SlotStatus, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        BookingSlotCell(
            status = status,
            modifier = Modifier.size(22.dp),
            iconSize = 12.dp,
            borderColor = Color.Transparent
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}
