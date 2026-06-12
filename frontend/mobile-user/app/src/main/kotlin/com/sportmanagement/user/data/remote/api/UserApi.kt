package com.sportmanagement.user.data.remote.api
import com.sportmanagement.user.data.remote.dto.SportCategoryDto
import com.sportmanagement.user.data.remote.dto.UserFieldDto
import com.sportmanagement.user.data.remote.dto.UserProfileDto
import com.sportmanagement.user.data.remote.dto.UserStatDto
import com.sportmanagement.user.domain.model.BookingScheduleData
import com.sportmanagement.user.domain.model.MatchPostPreview
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

data class CreateBookingRequest(
    val fieldId: Int,
    val courtId: Int?,
    val startTime: String,
    val endTime: String,
    val price: Int,
    val note: String?,
    val customerName: String?,
    val customerPhone: String?
)

data class FindOpponentPayload(
    val teamName: String,
    val playerCount: Int,
    val level: String,
    val description: String
)

data class MatchRequestResultDto(
    val matchRequestId: Int,
    val status: String
)

data class CreateBookingResponse(
    val bookingId: Int,
    val courtId: Int?,
    val status: String?,
    val pendingHoldSeconds: Int
)

data class CreateBatchBookingResponse(
    val booking: CreateBookingResponse,
    val pendingHoldSeconds: Int
)

data class FieldReviewDto(
    val reviewId: Int,
    val fieldId: Int,
    val customerName: String,
    val customerAvatarUrl: String,
    val rating: Int,
    val comment: String,
    val createdAt: String,
    val imageUrls: List<String>
)

data class FieldReviewStatsDto(
    val averageRating: Double,
    val totalReviews: Int,
    val fiveStar: Int,
    val fourStar: Int,
    val threeStar: Int,
    val twoStar: Int,
    val oneStar: Int
)

