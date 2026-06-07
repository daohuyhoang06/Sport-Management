package com.sportmanagement.user.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.sportmanagement.user.data.remote.api.AuthApi
import com.sportmanagement.user.data.remote.api.RegisterRequestDto
import com.sportmanagement.user.data.remote.api.UserApi
import com.sportmanagement.user.data.remote.api.AuthSessionDto
import com.sportmanagement.user.data.remote.api.UpdateProfileRequestDto
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
    private val authApi: AuthApi = AuthApi(),
) : UserRepository {

    private val cacheByLocationKey = mutableMapOf<String, List<UserField>>()
    private val cacheUpdatedAtByLocationKey = mutableMapOf<String, Long>()
    private val prefs by lazy { appContext?.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE) }
    private var diskCacheLoaded = false

    override fun isLoggedIn(): Boolean {
        val authToken = prefs?.getString(AUTH_TOKEN_KEY, null).orEmpty()
        return authToken.isNotBlank() && getCachedProfile() != null
    }

    override fun getCachedProfile(): UserProfile? {
        val raw = prefs?.getString(AUTH_PROFILE_KEY, null) ?: return null
        return runCatching { JSONObject(raw).toUserProfile() }.getOrNull()
    }

    override fun getCachedHomeFields(latitude: Double?, longitude: Double?): List<UserField> {
        ensureDiskCacheLoaded()
        val key = locationKey(latitude, longitude)
        return getFreshCachedFields(key)
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

    override fun getPreferredSportTypeKeys(): Set<String> {
        val raw = prefs?.getString(PREFERRED_SPORT_TYPES_KEY, null) ?: return emptySet()
        return runCatching {
            val arr = JSONArray(raw)
            buildSet {
                for (index in 0 until arr.length()) {
                    val value = arr.optString(index).trim()
                    if (value.isNotBlank()) add(value)
                }
            }
        }.getOrDefault(emptySet())
    }

    override fun savePreferredSportTypeKeys(sportTypeKeys: Set<String>) {
        val normalized = sportTypeKeys.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        val arr = JSONArray()
        normalized.forEach { arr.put(it) }
        prefs?.edit()?.putString(PREFERRED_SPORT_TYPES_KEY, arr.toString())?.apply()
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
        val authToken = getAuthToken() ?: return emptyList()
        val savedLocation = getSavedUserLocation()
        return runCatching {
            api.getFavoriteFields(authToken).map {
                mapDtoToField(it, savedLocation?.first, savedLocation?.second)
            }
        }.getOrDefault(emptyList())
    }

    override suspend fun setFavoriteField(fieldId: Int, isFavorite: Boolean): List<UserField> {
        val authToken = getAuthToken()
            ?: throw IllegalStateException("Phiên đăng nhập đã hết hạn")

        if (isFavorite) {
            api.addFavoriteField(authToken, fieldId)
        } else {
            api.removeFavoriteField(authToken, fieldId)
        }
        return getFavoriteFields()
    }

    override suspend fun login(identifier: String, password: String): UserProfile {
        val session = authApi.login(
            identifier = identifier.trim(),
            password = password
        )
        persistAuthSession(session)
        return session.user.toUserProfile()
    }

    override suspend fun loginWithGoogle(idToken: String): UserProfile {
        val session = authApi.loginWithGoogle(idToken.trim())
        persistAuthSession(session)
        return session.user.toUserProfile()
    }

    override suspend fun register(
        fullName: String,
        email: String,
        password: String,
        phone: String?,
        birthday: String?,
        address: String?,
        favoriteSportTypeKeys: Set<String>
    ): UserProfile {
        val session = authApi.register(
            RegisterRequestDto(
                name = fullName.trim(),
                email = email.trim(),
                password = password,
                phone = phone?.trim(),
                birthday = birthday?.trim(),
                address = address?.trim(),
                favoriteSportKeys = favoriteSportTypeKeys
            )
        )
        persistAuthSession(session)
        savePreferredSportTypeKeys(favoriteSportTypeKeys)
        return session.user.toUserProfile()
    }

    override suspend fun updateProfile(profile: UserProfile): UserProfile {
        val authToken = getAuthToken()
            ?: throw IllegalStateException("Phiên đăng nhập đã hết hạn")

        var updatedUser = authApi.updateMe(
            token = authToken,
            request = UpdateProfileRequestDto(
                name = profile.name.trim().ifBlank { null },
                phone = profile.phone.trim().ifBlank { null },
                birthday = profile.birthday.trim().ifBlank { null },
                gender = profile.gender.trim().ifBlank { null },
                address = profile.location.trim().ifBlank { null },
                favoriteSportKeys = profile.preferredSportTypeKeys
            )
        )

        val avatarUri = profile.avatarUrl.trim()
        if (avatarUri.startsWith("content://")) {
            val context = appContext
                ?: throw IllegalStateException("Thiếu context để tải ảnh đại diện")
            val uri = Uri.parse(avatarUri)
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val extension = MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(mimeType)
                ?.ifBlank { null }
                ?: "jpg"
            val fileName = "avatar.$extension"
            val imageBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalStateException("Không đọc được ảnh đại diện")

            updatedUser = authApi.uploadAvatar(
                token = authToken,
                imageBytes = imageBytes,
                fileName = fileName,
                mimeType = mimeType
            )
        }

        val updatedProfile = updatedUser.toUserProfile()
        persistProfile(updatedProfile)
        return updatedProfile
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ) {
        val authToken = getAuthToken()
            ?: throw IllegalStateException("Phiên đăng nhập đã hết hạn")

        authApi.changePassword(
            token = authToken,
            currentPassword = currentPassword.trim(),
            newPassword = newPassword
        )
    }

    override fun logout() {
        prefs?.edit()
            ?.remove(AUTH_TOKEN_KEY)
            ?.remove(AUTH_REFRESH_TOKEN_KEY)
            ?.remove(AUTH_FIREBASE_TOKEN_KEY)
            ?.remove(AUTH_FIREBASE_REFRESH_TOKEN_KEY)
            ?.remove(AUTH_PROFILE_KEY)
            ?.remove(PREFERRED_SPORT_TYPES_KEY)
            ?.apply()
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

    override suspend fun getProfile(): UserProfile {
        val cached = getCachedProfile()
        val authToken = getAuthToken()

        if (authToken.isNullOrBlank()) {
            return cached ?: UserProfile(
                name = "",
                email = "",
                phone = "",
                membership = "Đồng"
            )
        }

        return runCatching {
            val profile = authApi.getMe(authToken).toUserProfile()
            persistProfile(profile)
            profile
        }.getOrElse {
            cached ?: UserProfile(
                name = "",
                email = "",
                phone = "",
                membership = "Đồng"
            )
        }
    }

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
            return if (safePage == 1) getFreshCachedFields(key) else emptyList()
        }

        val updated = if (safePage == 1) {
            loadedPage
        } else {
            mergeDedup(cacheByLocationKey[key].orEmpty(), loadedPage)
        }

        cacheByLocationKey[key] = updated
        cacheUpdatedAtByLocationKey[key] = System.currentTimeMillis()
        enforceCacheLimit()
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
                val entry = root.opt(key)
                val (arr, updatedAt) = when (entry) {
                    is JSONObject -> {
                        val fields = entry.optJSONArray("fields") ?: JSONArray()
                        val ts = entry.optLong("updatedAt", 0L).takeIf { it > 0L } ?: System.currentTimeMillis()
                        fields to ts
                    }
                    is JSONArray -> {
                        entry to System.currentTimeMillis()
                    }
                    else -> {
                        JSONArray() to 0L
                    }
                }
                val list = mutableListOf<UserField>()
                for (i in 0 until arr.length()) {
                    val item = arr.optJSONObject(i) ?: continue
                    list.add(item.toUserField())
                }
                if (list.isNotEmpty()) {
                    cacheByLocationKey[key] = list
                    cacheUpdatedAtByLocationKey[key] = updatedAt
                }
            }
            val removedExpired = evictExpiredCacheEntries()
            val removedOverflow = enforceCacheLimit()
            if (removedExpired || removedOverflow) {
                persistDiskCache()
            }
        }
    }

    private fun persistDiskCache() {
        val editor = prefs?.edit() ?: return
        val root = JSONObject()
        cacheByLocationKey.forEach { (key, fields) ->
            val entry = JSONObject()
                .put("updatedAt", cacheUpdatedAtByLocationKey[key] ?: System.currentTimeMillis())
                .put("fields", fields.toJsonArray())
            root.put(key, entry)
        }
        editor.putString(CACHE_FIELDS_KEY, root.toString()).apply()
    }

    private fun getFreshCachedFields(key: String): List<UserField> {
        val updatedAt = cacheUpdatedAtByLocationKey[key] ?: return emptyList()
        if (!isCacheFresh(updatedAt)) {
            removeCacheKey(key)
            persistDiskCache()
            return emptyList()
        }
        return cacheByLocationKey[key].orEmpty()
    }

    private fun isCacheFresh(updatedAtMillis: Long): Boolean {
        return System.currentTimeMillis() - updatedAtMillis <= CACHE_FIELDS_TTL_MS
    }

    private fun evictExpiredCacheEntries(): Boolean {
        if (cacheByLocationKey.isEmpty()) return false
        val now = System.currentTimeMillis()
        val expiredKeys = cacheUpdatedAtByLocationKey
            .filterValues { now - it > CACHE_FIELDS_TTL_MS }
            .keys
            .toList()
        if (expiredKeys.isEmpty()) return false
        expiredKeys.forEach { removeCacheKey(it) }
        return true
    }

    private fun enforceCacheLimit(): Boolean {
        val overflow = cacheByLocationKey.size - MAX_LOCATION_CACHE_KEYS
        if (overflow <= 0) return false
        val keysToRemove = cacheUpdatedAtByLocationKey
            .entries
            .sortedBy { it.value }
            .take(overflow)
            .map { it.key }
        keysToRemove.forEach { removeCacheKey(it) }
        return keysToRemove.isNotEmpty()
    }

    private fun removeCacheKey(key: String) {
        cacheByLocationKey.remove(key)
        cacheUpdatedAtByLocationKey.remove(key)
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
        private const val CACHE_FIELDS_TTL_MS = 30 * 60 * 1000L
        private const val MAX_LOCATION_CACHE_KEYS = 8
        private const val CACHE_LAST_LAT_KEY = "last_user_lat"
        private const val CACHE_LAST_LNG_KEY = "last_user_lng"
        private const val CACHE_RECENT_SEARCHES_KEY = "recent_field_searches"
        private const val MAX_RECENT_SEARCHES = 5
        private const val AUTH_TOKEN_KEY = "auth_token"
        private const val AUTH_REFRESH_TOKEN_KEY = "auth_refresh_token"
        private const val AUTH_FIREBASE_TOKEN_KEY = "auth_firebase_token"
        private const val AUTH_FIREBASE_REFRESH_TOKEN_KEY = "auth_firebase_refresh_token"
        private const val AUTH_PROFILE_KEY = "auth_profile"
        private const val PREFERRED_SPORT_TYPES_KEY = "preferred_sport_type_keys"
    }

    private fun persistAuthSession(session: AuthSessionDto) {
        prefs?.edit()?.apply {
            putString(AUTH_TOKEN_KEY, session.token)
            putString(AUTH_REFRESH_TOKEN_KEY, session.refreshToken)
            putString(AUTH_FIREBASE_TOKEN_KEY, session.firebaseToken)
            putString(AUTH_FIREBASE_REFRESH_TOKEN_KEY, session.firebaseRefreshToken)
            putString(AUTH_PROFILE_KEY, session.user.toJson().toString())
        }?.apply()
        savePreferredSportTypeKeys(session.user.favoriteSportKeys)
    }

    private fun getAuthToken(): String? =
        prefs?.getString(AUTH_TOKEN_KEY, null)?.takeIf { it.isNotBlank() }

    private fun persistProfile(profile: UserProfile) {
        prefs?.edit()
            ?.putString(AUTH_PROFILE_KEY, profile.toJson().toString())
            ?.apply()
        savePreferredSportTypeKeys(profile.preferredSportTypeKeys)
    }
}

