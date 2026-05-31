package com.sportmanagement.user.ui.components.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.ui.theme.AppHomeVenueCornerRadius

@Composable
fun MenuCard() {
    val menuItems = listOf(
        MenuItemData(icon = Icons.Outlined.CalendarMonth, label = "Lịch sử đặt sân"),
        MenuItemData(icon = Icons.Outlined.FavoriteBorder, label = "Sân yêu thích"),
        MenuItemData(icon = Icons.Outlined.Notifications, label = "Thông báo"),
        MenuItemData(icon = Icons.Outlined.CreditCard, label = "Phương thức thanh toán"),
        MenuItemData(icon = Icons.Outlined.HelpOutline, label = "Hỗ trợ & FAQ"),
        MenuItemData(icon = Icons.Outlined.Settings, label = "Cài đặt")
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-8).dp)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(AppHomeVenueCornerRadius),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            menuItems.forEachIndexed { index, item ->
                MenuItemRow(item = item)

                if (index < menuItems.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuItemRow(item: MenuItemData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = item.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(22.dp)
        )
    }
}

private data class MenuItemData(
    val icon: ImageVector,
    val label: String
)
