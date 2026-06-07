package com.sportmanagement.user.ui.screens

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.sportmanagement.user.R
import com.sportmanagement.user.domain.model.SportCategory
import com.sportmanagement.user.domain.model.SportIconType
import com.sportmanagement.user.domain.model.UserField
import com.sportmanagement.user.ui.components.SportMarkerIcon
import com.sportmanagement.user.ui.components.sportIconDrawableRes
import com.sportmanagement.user.ui.components.sportMarkerBaseDrawableRes
import com.sportmanagement.user.ui.components.field.FieldDetailBottomSheet
import com.sportmanagement.user.ui.theme.AppCardCornerRadius
import com.sportmanagement.user.ui.theme.AppMapCategoryChipHeight
import com.sportmanagement.user.ui.theme.AppMapCategoryChipWidth
import com.sportmanagement.user.ui.theme.AppSearchCornerRadius
import com.sportmanagement.user.ui.theme.AppMapSearchBarHeight
import com.sportmanagement.user.ui.theme.AppPillCornerRadius
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import android.graphics.Color as AndroidColor
import java.text.Normalizer
import kotlinx.coroutines.delay

private val KineticBlue = Color(0xFF1A4B8E)
private const val MAP_STYLE = "https://tiles.openfreemap.org/styles/bright"
private const val HANOI_KEYWORD = "ha noi"

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun UserMapScreen(
    padding: PaddingValues,
    sportCategories: List<SportCategory>,
    nearby: List<UserField>,
    favoriteFields: List<UserField> = emptyList(),
    searchResults: List<UserField> = emptyList(),
    recentSearches: List<String> = emptyList(),
    isSearchLoading: Boolean = false,
    isSearchLoadingMore: Boolean = false,
    hasMoreSearchResults: Boolean = false,
    onSearchOpened: () -> Unit = {},
    onSearchRequest: (String?, String?, String?) -> Unit = { _, _, _ -> },
    onClearSearch: () -> Unit = {},
    onLoadMoreSearchResults: () -> Unit = {},
    onRememberSearch: (String) -> Unit = {},
    onCurrentLocationDetected: (Double, Double) -> Unit = { _, _ -> },
    onBookFieldClick: (UserField) -> Unit = {},
    onFavoriteFieldClick: (UserField, Boolean) -> Unit = { _, _ -> },
    onShareFieldClick: (UserField) -> Unit = {}
) {
    val context = LocalContext.current
    ensureMapLibreInitialized(context)
    val focusManager = LocalFocusManager.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val layoutDirection = LocalLayoutDirection.current
    val topInsetPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    var selectedCategoryIndex by remember { mutableIntStateOf(-1) }
    var showHighlights by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showSearchPopup by rememberSaveable { mutableStateOf(false) }
    var selectedFieldName by rememberSaveable { mutableStateOf<String?>(null) }
    var isLocationPermissionGranted by remember { mutableStateOf(checkLocationPermission(context)) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var showFieldList by rememberSaveable { mutableStateOf(false) }
    var selectedFieldForDetail by remember { mutableStateOf<UserField?>(null) }
    val favoriteFieldIds = remember(favoriteFields) { favoriteFields.map { it.fieldId }.toSet() }

    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    val hanoiFields = remember(nearby) {
        nearby.filter {
            it.latitude != null &&
                it.longitude != null &&
                normalizeForSearch(it.location).contains(HANOI_KEYWORD)
        }
    }
    val selectedSportType = remember(selectedCategoryIndex, sportCategories) {
        sportCategories.getOrNull(selectedCategoryIndex)?.iconType
    }
    val visibleFields = remember(hanoiFields, selectedSportType) {
        if (selectedSportType == null) hanoiFields
        else hanoiFields.filter { it.sportIconType == selectedSportType }
    }
    val normalizedQuery = remember(searchQuery) { normalizeForSearch(searchQuery) }
    val localSuggestions = remember(visibleFields, normalizedQuery) {
        if (normalizedQuery.isBlank()) {
            emptyList()
        } else {
            visibleFields.filter {
                normalizeForSearch(it.name).contains(normalizedQuery) ||
                    normalizeForSearch(it.location).contains(normalizedQuery)
            }.take(8)
        }
    }
    val typedSuggestions = remember(searchQuery, searchResults, localSuggestions) {
        if (searchQuery.trim().isBlank()) emptyList()
        else if (searchResults.isNotEmpty()) searchResults.take(8)
        else localSuggestions
    }

    LaunchedEffect(searchQuery) {
        val cleaned = searchQuery.trim()
        if (cleaned.isBlank()) {
            onClearSearch()
            return@LaunchedEffect
        }

        delay(300)
        if (cleaned == searchQuery.trim()) {
            onSearchRequest(cleaned, null, null)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        isLocationPermissionGranted = granted
        if (granted) {
            requestCurrentLocationPoint(context) { point ->
                currentLocation = point
                point?.let { onCurrentLocationDetected(it.latitude, it.longitude) }
            }
        }
    }

    LaunchedEffect(isLocationPermissionGranted) {
        if (isLocationPermissionGranted) {
            requestCurrentLocationPoint(context) { point ->
                currentLocation = point
                point?.let {
                    onCurrentLocationDetected(it.latitude, it.longitude)
                }
            }
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    val mapView = remember {
        MapView(context).apply {
            getMapAsync { map ->
                map.setStyle(MAP_STYLE) { style ->
                    style.layers.forEach { layer ->
                        val layerId = layer.id.lowercase()
                        if (layer is LineLayer) {
                            if (layerId.contains("pedestrian") || layerId.contains("path") || layerId.contains("footway")) {
                                layer.setProperties(PropertyFactory.visibility(Property.NONE))
                                return@forEach
                            }

                            if (layerId.contains("motorway") || 
                                layerId.contains("trunk") || 
                                layerId.contains("primary") ||
                                layerId.contains("major")
                            ) {
                                layer.setProperties(
                                    PropertyFactory.lineColor(AndroidColor.parseColor("#FFD700")),
                                    PropertyFactory.lineWidth(2.5f),
                                    PropertyFactory.lineOpacity(1f)
                                )
                            } 
                            else if (layerId.contains("road") || 
                                     layerId.contains("street") || 
                                     layerId.contains("minor") ||
                                     layerId.contains("service") ||
                                     layerId.contains("secondary") ||
                                     layerId.contains("tertiary")
                            ) {
                                layer.setProperties(
                                    PropertyFactory.lineColor(AndroidColor.parseColor("#C0C0C0")),
                                    PropertyFactory.lineOpacity(0.4f),
                                    PropertyFactory.lineWidth(0.8f)
                                )
                            }
                        }
                    }
                }

                map.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(21.0285, 105.8542))
                    .zoom(12.0)
                    .build()
                mapLibreMap = map
            }
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    LaunchedEffect(visibleFields, currentLocation, selectedFieldName, mapLibreMap) {
        val map = mapLibreMap ?: return@LaunchedEffect
        val iconFactory = IconFactory.getInstance(context)
        map.clear()
        currentLocation?.let {
            map.addMarker(
                MarkerOptions()
                    .position(it)
                    .title(context.getString(R.string.map_my_location_marker_title))
            )
        }
        visibleFields.forEach { field ->
            val point = fieldPoint(field) ?: return@forEach
            map.addMarker(
                MarkerOptions()
                    .position(point)
                    .title(field.name)
                    .snippet(field.location)
                    .icon(
                        iconFactory.fromBitmap(
                                createSportMarkerBitmap(
                                    context = context,
                                    sportIconType = field.sportIconType,
                                    markerWidthDp = 38f,
                                    markerHeightDp = 48f,
                                    centerYDp = 17f,
                                    iconSizeDp = 21f
                                )
                            )
                        )
            )
        }
    }

    LaunchedEffect(mapLibreMap, visibleFields) {
        val map = mapLibreMap ?: return@LaunchedEffect
        map.setOnMarkerClickListener { marker ->
            val clickedField = visibleFields.firstOrNull { it.name == marker.title }
            val target = clickedField?.let(::fieldPoint) ?: marker.position
            selectedFieldName = clickedField?.name
            selectedFieldForDetail = clickedField
            showFieldList = false
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 17.6))
            true
        }
    }

    val jumpToField: (UserField) -> Unit = { field ->
        val point = fieldPoint(field)
        if (point != null) {
            selectedFieldName = field.name
            mapLibreMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 16.8))
        }
    }
    val collapseSearch: () -> Unit = {
        searchQuery = ""
        showSearchPopup = false
        focusManager.clearFocus()
        onClearSearch()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = padding.calculateStartPadding(layoutDirection),
                end = padding.calculateEndPadding(layoutDirection),
                bottom = padding.calculateBottomPadding()
            )
    ) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = topInsetPadding + 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                shape = RoundedCornerShape(if (showSearchPopup) 16.dp else AppSearchCornerRadius),
                color = Color.White,
                border = if (showSearchPopup) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = if (showSearchPopup) 2.dp else 4.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(AppMapSearchBarHeight)
                    ) {
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                showSearchPopup = true
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        showSearchPopup = true
                                        onSearchOpened()
                                    }
                                },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.None,
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    focusManager.clearFocus()
                                }
                            ),
                            decorationBox = { innerTextField ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (showSearchPopup) {
                                        IconButton(onClick = collapseSearch) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowBack,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (searchQuery.isBlank()) {
                                            Text(
                                                text = stringResource(R.string.map_search_placeholder_fields),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        innerTextField()
                                    }
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(
                                            onClick = {
                                                searchQuery = ""
                                                onClearSearch()
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = stringResource(R.string.map_clear_search_content_description),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }

                    AnimatedVisibility(
                        visible = showSearchPopup,
                        enter = androidx.compose.animation.fadeIn(tween(90)) +
                            androidx.compose.animation.expandVertically(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ),
                        exit = androidx.compose.animation.fadeOut(tween(70)) +
                            androidx.compose.animation.shrinkVertically(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                    ) {
                        Column {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 260.dp),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                if (searchQuery.trim().isBlank()) {
                                    items(recentSearches.take(8), key = { "recent_$it" }) { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    searchQuery = item
                                                    onRememberSearch(item)
                                                    showSearchPopup = true
                                                }
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(10.dp))
                                            Text(
                                                text = item,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                    if (recentSearches.isEmpty()) {
                                        item {
                                            Text(
                                                text = "Chưa có lịch sử tìm kiếm",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                            )
                                        }
                                    }
                                } else {
                                    if (isSearchLoading && typedSuggestions.isEmpty()) {
                                        item {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                            }
                                        }
                                    } else if (typedSuggestions.isEmpty()) {
                                        item {
                                            Text(
                                                text = stringResource(R.string.map_no_results),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                            )
                                        }
                                    } else {
                                        items(typedSuggestions, key = { it.fieldId }) { field ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        selectedFieldName = field.name
                                                        selectedFieldForDetail = field
                                                        searchQuery = field.name
                                                        showSearchPopup = false
                                                        onRememberSearch(field.name)
                                                        jumpToField(field)
                                                        focusManager.clearFocus()
                                                    }
                                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                    tint = Color(0xFF1A4B8E),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = field.name,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Text(
                                                        text = field.location,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!showSearchPopup) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(sportCategories) { index, category ->
                        MapSportCategoryItem(
                            category = category,
                            isSelected = selectedCategoryIndex == index,
                            onClick = { selectedCategoryIndex = if (selectedCategoryIndex == index) -1 else index }
                        )
                    }
                }
            }
        }

        val actionButtonContainer = KineticBlue.copy(alpha = 0.82f)
        val actionButtonContent = Color.White
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallFloatingActionButton(
                onClick = { showFieldList = !showFieldList },
                containerColor = actionButtonContainer,
                contentColor = actionButtonContent
            ) {
                Icon(
                    imageVector = if (showFieldList) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.map_toggle_list_content_description)
                )
            }
            SmallFloatingActionButton(
                onClick = {
                    if (isLocationPermissionGranted) {
                        requestCurrentLocationPoint(context) { point ->
                            currentLocation = point
                            point?.let { mapLibreMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 15.0)) }
                        }
                    } else {
                        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                    }
                },
                containerColor = actionButtonContainer,
                contentColor = actionButtonContent
            ) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = stringResource(R.string.map_my_location_content_description)
                )
            }
        } 

        if (showFieldList) {
            ModalBottomSheet(
                onDismissRequest = { showFieldList = false },
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                MapFieldListSheet(
                    fields = visibleFields,
                    currentLocation = currentLocation,
                    onFieldClick = { field ->
                        selectedFieldForDetail = field
                        jumpToField(field)
                        showFieldList = false
                    }
                )
            }
        }

        selectedFieldForDetail?.let { field ->
            FieldDetailBottomSheet(
                field = field,
                isFavorite = field.fieldId in favoriteFieldIds,
                onDismissRequest = { selectedFieldForDetail = null },
                onFavoriteClick = {
                    onFavoriteFieldClick(field, field.fieldId !in favoriteFieldIds)
                },
                onBookClick = {
                    selectedFieldForDetail = null
                    onBookFieldClick(it)
                }
            )
        }

    }
}

