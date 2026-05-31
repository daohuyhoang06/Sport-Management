package com.sportmanagement.user.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.sportmanagement.user.R

enum class UserTab(
    @StringRes val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Home(
        R.string.tab_home,
        Icons.Rounded.Home,
        Icons.Outlined.Home
    ),
    Map(
        R.string.tab_map,
        Icons.Rounded.Map,
        Icons.Outlined.Map
    ),
    Inbox(
        R.string.tab_inbox,
        Icons.Rounded.ChatBubble,
        Icons.Outlined.ChatBubbleOutline
    ),
    Profile(
        R.string.tab_profile,
        Icons.Rounded.Person,
        Icons.Outlined.Person
    )
}
