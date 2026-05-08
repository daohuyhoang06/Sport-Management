package com.sportmanagement.user.ui.components.field

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
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
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import com.sportmanagement.user.R
import com.sportmanagement.user.domain.model.SportIconType
import com.sportmanagement.user.domain.model.UserField
import com.sportmanagement.user.ui.theme.AppCtaAmber
import com.sportmanagement.user.ui.theme.AppCtaCompactHorizontalPadding
import com.sportmanagement.user.ui.theme.AppCtaCompactVerticalPadding
import com.sportmanagement.user.ui.theme.AppCtaCornerRadius
import com.sportmanagement.user.ui.theme.AppOnCtaAmber
import java.text.Normalizer

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
    onDismissRequest: () -> Unit,
    onBookClick: (UserField) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val colors = MaterialTheme.colorScheme
    var selectedTab by rememberSaveable(field.name) { mutableIntStateOf(0) }
    var isFavorite by rememberSaveable(field.name) { mutableStateOf(false) }
    var previewImage by remember { mutableStateOf<Int?>(null) }
    val bookingLink = remember(field.name) { bookingLinkFor(field.name) }
    val hotline = remember(field.name) { context.getString(R.string.field_detail_default_hotline) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        dragHandle = null,
        containerColor = colors.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(232.dp)
                                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.field_default),
                                contentDescription = field.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
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
                                        onClick = { isFavorite = !isFavorite }
                                    )
                                    Button(
                                        onClick = { onBookClick(field) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AppCtaAmber,
                                            contentColor = AppOnCtaAmber
                                        ),
                                        shape = RoundedCornerShape(AppCtaCornerRadius),
                                        contentPadding = PaddingValues(horizontal = AppCtaCompactHorizontalPadding, vertical = AppCtaCompactVerticalPadding)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.home_book_button),
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
                                .offset(y = (-48).dp),
                            shape = RoundedCornerShape(16.dp),
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
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(colors.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = sportTypeIcon(field.sportIconType),
                                            contentDescription = null,
                                            tint = colors.onPrimaryContainer,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = field.name,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = colors.onSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            AssistChip(
                                                onClick = {},
                                                label = { Text(stringResource(sportLabelRes(field.sportIconType))) },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = colors.secondaryContainer,
                                                    labelColor = colors.onSecondaryContainer
                                                )
                                            )
                                            AssistChip(
                                                onClick = {},
                                                label = { Text(field.price) },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = colors.tertiaryContainer,
                                                    labelColor = colors.onTertiaryContainer
                                                )
                                            )
                                        }
                                    }
                                }

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

                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = 168.dp)
                            .zIndex(2f),
                        shape = RoundedCornerShape(999.dp),
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
                                text = ratingLabel(context, field.rating),
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.onPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
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
                                onClick = { selectedTab = index },
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
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    when (selectedTab) {
                        0 -> InfoTabContent(field)
                        1 -> ServiceTabContent()
                        2 -> GalleryTabContent(onPreview = { previewImage = it })
                        3 -> PolicyTabContent()
                        else -> ReviewTabContent()
                    }
                }
            }

            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
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
                                onClick = {
                                    clipboard.setText(AnnotatedString(bookingLink))
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.field_detail_link_copied),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                label = { Text(stringResource(R.string.field_detail_copy_link)) },
                                leadingIcon = {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                                }
                            )
                            AssistChip(
                                onClick = { openExternalUrl(context, bookingLink) },
                                label = { Text(stringResource(R.string.field_detail_open_link)) },
                                leadingIcon = {
                                    Icon(Icons.Default.Launch, contentDescription = null)
                                }
                            )
                        }
                    }
                }
                }
            }
        }

        previewImage?.let { imageRes ->
            Dialog(onDismissRequest = { previewImage = null }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
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
private fun CircleActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    iconTint: Color = Color.Unspecified
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .size(40.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = colors.surface.copy(alpha = 0.92f),
        shadowElevation = 3.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (iconTint == Color.Unspecified) colors.onSurface else iconTint
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
            color = colors.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        if (trailingText != null && onTrailingClick != null) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.labelLarge,
                color = colors.primary,
                modifier = Modifier.clickable(onClick = onTrailingClick)
            )
        }
    }
}

