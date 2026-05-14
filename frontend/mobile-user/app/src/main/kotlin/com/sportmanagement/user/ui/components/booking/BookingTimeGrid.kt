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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.domain.model.BookingSubCourt
import com.sportmanagement.user.domain.model.BookingTimeGridData
import com.sportmanagement.user.domain.model.BookingTimeGridSupport
import com.sportmanagement.user.domain.model.BookingTimeRange
import com.sportmanagement.user.domain.model.SlotStatus

@Composable
fun BookingTimeGrid(
    gridData: BookingTimeGridData,
    cellWidth: Dp,
    selectedSlots: List<BookingTimeRange>,
    onSlotClick: (courtId: String, startTime: String, endTime: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val horizontalScroll = rememberScrollState()
    val stepMinutes = gridData.gridStepMinutes.coerceAtLeast(1)
    val selectionMinutes = gridData.minBookingMinutes.coerceAtLeast(1)
    val closeMinutes = remember(gridData.closeTime) {
        BookingTimeGridSupport.parseTimeToMinutes(gridData.closeTime)
    }

    val headerSlotStarts = remember(gridData.openTime, gridData.closeTime, stepMinutes) {
        BookingTimeGridSupport.generateGridSlotStarts(
            openTime = gridData.openTime,
            closeTime = gridData.closeTime,
            gridStepMinutes = stepMinutes
        )
    }
    val bookingSlotStarts = remember(gridData.openTime, gridData.closeTime, selectionMinutes) {
        generateBookingSlotStarts(
            openTime = gridData.openTime,
            closeTime = gridData.closeTime,
            minBookingMinutes = selectionMinutes
        )
    }

    val leftLabelWidth = 68.dp
    val cellHeight = 52.dp
    val headerHeight = 34.dp
    val bookingCellWidth = cellWidth * (selectionMinutes.toFloat() / stepMinutes.toFloat())
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        BookingTimeGridHeader(
            slotStarts = headerSlotStarts,
            cellWidth = cellWidth,
            headerHeight = headerHeight,
            leftLabelWidth = leftLabelWidth,
            borderColor = borderColor,
            horizontalScroll = horizontalScroll
        )

        gridData.courts.forEach { court ->
            BookingTimeGridRow(
                court = court,
                gridData = gridData,
                bookingSlotStarts = bookingSlotStarts,
                closeMinutes = closeMinutes,
                selectedSlots = selectedSlots,
                bookingCellWidth = bookingCellWidth,
                cellHeight = cellHeight,
                leftLabelWidth = leftLabelWidth,
                borderColor = borderColor,
                horizontalScroll = horizontalScroll,
                onSlotClick = onSlotClick
            )
        }
    }
}

@Composable
private fun BookingTimeGridHeader(
    slotStarts: List<Int>,
    cellWidth: Dp,
    headerHeight: Dp,
    leftLabelWidth: Dp,
    borderColor: Color,
    horizontalScroll: ScrollState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Box(
            modifier = Modifier
                .width(leftLabelWidth)
                .height(headerHeight)
                .border(1.dp, borderColor)
        )
        Row(
            modifier = Modifier
                .horizontalScroll(horizontalScroll)
                .height(headerHeight)
        ) {
            slotStarts.forEach { slotStart ->
                val timeLabel = BookingTimeGridSupport.formatMinutesAsTime(slotStart)
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
                            text = timeLabel,
                            style = MaterialTheme.typography.labelSmall,
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
}

@Composable
private fun BookingTimeGridRow(
    court: BookingSubCourt,
    gridData: BookingTimeGridData,
    bookingSlotStarts: List<Int>,
    closeMinutes: Int,
    selectedSlots: List<BookingTimeRange>,
    bookingCellWidth: Dp,
    cellHeight: Dp,
    leftLabelWidth: Dp,
    borderColor: Color,
    horizontalScroll: ScrollState,
    onSlotClick: (courtId: String, startTime: String, endTime: String) -> Unit
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
                text = court.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            modifier = Modifier
                .horizontalScroll(horizontalScroll)
                .height(cellHeight)
        ) {
            bookingSlotStarts.forEach { slotStart ->
                val cellState = resolveBookingBlockState(
                    courtId = court.id,
                    slotStart = slotStart,
                    closeMinutes = closeMinutes,
                    gridData = gridData,
                    selectedSlots = selectedSlots
                )

                BookingSlotCell(
                    status = cellState.status,
                    modifier = Modifier
                        .width(bookingCellWidth)
                        .fillMaxSize()
                        .border(1.dp, borderColor)
                        .then(
                            cellState.clickRange?.let { clickRange ->
                                Modifier.clickable {
                                    onSlotClick(
                                        clickRange.courtId,
                                        clickRange.startTime,
                                        clickRange.endTime
                                    )
                                }
                            } ?: Modifier
                        ),
                    iconSize = bookingCellWidth * 0.26f,
                    borderColor = Color.Transparent,
                    showSelectionIcon = cellState.showSelectionIcon
                )
            }
        }
    }
}

