package com.sportmanagement.user.domain.model

enum class HomeSearchMode {
    AREA,
    DISTANCE
}

data class HomeSearchProvinceOption(
    val regionName: String,
    val provinceName: String,
    val districtNames: List<String>
)

data class HomeSearchFilterOptions(
    val sports: List<SportCategory> = emptyList(),
    val provinces: List<HomeSearchProvinceOption> = emptyList(),
    val radiusOptionsKm: List<Int> = emptyList()
) {
    val regionNames: List<String>
        get() = provinces.map { it.regionName }.distinct()
}

data class HomeSearchCriteria(
    val mode: HomeSearchMode = HomeSearchMode.AREA,
    val selectedSportTypes: Set<SportIconType> = emptySet(),
    val selectedRegionName: String? = null,
    val selectedProvinceName: String? = null,
    val selectedDistrictName: String? = null,
    val selectedRadiusKm: Float = 10f,
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null
)
