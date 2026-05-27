package com.sportmanagement.manager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Stadium
import androidx.compose.ui.graphics.vector.ImageVector

enum class ManagerTab(
    val label: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
) {
    Dashboard(
        label = "Dashboard",
        filledIcon = Icons.Filled.Dashboard,
        outlinedIcon = Icons.Outlined.Dashboard
    ),
    Pitches(
        label = "Pitches",
        filledIcon = Icons.Filled.Stadium,
        outlinedIcon = Icons.Outlined.Stadium
    ),
    Bookings(
        label = "Bookings",
        filledIcon = Icons.Filled.EventNote,
        outlinedIcon = Icons.Outlined.EventNote
    ),
    Services(
        label = "Services",
        filledIcon = Icons.Filled.Layers,
        outlinedIcon = Icons.Outlined.Layers
    ),
    Messages(
        label = "Messages",
        filledIcon = Icons.Filled.Chat,
        outlinedIcon = Icons.Outlined.Chat
    )
}
