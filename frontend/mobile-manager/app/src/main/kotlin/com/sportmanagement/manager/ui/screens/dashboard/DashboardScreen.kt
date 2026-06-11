package com.sportmanagement.manager.ui.screens.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sportmanagement.manager.domain.model.DashboardStats
import com.sportmanagement.manager.domain.model.RevenuePeriod
import com.sportmanagement.manager.domain.model.UpcomingBooking
import com.sportmanagement.manager.domain.model.WeeklyRevenuePoint
import com.sportmanagement.manager.ui.theme.Amber
import com.sportmanagement.manager.ui.theme.AmberContainer
import com.sportmanagement.manager.ui.theme.AmberText
import com.sportmanagement.manager.ui.theme.SurfaceContainerLow
import com.sportmanagement.manager.ui.viewmodel.DashboardViewModel
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun DashboardScreen(
    padding: PaddingValues,
    viewModel: DashboardViewModel = viewModel(),
    onAddBooking: () -> Unit = {},
    onViewSchedule: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.error != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = uiState.error ?: "Lỗi tải dữ liệu",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Button(onClick = { viewModel.loadStats(); viewModel.loadMonthlyRevenue() }) {
                        Text("Thử lại")
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        MetricsSection(stats = uiState.stats)
                    }
                    item {
                        RevenueTrendCard(
                            points = uiState.activeRevenue,
                            selectedPeriod = uiState.selectedPeriod,
                            onPeriodSelected = viewModel::onPeriodSelected
                        )
                    }
                    item {
                        QuickActionsSection(
                            onAddBooking = onAddBooking,
                            onViewSchedule = onViewSchedule
                        )
                    }
                    uiState.upcomingBooking?.let { booking ->
                        item {
                            UpcomingSlotCard(booking = booking)
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

// ─── Metrics ─────────────────────────────────────────────────────────────────

@Composable
private fun MetricsSection(stats: DashboardStats) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        RevenueCard(stats = stats)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                label = "SỐ LƯỢT ĐẶT",
                value = stats.bookingCount.toString(),
                icon = Icons.Filled.EventAvailable,
                iconBgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                iconTint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "TỶ LỆ LẤP ĐẦY",
                value = "${stats.occupancyRate}%",
                icon = Icons.Filled.PieChart,
                iconBgColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                iconTint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }
        TopPitchCard(pitchName = stats.topPitchName, revenue = stats.topPitchRevenue)
    }
}

