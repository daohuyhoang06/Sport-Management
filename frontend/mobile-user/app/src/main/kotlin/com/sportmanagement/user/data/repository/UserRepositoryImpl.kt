package com.sportmanagement.user.data.repository

import android.content.Context
import com.sportmanagement.user.data.remote.api.UserApi
import com.sportmanagement.user.data.remote.mapper.UserMapper.toDomain
import com.sportmanagement.user.domain.model.BookingScheduleData
import com.sportmanagement.user.domain.model.HomeSearchFilterOptions
import com.sportmanagement.user.domain.model.HomeSearchProvinceOption
import com.sportmanagement.user.domain.model.SportCategory
import com.sportmanagement.user.domain.model.SportIconType
import com.sportmanagement.user.domain.model.UserField
import com.sportmanagement.user.domain.model.UserProfile
import com.sportmanagement.user.domain.model.UserStat
import com.sportmanagement.user.domain.repository.UserRepository
import com.sportmanagement.user.domain.model.VenueCardType
import org.json.JSONArray
import org.json.JSONObject

class UserRepositoryImpl(
    private val appContext: Context? = null,
    private val api: UserApi = UserApi(),
) : UserRepository {

    private val cacheByLocationKey = mutableMapOf<String, List<UserField>>()
    private val prefs by lazy { appContext?.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE) }
    private var diskCacheLoaded = false

    override fun getCachedHomeFields(latitude: Double?, longitude: Double?): List<UserField> {
        ensureDiskCacheLoaded()
        return cacheByLocationKey[locationKey(latitude, longitude)].orEmpty()
    }

    override fun getSavedUserLocation(): Pair<Double, Double>? {
        val latBits = prefs?.getLong(CACHE_LAST_LAT_KEY, Long.MIN_VALUE) ?: Long.MIN_VALUE
        val lngBits = prefs?.getLong(CACHE_LAST_LNG_KEY, Long.MIN_VALUE) ?: Long.MIN_VALUE
        if (latBits == Long.MIN_VALUE || lngBits == Long.MIN_VALUE) return null
        return Double.fromBits(latBits) to Double.fromBits(lngBits)
    }

    override fun saveUserLocation(latitude: Double, longitude: Double) {
        prefs?.edit()
            ?.putLong(CACHE_LAST_LAT_KEY, latitude.toBits())
            ?.putLong(CACHE_LAST_LNG_KEY, longitude.toBits())
            ?.apply()
    }

    override fun getRecentFieldSearches(): List<String> {
        val raw = prefs?.getString(CACHE_RECENT_SEARCHES_KEY, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            List(arr.length()) { index -> arr.optString(index) }
                .filter { it.isNotBlank() }
                .take(MAX_RECENT_SEARCHES)
        }.getOrDefault(emptyList())
    }

    override fun saveRecentFieldSearch(query: String) {
        val cleaned = query.trim()
        if (cleaned.isBlank()) return
        val updated = (listOf(cleaned) + getRecentFieldSearches().filterNot {
            it.equals(cleaned, ignoreCase = true)
        }).take(MAX_RECENT_SEARCHES)
        val arr = JSONArray()
        updated.forEach { arr.put(it) }
        prefs?.edit()?.putString(CACHE_RECENT_SEARCHES_KEY, arr.toString())?.apply()
    }

    override suspend fun getHomeFields(
        latitude: Double?,
        longitude: Double?
    ): List<UserField> = getHomeFieldsPage(page = 1, limit = DEFAULT_PAGE_SIZE, latitude = latitude, longitude = longitude)

    override suspend fun getHomeFieldsPage(
        page: Int,
        limit: Int,
        latitude: Double?,
        longitude: Double?
    ): List<UserField> = fetchFieldsPage(page, limit, latitude, longitude)

    override suspend fun getSportCategories(): List<SportCategory> {
        return SportIconType.entries.map { type ->
            SportCategory(name = sportDisplayName(type), iconType = type)
        }
    }

    override suspend fun getMapCategories(): List<String> = getSportCategories().map { it.name }

    override suspend fun getNearbyFields(
        latitude: Double?,
        longitude: Double?
    ): List<UserField> {
        if (latitude == null || longitude == null) return emptyList()
        val result = mutableListOf<UserField>()
        var page = 1
        while (page <= MAX_NEARBY_PAGES) {
            val pageItems = fetchFieldsPage(
                page = page,
                limit = NEARBY_FETCH_LIMIT,
                latitude = latitude,
                longitude = longitude
            )
            if (pageItems.isEmpty()) break
            result.addAll(pageItems)
            if (pageItems.size < NEARBY_FETCH_LIMIT) break
            page += 1
        }
        return mergeDedup(emptyList(), result)
    }

    override suspend fun getNearbyFieldsPage(
        page: Int,
        limit: Int,
        latitude: Double?,
        longitude: Double?
    ): List<UserField> = fetchFieldsPage(page, limit, latitude, longitude)

    override suspend fun getFavoriteFields(): List<UserField> {
        ensureDiskCacheLoaded()
        val cached = cacheByLocationKey.values.firstOrNull().orEmpty()
        if (cached.isEmpty()) return emptyList()
        return cached.sortedByDescending { it.rating.toDoubleOrNull() ?: 0.0 }.take(5)
    }

    override suspend fun searchFieldsPage(
        keyword: String?,
        address: String?,
        sportType: String?,
        latitude: Double?,
        longitude: Double?,
        radiusKm: Double?,
        sortBy: String?,
        page: Int,
        limit: Int
    ): List<UserField> {
        val savedLocation = getSavedUserLocation()
        val effectiveLatitude = latitude ?: savedLocation?.first
        val effectiveLongitude = longitude ?: savedLocation?.second
        val safePage = if (page < 1) 1 else page
        val safeLimit = limit.coerceIn(1, 100)

        return runCatching {
            api.searchFields(
                keyword = keyword,
                address = address,
                sportType = sportType,
                latitude = effectiveLatitude,
                longitude = effectiveLongitude,
                radiusKm = radiusKm,
                sortBy = sortBy,
                page = safePage,
                limit = safeLimit
            ).map { dto -> mapDtoToField(dto, effectiveLatitude, effectiveLongitude) }
        }.getOrDefault(emptyList())
    }

    override suspend fun getProfile(): UserProfile = UserProfile(
        name = "",
        email = "",
        phone = "",
        membership = ""
    )

    override suspend fun getStats(): List<UserStat> = emptyList()

    override suspend fun getBookingSchedule(): BookingScheduleData = BookingScheduleData(selectedDate = "")

    override suspend fun getFieldGrid(fieldId: Int, date: String): BookingScheduleData {
        return runCatching {
            api.getFieldGrid(fieldId, date)
        }.getOrDefault(BookingScheduleData(selectedDate = date))
    }

    override suspend fun getHomeSearchFilterOptions(): HomeSearchFilterOptions {
        ensureDiskCacheLoaded()
        val fields = cacheByLocationKey.values.firstOrNull().orEmpty()
        if (fields.isEmpty()) {
            return HomeSearchFilterOptions(
                sports = getSportCategories(),
                provinces = emptyList(),
                radiusOptionsKm = listOf(3, 5, 10, 20, 30),
            )
        }

        val provinces = fields
            .filter { it.province.isNotBlank() }
            .groupBy { it.province }
            .map { (provinceName, grouped) ->
                HomeSearchProvinceOption(
                    regionName = grouped.firstOrNull()?.region.orEmpty(),
                    provinceName = provinceName,
                    districtNames = grouped.map { it.district }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .sorted(),
                )
            }
            .sortedBy { it.provinceName }

        return HomeSearchFilterOptions(
            sports = getSportCategories(),
            provinces = provinces,
            radiusOptionsKm = listOf(3, 5, 10, 20, 30),
        )
    }

    private suspend fun fetchFieldsPage(
        page: Int,
        limit: Int,
        latitude: Double?,
        longitude: Double?
    ): List<UserField> {
        val safePage = if (page < 1) 1 else page
        val safeLimit = limit.coerceIn(1, 100)
        val key = locationKey(latitude, longitude)

        val loadedPage = runCatching {
            api.getNearbyFields(latitude, longitude, page = safePage, limit = safeLimit)
                .map { dto -> mapDtoToField(dto, latitude, longitude) }
        }.getOrElse {
            // Do not fallback to mock for paged field list. Keep UI in loading/empty state if no cache.
            return if (safePage == 1) cacheByLocationKey[key].orEmpty() else emptyList()
        }

        val updated = if (safePage == 1) {
            loadedPage
        } else {
            mergeDedup(cacheByLocationKey[key].orEmpty(), loadedPage)
        }

        cacheByLocationKey[key] = updated
        persistDiskCache()
        return loadedPage
    }

    private fun mapDtoToField(dto: com.sportmanagement.user.data.remote.dto.UserFieldDto, latitude: Double?, longitude: Double?): UserField {
        val domain = dto.toDomain()
        val distanceKm = domain.distanceKm
            ?: if (latitude != null && longitude != null && domain.latitude != null && domain.longitude != null) {
                haversineMeters(latitude, longitude, domain.latitude, domain.longitude) / 1000.0
            } else {
                null
            }
        val distance = domain.distance.takeIf { it.isNotBlank() }
            ?: defaultDistanceLabel(distanceKm, domain.latitude, domain.longitude)
        return domain.copy(distance = distance, distanceKm = distanceKm)
    }

    private fun ensureDiskCacheLoaded() {
        if (diskCacheLoaded) return
        diskCacheLoaded = true
        val raw = prefs?.getString(CACHE_FIELDS_KEY, null) ?: return
        runCatching {
            val root = JSONObject(raw)
            root.keys().forEach { key ->
                val arr = root.optJSONArray(key) ?: JSONArray()
                val list = mutableListOf<UserField>()
                for (i in 0 until arr.length()) {
                    val item = arr.optJSONObject(i) ?: continue
                    list.add(item.toUserField())
                }
                cacheByLocationKey[key] = list
            }
        }
    }

    private fun persistDiskCache() {
        val editor = prefs?.edit() ?: return
        val root = JSONObject()
        cacheByLocationKey.forEach { (key, fields) ->
            root.put(key, fields.toJsonArray())
        }
        editor.putString(CACHE_FIELDS_KEY, root.toString()).apply()
    }

    private fun mergeDedup(existing: List<UserField>, incoming: List<UserField>): List<UserField> {
        if (incoming.isEmpty()) return existing
        val seen = existing.map { dedupKey(it) }.toMutableSet()
        val merged = existing.toMutableList()
        incoming.forEach { item ->
            if (seen.add(dedupKey(item))) {
                merged.add(item)
            }
        }
        return merged
    }

    private fun dedupKey(field: UserField): String {
        val lat = field.latitude?.toString().orEmpty()
        val lng = field.longitude?.toString().orEmpty()
        return "${field.name}|${field.location}|$lat|$lng"
    }

    private fun locationKey(latitude: Double?, longitude: Double?): String {
        return if (latitude != null && longitude != null) {
            "loc:${"%.4f".format(latitude)},${"%.4f".format(longitude)}"
        } else {
            "default"
        }
    }

    private fun defaultDistanceLabel(
        distanceKm: Double?,
        latitude: Double?,
        longitude: Double?
    ): String {
        val metersFromDistance = distanceKm?.times(1000.0)
        val meters = if (metersFromDistance != null) {
            metersFromDistance
        } else if (latitude != null && longitude != null) {
            haversineMeters(HANOI_LAT, HANOI_LON, latitude, longitude)
        } else {
            return ""
        }
        return if (meters < 1000) {
            "${meters.toInt()} m"
        } else {
            String.format("%.1f km", meters / 1000.0)
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

    private fun sportDisplayName(type: SportIconType): String {
        return when (type) {
            SportIconType.FOOTBALL -> "Bóng đá"
            SportIconType.TENNIS -> "Tennis"
            SportIconType.BADMINTON -> "Cầu lông"
            SportIconType.VOLLEYBALL -> "Bóng chuyền"
            SportIconType.PICKLEBALL -> "Pickleball"
        }
    }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 5
        private const val NEARBY_FETCH_LIMIT = 100
        private const val MAX_NEARBY_PAGES = 20
        private const val HANOI_LAT = 21.0285
        private const val HANOI_LON = 105.8542
        private const val CACHE_PREFS = "user_repository_cache"
        private const val CACHE_FIELDS_KEY = "fields_by_location"
        private const val CACHE_LAST_LAT_KEY = "last_user_lat"
        private const val CACHE_LAST_LNG_KEY = "last_user_lng"
        private const val CACHE_RECENT_SEARCHES_KEY = "recent_field_searches"
        private const val MAX_RECENT_SEARCHES = 5
    }
}

