package com.sportmanagement.user.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = White,
    secondary = AccentOrange,
    background = SoftBackground,
    surface = White,
    primaryContainer = PaleMint,
    secondaryContainer = WarmSand
)

private val DarkColors = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = White,
    secondary = AccentOrange
)

@Composable
fun SportUserTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content
    )
}