@Composable
private fun TopPitchCard(pitchName: String, revenue: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(Color(0xFFF59E0B))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "SÂN DOANH THU CAO NHẤT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = pitchName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = formatVnd(revenue) + " VND",
                    fontSize = 13.sp,
                    color = Color(0xFFF59E0B),
                    fontWeight = FontWeight.SemiBold
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFEF3C7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun RevenueCard(stats: DashboardStats) {
    val accentColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(accentColor)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DOANH THU HÔM NAY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    letterSpacing = 0.8.sp
                )
                Icon(
                    imageVector = Icons.Filled.Payments,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = formatVnd(stats.revenue),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 28.sp
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "VND",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            Spacer(Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "+${stats.revenueTrendPercent}% so với hôm qua",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                letterSpacing = 0.8.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

// ─── Revenue Trend Chart ──────────────────────────────────────────────────────

@Composable
private fun RevenueTrendCard(
    points: List<WeeklyRevenuePoint>,
    selectedPeriod: RevenuePeriod,
    onPeriodSelected: (RevenuePeriod) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Xu hướng doanh thu",
                    style = MaterialTheme.typography.headlineSmall
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PeriodDropdown(
                        selectedPeriod = selectedPeriod,
                        onPeriodSelected = onPeriodSelected
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerLow)
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = "Xuất báo cáo",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 180.dp)
                    .height(140.dp)
            ) {
                RevenueLineChart(
                    points = points,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.82f)
                        .align(Alignment.TopCenter)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    points.forEach { point ->
                        Text(
                            text = point.dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            letterSpacing = 0.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodDropdown(
    selectedPeriod: RevenuePeriod,
    onPeriodSelected: (RevenuePeriod) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceContainerLow)
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = selectedPeriod.label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 12.sp
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.outline
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            RevenuePeriod.entries.forEach { period ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = period.label,
                            fontWeight = if (period == selectedPeriod) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onPeriodSelected(period)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun RevenueLineChart(
    points: List<WeeklyRevenuePoint>,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gradientTop = lineColor.copy(alpha = 0.22f)
    val gradientBottom = lineColor.copy(alpha = 0f)

    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas

        val w = size.width
        val h = size.height
        val n = points.size
        val topPad = h * 0.08f
        val drawH = h - topPad

        val xs = points.indices.map { i -> i * w / (n - 1).toFloat() }
        val ys = points.map { p -> topPad + drawH * (1f - p.normalizedValue) }

        // Smooth cubic bezier path
        val linePath = Path()
        linePath.moveTo(xs[0], ys[0])
        for (i in 0 until n - 1) {
            val cx = (xs[i] + xs[i + 1]) / 2f
            linePath.cubicTo(cx, ys[i], cx, ys[i + 1], xs[i + 1], ys[i + 1])
        }

        // Fill path
        val fillPath = Path()
        fillPath.addPath(linePath)
        fillPath.lineTo(xs.last(), h)
        fillPath.lineTo(xs.first(), h)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(gradientTop, gradientBottom),
                startY = 0f,
                endY = h
            )
        )

        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

// ─── Quick Actions ────────────────────────────────────────────────────────────

@Composable
private fun QuickActionsSection(
    onAddBooking: () -> Unit = {},
    onViewSchedule: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Thao tác nhanh",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
        QuickActionButton(
            categoryLabel = "ĐẶT SÂN",
            categoryIcon = Icons.Filled.EventNote,
            title = "Thêm đặt sân mới",
            subtitle = "Tạo lịch đặt nhanh cho khách",
            icon = Icons.Filled.AddCircle,
            isPrimary = true,
            onClick = onAddBooking
        )
        QuickActionButton(
            categoryLabel = "LỊCH TRÌNH",
            categoryIcon = Icons.Filled.EventNote,
            title = "Lịch trình hôm nay",
            subtitle = "Xem lịch đặt sân trong ngày",
            icon = Icons.Filled.CalendarMonth,
            isPrimary = false,
            onClick = onViewSchedule
        )
    }
}

@Composable
private fun QuickActionButton(
    categoryLabel: String,
    categoryIcon: ImageVector,
    title: String,
    subtitle: String,
    icon: ImageVector,
    isPrimary: Boolean,
    onClick: () -> Unit = {}
) {
    val bgColor = if (isPrimary) MaterialTheme.colorScheme.primary else Color.White
    val textColor = if (isPrimary) Color.White else MaterialTheme.colorScheme.onBackground
    val subtitleColor = if (isPrimary) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline
    val categoryColor = if (isPrimary) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline
    val iconBgColor = if (isPrimary) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    val iconTint = if (isPrimary) Color.White else MaterialTheme.colorScheme.primary

    val rowModifier = if (isPrimary) {
        Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(16.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = categoryLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = categoryColor,
                    letterSpacing = 0.8.sp
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = subtitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─── Upcoming Slot ────────────────────────────────────────────────────────────

@Composable
private fun UpcomingSlotCard(booking: UpcomingBooking) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
    ) {
        // Left amber accent
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(Amber)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AmberContainer)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "SẮP DIỄN RA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberText,
                        letterSpacing = 0.8.sp
                    )
                }
                Text(
                    text = "Bắt đầu trong ${booking.minutesUntilStart}p",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${booking.courtName} - ${booking.teamName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${booking.startTime} - ${booking.endTime} • ${if (booking.isPaid) "Đã thanh toán" else "Chờ thanh toán"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun formatVnd(amount: Long): String =
    NumberFormat.getNumberInstance(Locale("vi", "VN")).format(amount)

// ─── Preview ──────────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
    device = "id:pixel_6"
)
@Composable
private fun DashboardScreenPreview() {
    com.sportmanagement.manager.ui.theme.SportManagerTheme {
        DashboardScreen(
            padding = androidx.compose.foundation.layout.PaddingValues(0.dp),
            viewModel = DashboardViewModel()
        )
    }
}

