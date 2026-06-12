package com.sportmanagement.user.ui.components.field

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.compose.SubcomposeAsyncImage
import com.sportmanagement.user.R
import com.sportmanagement.user.domain.model.FieldDetailPolicy
import com.sportmanagement.user.domain.model.FieldDetailService
import com.sportmanagement.user.domain.model.FieldReview
import com.sportmanagement.user.domain.model.FieldReviewStats
import com.sportmanagement.user.domain.model.SportIconType
import com.sportmanagement.user.domain.model.UserField
import com.sportmanagement.user.domain.model.UserFieldDetailData
import com.sportmanagement.user.ui.components.booking.bookingCardTitleStyle
import com.sportmanagement.user.ui.components.SportCircleAvatar
import com.sportmanagement.user.ui.components.SportMarkerIcon
import com.sportmanagement.user.ui.components.isGeneratedFieldAvatarUrl
import com.sportmanagement.user.ui.components.home.HomeVenueTitleText
import com.sportmanagement.user.ui.components.sportAvatarBackgroundColor
import com.sportmanagement.user.ui.components.sportFieldDrawableRes
import com.sportmanagement.user.ui.components.sportIconDrawableRes
import com.sportmanagement.user.ui.share.FieldShareLink
import com.sportmanagement.user.ui.theme.AppBadgeCornerRadius
import com.sportmanagement.user.ui.theme.AppCardCornerRadius
import com.sportmanagement.user.ui.theme.AppCtaCompactHorizontalPadding
import com.sportmanagement.user.ui.theme.AppCtaCompactVerticalPadding
import com.sportmanagement.user.ui.theme.AppCtaAmber
import com.sportmanagement.user.ui.theme.AppCtaCornerRadius
import com.sportmanagement.user.ui.theme.AppInputCornerRadius
import com.sportmanagement.user.ui.theme.AppPillCornerRadius
import com.sportmanagement.user.ui.theme.AppMediaCornerRadius
import com.sportmanagement.user.ui.theme.AppSheetTopCornerRadius
import com.sportmanagement.user.ui.theme.AppOnCtaAmber
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.text.NumberFormat
import java.util.Locale

