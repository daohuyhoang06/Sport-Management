package com.sportmanagement.manager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ManagerColors = lightColorScheme(
    primary = DeepBlue,
    onPrimary = White,
    secondary = AlertAmber,
    background = Mist,
    surface = White,
    primaryContainer = PaleBlue,
    secondaryContainer = PaleAmber
)

@Composable
fun SportManagerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ManagerColors,
        typography = AppTypography,
        content = content
    )
}
