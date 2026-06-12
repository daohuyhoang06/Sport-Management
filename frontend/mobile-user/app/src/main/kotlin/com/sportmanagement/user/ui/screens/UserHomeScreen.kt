package com.sportmanagement.user.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.sportmanagement.user.domain.model.FieldReview
import com.sportmanagement.user.domain.model.FieldReviewStats
import com.sportmanagement.user.domain.model.SportCategory
import com.sportmanagement.user.domain.model.UserField
import com.sportmanagement.user.domain.model.UserFieldDetailData
import com.sportmanagement.user.ui.components.field.formatFieldRating
import com.sportmanagement.user.ui.components.field.resolveFieldReviewMetrics
import com.sportmanagement.user.ui.components.field.FieldDetailBottomSheet
import com.sportmanagement.user.ui.components.home.HomeHeaderSection
import com.sportmanagement.user.ui.components.home.HomeSportCategorySection
import com.sportmanagement.user.ui.components.home.HomeStickyHeaderSection
import com.sportmanagement.user.ui.components.home.HomeVenueCard
import kotlinx.coroutines.delay
import java.text.Normalizer

@Composable
fun UserHomeScreen(
    padding: PaddingValues,
    fields: List<UserField>,
    favoriteFields: List<UserField>,
    fieldReviewStatsByFieldId: Map<Int, FieldReviewStats>,
    fieldReviewsByFieldId: Map<Int, List<FieldReview>>,
    loadingFieldReviewIds: Set<Int>,
    fieldDetailDataByFieldId: Map<Int, UserFieldDetailData> = emptyMap(),
    sportCategories: List<SportCategory>,
    userName: String,
    userAvatarUrl: String,
    isLoggedIn: Boolean,
    isInitialLoading: Boolean,
    isLoadingMore: Boolean,
    hasMoreData: Boolean,
    searchResults: List<UserField>,
    recentSearches: List<String>,
    isSearchLoading: Boolean,
    isSearchLoadingMore: Boolean,
    hasMoreSearchResults: Boolean,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onFilterClick: () -> Unit,
    onFavoriteHeaderClick: () -> Unit,
    onSearchOpened: () -> Unit,
    onSearchRequest: (String?, String?, String?) -> Unit,
    onClearSearch: () -> Unit,
    onLoadMoreSearchResults: () -> Unit,
    onRememberSearch: (String) -> Unit,
    onCurrentLocationDetected: (Double, Double) -> Unit,
    onLocationUnavailable: () -> Unit,
    onLoadMore: () -> Unit,
    onBookFieldClick: (UserField) -> Unit,
    onFavoriteFieldClick: (UserField, Boolean) -> Unit,
    onShareFieldClick: (UserField) -> Unit,
    onFieldDetailOpened: (Int) -> Unit,
    deepLinkFieldIdToOpen: Int? = null,
    onDeepLinkFieldConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var lastRequestedSearchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategoryIndex by rememberSaveable { mutableIntStateOf(-1) }
    var selectedFieldForDetail by remember { mutableStateOf<UserField?>(null) }
    var hasRequestedHomeLocation by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val showStickyHeader by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset > 140
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && !hasRequestedHomeLocation) {
            requestHomeLocation(
                context = context,
                onResult = { latitude, longitude ->
                    onCurrentLocationDetected(latitude, longitude)
                    hasRequestedHomeLocation = true
                },
                onUnavailable = {
                    onLocationUnavailable()
                    hasRequestedHomeLocation = true
                }
            )
        } else {
            hasRequestedHomeLocation = true
            onLocationUnavailable()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasRequestedHomeLocation) {
            if (hasFineLocationPermission(context)) {
                requestHomeLocation(
                    context = context,
                    onResult = { latitude, longitude ->
                        onCurrentLocationDetected(latitude, longitude)
                        hasRequestedHomeLocation = true
                    },
                    onUnavailable = {
                        onLocationUnavailable()
                        hasRequestedHomeLocation = true
                    }
                )
            } else {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    val selectedSportType = remember(selectedCategoryIndex, sportCategories) {
        sportCategories.getOrNull(selectedCategoryIndex)?.iconType
    }
    val homeFieldSearchIndex = remember(fields) {
        fields.map { field ->
            SearchableUserField(
                field = field,
                normalizedName = normalizeForSearch(field.name),
                normalizedLocation = normalizeForSearch(field.location)
            )
        }
    }
    val searchResultIndex = remember(searchResults) {
        searchResults.map { field ->
            SearchableUserField(
                field = field,
                normalizedName = normalizeForSearch(field.name),
                normalizedLocation = normalizeForSearch(field.location)
            )
        }
    }
    val normalizedQuery = remember(searchQuery) { normalizeForSearch(searchQuery) }
    val isSearching = normalizedQuery.isNotBlank()
    val filteredHomeFields = remember(homeFieldSearchIndex, selectedSportType, normalizedQuery) {
        homeFieldSearchIndex.filter { entry ->
            val byCategory = selectedSportType == null || entry.field.sportIconType == selectedSportType
            val bySearch = normalizedQuery.isBlank() ||
                entry.normalizedName.contains(normalizedQuery) ||
                entry.normalizedLocation.contains(normalizedQuery)
            byCategory && bySearch
        }.map(SearchableUserField::field)
    }
    val filteredSearchResults = remember(searchResultIndex, selectedSportType, normalizedQuery) {
        searchResultIndex.filter { entry ->
            val byCategory = selectedSportType == null || entry.field.sportIconType == selectedSportType
            val bySearch = entry.normalizedName.contains(normalizedQuery) ||
                entry.normalizedLocation.contains(normalizedQuery)
            byCategory && bySearch
        }.map(SearchableUserField::field)
    }
    val visibleFields = if (isSearching) filteredSearchResults else filteredHomeFields
    val favoriteFieldIds = remember(favoriteFields) { favoriteFields.map { it.fieldId }.toSet() }
    val resolvedFieldRatings = remember(fields, fieldReviewStatsByFieldId, fieldReviewsByFieldId) {
        fields.associate { field ->
            val metrics = resolveFieldReviewMetrics(
                reviewStats = fieldReviewStatsByFieldId[field.fieldId],
                reviews = fieldReviewsByFieldId[field.fieldId].orEmpty(),
                fallbackRating = field.rating
            )
            field.fieldId to formatFieldRating(metrics.averageRating)
        }
    }

    LaunchedEffect(deepLinkFieldIdToOpen, fields, searchResults) {
        val targetId = deepLinkFieldIdToOpen ?: return@LaunchedEffect
        val fieldFromData = (fields + searchResults).firstOrNull { it.fieldId == targetId }
        if (fieldFromData != null) {
            selectedFieldForDetail = fieldFromData
            onDeepLinkFieldConsumed()
        }
    }

    LaunchedEffect(selectedFieldForDetail?.fieldId) {
        selectedFieldForDetail?.fieldId?.takeIf { it > 0 }?.let(onFieldDetailOpened)
    }

    LaunchedEffect(searchQuery) {
        val cleaned = searchQuery.trim()
        if (cleaned.isBlank()) {
            lastRequestedSearchQuery = ""
            onClearSearch()
            return@LaunchedEffect
        }

        onSearchOpened()
        delay(400)
        if (cleaned == searchQuery.trim()) {
            lastRequestedSearchQuery = normalizeForSearch(cleaned)
            onSearchRequest(cleaned, null, null)
        }
    }

    val shouldShowSearchEmptyState = isSearching &&
        normalizedQuery == lastRequestedSearchQuery &&
        lastRequestedSearchQuery.isNotBlank() &&
        !isSearchLoading &&
        !isSearchLoadingMore &&
        visibleFields.isEmpty()

    val shouldAutoLoadMore by remember(
        listState,
        visibleFields,
        isInitialLoading,
        isLoadingMore,
        hasMoreData,
        isSearchLoading,
        isSearchLoadingMore,
        hasMoreSearchResults,
        normalizedQuery,
        selectedCategoryIndex
    ) {
        derivedStateOf {
            if (isSearching) {
                if (isSearchLoading || isSearchLoadingMore || !hasMoreSearchResults || visibleFields.isEmpty()) {
                    return@derivedStateOf false
                }
            } else {
                val isDefaultFeed = normalizedQuery.isBlank() && selectedCategoryIndex == -1
                if (!isDefaultFeed || isInitialLoading || isLoadingMore || !hasMoreData || visibleFields.isEmpty()) {
                    return@derivedStateOf false
                }
            }
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleIndex >= totalItems - 7
        }
    }

    LaunchedEffect(shouldAutoLoadMore) {
        if (shouldAutoLoadMore) {
            if (isSearching) onLoadMoreSearchResults() else onLoadMore()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = padding.calculateStartPadding(layoutDirection),
                    end = padding.calculateEndPadding(layoutDirection),
                    bottom = padding.calculateBottomPadding()
                ),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                HomeHeaderSection(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onFilterClick = onFilterClick,
                    onFavoriteClick = onFavoriteHeaderClick,
                    userName = userName,
                    userAvatarUrl = userAvatarUrl,
                    isLoggedIn = isLoggedIn,
                    onLoginClick = onLoginClick,
                    onRegisterClick = onRegisterClick
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

            if ((isInitialLoading && fields.isEmpty() && !isSearching) || (isSearching && isSearchLoading && visibleFields.isEmpty())) {
                items(5) {
                    HomeVenueSkeletonCard()
                    Spacer(Modifier.height(12.dp))
                }
            } else {
                items(visibleFields, key = { "field_${it.fieldId}_${it.name}" }) { field ->
                    HomeVenueCard(
                        field = field,
                        displayRating = resolvedFieldRatings[field.fieldId] ?: field.rating.ifBlank { "0.0" },
                        isFavorite = field.fieldId in favoriteFieldIds,
                        onCardClick = {
                            selectedFieldForDetail = field
                            if (isSearching) onRememberSearch(field.name)
                        },
                        onBookClick = { onBookFieldClick(field) },
                        onFavoriteClick = {
                            onFavoriteFieldClick(field, field.fieldId !in favoriteFieldIds)
                        },
                        onShareClick = { onShareFieldClick(field) }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                if (shouldShowSearchEmptyState) {
                    item {
                        Text(
                            text = "Không tìm thấy sân phù hợp",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }

                if ((isSearching && isSearchLoadingMore) || (!isSearching && isLoadingMore)) {
                    items(2) {
                        HomeVenueSkeletonCard()
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }

        if (showStickyHeader) {
            HomeStickyHeaderSection(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onFilterClick = onFilterClick,
                onFavoriteClick = onFavoriteHeaderClick,
                modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter)
            )
        }

        selectedFieldForDetail?.let { selectedField ->
            FieldDetailBottomSheet(
                field = selectedField,
                isFavorite = selectedField.fieldId in favoriteFieldIds,
                reviewStats = fieldReviewStatsByFieldId[selectedField.fieldId],
                reviews = fieldReviewsByFieldId[selectedField.fieldId].orEmpty(),
                isReviewLoading = selectedField.fieldId in loadingFieldReviewIds,
                fieldDetailData = fieldDetailDataByFieldId[selectedField.fieldId],
                onDismissRequest = { selectedFieldForDetail = null },
                onFavoriteClick = {
                    onFavoriteFieldClick(selectedField, selectedField.fieldId !in favoriteFieldIds)
                },
                onBookClick = { field ->
                    selectedFieldForDetail = null
                    onBookFieldClick(field)
                }
            )
        }
    }
}

private fun normalizeForSearch(text: String): String {
    val normalized = Normalizer.normalize(text.trim().lowercase(), Normalizer.Form.NFD)
    return normalized.replace("đ", "d").replace("\\p{M}+".toRegex(), "")
}


private data class SearchableUserField(
    val field: UserField,
    val normalizedName: String,
    val normalizedLocation: String
)

@Composable
fun HomeVenueSkeletonCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            )
            Spacer(Modifier.height(12.dp))
            Row {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), CircleShape)
                )
                Spacer(Modifier.size(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .height(14.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(12.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.45f)
                            .height(12.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    )
                }
            }
        }
    }
}

private fun hasFineLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

private fun requestHomeLocation(
    context: Context,
    onResult: (Double, Double) -> Unit,
    onUnavailable: () -> Unit
) {
    if (!hasFineLocationPermission(context)) {
        onUnavailable()
        return
    }
    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    try {
        var deliveredLastKnown = false
        fusedClient.lastLocation
            .addOnSuccessListener { lastLocation ->
                if (lastLocation != null) {
                    deliveredLastKnown = true
                    onResult(lastLocation.latitude, lastLocation.longitude)
                }
            }
            .addOnCompleteListener {
                fusedClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                ).addOnSuccessListener { location ->
                    if (location != null) {
                        onResult(location.latitude, location.longitude)
                    } else if (!deliveredLastKnown) {
                        onUnavailable()
                    }
                }.addOnFailureListener {
                    if (!deliveredLastKnown) {
                        onUnavailable()
                    }
                }
            }
    } catch (_: SecurityException) {
        onUnavailable()
    }
}