private fun ensureMapLibreInitialized(context: Context) {
    MapLibre.getInstance(context.applicationContext, null, WellKnownTileServer.MapLibre)
}

private fun fieldPoint(field: UserField): LatLng? {
    val latitude = field.latitude ?: return null
    val longitude = field.longitude ?: return null
    return LatLng(latitude, longitude)
}

private fun checkLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

private fun requestCurrentLocationPoint(context: Context, onResult: (LatLng?) -> Unit) {
    if (!checkLocationPermission(context)) return
    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    try {
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { loc ->
                onResult(loc?.let { LatLng(it.latitude, it.longitude) })
            }
    } catch (e: SecurityException) { onResult(null) }
}

@Composable
private fun MapSportCategoryItem(category: SportCategory, isSelected: Boolean, onClick: () -> Unit) {
    val accent = getSportMarkerColor(category.iconType)
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) accent.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.97f),
        animationSpec = tween(300),
        label = "containerColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) accent else Color(0xFF425266),
        animationSpec = tween(300),
        label = "textColor"
    )
    val shadow by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 3.dp,
        animationSpec = tween(300),
        label = "shadow"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accent.copy(alpha = 0.85f) else Color(0xFFD9E4F2),
        animationSpec = tween(300),
        label = "borderColor"
    )

    Surface(
        modifier = Modifier
            .widthIn(min = AppMapCategoryChipWidth)
            .height(AppMapCategoryChipHeight)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AppPillCornerRadius),
        shadowElevation = shadow,
        color = containerColor,
        border = BorderStroke(
            width = if (isSelected) 1.8.dp else 1.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SportMarkerIcon(
                iconType = category.iconType,
                contentDescription = stringResource(R.string.map_category_field_format, category.name),
                markerSize = 28.dp,
                iconSize = 14.dp
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = category.name.replaceFirstChar { ch ->
                    if (ch.isLowerCase()) ch.titlecase() else ch.toString()
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor
                ,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip
            )
        }
    }
}

