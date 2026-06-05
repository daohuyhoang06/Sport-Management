package com.sportmanagement.user.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.ui.navigation.UserTab
import com.sportmanagement.user.ui.theme.AppControlCornerRadius
import com.sportmanagement.user.ui.theme.AppHeaderGradientEnd
import com.sportmanagement.user.ui.theme.AppHeaderGradientStart
import com.sportmanagement.user.ui.theme.AppNavIconGradientEnd
import com.sportmanagement.user.ui.theme.AppNavIconGradientStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserBottomBar(
    selectedTab: UserTab,
    inboxUnreadCount: Int = 0,
    onTabSelected: (UserTab) -> Unit
) {
    val accentColor = AppHeaderGradientEnd
    val selectedTextColor = AppHeaderGradientStart
    val outlineColor = accentColor.copy(alpha = 0.38f)
    val inactiveColor = Color(0xFF7A8A9A)
    val containerColor = Color.White.copy(alpha = 0.94f)
    val containerShape = RoundedCornerShape(
        topStart = AppControlCornerRadius,
        topEnd = AppControlCornerRadius
    )
    val scope = rememberCoroutineScope()
    var animatingTab by remember { mutableStateOf<UserTab?>(null) }
    val glowBrush = Brush.horizontalGradient(
        listOf(
            Color.Transparent,
            accentColor.copy(alpha = 0.28f),
            Color.Transparent
        )
    )
    val selectedIconBrush = Brush.horizontalGradient(
        colors = listOf(AppNavIconGradientStart, AppNavIconGradientEnd)
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = containerShape,
        color = containerColor,
        border = BorderStroke(1.dp, outlineColor),
        shadowElevation = 8.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(top = 1.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(glowBrush)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
            ) {
                UserTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val tabTitle = stringResource(tab.titleRes)
                    val itemScale by animateFloatAsState(
                        targetValue = if (animatingTab == tab) 1.15f else 1f,
                        animationSpec = tween(durationMillis = 120),
                        label = "bottom_tab_item_scale"
                    )
                    val badgeCount = if (tab == UserTab.Inbox) inboxUnreadCount else 0
                    CompositionLocalProvider(LocalRippleConfiguration provides null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .height(62.dp)
                                .clickable(
                                    onClick = {
                                        animatingTab = tab
                                        onTabSelected(tab)
                                        scope.launch {
                                            delay(170)
                                            if (animatingTab == tab) animatingTab = null
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .graphicsLayer {
                                        scaleX = itemScale
                                        scaleY = itemScale
                                    }
                            ) {
                                Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.TopEnd) {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tabTitle,
                                        tint = if (isSelected) Color.White else inactiveColor,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .then(
                                                if (isSelected) {
                                                    Modifier
                                                        .graphicsLayer {
                                                            compositingStrategy = CompositingStrategy.Offscreen
                                                        }
                                                        .drawWithCache {
                                                            onDrawWithContent {
                                                                drawContent()
                                                                drawRect(
                                                                    brush = selectedIconBrush,
                                                                    blendMode = BlendMode.SrcIn
                                                                )
                                                            }
                                                        }
                                                } else {
                                                    Modifier
                                                }
                                            )
                                    )
                                    if (badgeCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .offset(x = 9.dp, y = (-4).dp)
                                                .size(if (badgeCount > 9) 20.dp else 16.dp)
                                                .background(Color(0xFFE53935), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = tabTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (isSelected) selectedTextColor else inactiveColor,
                                    modifier = Modifier.padding(top = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
