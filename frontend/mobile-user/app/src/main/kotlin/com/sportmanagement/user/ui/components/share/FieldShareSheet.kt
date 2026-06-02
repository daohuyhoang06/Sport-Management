package com.sportmanagement.user.ui.components.share

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.R
import com.sportmanagement.user.domain.model.UserField
import com.sportmanagement.user.ui.components.SportCircleAvatar
import com.sportmanagement.user.ui.theme.AppCtaCornerRadius
import com.sportmanagement.user.ui.theme.AppHomeVenueCornerRadius
import com.sportmanagement.user.ui.theme.AppSheetTopCornerRadius

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FieldShareSheet(
    field: UserField,
    shareUrl: String,
    onDismiss: () -> Unit,
    onCopyLink: () -> Unit,
    onShareNow: () -> Unit,
    onOpenLink: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(
            topStart = AppSheetTopCornerRadius,
            topEnd = AppSheetTopCornerRadius
        ),
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.padding(top = 10.dp, bottom = 12.dp),
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.85f)
                ) {
                    Spacer(modifier = Modifier.size(width = 42.dp, height = 5.dp))
                }
            }
        },
        containerColor = Color(0xFFF8FBFF)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 2.dp)
        ) {
            Text(
                text = stringResource(R.string.share_sheet_title),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            ShareHeroCard(
                field = field,
                onOpenLink = onOpenLink
            )

            Spacer(modifier = Modifier.height(14.dp))

            ShareLinkCard(
                shareUrl = shareUrl,
                onCopyLink = onCopyLink
            )

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = onShareNow,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(AppCtaCornerRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.share_sheet_share_now),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShareHeroCard(
    field: UserField,
    onOpenLink: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenLink),
        shape = RoundedCornerShape(AppHomeVenueCornerRadius),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 10.dp,
        tonalElevation = 2.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 174.dp, max = 188.dp)
        ) {
            val compact = maxWidth < 360.dp
            val ultraCompact = maxWidth < 340.dp
            val contentPadding = if (ultraCompact) 14.dp else 16.dp
            val avatarContainerSize = if (ultraCompact) 42.dp else if (compact) 46.dp else 54.dp
            val avatarSize = if (ultraCompact) 32.dp else if (compact) 36.dp else 42.dp
            val avatarIconSize = if (ultraCompact) 17.dp else if (compact) 19.dp else 22.dp
            val titleStyle = when {
                ultraCompact -> MaterialTheme.typography.titleLarge
                compact -> MaterialTheme.typography.headlineSmall
                else -> MaterialTheme.typography.headlineMedium
            }
            val locationStyle = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium
            val chipSpacing = if (ultraCompact) 6.dp else 8.dp

            Image(
                painter = painterResource(id = R.drawable.banner_app),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x220C4A6E),
                                Color(0x88122B45)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(contentPadding),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.92f),
                    shadowElevation = 6.dp
                ) {
                    Box(
                        modifier = Modifier.size(avatarContainerSize),
                        contentAlignment = Alignment.Center
                    ) {
                        SportCircleAvatar(
                            iconType = field.sportIconType,
                            size = avatarSize,
                            iconSize = avatarIconSize
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = field.name,
                        style = titleStyle,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = titleStyle.lineHeight,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = field.location,
                        style = locationStyle,
                        color = Color.White.copy(alpha = 0.92f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(chipSpacing),
                        verticalArrangement = Arrangement.spacedBy(chipSpacing)
                    ) {
                        ShareMetaChip(
                            icon = Icons.Default.AccessTime,
                            text = field.hours.ifBlank { "05:00 - 23:00" },
                            compact = compact
                        )
                        ShareMetaChip(
                            icon = Icons.Default.MonetizationOn,
                            text = field.price.ifBlank { "185.000đ/giờ" },
                            compact = compact
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShareMetaChip(
    icon: ImageVector,
    text: String,
    compact: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 8.dp else 10.dp,
                vertical = if (compact) 6.dp else 7.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(if (compact) 14.dp else 15.dp)
            )
            Text(
                text = text,
                style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ShareLinkCard(
    shareUrl: String,
    onCopyLink: () -> Unit
) {
    val displayUrl = shareUrl
        .replace("://", "://\u200B")
        .replace("/", "/\u200B")
        .replace(".", ".\u200B")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppHomeVenueCornerRadius),
        color = Color.White,
        shadowElevation = 2.dp,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, Color(0xFFE4EBF3))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShareLinkIcon()

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.share_sheet_link_label),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = displayUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clickable(onClick = onCopyLink),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.share_sheet_copy_link),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun ShareLinkIcon() {
    Surface(
        shape = CircleShape,
        color = Color(0xFFF3F7FB)
    ) {
        Box(
            modifier = Modifier.size(34.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = null,
                tint = Color(0xFF5D7188),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