private val sheetTabRes = listOf(
    R.string.field_detail_tab_info,
    R.string.field_detail_tab_services,
    R.string.field_detail_tab_gallery,
    R.string.field_detail_tab_policies,
    R.string.field_detail_tab_reviews
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldDetailBottomSheet(
    field: UserField,
    isFavorite: Boolean,
    reviewStats: FieldReviewStats? = null,
    reviews: List<FieldReview> = emptyList(),
    isReviewLoading: Boolean = false,
    fieldDetailData: UserFieldDetailData? = null,
    onDismissRequest: () -> Unit,
    onFavoriteClick: () -> Unit,
    onBookClick: (UserField) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val density = LocalDensity.current
    val colors = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var selectedTab by rememberSaveable(field.name) { mutableIntStateOf(0) }
    var previewImageUrl by remember { mutableStateOf<String?>(null) }
    var ratingBadgeHeightPx by remember { mutableIntStateOf(0) }
    val bookingLink = remember(field.fieldId, field.name) { bookingLinkFor(field) }
    val resolvedDetailData = remember(field.fieldId, fieldDetailData) {
        fieldDetailData?.takeIf { it.fieldId == field.fieldId }
    }
    val hotline = remember(field.phone, resolvedDetailData) {
        resolvedDetailData?.phone?.trim().orEmpty().ifBlank {
            field.phone.trim()
        }.ifBlank {
            context.getString(R.string.field_detail_default_hotline)
        }
    }
    val galleryUrls = remember(field, resolvedDetailData) {
        buildList {
            addAll(resolvedDetailData?.galleryUrls.orEmpty())
            add(field.cardImageUrl)
            add(field.avatarImageUrl)
            add(field.imageUrl)
        }
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.endsWith("placeholder.svg", ignoreCase = true) }
            .distinct()
    }
    val headerImageHeight = 232.dp
    val infoCardOverlap = 28.dp
    val resolvedReviewMetrics = remember(reviewStats, reviews, field.rating) {
        resolveFieldReviewMetrics(reviewStats, reviews, field.rating)
    }
    val ratingBadgeOffsetY = remember(ratingBadgeHeightPx, density) {
        val badgeHeightDp = with(density) { ratingBadgeHeightPx.toDp() }
        val cardTopBoundaryY = headerImageHeight - infoCardOverlap
        cardTopBoundaryY - (badgeHeightDp / 2)
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = colors.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = AppSheetTopCornerRadius, topEnd = AppSheetTopCornerRadius)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(headerImageHeight)
                                .clip(RoundedCornerShape(topStart = AppInputCornerRadius, topEnd = AppInputCornerRadius))
                        ) {
                            FieldDetailHeaderImage(
                                field = field,
                                extraImageUrls = galleryUrls,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0x66000000),
                                                Color(0x22000000),
                                                Color(0xAA000000)
                                            )
                                        )
                                    )
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircleActionButton(
                                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                                    onClick = onDismissRequest
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircleActionButton(
                                        icon = Icons.Default.Directions,
                                        onClick = { openDirections(context, field) }
                                    )
                                    CircleActionButton(
                                        icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        iconTint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                        onClick = onFavoriteClick
                                    )
                                    Button(
                                        onClick = { onBookClick(field) },
                                        modifier = Modifier.defaultMinSize(minWidth = 70.dp, minHeight = 34.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AppCtaAmber,
                                            contentColor = AppOnCtaAmber
                                        ),
                                        shape = RoundedCornerShape(AppCtaCornerRadius),
                                        contentPadding = PaddingValues(
                                            horizontal = (AppCtaCompactHorizontalPadding - 2.dp),
                                            vertical = AppCtaCompactVerticalPadding
                                        )
                                    ) {
                                        Text(
                                            text = stringResource(R.string.home_book_button),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .offset(y = -infoCardOverlap),
                            shape = RoundedCornerShape(AppCardCornerRadius),
                            colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLowest),
                            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 14.dp, end = 14.dp, bottom = 14.dp, top = 30.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    FieldDetailAvatar(
                                        field = field,
                                        size = 52.dp,
                                        iconSize = 38.dp
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = field.name,
                                            style = bookingCardTitleStyle(),
                                            color = colors.onSurface,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 2,
                                            softWrap = true,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        SportTypeSelectedPill(type = field.sportIconType)
                                    }
                                }
                                HorizontalDivider(
                                    color = colors.outlineVariant.copy(alpha = 0.65f),
                                    thickness = 1.dp
                                )

                                InfoLine(
                                    icon = Icons.Default.LocationOn,
                                    title = field.location,
                                    trailingText = stringResource(R.string.field_detail_open_map),
                                    onTrailingClick = { openDirections(context, field) }
                                )
                                InfoLine(
                                    icon = Icons.Default.AccessTime,
                                    title = field.hours
                                )
                                InfoLine(
                                    icon = Icons.Default.Call,
                                    title = stringResource(R.string.field_detail_hotline_format, hotline),
                                    trailingText = stringResource(R.string.field_detail_call_quick),
                                    onTrailingClick = { dialHotline(context, hotline) }
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = ratingBadgeOffsetY)
                            .zIndex(2f),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Surface(
                            modifier = Modifier
                                .onSizeChanged { size -> ratingBadgeHeightPx = size.height },
                            shape = RoundedCornerShape(AppPillCornerRadius),
                            color = colors.primary,
                            shadowElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = ratingLabel(
                                        averageRating = resolvedReviewMetrics.averageRating,
                                        reviewCount = resolvedReviewMetrics.reviewCount
                                    ),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colors.onPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        edgePadding = 0.dp,
                        containerColor = Color.Transparent,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = colors.primary,
                                height = 2.dp
                            )
                        },
                        divider = {}
                    ) {
                        sheetTabRes.forEachIndexed { index, labelRes ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = {
                                    selectedTab = index
                                    scope.launch { sheetState.expand() }
                                },
                                text = {
                                    Text(
                                        text = stringResource(labelRes),
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1
                                    )
                                },
                                selectedContentColor = colors.primary,
                                unselectedContentColor = colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .heightIn(min = 240.dp)
                ) {
                    when (selectedTab) {
                        0 -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            InfoTabContent(field, resolvedDetailData, hotline)
                            OnlineBookingLinkCard(
                                bookingLink = bookingLink,
                                onCopyLink = {
                                    clipboard.setText(AnnotatedString(bookingLink))
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.field_detail_link_copied),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onOpenLink = { openExternalUrl(context, bookingLink) }
                            )
                        }
                        1 -> ServiceTabContent(field, resolvedDetailData)
                        2 -> GalleryTabContent(
                            galleryUrls = galleryUrls,
                            onPreview = { previewImageUrl = it }
                        )
                        3 -> PolicyTabContent(resolvedDetailData)
                        else -> ReviewTabContent(
                            fieldSportType = field.sportIconType,
                            reviews = reviews,
                            isLoading = isReviewLoading
                        )
                    }
                }
            }
        }

        previewImageUrl?.let { imageUrl ->
            Dialog(onDismissRequest = { previewImageUrl = null }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AppCardCornerRadius))
                        .background(Color.Black)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(false)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