@Composable
private fun MapFieldListSheet(
    fields: List<UserField>,
    currentLocation: LatLng?,
    onFieldClick: (UserField) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .padding(bottom = 20.dp)
    ) {
        Spacer(Modifier.height(6.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(fields, key = { it.name }) { field ->
                MapFieldListItem(
                    field = field,
                    currentLocation = currentLocation,
                    onClick = { onFieldClick(field) }
                )
            }
        }
    }
}

@Composable
private fun MapFieldListItem(
    field: UserField,
    currentLocation: LatLng?,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val distanceText = remember(currentLocation, field.latitude, field.longitude) {
        formatDistanceLabel(context, currentLocation, field)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AppCardCornerRadius),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SportMarkerIcon(
                iconType = field.sportIconType,
                contentDescription = field.name,
                markerSize = 26.dp,
                iconSize = 12.dp,
                iconOffsetY = (-1).dp
            )
            Spacer(Modifier.width(10.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = field.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(
                        R.string.map_field_distance_location_format,
                        distanceText,
                        field.location
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B)
                )
            }
            Icon(
                imageVector = Icons.Default.Directions,
                contentDescription = stringResource(R.string.map_directions_content_description),
                tint = Color(0xFF0F172A),
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xF2FFFFFF))
                    .clickable { openDirections(context, field) }
                    .padding(7.dp)
            )
        }
    }
}