private data class GridCellState(
    val status: SlotStatus,
    val clickRange: BookingTimeRange? = null,
    val showSelectionIcon: Boolean = false
)

private fun resolveBookingBlockState(
    courtId: String,
    slotStart: Int,
    closeMinutes: Int,
    gridData: BookingTimeGridData,
    selectedSlots: List<BookingTimeRange>
): GridCellState {
    val selectionMinutes = gridData.minBookingMinutes.coerceAtLeast(1)
    val requestedEnd = slotStart + selectionMinutes
    if (requestedEnd > closeMinutes) {
        return GridCellState(status = SlotStatus.DISABLED)
    }

    val requestedRange = BookingTimeRange(
        courtId = courtId,
        startTime = BookingTimeGridSupport.formatMinutesAsTime(slotStart),
        endTime = BookingTimeGridSupport.formatMinutesAsTime(requestedEnd)
    )

    val selectedRange = selectedSlots.firstOrNull { slot ->
        slot.courtId == courtId &&
            BookingTimeGridSupport.rangeOverlaps(slot, requestedRange)
    }
    if (selectedRange != null) {
        return GridCellState(
            status = SlotStatus.SELECTED,
            clickRange = selectedRange,
            showSelectionIcon = BookingTimeGridSupport.parseTimeToMinutes(selectedRange.startTime) == slotStart
        )
    }

    val overlapsBookedRange = gridData.bookedSlots.any { slot ->
        slot.courtId == courtId &&
            BookingTimeGridSupport.rangeOverlaps(slot, requestedRange)
    }
    if (overlapsBookedRange) {
        return GridCellState(status = SlotStatus.BOOKED)
    }

    val overlapsBlockedRange = gridData.blockedSlots.any { slot ->
        slot.courtId == courtId &&
            BookingTimeGridSupport.rangeOverlaps(slot, requestedRange)
    }
    if (overlapsBlockedRange) {
        return GridCellState(status = SlotStatus.LOCKED)
    }

    return GridCellState(
        status = SlotStatus.AVAILABLE,
        clickRange = requestedRange
    )
}

private fun generateBookingSlotStarts(
    openTime: String,
    closeTime: String,
    minBookingMinutes: Int
): List<Int> {
    val openMinutes = BookingTimeGridSupport.parseTimeToMinutes(openTime)
    val closeMinutes = BookingTimeGridSupport.parseTimeToMinutes(closeTime)
    val blockMinutes = minBookingMinutes.coerceAtLeast(1)
    if (closeMinutes <= openMinutes) return emptyList()

    val starts = mutableListOf<Int>()
    var current = openMinutes
    while (current + blockMinutes <= closeMinutes) {
        starts += current
        current += blockMinutes
    }
    return starts
}