class UserApi(
    private val baseUrl: String = ApiConfig.BASE_URL
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

    suspend fun getFavoriteFields(token: String): List<UserFieldDto> = withContext(Dispatchers.IO) {
        readFieldArray("$baseUrl/api/user/favorites", token)
    }

    suspend fun addFavoriteField(token: String, fieldId: Int) = withContext(Dispatchers.IO) {
        writeFavorite("$baseUrl/api/user/favorites/$fieldId", "POST", token)
    }

    suspend fun removeFavoriteField(token: String, fieldId: Int) = withContext(Dispatchers.IO) {
        writeFavorite("$baseUrl/api/user/favorites/$fieldId", "DELETE", token)
    }

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
                val courtId = c.optString("id")
                val rawCourtName = c.optString("name").trim()
                val courtName = when {
                    courtId.equals("default", ignoreCase = true) -> "S\u00E2n 1"
                    rawCourtName.isNotBlank() -> rawCourtName
                    else -> courtId
                }
                BookingSubCourt(courtId, courtName)
            }

            val bookedArray = gridObj.optJSONArray("bookedSlots") ?: JSONArray()
            val bookedSlots = List(bookedArray.length()) { i ->
                val b = bookedArray.getJSONObject(i)
                BookingTimeRange(b.optString("courtId"), b.optString("startTime"), b.optString("endTime"))
            }

            val blockedArray = gridObj.optJSONArray("blockedSlots") ?: JSONArray()
            val blockedSlots = List(blockedArray.length()) { i ->
                val b = blockedArray.getJSONObject(i)
                BookingTimeRange(b.optString("courtId"), b.optString("startTime"), b.optString("endTime"))
            }

            val matchPostsArray = gridObj.optJSONArray("matchPosts") ?: JSONArray()
            val matchPosts = List(matchPostsArray.length()) { i ->
                val row = matchPostsArray.getJSONObject(i)
                MatchPostPreview(
                    matchPostId = row.optInt("matchPostId"),
                    bookingId = row.optInt("bookingId"),
                    fieldId = row.optInt("fieldId"),
                    courtId = row.optString("courtId"),
                    startTime = row.optString("startTime"),
                    endTime = row.optString("endTime"),
                    teamName = row.optString("teamName"),
                    playerCount = row.optInt("playerCount"),
                    level = row.optString("level"),
                    levelLabel = row.optString("levelLabel"),
                    description = row.optString("description"),
                    status = row.optString("status")
                )
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
                    blockedSlots = blockedSlots,
                    matchPosts = matchPosts
                ),
                pricePerHour = obj.optInt("pricePerHour"),
                estimatedPrice = obj.optString("estimatedPrice")
            )
        } finally {
            connection.disconnect()
        }
    }

    suspend fun getFieldReviews(fieldId: Int): List<FieldReviewDto> = withContext(Dispatchers.IO) {
        val endpoint = "$baseUrl/api/user/reviews?field_id=$fieldId"
        val array = readJsonArray(endpoint)
        List(array.length()) { index ->
            val item = array.optJSONObject(index) ?: JSONObject()
            val imagesArray = item.optJSONArray("images") ?: JSONArray()
            FieldReviewDto(
                reviewId = item.optInt("review_id"),
                fieldId = item.optInt("field_id"),
                customerName = item.optString("customer_name"),
                customerAvatarUrl = resolveMediaUrl(item.optString("customer_avatar_url")),
                rating = item.optInt("rating"),
                comment = item.optString("comment"),
                createdAt = item.optString("created_at"),
                imageUrls = List(imagesArray.length()) { imageIndex ->
                    resolveMediaUrl(imagesArray.optString(imageIndex))
                }.filter { it.isNotBlank() }
            )
        }
    }

    suspend fun getFieldReviewStats(fieldId: Int): FieldReviewStatsDto = withContext(Dispatchers.IO) {
        val endpoint = "$baseUrl/api/user/reviews/stats/$fieldId"
        val root = readJsonObject(endpoint)
        FieldReviewStatsDto(
            averageRating = root.optDouble("average_rating", 0.0).takeIf { it.isFinite() } ?: 0.0,
            totalReviews = root.optInt("total_reviews", 0),
            fiveStar = root.optInt("five_star", 0),
            fourStar = root.optInt("four_star", 0),
            threeStar = root.optInt("three_star", 0),
            twoStar = root.optInt("two_star", 0),
            oneStar = root.optInt("one_star", 0)
        )
    }

    suspend fun createBooking(
        token: String,
        request: CreateBookingRequest,
        findOpponent: FindOpponentPayload? = null
    ): CreateBookingResponse = withContext(Dispatchers.IO) {
        val endpoint = "$baseUrl/api/user/bookings"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 30_000
            readTimeout = 30_000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
        }

        try {
            val body = JSONObject().apply {
                put("field_id", request.fieldId)
                request.courtId?.let { put("court_id", it) }
                put("start_time", request.startTime)
                put("end_time", request.endTime)
                put("price", request.price)
                request.note?.takeIf { it.isNotBlank() }?.let { put("note", it) }
                request.customerName?.takeIf { it.isNotBlank() }?.let { put("customer_name", it) }
                request.customerPhone?.takeIf { it.isNotBlank() }?.let { put("customer_phone", it) }
                findOpponent?.let {
                    put(
                        "find_opponent",
                        JSONObject().apply {
                            put("enabled", true)
                            put("team_name", it.teamName)
                            put("player_count", it.playerCount)
                            put("level", it.level)
                            put("description", it.description)
                        }
                    )
                }
            }.toString()

            connection.outputStream.use { stream ->
                stream.write(body.toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            val responseText = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }

            if (responseCode !in 200..299) {
                throw IOException(parseApiError(responseText) ?: "HTTP $responseCode")
            }

            val root = JSONObject(responseText)
            val booking = root.optJSONObject("booking") ?: JSONObject()
            CreateBookingResponse(
                bookingId = booking.optInt("booking_id"),
                courtId = booking.optIntOrNull("court_id"),
                status = booking.optStringOrNull("status"),
                pendingHoldSeconds = root.optInt("pending_hold_seconds", 0)
            )
        } finally {
            connection.disconnect()
        }
    }

    suspend fun createBookings(
        token: String,
        fieldId: Int,
        requests: List<CreateBookingRequest>,
        note: String?,
        customerName: String?,
        customerPhone: String?,
        findOpponent: FindOpponentPayload? = null
    ): CreateBatchBookingResponse = withContext(Dispatchers.IO) {
        val endpoint = "$baseUrl/api/user/bookings/batch"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 30_000
            readTimeout = 30_000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
        }

        try {
            val bookingsArray = JSONArray()
            requests.forEach { request ->
                bookingsArray.put(
                    JSONObject().apply {
                        request.courtId?.let { put("court_id", it) }
                        put("start_time", request.startTime)
                        put("end_time", request.endTime)
                        put("price", request.price)
                    }
                )
            }

            val body = JSONObject().apply {
                put("field_id", fieldId)
                put("bookings", bookingsArray)
                note?.takeIf { it.isNotBlank() }?.let { put("note", it) }
                customerName?.takeIf { it.isNotBlank() }?.let { put("customer_name", it) }
                customerPhone?.takeIf { it.isNotBlank() }?.let { put("customer_phone", it) }
                findOpponent?.let {
                    put(
                        "find_opponent",
                        JSONObject().apply {
                            put("enabled", true)
                            put("team_name", it.teamName)
                            put("player_count", it.playerCount)
                            put("level", it.level)
                            put("description", it.description)
                        }
                    )
                }
            }.toString()

            connection.outputStream.use { stream ->
                stream.write(body.toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            val responseText = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }

            if (responseCode !in 200..299) {
                throw IOException(parseApiError(responseText) ?: "HTTP $responseCode")
            }

            val root = JSONObject(responseText)
            val bookingObject = root.optJSONObject("booking")
                ?: root.optJSONArray("bookings")
                    ?.optJSONObject(0)
                ?: JSONObject()
            val booking = CreateBookingResponse(
                bookingId = bookingObject.optInt("booking_id"),
                courtId = bookingObject.optIntOrNull("court_id"),
                status = bookingObject.optStringOrNull("status"),
                pendingHoldSeconds = root.optInt("pending_hold_seconds", 0)
            )
            CreateBatchBookingResponse(
                booking = booking,
                pendingHoldSeconds = root.optInt("pending_hold_seconds", 0)
            )
        } finally {
            connection.disconnect()
        }
    }

    suspend fun submitMatchRequest(
        token: String,
        matchPostId: Int,
        teamName: String,
        playerCount: Int,
        level: String,
        message: String
    ): MatchRequestResultDto = withContext(Dispatchers.IO) {
        val root = postJsonWithResponse(
            "$baseUrl/api/user/match-posts/$matchPostId/requests",
            token,
            JSONObject().apply {
                put("team_name", teamName)
                put("player_count", playerCount)
                put("level", level)
                put("message", message)
            }
        )
        val data = root.optJSONObject("data") ?: JSONObject()
        MatchRequestResultDto(
            matchRequestId = data.optInt("match_request_id"),
            status = data.optString("status")
        )
    }

    private suspend fun getFields(
        latitude: Double? = null,
        longitude: Double? = null,
        page: Int = 1,
        limit: Int = 5
    ): List<UserFieldDto> = withContext(Dispatchers.IO) {
        val safePage = if (page < 1) 1 else page
        val safeLimit = limit.coerceIn(1, 100)
        val defaultEndpoint = "$baseUrl/api/user/fields?page=$safePage&limit=$safeLimit"

        if (latitude == null || longitude == null) {
            return@withContext readFieldArray(defaultEndpoint)
        }

        val nearbyEndpoint =
            "$baseUrl/api/user/fields/nearby?lat=$latitude&lng=$longitude&page=$safePage&limit=$safeLimit"

        val nearbyFields = runCatching { readFieldArray(nearbyEndpoint) }.getOrDefault(emptyList())
        if (nearbyFields.isNotEmpty()) {
            return@withContext nearbyFields
        }

        readFieldArray(defaultEndpoint)
    }

    private fun postJsonWithResponse(
        endpoint: String,
        token: String,
        body: JSONObject
    ): JSONObject {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 30_000
            readTimeout = 30_000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
        }

        return try {
            connection.outputStream.use { stream ->
                stream.write(body.toString().toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            val responseText = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }

            if (responseCode !in 200..299) {
                throw IOException(parseApiError(responseText) ?: "HTTP $responseCode")
            }

            JSONObject(responseText)
        } finally {
            connection.disconnect()
        }
    }

    private fun readFieldArray(
        endpoint: String,
        token: String? = null
    ): List<UserFieldDto> {
        val array = readJsonArray(endpoint, token)
        return List(array.length()) { idx -> array.getJSONObject(idx).toFieldDto() }
    }

    private fun readJsonArray(
        endpoint: String,
        token: String? = null
    ): JSONArray {
        val responseText = readJsonResponse(endpoint, token)
        return JSONArray(responseText)
    }

    private fun readJsonObject(
        endpoint: String,
        token: String? = null
    ): JSONObject {
        val responseText = readJsonResponse(endpoint, token)
        return JSONObject(responseText)
    }

    private fun readJsonResponse(
        endpoint: String,
        token: String? = null
    ): String {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 30_000
            readTimeout = 30_000
            setRequestProperty("Accept", "application/json")
            token?.takeIf { it.isNotBlank() }?.let {
                setRequestProperty("Authorization", "Bearer $it")
            }
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

            responseText
        } finally {
            connection.disconnect()
        }
    }

    private fun resolveMediaUrl(value: String?): String {
        val trimmed = value?.trim().orEmpty()
        if (trimmed.isBlank() || trimmed.equals("null", ignoreCase = true)) return ""
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }
        return "$baseUrl${if (trimmed.startsWith("/")) trimmed else "/$trimmed"}"
    }

    private fun writeFavorite(
        endpoint: String,
        method: String,
        token: String
    ) {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 30_000
            readTimeout = 30_000
            doOutput = false
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
        }

        try {
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                val responseText = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                throw IOException("HTTP $responseCode: $responseText")
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun JSONObject.toFieldDto(): UserFieldDto {
        val tagsArray = optJSONArray("tags") ?: JSONArray()
        val tags = List(tagsArray.length()) { idx -> tagsArray.optString(idx) }
            .filter { it.isNotBlank() }

        return UserFieldDto(
            fieldId = optIntOrNull("field_id")
                ?: optIntOrNull("fieldId")
                ?: optIntOrNull("id"),
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
            imageUrl = resolveMediaUrl(optStringOrNull("imageUrl") ?: optStringOrNull("image")),
            isProLeague = optBooleanOrNull("is_pro_league") ?: optBooleanOrNull("isProLeague"),
            tags = tags,
            availability = optStringOrNull("availability"),
            cardType = optStringOrNull("card_type") ?: optStringOrNull("cardType"),
            region = optStringOrNull("region"),
            province = optStringOrNull("province"),
            district = optStringOrNull("district"),
            phone = optStringOrNull("phone"),
            contactPhone = optStringOrNull("contact_phone")
                ?: optStringOrNull("owner_phone")
                ?: optStringOrNull("phone"),
            avatarImageUrl = resolveMediaUrl(
                optStringOrNull("avatar_image_url") ?: optStringOrNull("avatarImageUrl")
            ),
            cardImageUrl = resolveMediaUrl(
                optStringOrNull("card_image_url") ?: optStringOrNull("cardImageUrl")
            )
        )
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

private fun parseApiError(responseText: String): String? =
    runCatching { JSONObject(responseText).optString("message").takeIf { it.isNotBlank() } }.getOrNull()

