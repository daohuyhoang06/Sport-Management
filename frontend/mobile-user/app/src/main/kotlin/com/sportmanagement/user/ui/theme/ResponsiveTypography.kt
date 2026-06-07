package com.sportmanagement.user.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

fun responsiveSharedTitleStyle(screenWidthDp: Int): TextStyle {
    val fontSize: TextUnit = when {
        screenWidthDp < 360 -> 14.sp
        screenWidthDp < 400 -> 15.sp
        else -> 16.sp
    }

    val lineHeight: TextUnit = when {
        screenWidthDp < 360 -> 17.sp
        screenWidthDp < 400 -> 18.sp
        else -> 19.sp
    }

    return AppTypography.titleMedium.copy(fontSize = fontSize, lineHeight = lineHeight)
}
