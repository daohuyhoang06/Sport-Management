package com.sportmanagement.manager.ui.screens.services

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportmanagement.manager.domain.model.ServiceCategory
import com.sportmanagement.manager.domain.model.ServiceDetailItem
import com.sportmanagement.manager.domain.model.ServiceItemStatus
import com.sportmanagement.manager.domain.model.StockTransaction
import com.sportmanagement.manager.domain.model.StockTxType
import com.sportmanagement.manager.ui.theme.AmberContainer
import com.sportmanagement.manager.ui.theme.AmberText
import java.text.NumberFormat
import java.util.Locale

fun categoryIcon(category: ServiceCategory): ImageVector = when (category) {
    ServiceCategory.BEVERAGE -> Icons.Filled.LocalDrink
    ServiceCategory.FOOD -> Icons.Filled.Fastfood
    ServiceCategory.EQUIPMENT -> Icons.Filled.ShoppingBag
    ServiceCategory.PERSONNEL -> Icons.Filled.Person
    ServiceCategory.OTHER -> Icons.Filled.Category
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    service: ServiceDetailItem,
    onBackClick: () -> Unit,
    onToggleActive: (String) -> Unit,
    onAdjustStock: (String, Int) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("THÔNG TIN", "TỒN KHO", "THỐNG KÊ")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            actions = {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Chỉnh sửa",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            modifier = Modifier.shadow(4.dp)
        )

        ServiceHeroCard(
            service = service,
            onToggleActive = { onToggleActive(service.id) }
        )

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 16.dp,
            modifier = Modifier.shadow(2.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    item { ServiceInfoCard(service = service) }
                    item { StockLevelCard(service = service) }
                    if (service.description.isNotBlank()) {
                        item { DescriptionCard(description = service.description) }
                    }
                }
                1 -> {
                    item {
                        StockManagementCard(
                            service = service,
                            onAdjust = { delta -> onAdjustStock(service.id, delta) }
                        )
                    }
                    if (service.stockTransactions.isNotEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    SectionHeader("GIAO DỊCH GẦN ĐÂY")
                                }
                            }
                        }
                        items(service.stockTransactions.take(8)) { tx ->
                            StockTransactionRow(tx)
                        }
                    }
                }
                2 -> {
                    item { SalesStatsCard(service = service) }
                    item { RevenueCard(service = service) }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ServiceHeroCard(service: ServiceDetailItem, onToggleActive: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon(service.category),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ServiceStatusBadge(status = service.status)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = service.category.label.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = formatVnd(service.price) + "đ/đơn vị",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (service.isActive) "Đang bán" else "Tắt",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Switch(
                    checked = service.isActive,
                    onCheckedChange = { onToggleActive() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@Composable
private fun ServiceInfoCard(service: ServiceDetailItem) {
    SectionCard(title = "THÔNG TIN DỊCH VỤ") {
        InfoDetailRow("Danh mục", service.category.label)
        InfoDetailRow("Đơn giá", formatVnd(service.price) + "đ")
        InfoDetailRow(
            "Trạng thái",
            service.status.label,
            valueColor = when (service.status) {
                ServiceItemStatus.AVAILABLE -> Color(0xFF15803D)
                ServiceItemStatus.OUT_OF_STOCK -> Color(0xFFB45309)
                ServiceItemStatus.DISABLED -> MaterialTheme.colorScheme.outline
            }
        )
        if (service.stock >= 0) {
            InfoDetailRow("Tồn kho", "${service.stock} đơn vị")
        } else {
            InfoDetailRow("Tồn kho", "Không giới hạn")
        }
    }
}

@Composable
private fun StockLevelCard(service: ServiceDetailItem) {
    if (service.stock < 0 || service.maxStock <= 0) return

    SectionCard(title = "MỨC TỒN KHO") {
        val ratio = (service.stock.toFloat() / service.maxStock).coerceIn(0f, 1f)
        val barColor = when {
            ratio > 0.5f -> Color(0xFF15803D)
            ratio > 0.2f -> Color(0xFFF59E0B)
            else -> Color(0xFFE11D48)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${service.stock} / ${service.maxStock} đơn vị",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${(ratio * 100).toInt()}%",
                fontWeight = FontWeight.Bold,
                color = barColor,
                fontSize = 13.sp
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceContainerLow,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun DescriptionCard(description: String) {
    SectionCard(title = "MÔ TẢ") {
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun StockManagementCard(service: ServiceDetailItem, onAdjust: (Int) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SectionHeader("TỒN KHO HIỆN TẠI")
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (service.stock >= 0) "${service.stock}" else "∞",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "đơn vị",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            if (service.stock >= 0) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "ĐIỀU CHỈNH NHANH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.outline,
                        letterSpacing = 0.6.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(-10, -5, -1).forEach { delta ->
                            AdjustChip(
                                label = "$delta",
                                color = Color(0xFFE11D48),
                                bgColor = Color(0xFFFFE4E6),
                                onClick = { onAdjust(delta) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        listOf(1, 5, 10).forEach { delta ->
                            AdjustChip(
                                label = "+$delta",
                                color = Color(0xFF15803D),
                                bgColor = Color(0xFFDCFCE7),
                                onClick = { onAdjust(delta) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onAdjust(20) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("NHẬP HÀNG")
                    }
                    OutlinedButton(
                        onClick = { onAdjust(-5) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("XUẤT KHO")
                    }
                }
            }
        }
    }
}

@Composable
private fun StockTransactionRow(tx: StockTransaction) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when (tx.type) {
                            StockTxType.IMPORT, StockTxType.RETURN -> Color(0xFFDCFCE7)
                            StockTxType.SALE -> Color(0xFFFFE4E6)
                            StockTxType.ADJUST -> Color(0xFFFEF3C7)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (tx.type) {
                        StockTxType.IMPORT, StockTxType.RETURN -> "↑"
                        StockTxType.SALE -> "↓"
                        StockTxType.ADJUST -> "⟳"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (tx.type) {
                        StockTxType.IMPORT, StockTxType.RETURN -> Color(0xFF15803D)
                        StockTxType.SALE -> Color(0xFFE11D48)
                        StockTxType.ADJUST -> Color(0xFFB45309)
                    }
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.type.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = tx.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (tx.quantity > 0) "+" else ""}${tx.quantity}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (tx.quantity > 0) Color(0xFF15803D) else Color(0xFFE11D48)
                )
                Text(
                    text = tx.timestamp,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun SalesStatsCard(service: ServiceDetailItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader("HIỆU SUẤT BÁN HÀNG")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBox(
                    label = "ĐÃ BÁN",
                    value = "${service.soldCount}",
                    unit = "đơn vị",
                    color = MaterialTheme.colorScheme.primary
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(60.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                StatBox(
                    label = "DOANH THU",
                    value = formatVndShort(service.revenue),
                    unit = "đồng",
                    color = Color(0xFF15803D)
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(60.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                StatBox(
                    label = "HÀNG MỚI",
                    value = "${service.stockTransactions.count { it.type == StockTxType.IMPORT }}",
                    unit = "lần nhập",
                    color = Color(0xFFF59E0B)
                )
            }
        }
    }
}

@Composable
private fun RevenueCard(service: ServiceDetailItem) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TỔNG DOANH THU",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 0.8.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = formatVnd(service.revenue) + "đ",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.TrendingUp,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun AdjustChip(
    label: String,
    color: Color,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = color
        )
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionHeader(title)
            content()
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.outline,
        letterSpacing = 0.8.sp
    )
}

@Composable
private fun InfoDetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ServiceStatusBadge(status: ServiceItemStatus) {
    val (bg, text) = when (status) {
        ServiceItemStatus.AVAILABLE -> Color(0xFFE7F5EC) to Color(0xFF15803D)
        ServiceItemStatus.OUT_OF_STOCK -> AmberContainer to AmberText
        ServiceItemStatus.DISABLED -> Color(0xFFE2E8F0) to Color(0xFF64748B)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = text
        )
    }
}

@Composable
private fun StatBox(label: String, value: String, unit: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.outline,
            letterSpacing = 0.6.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = unit,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

private fun formatVnd(amount: Long): String =
    NumberFormat.getNumberInstance(Locale("vi", "VN")).format(amount)

private fun formatVndShort(amount: Long): String = when {
    amount >= 1_000_000_000 -> String.format("%.1fT", amount / 1_000_000_000.0)
    amount >= 1_000_000 -> String.format("%.1fM", amount / 1_000_000.0)
    amount >= 1_000 -> String.format("%.0fK", amount / 1_000.0)
    else -> amount.toString()
}
