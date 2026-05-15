package com.sportmanagement.user.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.sportmanagement.user.R
import com.sportmanagement.user.domain.model.HomeSearchCriteria
import com.sportmanagement.user.domain.model.HomeSearchFilterOptions
import com.sportmanagement.user.domain.model.HomeSearchMode
import com.sportmanagement.user.domain.model.HomeSearchProvinceOption
import com.sportmanagement.user.domain.model.SportCategory
import com.sportmanagement.user.domain.model.SportIconType
import com.sportmanagement.user.ui.components.SportMarkerIcon
import com.sportmanagement.user.ui.theme.AppCardCornerRadius
import com.sportmanagement.user.ui.theme.AppCtaCornerRadius
import com.sportmanagement.user.ui.theme.AppHeaderGradientEnd
import com.sportmanagement.user.ui.theme.AppHeaderGradientStart
import com.sportmanagement.user.ui.theme.AppSearchCornerRadius
import com.sportmanagement.user.ui.theme.AppScreenHorizontalPadding
import org.maplibre.android.geometry.LatLng
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeSearchFilterScreen(
    filterOptions: HomeSearchFilterOptions,
    initialCriteria: HomeSearchCriteria,
    onBackClick: () -> Unit,
    onApplyFilters: (HomeSearchCriteria) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var criteria by remember(initialCriteria) { mutableStateOf(initialCriteria) }
    var showAreaPicker by remember { mutableStateOf(false) }
    var provinceSearch by rememberSaveable { mutableStateOf("") }
    var districtSearch by rememberSaveable { mutableStateOf("") }
    var draftProvince by rememberSaveable { mutableStateOf(criteria.selectedProvinceName) }
    var draftDistrict by rememberSaveable { mutableStateOf(criteria.selectedDistrictName) }
    var currentLocationText by rememberSaveable {
        mutableStateOf(buildLocationLabel(criteria, context))
    }
    var isLocationPermissionGranted by remember {
        mutableStateOf(checkLocationPermission(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isLocationPermissionGranted = granted
        if (granted) {
            refreshCurrentLocation(context) { point, addressText ->
                currentLocationText = addressText
                criteria = criteria.copy(
                    currentLatitude = point?.latitude,
                    currentLongitude = point?.longitude
                )
            }
        }
    }

    val provinces = remember(filterOptions.provinces) {
        filterOptions.provinces.sortedBy { it.provinceName }
    }
    val selectedProvinceOption = remember(draftProvince, provinces) {
        provinces.firstOrNull { it.provinceName == draftProvince }
    }
    val visibleDistricts = remember(selectedProvinceOption) {
        selectedProvinceOption?.districtNames.orEmpty()
    }
    val radiusValues = remember(filterOptions.radiusOptionsKm) {
        filterOptions.radiusOptionsKm
            .filter { it > 0 }
            .distinct()
            .sorted()
    }
    val allSportTypes = remember(filterOptions.sports) {
        filterOptions.sports.map { it.iconType }.toSet()
    }
    val minRadiusKm = radiusValues.firstOrNull()?.toFloat() ?: 1f
    val maxRadiusKm = radiusValues.lastOrNull()?.toFloat() ?: 20f
    val distanceRangeStart = min(minRadiusKm, maxRadiusKm)
    val distanceRangeEnd = max(minRadiusKm, maxRadiusKm)
    val sliderSteps = max(0, radiusValues.size - 2)

    val filteredProvinces = remember(provinceSearch, provinces) {
        val query = provinceSearch.trim()
        if (query.isEmpty()) provinces
        else provinces.filter { it.provinceName.contains(query, ignoreCase = true) }
    }
    val filteredDistricts = remember(districtSearch, visibleDistricts) {
        val query = districtSearch.trim()
        if (query.isEmpty()) visibleDistricts
        else visibleDistricts.filter { it.contains(query, ignoreCase = true) }
    }

    val bookingLikeBackground = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.36f)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = bookingLikeBackground,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = {
                        val regionName = filterOptions.provinces
                            .firstOrNull { it.provinceName == criteria.selectedProvinceName }
                            ?.regionName
                        onApplyFilters(
                            criteria.copy(
                                selectedRegionName = regionName,
                                selectedRadiusKm = criteria.selectedRadiusKm.coerceIn(
                                    minimumValue = distanceRangeStart,
                                    maximumValue = distanceRangeEnd
                                )
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(AppCtaCornerRadius),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_search_filter_search_button),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            item {
                HomeSearchFilterHeader(
                    onBackClick = onBackClick,
                    onResetClick = {
                        criteria = HomeSearchCriteria()
                        currentLocationText = buildLocationLabel(HomeSearchCriteria(), context)
                    }
                )
            }

            item {
                FilterContentCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.home_search_filter_sport_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        AllSportsFilterChip(
                            selected = allSportTypes.isNotEmpty() && criteria.selectedSportTypes == allSportTypes,
                            onClick = {
                                criteria = criteria.copy(
                                    selectedSportTypes = if (criteria.selectedSportTypes == allSportTypes) {
                                        emptySet()
                                    } else {
                                        allSportTypes
                                    }
                                )
                            }
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        filterOptions.sports.forEach { sport ->
                            SportMarkerFilterChip(
                                sport = sport,
                                selected = sport.iconType in criteria.selectedSportTypes,
                                onClick = {
                                    criteria = criteria.copy(
                                        selectedSportTypes = toggleSport(
                                            current = criteria.selectedSportTypes,
                                            target = sport.iconType
                                        )
                                    )
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.home_search_filter_mode_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))

                    LocationModeOption(
                        selected = criteria.mode == HomeSearchMode.AREA,
                        text = stringResource(R.string.home_search_filter_mode_area),
                        onSelect = { criteria = criteria.copy(mode = HomeSearchMode.AREA) }
                    )
                    if (criteria.mode == HomeSearchMode.AREA) {
                        Spacer(modifier = Modifier.height(4.dp))
                        AreaModeSection(
                            provinceName = criteria.selectedProvinceName,
                            districtName = criteria.selectedDistrictName,
                            onOpenPicker = {
                                draftProvince = criteria.selectedProvinceName
                                draftDistrict = criteria.selectedDistrictName
                                provinceSearch = ""
                                districtSearch = ""
                                showAreaPicker = true
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LocationModeOption(
                        selected = criteria.mode == HomeSearchMode.DISTANCE,
                        text = if (criteria.mode == HomeSearchMode.DISTANCE) {
                            stringResource(
                                R.string.home_search_filter_mode_distance_with_value,
                                criteria.selectedRadiusKm.toInt()
                            )
                        } else {
                            stringResource(R.string.home_search_filter_mode_distance)
                        },
                        onSelect = { criteria = criteria.copy(mode = HomeSearchMode.DISTANCE) }
                    )
                    if (criteria.mode == HomeSearchMode.DISTANCE) {
                        Spacer(modifier = Modifier.height(4.dp))
                        DistanceModeSection(
                            distanceKm = criteria.selectedRadiusKm.coerceIn(
                                minimumValue = distanceRangeStart,
                                maximumValue = distanceRangeEnd
                            ),
                            minDistanceKm = distanceRangeStart,
                            maxDistanceKm = distanceRangeEnd,
                            sliderSteps = sliderSteps,
                            currentLocationText = currentLocationText,
                            onDistanceChange = { value ->
                                criteria = criteria.copy(selectedRadiusKm = value)
                            },
                            onRequestCurrentLocation = {
                                if (isLocationPermissionGranted) {
                                    refreshCurrentLocation(context) { point, addressText ->
                                        currentLocationText = addressText
                                        criteria = criteria.copy(
                                            currentLatitude = point?.latitude,
                                            currentLongitude = point?.longitude
                                        )
                                    }
                                } else {
                                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAreaPicker) {
        AreaPickerDialog(
            provinceSearch = provinceSearch,
            districtSearch = districtSearch,
            provinces = filteredProvinces,
            districts = filteredDistricts,
            selectedProvince = draftProvince,
            selectedDistrict = draftDistrict,
            onProvinceSearchChange = { provinceSearch = it },
            onDistrictSearchChange = { districtSearch = it },
            onSelectProvince = { province ->
                draftProvince = province.provinceName
                draftDistrict = null
                districtSearch = ""
            },
            onSelectDistrict = { district ->
                draftDistrict = district
            },
            onClear = {
                draftProvince = null
                draftDistrict = null
                provinceSearch = ""
                districtSearch = ""
            },
            onConfirm = {
                criteria = criteria.copy(
                    mode = HomeSearchMode.AREA,
                    selectedProvinceName = draftProvince,
                    selectedDistrictName = draftDistrict
                )
                showAreaPicker = false
            },
            onDismissRequest = { showAreaPicker = false }
        )
    }
}

@Composable
private fun HomeSearchFilterHeader(
    onBackClick: () -> Unit,
    onResetClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(AppHeaderGradientStart, AppHeaderGradientEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.booking_back_content_description),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    text = stringResource(R.string.home_search_filter_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(
                    onClick = onResetClick,
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.home_search_filter_reset_content_description),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterContentCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppScreenHorizontalPadding, vertical = 14.dp),
        shape = RoundedCornerShape(AppCardCornerRadius),
        color = Color.White,
        tonalElevation = 3.dp,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = content
        )
    }
}

@Composable
private fun LocationModeOption(
    selected: Boolean,
    text: String,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AreaModeSection(
    provinceName: String?,
    districtName: String?,
    onOpenPicker: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            onClick = onOpenPicker,
            shape = RoundedCornerShape(AppCtaCornerRadius),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .weight(1f)
                .height(54.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterSelectionCompact(
                    value = provinceName ?: stringResource(R.string.home_search_filter_province_placeholder),
                    modifier = Modifier.weight(1f),
                    onClick = onOpenPicker
                )
                Spacer(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(width = 1.dp, height = 24.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                FilterSelectionCompact(
                    value = districtName ?: stringResource(R.string.home_search_filter_district_placeholder),
                    modifier = Modifier.weight(1f),
                    onClick = onOpenPicker
                )
            }
        }
    }
}

@Composable
private fun FilterSelectionCompact(
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DistanceModeSection(
    distanceKm: Float,
    minDistanceKm: Float,
    maxDistanceKm: Float,
    sliderSteps: Int,
    currentLocationText: String,
    onDistanceChange: (Float) -> Unit,
    onRequestCurrentLocation: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentLocationText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            TextButton(onClick = onRequestCurrentLocation) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = stringResource(R.string.home_search_filter_gps_button),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(
                    R.string.home_search_filter_distance_bound_format,
                    minDistanceKm.toInt()
                ),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(
                    R.string.home_search_filter_mode_distance_with_value,
                    distanceKm.toInt()
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(
                    R.string.home_search_filter_distance_bound_format,
                    maxDistanceKm.toInt()
                ),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Slider(
            value = distanceKm,
            onValueChange = onDistanceChange,
            valueRange = minDistanceKm..maxDistanceKm,
            steps = sliderSteps
        )
    }
}

@Composable
private fun AreaPickerDialog(
    provinceSearch: String,
    districtSearch: String,
    provinces: List<HomeSearchProvinceOption>,
    districts: List<String>,
    selectedProvince: String?,
    selectedDistrict: String?,
    onProvinceSearchChange: (String) -> Unit,
    onDistrictSearchChange: (String) -> Unit,
    onSelectProvince: (HomeSearchProvinceOption) -> Unit,
    onSelectDistrict: (String) -> Unit,
    onClear: () -> Unit,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(AppCardCornerRadius),
            color = Color.White,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = stringResource(R.string.home_search_filter_picker_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PickerSearchField(
                        value = provinceSearch,
                        placeholder = stringResource(R.string.home_search_filter_picker_search_province),
                        modifier = Modifier.weight(1f),
                        onValueChange = onProvinceSearchChange
                    )
                    PickerSearchField(
                        value = districtSearch,
                        placeholder = stringResource(R.string.home_search_filter_picker_search_district),
                        modifier = Modifier.weight(1f),
                        onValueChange = onDistrictSearchChange
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(AppCardCornerRadius),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        LazyColumn {
                            items(provinces) { province ->
                                PickerOptionRow(
                                    text = province.provinceName,
                                    selected = selectedProvince == province.provinceName,
                                    onClick = { onSelectProvince(province) }
                                )
                            }
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(AppCardCornerRadius),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        LazyColumn {
                            items(districts) { district ->
                                PickerOptionRow(
                                    text = district,
                                    selected = selectedDistrict == district,
                                    onClick = { onSelectDistrict(district) }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onClear) {
                        Text(text = stringResource(R.string.home_search_filter_picker_clear))
                    }
                    Spacer(modifier = Modifier.size(6.dp))
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(AppCtaCornerRadius),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(text = stringResource(R.string.home_search_filter_picker_ok))
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerSearchField(
    value: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AppCardCornerRadius),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isBlank()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun PickerOptionRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else Color.Transparent
            )
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun SportMarkerFilterChip(
    sport: SportCategory,
    selected: Boolean,
    onClick: () -> Unit
) {
    val markerColor = sportMarkerColor(sport.iconType)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(AppSearchCornerRadius),
        color = if (selected) {
            markerColor.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (selected) {
                markerColor
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SportMarkerIcon(
                iconType = sport.iconType,
                contentDescription = null,
                markerSize = 22.dp,
                iconSize = 10.dp,
                iconOffsetY = (-1).dp
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = sport.name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) {
                    markerColor
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
private fun AllSportsFilterChip(
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(AppSearchCornerRadius),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.home_search_filter_all_button),
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

private fun sportMarkerColor(type: SportIconType): Color {
    return when (type) {
        SportIconType.FOOTBALL -> Color(0xFF3B82F6)
        SportIconType.PICKLEBALL -> Color(0xFF14B8A6)
        SportIconType.TENNIS -> Color(0xFF0EA5E9)
        SportIconType.BADMINTON -> Color(0xFFA855F7)
        SportIconType.VOLLEYBALL -> Color(0xFFF59E0B)
    }
}

private fun toggleSport(
    current: Set<SportIconType>,
    target: SportIconType
): Set<SportIconType> {
    return if (target in current) current - target else current + target
}

private fun buildLocationLabel(
    criteria: HomeSearchCriteria,
    context: Context
): String {
    return criteria.selectedDistrictName
        ?.let { district -> "$district, ${criteria.selectedProvinceName.orEmpty()}" }
        ?: context.getString(R.string.home_search_filter_current_location_placeholder)
}

private fun checkLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

private fun refreshCurrentLocation(
    context: Context,
    onResult: (LatLng?, String) -> Unit
) {
    requestCurrentLocationPoint(context) { point ->
        if (point == null) {
            onResult(
                null,
                context.getString(R.string.home_search_filter_current_location_unavailable)
            )
            return@requestCurrentLocationPoint
        }
        resolveShortAddress(context, point) { addressText ->
            onResult(point, addressText)
        }
    }
}

private fun requestCurrentLocationPoint(context: Context, onResult: (LatLng?) -> Unit) {
    if (!checkLocationPermission(context)) {
        onResult(null)
        return
    }
    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    try {
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { loc ->
                onResult(loc?.let { LatLng(it.latitude, it.longitude) })
            }
            .addOnFailureListener { onResult(null) }
    } catch (_: SecurityException) {
        onResult(null)
    }
}

private fun resolveShortAddress(
    context: Context,
    point: LatLng,
    onResult: (String) -> Unit
) {
    val geocoder = Geocoder(context, Locale("vi", "VN"))
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        geocoder.getFromLocation(point.latitude, point.longitude, 1) { addresses ->
            onResult(formatShortAddress(context, addresses.firstOrNull()))
        }
        return
    }

    @Suppress("DEPRECATION")
    val address = runCatching {
        geocoder.getFromLocation(point.latitude, point.longitude, 1)?.firstOrNull()
    }.getOrNull()
    onResult(formatShortAddress(context, address))
}

private fun formatShortAddress(
    context: Context,
    address: Address?
): String {
    if (address == null) return context.getString(R.string.home_search_filter_current_location_unavailable)

    val ward = address.subLocality?.takeIf { it.isNotBlank() }
    val district = address.subAdminArea?.takeIf { it.isNotBlank() } ?: address.locality?.takeIf { it.isNotBlank() }
    val province = address.adminArea?.takeIf { it.isNotBlank() }

    val parts = listOfNotNull(
        ward?.let { "P. $it" },
        district,
        province
    )
    return if (parts.isEmpty()) {
        context.getString(R.string.home_search_filter_current_location_unavailable)
    } else {
        parts.joinToString(", ")
    }
}