private fun normalizeForSearch(text: String): String {
    val normalized = Normalizer.normalize(text.trim().lowercase(), Normalizer.Form.NFD)
    return normalized.replace("đ", "d").replace("\\p{M}+".toRegex(), "")
}

private fun formatDistanceLabel(context: Context, currentLocation: LatLng?, field: UserField): String {
    val lat = field.latitude ?: return context.getString(R.string.map_distance_na)
    val lon = field.longitude ?: return context.getString(R.string.map_distance_na)
    val from = currentLocation ?: return context.getString(R.string.map_distance_na)
    val meters = haversineMeters(from.latitude, from.longitude, lat, lon)
    return if (meters < 1000) {
        context.getString(R.string.map_distance_meter_format, meters.toInt())
    } else {
        context.getString(R.string.map_distance_km_format, meters / 1000.0)
    }
}

private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
        kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
        kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    return earthRadius * c
}

private fun getSportMarkerColor(type: SportIconType): Color {
    return when (type) {
        SportIconType.FOOTBALL -> Color(0xFF3B82F6)
        SportIconType.PICKLEBALL -> Color(0xFF14B8A6)
        SportIconType.TENNIS -> Color(0xFF0EA5E9)
        SportIconType.BADMINTON -> Color(0xFFA855F7)
        SportIconType.VOLLEYBALL -> Color(0xFFF59E0B)
    }
}

