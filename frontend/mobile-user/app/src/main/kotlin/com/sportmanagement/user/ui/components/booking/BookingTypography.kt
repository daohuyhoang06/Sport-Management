package com.sportmanagement.user.ui.components.booking

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
internal fun bookingPageTitleStyle(): TextStyle {
    return MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
internal fun bookingCardTitleStyle(): TextStyle {
    return MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
internal fun bookingInfoLabelStyle(): TextStyle {
    return MaterialTheme.typography.bodyMedium.copy(
        fontWeight = FontWeight.Normal
    )
}

@Composable
internal fun bookingInfoValueStyle(): TextStyle {
    return MaterialTheme.typography.bodyMedium.copy(
        fontWeight = FontWeight.Medium
    )
}

@Composable
internal fun bookingHelperTextStyle(): TextStyle {
    return MaterialTheme.typography.bodySmall.copy(
        fontWeight = FontWeight.Normal
    )
}

@Composable
internal fun bookingCompactFieldLabelStyle(): TextStyle {
    return MaterialTheme.typography.bodySmall.copy(
        fontWeight = FontWeight.Normal
    )
}

@Composable
internal fun bookingSecondaryTextStyle(): TextStyle {
    return MaterialTheme.typography.titleSmall.copy(
        fontWeight = FontWeight.SemiBold
    )
}
