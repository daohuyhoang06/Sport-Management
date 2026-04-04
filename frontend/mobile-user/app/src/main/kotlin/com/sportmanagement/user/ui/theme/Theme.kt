package com.sportmanagement.user.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = White,
    secondary = SecondaryOrange,
    tertiary = TertiaryViolet,
    background = SoftBackground,
    surface = White,
    primaryContainer = BlueContainer,
    secondaryContainer = OrangeContainer
)

private val DarkColors = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = White,
    secondary = SecondaryOrange,
    tertiary = TertiaryViolet
)

@Composable
fun SportUserTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content
    )
}
