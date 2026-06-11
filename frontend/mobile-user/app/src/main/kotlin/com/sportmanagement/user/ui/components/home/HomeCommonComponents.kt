package com.sportmanagement.user.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportmanagement.user.R
import com.sportmanagement.user.domain.model.SportIconType
import com.sportmanagement.user.ui.theme.AppCtaAmber
import com.sportmanagement.user.ui.theme.AppCtaCompactHorizontalPadding
import com.sportmanagement.user.ui.theme.AppCtaCompactVerticalPadding
import com.sportmanagement.user.ui.theme.AppCtaCornerRadius
import com.sportmanagement.user.ui.theme.AppOnCtaAmber
import com.sportmanagement.user.ui.theme.AppPillCornerRadius
import com.sportmanagement.user.ui.theme.responsiveSharedTitleStyle

@Composable
internal fun HomeBookButton(
    sportIconType: SportIconType,
    onClick: () -> Unit
) {
    BoxWithConstraints {
        val compactLayout = maxWidth < 120.dp
        val minWidth = if (compactLayout) 58.dp else 64.dp
        val textSize: TextUnit = when {
            maxWidth < 100.dp -> 10.sp
            maxWidth < 116.dp -> 10.5.sp
            else -> 11.sp
        }

        Button(
            onClick = onClick,
            modifier = Modifier.defaultMinSize(minWidth = minWidth, minHeight = 30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppCtaAmber,
                contentColor = AppOnCtaAmber
            ),
            shape = RoundedCornerShape(AppCtaCornerRadius),
            contentPadding = PaddingValues(
                horizontal = if (compactLayout) 5.dp else (AppCtaCompactHorizontalPadding - 3.dp),
                vertical = if (compactLayout) 3.dp else (AppCtaCompactVerticalPadding - 1.dp)
            )
        ) {
            Text(
                text = stringResource(R.string.home_book_button),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = textSize),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip
            )
        }
    }
}

@Composable
internal fun HomeVenueTitleText(
    text: String,
    modifier: Modifier = Modifier
) {
    val titleStyle = responsiveSharedTitleStyle(LocalConfiguration.current.screenWidthDp)
    BoxWithConstraints(modifier = modifier) {
        Text(
            text = text,
            style = titleStyle,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            softWrap = true,
            overflow = TextOverflow.Clip
        )
    }
}

@Composable
internal fun HomeVenueDistanceLocationText(
    distance: String,
    location: String,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val compact = maxWidth < 210.dp
        val distanceSize = if (compact) 10.sp else 11.sp
        val locationSize = if (compact) 11.sp else 12.sp
        val locationLineHeight = if (compact) 13.sp else 14.sp

        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (distance.isNotBlank()) {
                Text(
                    text = "($distance) ",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = distanceSize),
                    color = Color(0xFFD62828),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
            Text(
                text = location,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = locationSize,
                    lineHeight = locationLineHeight,
                    textAlign = TextAlign.Start
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun HomeVenueHoursText(
    hours: String,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val compact = maxWidth < 220.dp
        val fontSize = if (compact) 10.5.sp else 11.5.sp
        val lineHeight = if (compact) 12.sp else 13.sp
        val maxLines = if (compact) 2 else 1

        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                modifier = Modifier.size(if (compact) 13.dp else 14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = hours,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = fontSize,
                    lineHeight = lineHeight
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = maxLines,
                softWrap = true,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
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
    val numericRating = rating.toDoubleOrNull() ?: 0.0

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
