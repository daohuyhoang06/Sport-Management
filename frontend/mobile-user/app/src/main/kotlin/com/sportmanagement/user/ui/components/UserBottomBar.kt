package com.sportmanagement.user.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportmanagement.user.ui.navigation.UserTab

@Composable
fun UserBottomBar(selectedTab: UserTab, onTabSelected: (UserTab) -> Unit) {
    val accentColor = MaterialTheme.colorScheme.primary
    val outlineColor = accentColor.copy(alpha = 0.38f)
    val inactiveColor = Color(0xFF7A8A9A)
    val containerColor = Color.White.copy(alpha = 0.94f)
    val containerShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    val glowBrush = Brush.horizontalGradient(
        listOf(
            Color.Transparent,
            accentColor.copy(alpha = 0.28f),
            Color.Transparent
        )
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = containerShape,
        color = containerColor,
        border = BorderStroke(1.dp, outlineColor),
        shadowElevation = 10.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .offset(y = (-4).dp)
                .padding(top = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(glowBrush)
            )

            NavigationBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent,
                tonalElevation = 0.dp
            ) {
                UserTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val tabTitle = stringResource(tab.titleRes)
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { onTabSelected(tab) },
                        icon = {
                            Box(
                                modifier = Modifier.size(30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tabTitle,
                                    modifier = Modifier.size(27.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = tabTitle,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accentColor,
                            selectedTextColor = accentColor,
                            unselectedIconColor = inactiveColor,
                            unselectedTextColor = inactiveColor,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}