private fun List<UserField>.toJsonArray(): JSONArray {
    val arr = JSONArray()
    forEach { field ->
        val item = JSONObject()
            .put("name", field.name)
            .put("location", field.location)
            .put("price", field.price)
            .put("rating", field.rating)
            .put("sportIconType", field.sportIconType.name)
            .put("latitude", field.latitude)
            .put("longitude", field.longitude)
            .put("distance", field.distance)
            .put("hours", field.hours)
            .put("imageUrl", field.imageUrl)
            .put("isProLeague", field.isProLeague)
            .put("availability", field.availability)
            .put("cardType", field.cardType.name)
            .put("region", field.region)
            .put("province", field.province)
            .put("district", field.district)
            .put("distanceKm", field.distanceKm)
        val tagsArray = JSONArray()
        field.tags.forEach { tagsArray.put(it) }
        item.put("tags", tagsArray)
        arr.put(item)
    }
    return arr
}

private fun JSONObject.toUserField(): UserField {
    val sportType = runCatching {
        SportIconType.valueOf(optString("sportIconType", SportIconType.FOOTBALL.name))
    }.getOrDefault(SportIconType.FOOTBALL)
    val cardType = runCatching {
        VenueCardType.valueOf(optString("cardType", VenueCardType.LARGE_IMAGE.name))
    }.getOrDefault(VenueCardType.LARGE_IMAGE)
    val tagsJson = optJSONArray("tags") ?: JSONArray()
    val tags = mutableListOf<String>()
    for (i in 0 until tagsJson.length()) {
        val tag = tagsJson.optString(i)
        if (tag.isNotBlank()) tags.add(tag)
    }

    return UserField(
        name = optString("name"),
        location = optString("location"),
        price = optString("price"),
        rating = optString("rating"),
        sportIconType = sportType,
        latitude = if (has("latitude") && !isNull("latitude")) optDouble("latitude") else null,
        longitude = if (has("longitude") && !isNull("longitude")) optDouble("longitude") else null,
        distance = optString("distance"),
        hours = optString("hours"),
        imageUrl = optString("imageUrl"),
        isProLeague = optBoolean("isProLeague", false),
        tags = tags,
        availability = optString("availability"),
        cardType = cardType,
        region = optString("region"),
        province = optString("province"),
        district = optString("district"),
        distanceKm = if (has("distanceKm") && !isNull("distanceKm")) optDouble("distanceKm") else null
    )
}