private fun FieldDetailHeaderImage(
    field: UserField,
    extraImageUrls: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val remoteImageUrl = listOf(
        *extraImageUrls.toTypedArray(),
        field.cardImageUrl.trim(),
        field.avatarImageUrl.trim().takeIf { !isGeneratedFieldAvatarUrl(it) }.orEmpty(),
        field.imageUrl.trim().takeIf { !isGeneratedFieldAvatarUrl(it) }.orEmpty()
    ).firstOrNull { it.isNotBlank() }.orEmpty()
    val fallbackPainter = painterResource(id = sportFieldDrawableRes(field.sportIconType))
    val shouldLoadRemoteImage =
        remoteImageUrl.isNotBlank() &&
            !remoteImageUrl.endsWith("placeholder.svg", ignoreCase = true)

    if (shouldLoadRemoteImage) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(remoteImageUrl)
                .size(1080, 560)
                .crossfade(false)
                .build(),
            contentDescription = field.name,
            modifier = modifier,
            contentScale = ContentScale.Crop,
            placeholder = fallbackPainter,
            error = fallbackPainter,
            fallback = fallbackPainter
        )
    } else {
        Image(
            painter = fallbackPainter,
            contentDescription = field.name,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun FieldDetailAvatar(
    field: UserField,
    size: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val remoteAvatarUrl = field.avatarImageUrl.trim()
    val shouldLoadRemoteImage =
        remoteAvatarUrl.isNotBlank() &&
            !remoteAvatarUrl.endsWith("placeholder.svg", ignoreCase = true) &&
            !isGeneratedFieldAvatarUrl(remoteAvatarUrl)
    val imageSizePx = with(density) { size.roundToPx() }

    if (shouldLoadRemoteImage) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(remoteAvatarUrl)
                .size(imageSizePx, imageSizePx)
                .crossfade(false)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(sportAvatarBackgroundColor(field.sportIconType)),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = sportIconDrawableRes(field.sportIconType)),
            error = painterResource(id = sportIconDrawableRes(field.sportIconType)),
            fallback = painterResource(id = sportIconDrawableRes(field.sportIconType))
        )
    } else {
        SportCircleAvatar(
            iconType = field.sportIconType,
            size = size,
            iconSize = iconSize
        )
    }
}

