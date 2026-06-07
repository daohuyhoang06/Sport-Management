package com.sportmanagement.user.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.domain.model.UserProfile
import com.sportmanagement.user.ui.components.profile.EditProfileBottomSheet
import com.sportmanagement.user.ui.components.profile.LogoutButton
import com.sportmanagement.user.ui.components.profile.MenuCard
import com.sportmanagement.user.ui.components.profile.ProfileHeaderSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    padding: PaddingValues,
    profile: UserProfile,
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onBookingHistoryClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onSupportClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onProfileUpdate: (UserProfile) -> Unit = {}
) {
    val layoutDirection = LocalLayoutDirection.current

    var showEditSheet by rememberSaveable {
        mutableStateOf(false)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = padding.calculateStartPadding(layoutDirection),
                    end = padding.calculateEndPadding(layoutDirection),
                    bottom = padding.calculateBottomPadding()
                ),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            item {
                ProfileHeaderSection(
                    profile = profile,
                    onEditClick = {
                        showEditSheet = true
                    },
                    isLoggedIn = isLoggedIn,
                    onLoginClick = onLoginClick,
                    onRegisterClick = onRegisterClick
                )

                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                MenuCard(
                    onBookingHistoryClick = onBookingHistoryClick,
                    onFavoriteClick = onFavoriteClick,
                    onSupportClick = onSupportClick,
                    onSettingsClick = onSettingsClick
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (isLoggedIn) {
                item {
                    LogoutButton(onLogoutClick = onLogoutClick)
                }
            }
        }

        if (showEditSheet && isLoggedIn) {
            EditProfileBottomSheet(
                profile = profile,
                onDismiss = {
                    showEditSheet = false
                },
                onSave = { updatedProfile ->
                    onProfileUpdate(updatedProfile)
                    showEditSheet = false
                },
                sheetState = sheetState
            )
        }
    }
}
