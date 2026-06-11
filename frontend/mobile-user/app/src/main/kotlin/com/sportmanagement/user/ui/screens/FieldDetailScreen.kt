package com.sportmanagement.user.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sportmanagement.user.domain.model.FieldDetail
import com.sportmanagement.user.domain.model.FieldDetailCourt
import com.sportmanagement.user.domain.model.FieldDetailPolicy
import com.sportmanagement.user.domain.model.FieldDetailService
import com.sportmanagement.user.domain.model.SportIconType
import com.sportmanagement.user.domain.model.mockFieldDetail
import com.sportmanagement.user.ui.components.isGeneratedFieldAvatarUrl
import com.sportmanagement.user.ui.components.sportFieldDrawableRes
import com.sportmanagement.user.ui.theme.SportUserTheme
import java.text.Normalizer
import java.text.NumberFormat
import java.util.Locale

@Composable
fun FieldDetailScreen(
    fieldDetail: FieldDetail,
    onBackClick: () -> Unit,
    onBookNowClick: (FieldDetail) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedGalleryIndex by remember { mutableStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BookNowBottomBar(
                pricePerSlot = fieldDetail.pricePerSlot,
                slotMinutes = fieldDetail.slotMinutes,
                onBookNowClick = { onBookNowClick(fieldDetail) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                FieldHeroSection(
                    detail = fieldDetail,
                    selectedGalleryIndex = selectedGalleryIndex,
                    onGalleryIndexChange = { selectedGalleryIndex = it },
                    onBackClick = onBackClick
                )
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    FieldTitleSection(detail = fieldDetail)
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            if (fieldDetail.tags.isNotEmpty()) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(fieldDetail.tags) { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(0.2f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            item {
                DetailCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionTitle("Thông tin")
                        InfoRow(Icons.Filled.LocationOn, "Địa chỉ", fieldDetail.location)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        InfoRow(Icons.Filled.Phone, "Hotline", fieldDetail.phone)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        InfoRow(Icons.Filled.Schedule, "Giờ hoạt động", fieldDetail.hours)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        InfoRow(Icons.Filled.CalendarToday, "Đặt sân", "${fieldDetail.slotMinutes} phút/slot · ${formatVnd(fieldDetail.pricePerSlot)}đ/slot")
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            if (fieldDetail.courts.isNotEmpty()) {
                item {
                    DetailCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SectionTitle("Sân con")
                                Text(
                                    text = "${fieldDetail.courts.count { it.isActive }} sân hoạt động",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            fieldDetail.courts.forEach { court ->
                                CourtRow(court = court)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            if (fieldDetail.services.isNotEmpty()) {
                item {
                    DetailCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SectionTitle("Dịch vụ")
                            fieldDetail.services.forEachIndexed { idx, service ->
                                ServiceRow(service = service)
                                if (idx < fieldDetail.services.lastIndex) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            if (fieldDetail.policies.isNotEmpty()) {
                item {
                    DetailCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SectionTitle("Chính sách & Nội quy")
                            fieldDetail.policies.forEachIndexed { idx, policy ->
                                PolicyRow(policy = policy)
                                if (idx < fieldDetail.policies.lastIndex) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

// ── Hero Section ──────────────────────────────────────────────────────────────

@Composable
private fun FieldHeroSection(
    detail: FieldDetail,
    selectedGalleryIndex: Int,
    onGalleryIndexChange: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val allImages = remember(detail.cardImageUrl, detail.avatarImageUrl, detail.galleryUrls) {
        buildList {
            add(detail.cardImageUrl)
            if (!isGeneratedFieldAvatarUrl(detail.avatarImageUrl)) {
                add(detail.avatarImageUrl)
            }
            addAll(detail.galleryUrls)
        }
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.endsWith("placeholder.svg", ignoreCase = true) }
            .distinct()
    }
    val heroImageUrl = allImages.getOrNull(selectedGalleryIndex) ?: allImages.firstOrNull()
    val fallbackPainter = painterResource(id = sportFieldDrawableRes(detail.resolveSportIconType()))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        if (heroImageUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(heroImageUrl)
                    .size(1080, 560)
                    .crossfade(false)
                    .build(),
                contentDescription = detail.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = fallbackPainter,
                error = fallbackPainter,
                fallback = fallbackPainter
            )
        } else {
            androidx.compose.foundation.Image(
                painter = fallbackPainter,
                contentDescription = detail.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(0.4f), Color.Transparent, Color.Black.copy(0.5f)),
                        startY = 0f, endY = Float.MAX_VALUE
                    )
                )
        )

        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(0.4f))
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
        }

        if (allImages.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                allImages.forEachIndexed { idx, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (idx == selectedGalleryIndex) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (idx == selectedGalleryIndex) Color.White
                                else Color.White.copy(0.5f)
                            )
                    )
                }
            }
        }
    }

    if (allImages.size > 1) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allImages.forEachIndexed { idx, url ->
                item {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .let {
                                if (idx == selectedGalleryIndex) {
                                    it.shadow(4.dp, RoundedCornerShape(8.dp))
                                } else {
                                    it
                                }
                            }
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onGalleryIndexChange(idx) }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(url)
                                .size(180, 180)
                                .crossfade(false)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                                .let {
                                    if (idx == selectedGalleryIndex)
                                        it.background(MaterialTheme.colorScheme.primary.copy(0.1f))
                                    else it
                                },
                            contentScale = ContentScale.Crop
                        )
                        if (idx == selectedGalleryIndex) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(0.25f))
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun FieldDetail.resolveSportIconType(): SportIconType {
    val normalized = Normalizer.normalize(sportType.trim(), Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
        .uppercase(Locale.ROOT)
    return when (normalized) {
        "FOOTBALL", "BONG DA" -> SportIconType.FOOTBALL
        "TENNIS" -> SportIconType.TENNIS
        "BADMINTON", "CAU LONG" -> SportIconType.BADMINTON
        "VOLLEYBALL", "BONG CHUYEN" -> SportIconType.VOLLEYBALL
        "PICKLEBALL" -> SportIconType.PICKLEBALL
        else -> SportIconType.FOOTBALL
    }
}

// ── Title Section ─────────────────────────────────────────────────────────────

@Composable
private fun FieldTitleSection(detail: FieldDetail) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = detail.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(0.3f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = detail.sportType,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = detail.rating.toString(),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = "(${detail.reviewCount} đánh giá)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

// ── Court Row ─────────────────────────────────────────────────────────────────

@Composable
private fun CourtRow(court: FieldDetailCourt) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = court.courtCode,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = court.courtName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (court.isActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outlineVariant)
            )
            Text(
                text = if (court.isActive) "Hoạt động" else "Tạm ngưng",
                fontSize = 12.sp,
                color = if (court.isActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline
            )
        }
    }
}

// ── Service Row ───────────────────────────────────────────────────────────────

@Composable
private fun ServiceRow(service: FieldDetailService) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = if (service.isFree) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = service.serviceName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = if (service.isFree) "Miễn phí" else "${formatVnd(service.price)}đ",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (service.isFree) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
        )
    }
}

// ── Policy Row ────────────────────────────────────────────────────────────────

@Composable
private fun PolicyRow(policy: FieldDetailPolicy) {
    val typeLabels = mapOf("payment" to "Thanh toán", "cancellation" to "Hủy đặt", "rules" to "Nội quy", "other" to "Khác")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(0.2f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = typeLabels[policy.policyType] ?: policy.policyType,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = policy.title,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = policy.content,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )
    }
}

// ── Book Now Bottom Bar ───────────────────────────────────────────────────────

@Composable
private fun BookNowBottomBar(
    pricePerSlot: Long,
    slotMinutes: Int,
    onBookNowClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "GIÁ TỪNG SLOT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.outline,
                    letterSpacing = 0.8.sp
                )
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${formatVnd(pricePerSlot)}đ",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "/$slotMinutes phút",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
            Button(
                onClick = onBookNowClick,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    Icons.Filled.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Đặt sân ngay", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun DetailCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    androidx.compose.material3.Card(
        modifier = modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) { content() }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(18.dp)
                .padding(top = 2.dp)
        )
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

private fun formatVnd(amount: Long): String =
    NumberFormat.getNumberInstance(Locale("vi", "VN")).format(amount)

// ── Preview ────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun FieldDetailScreenPreview() {
    SportUserTheme {
        FieldDetailScreen(
            fieldDetail = mockFieldDetail(),
            onBackClick = {},
            onBookNowClick = {}
        )
    }
}
