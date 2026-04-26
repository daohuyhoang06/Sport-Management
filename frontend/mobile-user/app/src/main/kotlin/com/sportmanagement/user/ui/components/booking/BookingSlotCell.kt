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
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant
) {
    val background = when (status) {
        SlotStatus.AVAILABLE -> MaterialTheme.colorScheme.surface
        SlotStatus.BOOKED -> MaterialTheme.colorScheme.error
        SlotStatus.LOCKED -> MaterialTheme.colorScheme.outline
        SlotStatus.EVENT -> MaterialTheme.colorScheme.outline
        SlotStatus.SELECTED -> MaterialTheme.colorScheme.primaryContainer
    }

    Box(
        modifier = modifier
            .background(background)
            .border(1.dp, borderColor),
        contentAlignment = Alignment.Center
    ) {
        if (status == SlotStatus.SELECTED) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
