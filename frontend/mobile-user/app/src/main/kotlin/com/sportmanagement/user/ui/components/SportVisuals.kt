package com.sportmanagement.user.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.R
import com.sportmanagement.user.domain.model.SportIconType

@Composable
fun SportMarkerIcon(
    iconType: SportIconType,
    contentDescription: String?,
    markerSize: Dp = 38.dp,
    iconSize: Dp = 18.dp,
    iconOffsetY: Dp = (-3).dp,
    iconTint: Color = Color.Unspecified
) {
    Box(
        modifier = Modifier.size(markerSize),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = sportMarkerBaseDrawableRes(iconType)),
            contentDescription = contentDescription,
            modifier = Modifier.size(markerSize)
        )
        Image(
            painter = painterResource(id = sportIconDrawableRes(iconType)),
            contentDescription = null,
            modifier = Modifier
                .size(iconSize)
                .offset(y = iconOffsetY),
            colorFilter = if (iconTint == Color.Unspecified) null else ColorFilter.tint(iconTint)
        )
    }
}

@Composable
fun SportCircleAvatar(
    iconType: SportIconType,
    size: Dp = 44.dp,
    iconSize: Dp = 20.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(sportAvatarBackgroundColor(iconType)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = sportIconDrawableRes(iconType)),
            contentDescription = null,
            modifier = Modifier.size(iconSize)
        )
    }
}

fun sportIconDrawableRes(type: SportIconType): Int {
    return when (type) {
        SportIconType.FOOTBALL -> R.drawable.football_25
        SportIconType.PICKLEBALL -> R.drawable.pickleball
        SportIconType.TENNIS -> R.drawable.tennis_25
        SportIconType.BADMINTON -> R.drawable.badminton_25
        SportIconType.VOLLEYBALL -> R.drawable.volleyball_25
    }
}

fun sportMarkerBaseDrawableRes(type: SportIconType): Int {
    return when (type) {
        SportIconType.FOOTBALL -> R.drawable.map_marker_base_football
        SportIconType.PICKLEBALL -> R.drawable.map_marker_base_pickleball
        SportIconType.TENNIS -> R.drawable.map_marker_base_tennis
        SportIconType.BADMINTON -> R.drawable.map_marker_base_badminton
        SportIconType.VOLLEYBALL -> R.drawable.map_marker_base_volleyball
    }
}

fun sportAvatarBackgroundColor(type: SportIconType): Color {
    return when (type) {
        SportIconType.FOOTBALL -> Color(0xFFE7F0FF)
        SportIconType.VOLLEYBALL -> Color(0xFFFFF2DB)
        SportIconType.PICKLEBALL -> Color(0xFFE8FFF7)
        SportIconType.BADMINTON -> Color(0xFFF3ECFF)
        SportIconType.TENNIS -> Color(0xFFE9F7FF)
    }
}

fun sportFieldDrawableRes(type: SportIconType): Int {
    return when (type) {
        SportIconType.FOOTBALL -> R.drawable.field_football
        SportIconType.VOLLEYBALL -> R.drawable.field_volleyball
        SportIconType.PICKLEBALL -> R.drawable.field_pickleball
        SportIconType.BADMINTON -> R.drawable.field_badminton
        SportIconType.TENNIS -> R.drawable.field_tennis
    }
}
