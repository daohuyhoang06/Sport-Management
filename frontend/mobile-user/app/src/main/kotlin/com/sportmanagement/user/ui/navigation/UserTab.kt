package com.sportmanagement.user.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.sportmanagement.user.R

enum class UserTab(@StringRes val titleRes: Int, val icon: ImageVector) {
    Home(R.string.tab_home, Icons.Default.Home),
    Map(R.string.tab_map, Icons.Default.Map),
    Favorites(R.string.tab_favorites, Icons.Default.Favorite),
    Profile(R.string.tab_profile, Icons.Default.Person)
}