private fun List<UserField>.toJsonArray(): JSONArray {
    val arr = JSONArray()
    forEach { field ->
        val item = JSONObject()
            .put("fieldId", field.fieldId)
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
        fieldId = optInt("fieldId", 0),
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

private fun AuthSessionDto.toUserProfile(): UserProfile = user.toUserProfile()

private fun com.sportmanagement.user.data.remote.api.AuthUserDto.toUserProfile(): UserProfile =
    UserProfile(
        id = id?.toString().orEmpty(),
        name = name,
        email = email,
        phone = phone,
        membership = normalizeMembership(membership),
        avatarUrl = avatarUrl.orEmpty(),
        birthday = formatBirthdayForUi(birthday),
        gender = gender.orEmpty(),
        location = address.orEmpty(),
        preferredSportTypeKeys = favoriteSportKeys,
        bookingCount = bookingCount.orEmpty().ifBlank { "0" },
        rating = rating.orEmpty().ifBlank { "0.0" }
    )

private fun com.sportmanagement.user.data.remote.api.AuthUserDto.toJson(): JSONObject =
    JSONObject()
        .put("id", id)
        .put("name", name)
        .put("email", email)
        .put("phone", phone)
        .put("role", role)
        .put("status", status)
        .put("birthday", birthday)
        .put("gender", gender)
        .put("location", address)
        .put("membership", normalizeMembership(membership))
        .put("avatarUrl", avatarUrl)
        .put("bookingCount", bookingCount)
        .put("rating", rating)
        .put("favoriteSportKeys", JSONArray(favoriteSportKeys.toList()))

private fun UserProfile.toJson(): JSONObject =
    JSONObject()
        .put("id", id)
        .put("name", name)
        .put("email", email)
        .put("phone", phone)
        .put("membership", normalizeMembership(membership))
        .put("avatarUrl", avatarUrl)
        .put("birthday", birthday)
        .put("gender", gender)
        .put("location", location)
        .put("bookingCount", bookingCount)
        .put("rating", rating)
        .put("favoriteSportKeys", JSONArray(preferredSportTypeKeys.toList()))

private fun JSONObject.toUserProfile(): UserProfile =
    UserProfile(
        id = optSanitizedString("id"),
        name = optSanitizedString("name"),
        email = optSanitizedString("email"),
        phone = optSanitizedString("phone"),
        membership = normalizeMembership(optSanitizedString("membership")),
        avatarUrl = optSanitizedString("avatarUrl"),
        birthday = optSanitizedString("birthday"),
        gender = optSanitizedString("gender"),
        location = optSanitizedString("location"),
        preferredSportTypeKeys = optStringSet("favoriteSportKeys"),
        bookingCount = optSanitizedString("bookingCount").ifBlank { "0" },
        rating = optSanitizedString("rating").ifBlank { "0.0" }
    )

private fun normalizeMembership(raw: String?): String {
    val value = raw?.trim().orEmpty().lowercase()
    return when (value) {
        "dong", "đồng" -> "Đồng"
        "bac", "bạc" -> "Bạc"
        "vang", "vàng" -> "Vàng"
        else -> "Đồng"
    }
}

private fun formatBirthdayForUi(raw: String?): String {
    val value = raw?.trim().orEmpty()
    if (!Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(value)) {
        return value
    }
    val parts = value.split("-")
    return "${parts[2]}/${parts[1]}/${parts[0]}"
}

private fun JSONObject.optSanitizedString(name: String): String {
    val raw = optString(name, "")
    return if (raw.equals("null", ignoreCase = true)) "" else raw
}

private fun JSONObject.optStringSet(name: String): Set<String> {
    val array = optJSONArray(name) ?: return emptySet()
    val result = linkedSetOf<String>()
    for (index in 0 until array.length()) {
        val value = array.optString(index).trim()
        if (value.isNotBlank()) {
            result.add(value)
        }
    }
    return result
}
