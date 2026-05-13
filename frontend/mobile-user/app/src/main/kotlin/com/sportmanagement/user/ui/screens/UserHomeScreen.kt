package com.sportmanagement.user.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.sportmanagement.user.ui.components.home.HomeHeaderSection
import com.sportmanagement.user.ui.components.home.HomeSportCategorySection
import com.sportmanagement.user.ui.components.home.HomeVenueCard
import com.sportmanagement.user.domain.model.SportCategory
import com.sportmanagement.user.domain.model.UserField


@Composable
fun UserHomeScreen(
    padding: PaddingValues,
    fields: List<UserField>,
    sportCategories: List<SportCategory>,
    userName: String,
    onBookFieldClick: (UserField) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableIntStateOf(-1) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            HomeHeaderSection(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                userName = userName
            )
            Spacer(Modifier.height(40.dp))
        }

        item {
            HomeSportCategorySection(
                sportCategories = sportCategories,
                selectedCategoryIndex = selectedCategoryIndex,
                onCategorySelected = { index ->
                    selectedCategoryIndex = if (selectedCategoryIndex == index) -1 else index
                }
            )
            Spacer(Modifier.height(16.dp))
        }

        items(fields) { field ->
            HomeVenueCard(
                field = field,
                onBookClick = { onBookFieldClick(field) }
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}