@Composable
private fun CircleActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    iconTint: Color = Color.Unspecified
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .size(36.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = colors.surface.copy(alpha = 0.92f),
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (iconTint == Color.Unspecified) colors.onSurface else iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SportTypeSelectedPill(type: SportIconType) {
    val accent = sportAccentColor(type)
    Surface(
        shape = RoundedCornerShape(AppPillCornerRadius),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.8.dp,
            color = accent.copy(alpha = 0.85f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SportMarkerIcon(
                iconType = type,
                contentDescription = null,
                markerSize = 18.dp,
                iconSize = 9.dp,
                iconOffsetY = (-1).dp
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(sportLabelRes(type)).lowercase(Locale("vi", "VN")),
                style = MaterialTheme.typography.bodySmall,
                color = accent,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun InfoLine(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    trailingText: String? = null,
    onTrailingClick: (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.primary
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (trailingText != null && onTrailingClick != null) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.primary,
                modifier = Modifier.clickable(onClick = onTrailingClick)
            )
        }
    }
}

@Composable
private fun InfoTabContent(
    field: UserField,
    detailData: UserFieldDetailData?,
    hotline: String
) {
    val colors = MaterialTheme.colorScheme
    val infoItems = buildList {
        add(stringResource(R.string.field_detail_info_sport) to stringResource(sportLabelRes(field.sportIconType)))
        add("Địa chỉ" to field.location)
        add(stringResource(R.string.field_detail_contact_label) to hotline)
        add("Giờ hoạt động" to field.hours)
        if (field.price.isNotBlank()) {
            add("Giá tham khảo" to field.price)
        }
        detailData?.courtCount?.takeIf { it > 0 }?.let { add("Số lượng sân" to "$it sân") }
        detailData?.availabilityNote?.trim()?.takeIf { it.isNotBlank() }?.let {
            add("Ghi chú vận hành" to it)
        }
    }
    Card(
        shape = RoundedCornerShape(AppCardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLowest)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            infoItems.forEach { (label, value) ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "$label:",
                        modifier = Modifier.widthIn(min = 120.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceTabContent(
    field: UserField,
    detailData: UserFieldDetailData?
) {
    val colors = MaterialTheme.colorScheme
    val services = remember(field.tags, detailData) {
        val remoteServices = detailData?.services.orEmpty().takeIf { it.isNotEmpty() }
        remoteServices ?: field.tags.mapNotNull { tag ->
            tag.trim().takeIf { it.isNotBlank() }?.let { FieldDetailService(it, true) }
        }
    }
    Card(
        shape = RoundedCornerShape(AppCardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLowest)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (services.isEmpty()) {
                Text(
                    text = stringResource(R.string.field_detail_empty_services),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            } else {
                services.forEach { service ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = serviceIcon(service.serviceName),
                            contentDescription = null,
                            tint = colors.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = service.serviceName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurface
                            )
                            if (!service.isFree) {
                                Text(
                                    text = stringResource(
                                        R.string.field_detail_service_price_format,
                                        formatVnd(service.price)
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GalleryTabContent(
    galleryUrls: List<String>,
    onPreview: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    if (galleryUrls.isEmpty()) {
        Card(
            shape = RoundedCornerShape(AppCardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLowest)
        ) {
            Text(
                text = stringResource(R.string.field_detail_empty_gallery),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(12.dp)
            )
        }
        return
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(galleryUrls) { imageUrl ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(false)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(width = 200.dp, height = 120.dp)
                    .clip(RoundedCornerShape(AppMediaCornerRadius))
                    .clickable { onPreview(imageUrl) },
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun PolicyTabContent(detailData: UserFieldDetailData?) {
    val colors = MaterialTheme.colorScheme
    val policies = remember(detailData) {
        val remotePolicies = detailData?.policies.orEmpty().takeIf { it.isNotEmpty() }
        remotePolicies ?: detailData?.availabilityNote
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let {
                listOf(FieldDetailPolicy("availability", "Ghi chú vận hành", it))
            }
            .orEmpty()
    }
    Card(
        shape = RoundedCornerShape(AppCardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLowest)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (policies.isEmpty()) {
                Text(
                    text = stringResource(R.string.field_detail_empty_policies),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            } else {
                policies.forEach { policy ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = policy.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = policy.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewTabContent(
    fieldSportType: SportIconType,
    reviews: List<FieldReview>,
    isLoading: Boolean
) {
    val colors = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(AppCardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLowest)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when {
                isLoading && reviews.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    }
                }

                reviews.isEmpty() -> {
                    Text(
                        text = "Chưa có đánh giá",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                }

                else -> {
                    HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f))
                    reviews.forEachIndexed { index, review ->
                        ReviewItemWithRemoteImages(
                            author = review.customerName.ifBlank { "Người chơi" },
                            avatarUrl = review.customerAvatarUrl,
                            comment = review.comment,
                            createdAt = review.createdAt,
                            rating = review.rating,
                            imageUrls = review.imageUrls,
                            avatarSportType = fieldSportType
                        )
                        if (index != reviews.lastIndex) {
                            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.45f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnlineBookingLinkCard(
    bookingLink: String,
    onCopyLink: () -> Unit,
    onOpenLink: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(AppCardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.field_detail_booking_link_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
            Text(
                text = bookingLink,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AssistChip(
                    onClick = onCopyLink,
                    label = { Text(stringResource(R.string.field_detail_copy_link)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                )
                AssistChip(
                    onClick = onOpenLink,
                    label = { Text(stringResource(R.string.field_detail_open_link)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Launch,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ReviewItemWithRemoteImages(
    author: String,
    avatarUrl: String,
    comment: String,
    createdAt: String,
    rating: Int,
    imageUrls: List<String>,
    avatarSportType: SportIconType
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ReviewAvatar(
                avatarUrl = avatarUrl,
                author = author
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = author,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                val reviewTimeLabel = remember(createdAt) { formatReviewCreatedAt(createdAt) }
                if (reviewTimeLabel.isNotBlank()) {
                    Text(
                        text = reviewTimeLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                                    text = stringResource(R.string.field_detail_rating_value, rating),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFE59C00),
                                    fontWeight = FontWeight.SemiBold
                                )
            Spacer(Modifier.width(4.dp))
            StarRating(rating = rating, iconSize = 13.dp)
        }

        Text(
            text = comment,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (imageUrls.isNotEmpty()) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                imageUrls.forEach { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(AppBadgeCornerRadius))
                    )
                }
            }
        }
    }
}

@Composable
private fun StarRating(rating: Int, iconSize: androidx.compose.ui.unit.Dp = 14.dp) {
    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        repeat(5) { index ->
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (index < rating) Color(0xFFE59C00) else Color(0xFFCFD3D8),
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@Composable
private fun ReviewAvatar(
    avatarUrl: String,
    author: String
) {
    val normalizedAvatarUrl = avatarUrl.trim()
    if (
        normalizedAvatarUrl.isBlank() ||
        normalizedAvatarUrl.equals("null", ignoreCase = true) ||
        normalizedAvatarUrl.endsWith("/null", ignoreCase = true) ||
        normalizedAvatarUrl.endsWith("/undefined", ignoreCase = true)
    ) {
        DefaultReviewAvatar(author)
        return
    }

    SubcomposeAsyncImage(
        model = normalizedAvatarUrl,
        contentDescription = author,
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop,
        error = {
            DefaultReviewAvatar(author)
        },
        loading = {
            DefaultReviewAvatar(author)
        }
    )
}

@Composable
private fun DefaultReviewAvatar(author: String) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                CircleShape
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = author,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(18.dp)
        )
    }
}

private fun serviceIcon(serviceName: String): androidx.compose.ui.graphics.vector.ImageVector {
    val normalized = serviceName.lowercase(Locale("vi", "VN"))
    return when {
        normalized.contains("parking") || normalized.contains("gửi xe") || normalized.contains("do xe") -> Icons.Default.Directions
        normalized.contains("nước") || normalized.contains("drink") || normalized.contains("uống") -> Icons.Default.Star
        normalized.contains("vợt") || normalized.contains("racket") || normalized.contains("cầu") || normalized.contains("bóng") -> Icons.Default.SportsTennis
        normalized.contains("huấn luyện") || normalized.contains("coach") || normalized.contains("trainer") -> Icons.Default.Person
        normalized.contains("thay đồ") || normalized.contains("locker") || normalized.contains("changing") -> Icons.Default.Person
        else -> Icons.Default.CheckCircle
    }
}

private fun formatVnd(value: Long): String {
    if (value <= 0L) return "Miễn phí"
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return "${formatter.format(value)}đ"
}

private fun sportLabelRes(type: SportIconType): Int {
    return when (type) {
        SportIconType.FOOTBALL -> R.string.field_detail_sport_football
        SportIconType.PICKLEBALL -> R.string.field_detail_sport_pickleball
        SportIconType.TENNIS -> R.string.field_detail_sport_tennis
        SportIconType.BADMINTON -> R.string.field_detail_sport_badminton
        SportIconType.VOLLEYBALL -> R.string.field_detail_sport_volleyball
    }
}

private fun sportAccentColor(type: SportIconType): Color {
    return when (type) {
        SportIconType.FOOTBALL -> Color(0xFF3B82F6)
        SportIconType.PICKLEBALL -> Color(0xFF14B8A6)
        SportIconType.TENNIS -> Color(0xFF0EA5E9)
        SportIconType.BADMINTON -> Color(0xFFA855F7)
        SportIconType.VOLLEYBALL -> Color(0xFFF59E0B)
    }
}

private fun sportAccentOnColor(type: SportIconType): Color {
    val accent = sportAccentColor(type)
    return if (accent.luminance() > 0.55f) Color(0xFF1A1A1A) else Color.White
}

private fun ratingLabel(
    averageRating: Double,
    reviewCount: Int
): String {
    return if (averageRating <= 0.0 || reviewCount <= 0) {
        "Chưa có đánh giá"
    } else {
        "${formatFieldRating(averageRating)} ($reviewCount đánh giá)"
    }
}

private fun formatReviewCreatedAt(value: String): String {
    val trimmed = value.trim()
    if (trimmed.isBlank()) {
        return ""
    }

    val candidates = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" to "dd/MM/yyyy HH:mm",
        "yyyy-MM-dd'T'HH:mm:ss'Z'" to "dd/MM/yyyy HH:mm",
        "yyyy-MM-dd HH:mm:ss" to "dd/MM/yyyy HH:mm",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX" to "dd/MM/yyyy HH:mm",
        "yyyy-MM-dd'T'HH:mm:ssXXX" to "dd/MM/yyyy HH:mm"
    )

    candidates.forEach { (inputPattern, outputPattern) ->
        runCatching {
            val inputFormat = java.text.SimpleDateFormat(inputPattern, Locale.getDefault()).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            val outputFormat = java.text.SimpleDateFormat(outputPattern, Locale("vi", "VN"))
            val parsed = inputFormat.parse(trimmed)
            if (parsed != null) {
                return outputFormat.format(parsed)
            }
        }
    }

    return trimmed
}

private fun bookingLinkFor(field: UserField): String {
    val fieldId = field.fieldId
    if (fieldId > 0) {
        return FieldShareLink.webFieldLink(fieldId)
    }
    val slug = normalizeSlug(field.name)
    return "https://sport-management.vn/field/$slug"
}

private fun normalizeSlug(value: String): String {
    val normalized = Normalizer.normalize(value.lowercase(), Normalizer.Form.NFD)
    return normalized
        .replace("đ", "d")
        .replace("\\p{M}+".toRegex(), "")
        .replace("[^a-z0-9]+".toRegex(), "-")
        .trim('-')
}

private fun openDirections(context: Context, field: UserField) {
    val uri = if (field.latitude != null && field.longitude != null) {
        Uri.parse("google.navigation:q=${field.latitude},${field.longitude}")
    } else {
        Uri.parse("geo:0,0?q=${Uri.encode(field.location)}")
    }
    val intent = Intent(Intent.ACTION_VIEW, uri)
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, context.getString(R.string.field_detail_error_open_directions), Toast.LENGTH_SHORT).show()
    }
}

private fun dialHotline(context: Context, hotline: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${hotline.replace(" ", "")}"))
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, context.getString(R.string.field_detail_error_open_call), Toast.LENGTH_SHORT).show()
    }
}

private fun openExternalUrl(context: android.content.Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, context.getString(R.string.field_detail_error_open_link), Toast.LENGTH_SHORT).show()
    }
}

