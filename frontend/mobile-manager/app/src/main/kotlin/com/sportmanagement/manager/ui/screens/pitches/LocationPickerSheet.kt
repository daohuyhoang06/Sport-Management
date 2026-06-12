package com.sportmanagement.manager.ui.screens.pitches

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import kotlin.coroutines.resume

private const val PICKER_STYLE = "https://tiles.openfreemap.org/styles/bright"
private val DEFAULT_CENTER = LatLng(21.0285, 105.8542) // Hà Nội

@SuppressLint("MissingPermission")
@Composable
fun LocationPickerSheet(
    initialLat: Double? = null,
    initialLng: Double? = null,
    onConfirm: (lat: Double, lng: Double, address: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val geocoder = remember { Geocoder(context) }

    var mapRef by remember { mutableStateOf<org.maplibre.android.maps.MapLibreMap?>(null) }
    var pickedLatLng by remember {
        mutableStateOf(
            if (initialLat != null && initialLng != null) LatLng(initialLat, initialLng) else null
        )
    }
    var pickedAddress by remember { mutableStateOf("") }
    var isGeocoding by remember { mutableStateOf(false) }
    var isLocating by remember { mutableStateOf(false) }

    // Pre-fill address for existing coordinates
    LaunchedEffect(Unit) {
        if (initialLat != null && initialLng != null && pickedAddress.isBlank()) {
            isGeocoding = true
            pickedAddress = reverseGeocode(geocoder, initialLat, initialLng)
            isGeocoding = false
        }
    }

    // Initialize MapLibre SDK once
    remember { MapLibre.getInstance(context.applicationContext, null, WellKnownTileServer.MapLibre) }

    val mapView = remember {
        MapView(context).apply {
            getMapAsync { map ->
                map.setStyle(PICKER_STYLE) {
                    val center = if (initialLat != null && initialLng != null)
                        LatLng(initialLat, initialLng) else DEFAULT_CENTER
                    map.cameraPosition = CameraPosition.Builder()
                        .target(center)
                        .zoom(if (initialLat != null) 15.0 else 12.0)
                        .build()
                    if (initialLat != null && initialLng != null) {
                        map.addMarker(MarkerOptions().position(center))
                    }
                }
                mapRef = map
            }
        }
    }

    // Tap listener — set after map is ready
    LaunchedEffect(mapRef) {
        val map = mapRef ?: return@LaunchedEffect
        map.addOnMapClickListener { latlng ->
            map.clear()
            map.addMarker(MarkerOptions().position(latlng))
            pickedLatLng = latlng
            scope.launch {
                isGeocoding = true
                pickedAddress = reverseGeocode(geocoder, latlng.latitude, latlng.longitude)
                isGeocoding = false
            }
            true
        }
    }

    // Bind MapView to lifecycle
    DisposableEffect(lifecycleOwner, mapView) {
        val obs = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START   -> mapView.onStart()
                Lifecycle.Event.ON_RESUME  -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE   -> mapView.onPause()
                Lifecycle.Event.ON_STOP    -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(obs)
            mapView.onDestroy()
        }
    }

    // GPS permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) jumpToCurrentLocation(context, mapRef) { latlng ->
            pickedLatLng = latlng
            scope.launch {
                isGeocoding = true
                pickedAddress = reverseGeocode(geocoder, latlng.latitude, latlng.longitude)
                isGeocoding = false
                isLocating = false
            }
        } else isLocating = false
    }

    // ── UI ──────────────────────────────────────────────────────────────────────

    Box(modifier = Modifier.fillMaxSize()) {

        // Map
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

        // Top bar: back button + hint
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .shadow(4.dp, CircleShape)
                    .background(Color.White, CircleShape)
                    .size(40.dp)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại")
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Chạm vào bản đồ để chọn vị trí sân",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // GPS FAB (above bottom bar)
        FloatingActionButton(
            onClick = {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                isLocating = true
                if (hasPermission) {
                    jumpToCurrentLocation(context, mapRef) { latlng ->
                        pickedLatLng = latlng
                        scope.launch {
                            isGeocoding = true
                            pickedAddress = reverseGeocode(geocoder, latlng.latitude, latlng.longitude)
                            isGeocoding = false
                            isLocating = false
                        }
                    }
                } else {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 104.dp),
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape
        ) {
            if (isLocating) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Filled.MyLocation, contentDescription = "Vị trí hiện tại")
            }
        }

        // Bottom confirm bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = Color.White,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Address preview
                if (pickedLatLng != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF4CAF50)
                        )
                        if (isGeocoding) {
                            Text("Đang lấy địa chỉ...", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        } else {
                            Text(
                                text = pickedAddress.ifBlank {
                                    "${"%.5f".format(pickedLatLng!!.latitude)}, ${"%.5f".format(pickedLatLng!!.longitude)}"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        val latlng = pickedLatLng ?: return@Button
                        onConfirm(latlng.latitude, latlng.longitude, pickedAddress)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = pickedLatLng != null && !isGeocoding,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (pickedLatLng != null) "Xác nhận vị trí này" else "Chạm vào bản đồ để chọn",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────────

@SuppressLint("MissingPermission")
private fun jumpToCurrentLocation(
    context: android.content.Context,
    map: org.maplibre.android.maps.MapLibreMap?,
    onResult: (LatLng) -> Unit
) {
    val cts = CancellationTokenSource()
    LocationServices.getFusedLocationProviderClient(context)
        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
        .addOnSuccessListener { loc ->
            if (loc != null) {
                val latlng = LatLng(loc.latitude, loc.longitude)
                map?.clear()
                map?.addMarker(MarkerOptions().position(latlng))
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 16.0))
                onResult(latlng)
            }
        }
}

private suspend fun reverseGeocode(geocoder: Geocoder, lat: Double, lng: Double): String {
    return withContext(Dispatchers.IO) {
        try {
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(lat, lng, 1) { addrs ->
                        if (cont.isActive) cont.resume(addrs)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(lat, lng, 1) ?: emptyList()
            }
            addresses.firstOrNull()?.getAddressLine(0)
                ?: "${"%.5f".format(lat)}, ${"%.5f".format(lng)}"
        } catch (_: Exception) {
            "${"%.5f".format(lat)}, ${"%.5f".format(lng)}"
        }
    }
}
