package com.sportmanagement.user.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.Canvas
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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

private val KineticBlue = Color(0xFF1A4B8E)
private const val MAP_STYLE = "https://tiles.openfreemap.org/styles/bright"
private const val HANOI_KEYWORD = "ha noi"

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun UserMapScreen(
    padding: PaddingValues,
    sportCategories: List<SportCategory>,
    nearby: List<UserField>
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedCategoryIndex by remember { mutableIntStateOf(-1) }
    var showHighlights by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showSuggestions by rememberSaveable { mutableStateOf(true) }
    var selectedFieldName by rememberSaveable { mutableStateOf<String?>(null) }
    var isLocationPermissionGranted by remember { mutableStateOf(checkLocationPermission(context)) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var showFieldList by rememberSaveable { mutableStateOf(false) }

    val normalizedQuery = remember(searchQuery) { normalizeForSearch(searchQuery) }
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

    val matchedField = remember(visibleFields, normalizedQuery) {
        if (normalizedQuery.isEmpty()) null
        else visibleFields.find { normalizeForSearch(it.name).contains(normalizedQuery) }
    }

    val suggestions = remember(visibleFields, normalizedQuery) {
        if (normalizedQuery.isEmpty()) emptyList()
        else visibleFields.filter { normalizeForSearch(it.name).contains(normalizedQuery) }.take(5)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        isLocationPermissionGranted = granted
        if (granted) {
            requestCurrentLocationPoint(context) { point -> currentLocation = point }
        }
    }

    val mapView = remember {
        MapView(context).apply {
            getMapAsync { map ->
                map.setStyle(MAP_STYLE) { style ->
                    style.layers.forEach { layer ->
                        val layerId = layer.id.lowercase()

                        if (layerId.contains("pedestrian") || layerId.contains("path") || layerId.contains("footway")) {
                            layer.setProperties(PropertyFactory.visibility(Property.NONE))
                        }

                        if (layer is LineLayer) {
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
        currentLocation?.let { map.addMarker(MarkerOptions().position(it).title("Vị trí của bạn")) }
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
                                sportDrawableRes = getMapSportDrawable(field.sportIconType),
                                sportIconType = field.sportIconType,
                                markerWidthDp = 42f,
                                markerHeightDp = 52f,
                                centerYDp = 19f,
                                iconSizeDp = 24f
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

    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                    onValueChange = { searchQuery = it; showSuggestions = true },
                    placeholder = { Text("Tìm kiếm sân thể thao...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = ""; showSuggestions = false }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        matchedField?.let(jumpToField)
                        focusManager.clearFocus()
                    })
                )
            }

            if (showSuggestions && suggestions.isNotEmpty()) {
                Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        suggestions.forEach { field ->
                            Text(
                                text = field.name,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    searchQuery = field.name
                                    showSuggestions = false
                                    jumpToField(field)
                                    focusManager.clearFocus()
                                }.padding(12.dp)
                            )
                        }
                    }
                }
            }

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
                    contentDescription = "Bật/tắt danh sách sân"
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
                Icon(Icons.Default.MyLocation, contentDescription = "Vị trí của tôi")
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
                        jumpToField(field)
                        showFieldList = false
                    }
                )
            }
        }
    }
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
    val context = LocalContext.current
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
    val markerBitmap = remember(category.iconType) {
        createSportMarkerBitmap(
            context = context,
            sportDrawableRes = getMapSportDrawable(category.iconType),
            sportIconType = category.iconType,
            markerWidthDp = 32f,
            markerHeightDp = 40f,
            centerYDp = 14f,
            iconSizeDp = 18f
        )
    }

    Surface(
        modifier = Modifier
            .height(52.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        shadowElevation = shadow,
        color = containerColor,
        border = BorderStroke(
            width = if (isSelected) 1.8.dp else 1.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                bitmap = markerBitmap.asImageBitmap(),
                contentDescription = "Sân ${category.name}",
                modifier = Modifier.size(30.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Sân ${category.name.lowercase()}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = textColor
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
    val markerBitmap = remember(field.sportIconType, context) {
        createSportMarkerBitmap(
            context = context,
            sportDrawableRes = getMapSportDrawable(field.sportIconType),
            sportIconType = field.sportIconType,
            markerWidthDp = 24f,
            markerHeightDp = 30f,
            centerYDp = 10.4f,
            iconSizeDp = 13f
        )
    }
    val distanceText = remember(currentLocation, field.latitude, field.longitude) {
        formatDistanceLabel(currentLocation, field)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                bitmap = markerBitmap.asImageBitmap(),
                contentDescription = field.name,
                modifier = Modifier.size(24.dp)
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
                    text = "$distanceText • ${field.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B)
                )
            }
            Icon(
                imageVector = Icons.Filled.SubdirectoryArrowRight,
                contentDescription = null,
                tint = Color(0xFF64748B)
            )
        }
    }
}

private fun getMapSportDrawable(type: SportIconType): Int {
    return when (type) {
        SportIconType.FOOTBALL -> R.drawable.football_25
        SportIconType.PICKLEBALL -> R.drawable.pickleball
        SportIconType.TENNIS -> R.drawable.tennis_25
        SportIconType.BADMINTON -> R.drawable.badminton_25
        SportIconType.VOLLEYBALL -> R.drawable.volleyball_25
    }
}

private fun normalizeForSearch(text: String): String {
    val normalized = Normalizer.normalize(text.trim().lowercase(), Normalizer.Form.NFD)
    return normalized.replace("đ", "d").replace("\\p{M}+".toRegex(), "")
}

private fun formatDistanceLabel(currentLocation: LatLng?, field: UserField): String {
    val lat = field.latitude ?: return "Khoảng cách N/A"
    val lon = field.longitude ?: return "Khoảng cách N/A"
    val from = currentLocation ?: LatLng(21.0285, 105.8542)
    val meters = haversineMeters(from.latitude, from.longitude, lat, lon)
    return if (meters < 1000) "${meters.toInt()}m" else String.format("%.1fkm", meters / 1000.0)
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

private fun getMapMarkerBaseDrawable(type: SportIconType): Int {
    return when (type) {
        SportIconType.FOOTBALL -> R.drawable.map_marker_base_football
        SportIconType.PICKLEBALL -> R.drawable.map_marker_base_pickleball
        SportIconType.TENNIS -> R.drawable.map_marker_base_tennis
        SportIconType.BADMINTON -> R.drawable.map_marker_base_badminton
        SportIconType.VOLLEYBALL -> R.drawable.map_marker_base_volleyball
    }
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
    sportDrawableRes: Int,
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

    val baseMarker = ContextCompat.getDrawable(context, getMapMarkerBaseDrawable(sportIconType))
    baseMarker?.setBounds(0, 0, markerWidth, markerHeight)
    baseMarker?.draw(canvas)

    val icon = ContextCompat.getDrawable(context, sportDrawableRes)
    val iconSize = (iconSizeDp * density).toInt()
    val iconLeft = (centerX - iconSize / 2f).toInt()
    val iconTop = (centerY - iconSize / 2f).toInt()
    icon?.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
    icon?.draw(canvas)

    return bitmap
}
