package com.sportmanagement.user.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.sportmanagement.user.ui.model.SportCategory
import com.sportmanagement.user.ui.model.SportIconType
import com.sportmanagement.user.ui.model.UserField
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.Normalizer

private val KineticBlue = Color(0xFF1A4B8E)

@Composable
fun UserMapScreen(
    padding: PaddingValues,
    sportCategories: List<SportCategory>,
    nearby: List<UserField>
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var selectedCategoryIndex by remember { mutableIntStateOf(-1) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var showHighlights by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showSuggestions by rememberSaveable { mutableStateOf(true) }
    var selectedFieldName by rememberSaveable { mutableStateOf<String?>(null) }
    var isLocationPermissionGranted by remember { mutableStateOf(checkLocationPermission(context)) }
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    val normalizedQuery = normalizeForSearch(searchQuery)

    val matchedField = remember(nearby, normalizedQuery) {
        if (normalizedQuery.isEmpty()) {
            null
        } else {
            nearby.firstOrNull { field ->
                normalizeForSearch(field.name) == normalizedQuery ||
                    normalizeForSearch(field.location) == normalizedQuery
            } ?: nearby.firstOrNull { field ->
                normalizeForSearch(field.name).contains(normalizedQuery) ||
                    normalizeForSearch(field.location).contains(normalizedQuery)
            }
        }
    }

    val suggestions: List<UserField> = remember(nearby, normalizedQuery) {
        if (normalizedQuery.isEmpty()) {
            emptyList<UserField>()
        } else {
            nearby.filter { field ->
                normalizeForSearch(field.name).contains(normalizedQuery) ||
                    normalizeForSearch(field.location).contains(normalizedQuery)
            }.take(5)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        isLocationPermissionGranted = granted
        if (granted) {
            requestCurrentLocationPoint(context) { point ->
                currentLocation = point
            }
        }
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(13.0)
            controller.setCenter(GeoPoint(21.0285, 105.8542))
        }
    }

    val jumpToField: (UserField) -> Unit = { field ->
        val index = nearby.indexOf(field).coerceAtLeast(0)
        val point = fieldPoint(field, index)
        selectedFieldName = field.name
        mapView.controller.animateTo(point)
        mapView.controller.setZoom(16.0)
    }

    val jumpToMatchedField = {
        matchedField?.let(jumpToField)
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView.onDetach()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    mapView.overlays.clear()
    currentLocation?.let { userPoint ->
        val userMarker = Marker(mapView).apply {
            position = userPoint
            title = "Vị trí hiện tại"
            snippet = "Bạn đang ở đây"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        mapView.overlays.add(userMarker)
    }

    nearby.take(6).forEachIndexed { index, field ->
        val marker = Marker(mapView).apply {
            position = fieldPoint(field, index)
            title = field.name
            snippet = field.location
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            if (selectedFieldName == field.name) {
                showInfoWindow()
            }
        }
        mapView.overlays.add(marker)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.invalidate()
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 6.dp,
                shape = RoundedCornerShape(28.dp),
                color = Color.White
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        showSuggestions = true
                        selectedFieldName = null
                    },
                    singleLine = true,
                    placeholder = { Text("Nhập tên sân hoặc khu vực", color = Color.Gray) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                showSuggestions = false
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Xóa tìm kiếm")
                            }
                        } else {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = Color.Gray)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            jumpToMatchedField()
                            showSuggestions = false
                            focusManager.clearFocus()
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = KineticBlue,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )
            }
            if (showSuggestions && suggestions.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        suggestions.forEach { field ->
                            Text(
                                text = "${field.name} - ${field.location}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        searchQuery = field.name
                                        showSuggestions = false
                                        jumpToField(field)
                                        focusManager.clearFocus()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
            if (normalizedQuery.isNotEmpty() && matchedField == null) {
                Text(
                    "Không có sân phù hợp trong dữ liệu hiện có",
                    color = MaterialTheme.colorScheme.error
                )
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sportCategories.size) { index ->
                    MapSportCategoryItem(
                        category = sportCategories[index],
                        isSelected = selectedCategoryIndex == index,
                        onClick = {
                            selectedCategoryIndex = if (selectedCategoryIndex == index) -1 else index
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showHighlights,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Card(shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Một số sân nổi bật tại Hà Nội", fontWeight = FontWeight.SemiBold)
                    nearby.take(3).forEach { field ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(field.name, fontWeight = FontWeight.SemiBold)
                                Text(field.location)
                            }
                            Text("${field.price} | ${field.rating}/5")
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = if (showHighlights) 190.dp else 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    if (isLocationPermissionGranted) {
                        requestCurrentLocationPoint(context) { point ->
                            currentLocation = point
                            point?.let {
                                mapView.controller.setCenter(it)
                                mapView.controller.setZoom(16.0)
                            }
                        }
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                shape = RoundedCornerShape(999.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Vị trí hiện tại",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            FloatingActionButton(
                onClick = { showHighlights = !showHighlights },
                shape = RoundedCornerShape(999.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = if (showHighlights) {
                        Icons.Default.KeyboardArrowDown
                    } else {
                        Icons.Default.KeyboardArrowUp
                    },
                    contentDescription = "Hiện danh sách sân",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

private fun checkLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

private fun requestCurrentLocationPoint(
    context: Context,
    onResult: (GeoPoint?) -> Unit
) {
    if (!checkLocationPermission(context)) {
        onResult(null)
        return
    }

    val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    try {
        val cancellationTokenSource = CancellationTokenSource()
        fusedClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            if (location != null) {
                onResult(GeoPoint(location.latitude, location.longitude))
            } else {
                fusedClient.lastLocation
                    .addOnSuccessListener { lastLocation ->
                        if (lastLocation != null) {
                            onResult(GeoPoint(lastLocation.latitude, lastLocation.longitude))
                        } else {
                            onResult(null)
                        }
                    }
                    .addOnFailureListener {
                        onResult(null)
                    }
            }
        }.addOnFailureListener {
            fusedClient.lastLocation
                .addOnSuccessListener { lastLocation ->
                    if (lastLocation != null) {
                        onResult(GeoPoint(lastLocation.latitude, lastLocation.longitude))
                    } else {
                        onResult(null)
                    }
                }
                .addOnFailureListener {
                    onResult(null)
                }
        }
    } catch (_: SecurityException) {
        onResult(null)
    }
}

private fun fieldPoint(field: UserField, index: Int): GeoPoint {
    val knownPoints = mapOf(
        "Sân bóng C500 Học viện An Ninh" to GeoPoint(21.0466, 105.7868),
        "Sân bóng Minh Kiệt" to GeoPoint(21.0368, 105.8215),
        "Sân vận động Mỹ Đình" to GeoPoint(21.0227, 105.7630),
        "Sân bóng Hoàng Mai" to GeoPoint(20.9748, 105.8639),
        "Sân bóng Bách Khoa" to GeoPoint(21.0043, 105.8427)
    )
    val fallbackPoints = listOf(
        GeoPoint(21.0285, 105.8542),
        GeoPoint(21.0170, 105.7830),
        GeoPoint(21.0040, 105.8470)
    )

    return knownPoints[field.name] ?: fallbackPoints[index % fallbackPoints.size]
}

@Composable
private fun MapSportCategoryItem(
    category: SportCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) KineticBlue else Color.White,
        animationSpec = tween(300), label = "bgColor"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else KineticBlue,
        animationSpec = tween(300), label = "iconColor"
    )
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 3.dp,
        animationSpec = tween(300), label = "elevation"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) KineticBlue else Color.DarkGray,
        animationSpec = tween(300), label = "textColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.size(60.dp),
            shape = CircleShape,
            shadowElevation = elevation,
            color = bgColor,
            border = BorderStroke(
                width = if (isSelected) 0.dp else 1.5.dp,
                color = if (isSelected) Color.Transparent else KineticBlue
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = getMapSportDrawable(category.iconType)),
                    contentDescription = category.name,
                    modifier = Modifier.size(30.dp),
                    colorFilter = ColorFilter.tint(iconColor)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            category.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

private fun getMapSportDrawable(type: SportIconType): Int {
    return when (type) {
        SportIconType.FOOTBALL -> com.sportmanagement.user.R.drawable.football_25
        SportIconType.PICKLEBALL -> com.sportmanagement.user.R.drawable.pickleball
        SportIconType.TENNIS -> com.sportmanagement.user.R.drawable.tennis_25
        SportIconType.BADMINTON -> com.sportmanagement.user.R.drawable.badminton_25
        SportIconType.VOLLEYBALL -> com.sportmanagement.user.R.drawable.volleyball_25
    }
}

private fun normalizeForSearch(text: String): String {
    val normalized = Normalizer.normalize(text.trim().lowercase(), Normalizer.Form.NFD)
    return normalized
        .replace("đ", "d")
        .replace("Đ", "D")
        .replace("\\p{M}+".toRegex(), "")
}
