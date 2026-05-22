package com.sportmanagement.user.data.remote.api

import com.sportmanagement.user.data.remote.dto.SportCategoryDto
import com.sportmanagement.user.data.remote.dto.UserFieldDto
import com.sportmanagement.user.data.remote.dto.UserProfileDto
import com.sportmanagement.user.data.remote.dto.UserStatDto
import com.sportmanagement.user.domain.model.BookingScheduleData
import com.sportmanagement.user.domain.model.BookingSubCourt
import com.sportmanagement.user.domain.model.BookingTimeGridData
import com.sportmanagement.user.domain.model.BookingTimeRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

class UserApi(
    private val baseUrl: String = BASE_URL
) {

    suspend fun getHomeFields(
        latitude: Double? = null,
        longitude: Double? = null,
        page: Int = 1,
        limit: Int = 5
    ): List<UserFieldDto> = getFields(latitude, longitude, page, limit)

    suspend fun getNearbyFields(
        latitude: Double? = null,
        longitude: Double? = null,
        page: Int = 1,
        limit: Int = 5
    ): List<UserFieldDto> = getFields(latitude, longitude, page, limit)

    suspend fun searchFields(
        keyword: String? = null,
        address: String? = null,
        sportType: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        radiusKm: Double? = null,
        sortBy: String? = null,
        page: Int = 1,
        limit: Int = 10
    ): List<UserFieldDto> = withContext(Dispatchers.IO) {
        val safePage = if (page < 1) 1 else page
        val safeLimit = limit.coerceIn(1, 100)
        val params = mutableListOf(
            "page" to safePage.toString(),
            "limit" to safeLimit.toString()
        )
        keyword?.trim()?.takeIf { it.isNotBlank() }?.let { params.add("keyword" to it) }
        address?.trim()?.takeIf { it.isNotBlank() }?.let { params.add("address" to it) }
        sportType?.trim()?.takeIf { it.isNotBlank() }?.let { params.add("sportType" to it) }
        if (latitude != null && longitude != null) {
            params.add("lat" to latitude.toString())
            params.add("lng" to longitude.toString())
        }
        radiusKm?.let { params.add("radius" to it.toString()) }
        sortBy?.trim()?.takeIf { it.isNotBlank() }?.let { params.add("sortBy" to it) }

        val endpoint = "$baseUrl/api/user/fields/search?${params.toQueryString()}"
        readFieldArray(endpoint)
    }

    suspend fun getFavoriteFields(): List<UserFieldDto> = getFields()

    suspend fun getSportCategories(): List<SportCategoryDto> = emptyList()

    suspend fun getMapCategories(): List<String> = emptyList()

    suspend fun getProfile(): UserProfileDto = UserProfileDto(
        name = "Người dùng",
        email = "user@example.com",
        phone = "0123456789",
        membership = "Đồng"
    )

    suspend fun getStats(): List<UserStatDto> = emptyList()

    suspend fun getFieldGrid(fieldId: Int, date: String): BookingScheduleData = withContext(Dispatchers.IO) {
        val yyyyMmDd = try {
            val parsed = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).parse(date)
            if (parsed != null) java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(parsed) else date
        } catch (e: Exception) {
            date
        }
        val endpoint = "$baseUrl/api/user/fields/$fieldId/grid?date=$yyyyMmDd"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 30_000
            readTimeout = 30_000
            setRequestProperty("Accept", "application/json")
        }

        try {
            val responseCode = connection.responseCode
            val responseText = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }

            if (responseCode !in 200..299) {
                throw IOException("HTTP $responseCode: $responseText")
            }

            val obj = JSONObject(responseText)
            val gridObj = obj.optJSONObject("grid") ?: JSONObject()
            
            val courtsArray = gridObj.optJSONArray("courts") ?: JSONArray()
            val courts = List(courtsArray.length()) { i ->
                val c = courtsArray.getJSONObject(i)
                BookingSubCourt(c.optString("id"), c.optString("name"))
            }

            val bookedArray = gridObj.optJSONArray("bookedSlots") ?: JSONArray()
            val bookedSlots = List(bookedArray.length()) { i ->
                val b = bookedArray.getJSONObject(i)
                BookingTimeRange(b.optString("courtId"), b.optString("startTime"), b.optString("endTime"))
            }

            BookingScheduleData(
                selectedDate = obj.optString("selectedDate"),
                grid = BookingTimeGridData(
                    openTime = gridObj.optString("openTime"),
                    closeTime = gridObj.optString("closeTime"),
                    gridStepMinutes = gridObj.optInt("gridStepMinutes", 30),
                    minBookingMinutes = gridObj.optInt("minBookingMinutes", 60),
                    courts = courts,
                    bookedSlots = bookedSlots,
                    blockedSlots = emptyList()
                ),
                pricePerHour = obj.optInt("pricePerHour"),
                estimatedPrice = obj.optString("estimatedPrice")
            )
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun getFields(
        latitude: Double? = null,
        longitude: Double? = null,
        page: Int = 1,
        limit: Int = 5
    ): List<UserFieldDto> = withContext(Dispatchers.IO) {
        if (latitude == null || longitude == null) {
            return@withContext emptyList()
        }
        val safePage = if (page < 1) 1 else page
        val safeLimit = limit.coerceIn(1, 100)
        val endpoint = "$baseUrl/api/user/fields/nearby?lat=$latitude&lng=$longitude&page=$safePage&limit=$safeLimit"

        readFieldArray(endpoint)
    }

    private fun readFieldArray(endpoint: String): List<UserFieldDto> {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 30_000
            readTimeout = 30_000
            setRequestProperty("Accept", "application/json")
        }

        return try {
            val responseCode = connection.responseCode
            val responseText = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }

            if (responseCode !in 200..299) {
                throw IOException("HTTP $responseCode")
            }

            val array = JSONArray(responseText)
            List(array.length()) { idx -> array.getJSONObject(idx).toFieldDto() }
        } finally {
            connection.disconnect()
        }
    }

    private fun JSONObject.toFieldDto(): UserFieldDto {
        val tagsArray = optJSONArray("tags") ?: JSONArray()
        val tags = List(tagsArray.length()) { idx -> tagsArray.optString(idx) }
            .filter { it.isNotBlank() }

        return UserFieldDto(
            fieldId = optIntOrNull("field_id"),
            name = optString("name").ifBlank { optString("field_name") },
            location = optString("location"),
            price = optString("price"),
            rating = optString("rating"),
            sportIconType = optStringOrNull("sport_icon_type"),
            latitude = optDoubleOrNull("latitude"),
            longitude = optDoubleOrNull("longitude"),
            distanceKm = optDoubleOrNull("distance_km"),
            distance = optStringOrNull("distance"),
            hours = optStringOrNull("hours") ?: optStringOrNull("openTime"),
            imageUrl = optStringOrNull("imageUrl") ?: optStringOrNull("image"),
            isProLeague = optBooleanOrNull("is_pro_league") ?: optBooleanOrNull("isProLeague"),
            tags = tags,
            availability = optStringOrNull("availability"),
            cardType = optStringOrNull("card_type") ?: optStringOrNull("cardType"),
            region = optStringOrNull("region"),
            province = optStringOrNull("province"),
            district = optStringOrNull("district")
        )
    }

    companion object {
        private const val BASE_URL = "http://10.0.2.2:5000"
    }
}

private fun List<Pair<String, String>>.toQueryString(): String =
    joinToString("&") { (key, value) ->
        "${key.urlEncode()}=${value.urlEncode()}"
    }

private fun String.urlEncode(): String = URLEncoder.encode(this, "UTF-8")

private fun JSONObject.optStringOrNull(name: String): String? =
    optString(name).takeIf { it.isNotBlank() && it != "null" }

private fun JSONObject.optIntOrNull(name: String): Int? =
    if (has(name) && !isNull(name)) optInt(name) else null

private fun JSONObject.optDoubleOrNull(name: String): Double? =
    if (has(name) && !isNull(name)) optDouble(name) else null

private fun JSONObject.optBooleanOrNull(name: String): Boolean? =
    if (has(name) && !isNull(name)) optBoolean(name) else null
