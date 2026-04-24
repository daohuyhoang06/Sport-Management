package com.sportmanagement.user.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class UserTab(val title: String, val icon: ImageVector) {
    Home("Trang chủ", Icons.Default.Home),
    Map("Bản đồ", Icons.Default.Map),
    Favorites("Yêu thích", Icons.Default.Favorite),
    Profile("Tài khoản", Icons.Default.Person)
}
