package com.sportmanagement.user.ui.components.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.domain.model.SlotStatus

@Composable
internal fun BookingSlotCell(
    status: SlotStatus,
    modifier: Modifier = Modifier,
    iconSize: Dp = 20.dp,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    showSelectionIcon: Boolean = status == SlotStatus.SELECTED
) {
    val background = when (status) {
        SlotStatus.AVAILABLE -> Color.White
        SlotStatus.BOOKED -> Color(0xFF6B7280)
        SlotStatus.LOCKED -> Color(0xFFEF4444)
        SlotStatus.EVENT -> MaterialTheme.colorScheme.outlineVariant
        SlotStatus.SELECTED -> MaterialTheme.colorScheme.primaryContainer
        SlotStatus.DISABLED -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }

    Box(
        modifier = modifier
            .background(background)
            .border(1.dp, borderColor),
        contentAlignment = Alignment.Center
    ) {
        if (status == SlotStatus.SELECTED && showSelectionIcon) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
