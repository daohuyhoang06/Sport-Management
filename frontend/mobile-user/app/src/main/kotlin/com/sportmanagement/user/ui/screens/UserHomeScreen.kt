package com.sportmanagement.user.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import com.sportmanagement.user.ui.components.search.FieldSearchPanel
import androidx.compose.material3.Surface
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.sportmanagement.user.domain.model.SportCategory
import com.sportmanagement.user.domain.model.UserField
import com.sportmanagement.user.ui.components.field.FieldDetailBottomSheet
import com.sportmanagement.user.ui.components.home.HomeHeaderSection
import com.sportmanagement.user.ui.components.home.HomeSportCategorySection
import com.sportmanagement.user.ui.components.home.HomeStickyHeaderSection
import com.sportmanagement.user.ui.components.home.HomeVenueCard
import kotlinx.coroutines.delay

@Composable
fun UserHomeScreen(
    padding: PaddingValues,
    fields: List<UserField>,
    sportCategories: List<SportCategory>,
    userName: String,
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
    onSearchOpened: () -> Unit,
    onSearchRequest: (String?, String?, String?) -> Unit,
    onClearSearch: () -> Unit,
    onLoadMoreSearchResults: () -> Unit,
    onRememberSearch: (String) -> Unit,
    onCurrentLocationDetected: (Double, Double) -> Unit,
    onLocationUnavailable: () -> Unit,
    onLoadMore: () -> Unit,
    onBookFieldClick: (UserField) -> Unit
) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableIntStateOf(-1) }
    var selectedFieldForDetail by remember { mutableStateOf<UserField?>(null) }
    var hasRequestedHomeLocation by rememberSaveable { mutableStateOf(false) }
    var showSearchPanel by rememberSaveable { mutableStateOf(false) }
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
    val normalizedQuery = remember(searchQuery) { searchQuery.trim() }
    val filteredFields = remember(fields, selectedSportType, normalizedQuery) {
        fields.filter { field ->
            val byCategory = selectedSportType == null || field.sportIconType == selectedSportType
            val bySearch = normalizedQuery.isBlank() ||
                field.name.contains(normalizedQuery, ignoreCase = true) ||
                field.location.contains(normalizedQuery, ignoreCase = true)
            byCategory && bySearch
        }
    }

    val shouldAutoLoadMore by remember(
        listState,
        filteredFields,
        isInitialLoading,
        isLoadingMore,
        hasMoreData,
        normalizedQuery,
        selectedCategoryIndex
    ) {
        derivedStateOf {
            val isDefaultFeed = normalizedQuery.isBlank() && selectedCategoryIndex == -1
            if (!isDefaultFeed || isInitialLoading || isLoadingMore || !hasMoreData || filteredFields.isEmpty()) {
                return@derivedStateOf false
            }
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleIndex >= totalItems - 4
        }
    }

    LaunchedEffect(shouldAutoLoadMore) {
        if (shouldAutoLoadMore) {
            onLoadMore()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = padding.calculateStartPadding(layoutDirection),
                    end = padding.calculateEndPadding(layoutDirection),
                    bottom = padding.calculateBottomPadding()
                )
                .imePadding()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                HomeHeaderSection(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onFilterClick = onFilterClick,
                    userName = userName,
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

            if (isInitialLoading && fields.isEmpty()) {
                items(5) {
                    HomeVenueSkeletonCard()
                    Spacer(Modifier.height(12.dp))
                }
            } else {
                items(filteredFields) { field ->
                    HomeVenueCard(
                        field = field,
                        onCardClick = { selectedFieldForDetail = field },
                        onBookClick = { onBookFieldClick(field) }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                if (isLoadingMore) {
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
                modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter)
            )
        }

        if (showSearchPanel) {
            FieldSearchPanel(
                results = searchResults,
                recentSearches = recentSearches,
                isLoading = isSearchLoading,
                isLoadingMore = isSearchLoadingMore,
                hasMoreResults = hasMoreSearchResults,
                onSearchRequest = onSearchRequest,
                onClearSearch = onClearSearch,
                onLoadMore = onLoadMoreSearchResults,
                onRememberSearch = onRememberSearch,
                onClose = {
                    showSearchPanel = false
                    onClearSearch()
                },
                onFieldClick = { field ->
                    onRememberSearch(field.name)
                    selectedFieldForDetail = field
                    showSearchPanel = false
                    onClearSearch()
                },
                onBookFieldClick = { field ->
                    onRememberSearch(field.name)
                    showSearchPanel = false
                    onClearSearch()
                    onBookFieldClick(field)
                }
            )
        }

        selectedFieldForDetail?.let { selectedField ->
            FieldDetailBottomSheet(
                field = selectedField,
                onDismissRequest = { selectedFieldForDetail = null },
                onBookClick = { field ->
                    selectedFieldForDetail = null
                    onBookFieldClick(field)
                }
            )
        }
    }
}

// Extracted to com.sportmanagement.user.ui.components.search.FieldSearchPanel

private data class SearchSportFilter(
    val label: String,
    val apiValue: String
)

private val popularSearchKeywords = listOf("Mỹ Đình", "Pickleball", "Sân A", "Tennis")
private val popularAreas = listOf("Cầu Giấy", "Mỹ Đình", "Nam Từ Liêm", "Hà Đông", "Tây Hồ")
private val searchSportFilters = listOf(
    SearchSportFilter("Bóng đá", "football"),
    SearchSportFilter("Cầu lông", "badminton"),
    SearchSportFilter("Tennis", "tennis"),
    SearchSportFilter("Pickleball", "pickleball"),
    SearchSportFilter("Bóng rổ", "basketball")
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
