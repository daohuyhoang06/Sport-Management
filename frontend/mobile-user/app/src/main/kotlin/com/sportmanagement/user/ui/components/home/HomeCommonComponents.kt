package com.sportmanagement.user.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.R
import com.sportmanagement.user.domain.model.SportIconType
import com.sportmanagement.user.ui.theme.AppCtaCompactHorizontalPadding
import com.sportmanagement.user.ui.theme.AppCtaCompactVerticalPadding
import com.sportmanagement.user.ui.theme.AppCtaCornerRadius
import com.sportmanagement.user.ui.theme.AppPillCornerRadius

@Composable
internal fun HomeBookButton(
    sportIconType: SportIconType,
    onClick: () -> Unit
) {
    val containerColor = homeSportAccentColor(sportIconType)
    val contentColor = if (containerColor.luminance() > 0.55f) Color(0xFF1A1A1A) else Color.White

    Button(
        onClick = onClick,
        modifier = Modifier.defaultMinSize(minWidth = 64.dp, minHeight = 30.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(AppCtaCornerRadius),
        contentPadding = PaddingValues(
            horizontal = (AppCtaCompactHorizontalPadding - 3.dp),
            vertical = (AppCtaCompactVerticalPadding - 1.dp)
        )
    ) {
        Text(
            text = stringResource(R.string.home_book_button),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
internal fun HomeSportNameChip(
    sportIconType: SportIconType,
    modifier: Modifier = Modifier
) {
    val accent = homeSportAccentColor(sportIconType)
    val contentColor = if (accent.luminance() > 0.55f) Color(0xFF1A1A1A) else Color.White
    Surface(
        modifier = modifier,
        color = accent,
        shape = RoundedCornerShape(AppPillCornerRadius),
    ) {
        Text(
            text = stringResource(homeSportNameRes(sportIconType)),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
internal fun HomeRatingChip(
    rating: String,
    modifier: Modifier = Modifier
) {
    val numericRating = rating.toDoubleOrNull()
    if (numericRating == null || numericRating <= 0.0) return

    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(AppPillCornerRadius),
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFE59C00),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(14.dp)
            )
            Text(
                text = String.format("%.1f", numericRating),
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF1F2937),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 18.dp)
            )
        }
    }
}

@Composable
internal fun HomeSmallCircleIcon(
    icon: ImageVector,
    onClick: () -> Unit,
    size: Int = 34,
    tint: Color = Color.Unspecified
) {
    val resolvedTint = if (tint == Color.Unspecified) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        tint
    }
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size((size * 0.5f).dp),
            tint = resolvedTint
        )
    }
}

internal fun homeSportNameRes(type: SportIconType): Int {
    return when (type) {
        SportIconType.FOOTBALL -> R.string.field_detail_sport_football
        SportIconType.PICKLEBALL -> R.string.field_detail_sport_pickleball
        SportIconType.TENNIS -> R.string.field_detail_sport_tennis
        SportIconType.BADMINTON -> R.string.field_detail_sport_badminton
        SportIconType.VOLLEYBALL -> R.string.field_detail_sport_volleyball
    }
}

internal fun homeSportAccentColor(type: SportIconType): Color {
    return when (type) {
        SportIconType.FOOTBALL -> Color(0xFF3B82F6)
        SportIconType.PICKLEBALL -> Color(0xFF14B8A6)
        SportIconType.TENNIS -> Color(0xFF0EA5E9)
        SportIconType.BADMINTON -> Color(0xFFA855F7)
        SportIconType.VOLLEYBALL -> Color(0xFFF59E0B)
    }
}