private fun createSportMarkerBitmap(
    context: Context,
    sportIconType: SportIconType,
    markerWidthDp: Float,
    markerHeightDp: Float,
    centerYDp: Float,
    iconSizeDp: Float
): Bitmap {
    val density = context.resources.displayMetrics.density
    val markerWidth = (markerWidthDp * density).toInt()
    val markerHeight = (markerHeightDp * density).toInt()
    val centerX = markerWidth / 2f
    val centerY = centerYDp * density

    val bitmap = Bitmap.createBitmap(markerWidth, markerHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val baseMarker = ContextCompat.getDrawable(context, sportMarkerBaseDrawableRes(sportIconType))
    baseMarker?.setBounds(0, 0, markerWidth, markerHeight)
    baseMarker?.draw(canvas)

    val icon = ContextCompat.getDrawable(context, sportIconDrawableRes(sportIconType))
    val iconSize = (iconSizeDp * density).toInt()
    val iconLeft = (centerX - iconSize / 2f).toInt()
    val iconTop = (centerY - iconSize / 2f).toInt()
    icon?.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
    icon?.draw(canvas)

    return bitmap
}

private fun openDirections(context: Context, field: UserField) {
    val uri = if (field.latitude != null && field.longitude != null) {
        Uri.parse("google.navigation:q=${field.latitude},${field.longitude}")
    } else {
        Uri.parse("geo:0,0?q=${Uri.encode(field.location)}")
    }
    val intent = Intent(Intent.ACTION_VIEW, uri)
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(
            context,
            context.getString(R.string.field_detail_error_open_directions),
            Toast.LENGTH_SHORT
        ).show()
    }
}
