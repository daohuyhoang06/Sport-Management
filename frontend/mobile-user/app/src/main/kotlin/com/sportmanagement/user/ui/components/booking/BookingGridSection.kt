package com.sportmanagement.user.ui.components.booking

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.domain.model.BookingScheduleData
import com.sportmanagement.user.domain.model.CourtRow
import com.sportmanagement.user.domain.model.SlotStatus
import com.sportmanagement.user.domain.model.TimeSlot

@Composable
fun BookingGridSection(
    scheduleData: BookingScheduleData,
    cellWidthValue: Float,
    selectedSlots: Set<String>,
    onSlotToggle: (String) -> Unit
) {
    val horizontalScroll = rememberScrollState()
    val cellWidth = cellWidthValue.dp
    val cellHeight = 56.dp
    val timeHeaderHeight = 40.dp
    val leftLabelWidth = 68.dp
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box(
                modifier = Modifier
                    .width(leftLabelWidth)
                    .height(timeHeaderHeight)
                    .border(1.dp, borderColor)
            )
            Row(
                modifier = Modifier
                    .horizontalScroll(horizontalScroll)
                    .height(timeHeaderHeight)
            ) {
                scheduleData.timeHeaders.forEach { header ->
                    Box(
                        modifier = Modifier
                            .width(cellWidth)
                            .fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 1.dp, bottom = 2.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = header,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(8.dp)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }
        }

        scheduleData.courts.forEachIndexed { courtIndex, court ->
            BookingCourtRow(
                row = court,
                courtIndex = courtIndex,
                leftLabelWidth = leftLabelWidth,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                borderColor = borderColor,
                horizontalScroll = horizontalScroll,
                selectedSlots = selectedSlots,
                onSlotToggle = { slotKey -> onSlotToggle(slotKey) }
            )
        }
    }
}

@Composable
private fun BookingCourtRow(
    row: CourtRow,
    courtIndex: Int,
    leftLabelWidth: Dp,
    cellWidth: Dp,
    cellHeight: Dp,
    borderColor: Color,
    horizontalScroll: ScrollState,
    selectedSlots: Set<String>,
    onSlotToggle: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Box(
            modifier = Modifier
                .width(leftLabelWidth)
                .height(cellHeight)
                .border(1.dp, borderColor)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = row.courtName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            modifier = Modifier
                .horizontalScroll(horizontalScroll)
                .height(cellHeight)
        ) {
            row.slots.forEachIndexed { slotIndex, slot ->
                val slotKey = "$courtIndex:$slotIndex"
                BookingGridCell(
                    slot = slot,
                    isSelected = selectedSlots.contains(slotKey),
                    onToggle = { onSlotToggle(slotKey) },
                    cellWidth = cellWidth,
                    borderColor = borderColor
                )
            }
        }
    }
}

@Composable
private fun BookingGridCell(
    slot: TimeSlot,
    isSelected: Boolean,
    onToggle: () -> Unit,
    cellWidth: Dp,
    borderColor: Color
) {
    val displayStatus = if (isSelected) SlotStatus.SELECTED else slot.status
    val isSelectable = slot.status == SlotStatus.AVAILABLE || isSelected

    BookingSlotCell(
        status = displayStatus,
        modifier = Modifier
            .width(cellWidth)
            .fillMaxSize()
            .border(1.dp, borderColor)
            .then(
                if (isSelectable) {
                    Modifier.clickable(onClick = onToggle)
                } else {
                    Modifier
                }
            ),
        iconSize = (cellWidth * 0.55f),
        borderColor = Color.Transparent
    )
}