@Composable
private fun InfoTabContent(field: UserField) {
    val colors = MaterialTheme.colorScheme
    val infoItems = listOf(
        stringResource(R.string.field_detail_info_sport) to stringResource(sportLabelRes(field.sportIconType)),
        stringResource(R.string.field_detail_info_surface_type) to stringResource(R.string.field_detail_info_surface_value),
        stringResource(R.string.field_detail_info_court_count) to stringResource(R.string.field_detail_info_court_count_value),
        stringResource(R.string.field_detail_info_capacity) to stringResource(R.string.field_detail_info_capacity_value),
        stringResource(R.string.field_detail_info_amenities) to stringResource(R.string.field_detail_info_amenities_value)
    )
    Card(
        shape = RoundedCornerShape(14.dp),
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
private fun ServiceTabContent() {
    val colors = MaterialTheme.colorScheme
    val services = listOf(
        stringResource(R.string.field_detail_service_racket_rental) to Icons.Default.SportsTennis,
        stringResource(R.string.field_detail_service_ball_shuttle_rental) to Icons.Default.SportsSoccer,
        stringResource(R.string.field_detail_service_drinks) to Icons.Default.Star,
        stringResource(R.string.field_detail_service_parking) to Icons.Default.Directions,
        stringResource(R.string.field_detail_service_changing_room) to Icons.Default.Person,
        stringResource(R.string.field_detail_service_coach) to Icons.Default.Person
    )
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLowest)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            services.forEach { (name, icon) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colors.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun GalleryTabContent(onPreview: (Int) -> Unit) {
    val gallery = listOf(
        R.drawable.field_default,
        R.drawable.field_default,
        R.drawable.field_default,
        R.drawable.field_default
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(gallery) { imageRes ->
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(width = 200.dp, height = 120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onPreview(imageRes) },
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun PolicyTabContent() {
    val colors = MaterialTheme.colorScheme
    val policies = listOf(
        stringResource(R.string.field_detail_policy_1),
        stringResource(R.string.field_detail_policy_2),
        stringResource(R.string.field_detail_policy_3),
        stringResource(R.string.field_detail_policy_4)
    )
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLowest)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            policies.forEach { line ->
                Text(
                    text = "• $line",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ReviewTabContent() {
    val colors = MaterialTheme.colorScheme
    val reviews = listOf(
        stringResource(R.string.field_detail_review_user_1_name) to stringResource(R.string.field_detail_review_user_1_comment),
        stringResource(R.string.field_detail_review_user_2_name) to stringResource(R.string.field_detail_review_user_2_comment),
        stringResource(R.string.field_detail_review_user_3_name) to stringResource(R.string.field_detail_review_user_3_comment)
    )
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLowest)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = colors.secondary)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.field_detail_review_summary),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            reviews.forEach { (name, comment) ->
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name.take(1),
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = comment,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
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

private fun sportTypeIcon(type: SportIconType) = when (type) {
    SportIconType.FOOTBALL -> Icons.Default.SportsSoccer
    SportIconType.PICKLEBALL -> Icons.Default.SportsTennis
    SportIconType.TENNIS -> Icons.Default.SportsTennis
    SportIconType.BADMINTON -> Icons.Default.SportsTennis
    SportIconType.VOLLEYBALL -> Icons.Default.SportsSoccer
}

private fun ratingLabel(context: Context, rating: String): String {
    return if (rating.isBlank() || rating == "0" || rating == "0.0") {
        context.getString(R.string.field_detail_rating_unavailable)
    } else {
        context.getString(R.string.field_detail_rating_good_format, rating)
    }
}

private fun bookingLinkFor(fieldName: String): String {
    val slug = normalizeSlug(fieldName)
    return "https://booking.sport-management.vn/san/$slug"
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
