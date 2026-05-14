package com.sportmanagement.user.domain.usecase

import com.sportmanagement.user.domain.model.HomeSearchCriteria
import com.sportmanagement.user.domain.model.HomeSearchFilterOptions
import com.sportmanagement.user.domain.model.HomeSearchMode
import com.sportmanagement.user.domain.model.UserField

class FilterHomeFieldsUseCase {

    operator fun invoke(
        fields: List<UserField>,
        criteria: HomeSearchCriteria,
        options: HomeSearchFilterOptions
    ): List<UserField> {
        val provincesInRegion = options.provinces
            .filter { criteria.selectedRegionName.isNullOrBlank() || it.regionName == criteria.selectedRegionName }
            .map { it.provinceName }
            .toSet()

        return fields.filter { field ->
            val matchesSport = criteria.selectedSportTypes.isEmpty() || field.sportIconType in criteria.selectedSportTypes
            val matchesArea = matchesAreaCriteria(
                field = field,
                criteria = criteria,
                provincesInRegion = provincesInRegion
            )
            val matchesDistance = matchesDistanceCriteria(field = field, criteria = criteria)

            matchesSport && if (criteria.mode == HomeSearchMode.AREA) matchesArea else matchesDistance
        }
    }

    private fun matchesAreaCriteria(
        field: UserField,
        criteria: HomeSearchCriteria,
        provincesInRegion: Set<String>
    ): Boolean {
        val matchesRegion = criteria.selectedRegionName.isNullOrBlank() ||
            field.region == criteria.selectedRegionName ||
            field.province in provincesInRegion
        val matchesProvince = criteria.selectedProvinceName.isNullOrBlank() || field.province == criteria.selectedProvinceName
        val matchesDistrict = criteria.selectedDistrictName.isNullOrBlank() || field.district == criteria.selectedDistrictName
        return matchesRegion && matchesProvince && matchesDistrict
    }

    private fun matchesDistanceCriteria(
        field: UserField,
        criteria: HomeSearchCriteria
    ): Boolean {
        val radius = criteria.selectedRadiusKm.toDouble()
        val currentLatitude = criteria.currentLatitude
        val currentLongitude = criteria.currentLongitude
        val fieldLatitude = field.latitude
        val fieldLongitude = field.longitude

        if (
            currentLatitude != null &&
            currentLongitude != null &&
            fieldLatitude != null &&
            fieldLongitude != null
        ) {
            val distanceKm = haversineKm(
                lat1 = currentLatitude,
                lon1 = currentLongitude,
                lat2 = fieldLatitude,
                lon2 = fieldLongitude
            )
            return distanceKm <= radius
        }

        return field.distanceKm == null || field.distanceKm <= radius
    }

    private fun haversineKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return earthRadiusKm * c
    }
}
