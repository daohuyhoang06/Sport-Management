package com.sportmanagement.user.ui.components.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.domain.model.UserField
import com.sportmanagement.user.ui.components.home.HomeVenueCard
import com.sportmanagement.user.ui.screens.HomeVenueSkeletonCard
import kotlinx.coroutines.delay

val popularSearchKeywords = listOf("Sân bóng 5 người", "Sân 7 người", "Giá rẻ", "Mới mở", "Gần đây")
val popularAreas = listOf("Cầu Giấy", "Đống Đa", "Thanh Xuân", "Hà Đông", "Nam Từ Liêm", "Bắc Từ Liêm")

@Composable
fun FieldSearchPanel(
    results: List<UserField>,
    recentSearches: List<String>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    hasMoreResults: Boolean,
    onSearchRequest: (String?, String?, String?) -> Unit,
    onClearSearch: () -> Unit,
    onLoadMore: () -> Unit,
    onRememberSearch: (String) -> Unit,
    onClose: () -> Unit,
    onFieldClick: (UserField) -> Unit,
    onBookFieldClick: (UserField) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var activeLabel by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val searchListState = rememberLazyListState()
    val showDiscovery = query.isBlank() && activeLabel.isBlank()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(query) {
        val cleaned = query.trim()
        if (cleaned.isBlank()) {
            if (activeLabel.isBlank()) {
                onClearSearch()
            }
            return@LaunchedEffect
        }
        activeLabel = ""
        delay(400)
        if (cleaned == query.trim()) {
            onSearchRequest(cleaned, null, null)
        }
    }

    val shouldLoadMore by remember(searchListState, results, isLoading, isLoadingMore, hasMoreResults, showDiscovery) {
        derivedStateOf {
            if (showDiscovery || isLoading || isLoadingMore || !hasMoreResults || results.isEmpty()) {
                return@derivedStateOf false
            }
            val totalItems = searchListState.layoutInfo.totalItemsCount
            val lastVisibleIndex = searchListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleIndex >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    singleLine = true,
                    placeholder = { Text("Tìm tên sân, khu vực") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotBlank() || activeLabel.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    query = ""
                                    activeLabel = ""
                                    onClearSearch()
                                }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    }
                )
            }

            if (activeLabel.isNotBlank()) {
                Text(
                    text = activeLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
            }

            LazyColumn(
                state = searchListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                if (showDiscovery) {
                    item {
                        DiscoverySection(
                            recentSearches = recentSearches,
                            onRecentClick = { value ->
                                query = value
                                onRememberSearch(value)
                            },
                            onPopularClick = { value ->
                                query = value
                                onRememberSearch(value)
                            },
                            onAreaClick = { area ->
                                activeLabel = area
                                query = ""
                                onRememberSearch(area)
                                onSearchRequest(null, area, null)
                            }
                        )
                    }
                } else {
                    if (isLoading && results.isEmpty()) {
                        items(4) {
                            HomeVenueSkeletonCard()
                            Spacer(Modifier.height(12.dp))
                        }
                    } else if (results.isEmpty()) {
                        item {
                            Text(
                                text = "Không tìm thấy sân phù hợp",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                    } else {
                        items(results, key = { it.fieldId }) { field ->
                            HomeVenueCard(
                                field = field,
                                onCardClick = { onFieldClick(field) },
                                onBookClick = { onBookFieldClick(field) }
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }

                    if (isLoadingMore) {
                        items(2) {
                            HomeVenueSkeletonCard()
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoverySection(
    recentSearches: List<String>,
    onRecentClick: (String) -> Unit,
    onPopularClick: (String) -> Unit,
    onAreaClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (recentSearches.isNotEmpty()) {
            SearchSectionTitle("Tìm kiếm gần đây")
            recentSearches.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRecentClick(item) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.size(12.dp))
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        SearchSectionTitle("Gợi ý phổ biến")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(popularSearchKeywords) { keyword ->
                SearchQuickChip(
                    label = keyword,
                    icon = Icons.Default.Search,
                    onClick = { onPopularClick(keyword) }
                )
            }
        }

        SearchSectionTitle("Khu vực phổ biến")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(popularAreas) { area ->
                SearchQuickChip(
                    label = area,
                    icon = Icons.Default.LocationOn,
                    onClick = { onAreaClick(area) }
                )
            }
        }
    }
}

@Composable
private fun SearchSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 10.dp)
    )
}

@Composable
private fun SearchQuickChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(42.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(21.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
