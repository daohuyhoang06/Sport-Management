package com.sportmanagement.user.data.remote.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class InboxItemDto(
    val id: Int,
    val type: String,
    val title: String,
    val subtitle: String,
    val detail: String,
    val time: String,
    val isRead: Boolean,
    val section: String,
    val targetType: String?,
    val targetId: Int?,
    val bookingId: Int?,
    val conversationId: Int?,
    val fieldId: Int?
)

data class NotificationDto(
    val id: Int,
    val type: String,
    val section: String,
    val title: String,
    val subtitle: String,
    val content: String,
    val time: String,
    val isRead: Boolean,
    val targetType: String?,
    val targetId: Int?,
    val bookingId: Int?,
    val fieldId: Int?,
    val peerUserId: Int?
)

data class ConversationMessageDto(
    val messageId: Int,
    val content: String,
    val createdAt: String,
    val isMine: Boolean
)

data class ConversationThreadDto(
    val conversationId: Int,
    val fieldName: String,
    val ownerPhone: String?,
    val messages: List<ConversationMessageDto>
)

data class ConversationListItemDto(
    val conversationId: Int,
    val fieldId: Int?,
    val fieldName: String,
    val fieldAvatar: String?,
    val ownerName: String?,
    val ownerPhone: String?,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int
)

data class CreateConversationResultDto(
    val conversationId: Int,
    val fieldId: Int?,
    val bookingId: Int?
)

data class BookingDetailDto(
    val bookingId: Int,
    val bookingCode: String,
    val status: String,
    val statusCode: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val timeRange: String,
    val totalPrice: String,
    val paymentMethod: String,
    val paymentStatus: String,
    val transactionId: String,
    val orderId: String,
    val ownerNote: String,
    val checkInCode: String,
    val shareUrl: String,
    val userName: String,
    val userPhone: String,
    val fieldId: Int,
    val fieldName: String,
    val fieldAddress: String,
    val fieldAvatar: String?,
    val ownerName: String,
    val ownerPhone: String,
    val canReview: Boolean,
    val reviewSubmitted: Boolean,
    val reviewId: Int?,
    val reviewRating: Int?,
    val reviewComment: String,
    val matchPost: BookingMatchPostDto?,
    val matchRequests: List<BookingMatchRequestDto>
)

data class ReviewSubmissionDto(
    val reviewId: Int,
    val rating: Int,
    val comment: String
)

data class BookingMatchPostDto(
    val matchPostId: Int,
    val ownerUserId: Int,
    val ownerUsername: String,
    val teamName: String,
    val playerCount: Int,
    val level: String,
    val levelLabel: String,
    val description: String,
    val status: String
)

data class BookingMatchRequestDto(
    val matchRequestId: Int,
    val requesterUserId: Int,
    val requesterUsername: String,
    val teamName: String,
    val playerCount: Int,
    val level: String,
    val levelLabel: String,
    val message: String,
    val status: String,
    val createdAt: String
)

class InboxApi(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    suspend fun getInbox(token: String): List<InboxItemDto> = withContext(Dispatchers.IO) {
        val root = getJson("$baseUrl/api/user/inbox", token)
        val data = root.optJSONObject("data") ?: JSONObject()
        val sections = data.optJSONArray("sections") ?: JSONArray()
        val result = mutableListOf<InboxItemDto>()

        for (i in 0 until sections.length()) {
            val sectionObj = sections.optJSONObject(i) ?: continue
            val sectionName = sectionObj.optString("section")
            val items = sectionObj.optJSONArray("items") ?: JSONArray()
            for (j in 0 until items.length()) {
                val row = items.optJSONObject(j) ?: continue
                result.add(
                    InboxItemDto(
                        id = row.optInt("id"),
                        type = row.optString("type"),
                        title = row.optString("title"),
                        subtitle = row.optString("subtitle"),
                        detail = row.optString("detail"),
                        time = row.optString("time"),
                        isRead = row.optBoolean("isRead", false),
                        section = row.optString("section").ifBlank { sectionName },
                        targetType = row.optString("targetType").takeIf { it.isNotBlank() },
                        targetId = row.optIntOrNull("targetId"),
                        bookingId = row.optIntOrNull("bookingId"),
                        conversationId = row.optIntOrNull("conversationId"),
                        fieldId = row.optIntOrNull("fieldId")
                    )
                )
            }
        }

        result
    }

    suspend fun getNotifications(token: String): List<NotificationDto> = withContext(Dispatchers.IO) {
        val root = getJson("$baseUrl/api/user/notifications", token)
        val items = root.optJSONObject("data")?.optJSONArray("items") ?: JSONArray()
        List(items.length()) { index ->
            items.optJSONObject(index).toNotificationDto()
        }
    }

    suspend fun getNotificationDetail(token: String, id: Int): NotificationDto = withContext(Dispatchers.IO) {
        val root = getJson("$baseUrl/api/user/notifications/$id", token)
        val data = root.optJSONObject("data") ?: JSONObject()
        data.toNotificationDto()
    }

    suspend fun markNotificationRead(token: String, id: Int) = withContext(Dispatchers.IO) {
        postJsonWithoutBody("$baseUrl/api/user/notifications/$id/read", token)
    }

    suspend fun markBookingNotificationsRead(token: String, bookingId: Int) = withContext(Dispatchers.IO) {
        postJsonWithoutBody("$baseUrl/api/user/notifications/booking/$bookingId/read", token)
    }

    suspend fun markAllNotificationsRead(token: String) = withContext(Dispatchers.IO) {
        postJsonWithoutBody("$baseUrl/api/user/notifications/read-all", token)
    }

    suspend fun markInboxReadAll(token: String) {
        postJsonWithoutBody("$baseUrl/api/user/inbox/read-all", token)
    }

    suspend fun getBookingDetail(token: String, bookingId: Int): BookingDetailDto = withContext(Dispatchers.IO) {
        val root = getJson("$baseUrl/api/user/bookings/$bookingId", token)
        val data = root.optJSONObject("data") ?: JSONObject()
        val user = data.optJSONObject("user") ?: JSONObject()
        val field = data.optJSONObject("field") ?: JSONObject()

        BookingDetailDto(
            bookingId = data.optInt("bookingId"),
            bookingCode = data.optString("bookingCode"),
            status = data.optString("status"),
            statusCode = data.optString("statusCode"),
            date = data.optString("date"),
            startTime = data.optString("startTime"),
            endTime = data.optString("endTime"),
            timeRange = data.optString("timeRange"),
            totalPrice = data.optString("totalPrice"),
            paymentMethod = data.optString("paymentMethod"),
            paymentStatus = data.optString("paymentStatus"),
            transactionId = data.optString("transactionId"),
            orderId = data.optString("orderId"),
            ownerNote = data.optString("ownerNote"),
            checkInCode = data.optString("checkInCode"),
            shareUrl = data.optString("shareUrl"),
            userName = user.optString("name"),
            userPhone = user.optString("phone"),
            fieldId = field.optInt("fieldId"),
            fieldName = field.optString("fieldName"),
            fieldAddress = field.optString("address"),
            fieldAvatar = field.optString("avatar").takeIf { it.isNotBlank() },
            ownerName = field.optString("ownerName"),
            ownerPhone = field.optString("ownerPhone"),
            canReview = data.optBoolean("canReview", false),
            reviewSubmitted = data.optBoolean("reviewSubmitted", false),
            reviewId = data.optIntOrNull("reviewId"),
            reviewRating = data.optIntOrNull("reviewRating"),
            reviewComment = data.optString("reviewComment"),
            matchPost = data.optJSONObject("matchPost")?.let { row ->
                BookingMatchPostDto(
                    matchPostId = row.optInt("matchPostId"),
                    ownerUserId = row.optInt("ownerUserId"),
                    ownerUsername = row.optString("ownerUsername"),
                    teamName = row.optString("teamName"),
                    playerCount = row.optInt("playerCount"),
                    level = row.optString("level"),
                    levelLabel = row.optString("levelLabel"),
                    description = row.optString("description"),
                    status = row.optString("status")
                )
            },
            matchRequests = (data.optJSONArray("matchRequests") ?: JSONArray()).let { items ->
                List(items.length()) { index ->
                    val row = items.optJSONObject(index) ?: JSONObject()
                    BookingMatchRequestDto(
                        matchRequestId = row.optInt("matchRequestId"),
                        requesterUserId = row.optInt("requesterUserId"),
                        requesterUsername = row.optString("requesterUsername"),
                        teamName = row.optString("teamName"),
                        playerCount = row.optInt("playerCount"),
                        level = row.optString("level"),
                        levelLabel = row.optString("levelLabel"),
                        message = row.optString("message"),
                        status = row.optString("status"),
                        createdAt = row.optString("createdAt")
                    )
                }
            }
        )
    }

    suspend fun submitBookingReview(
        token: String,
        bookingId: Int,
        fieldId: Int,
        rating: Int,
        comment: String
    ): ReviewSubmissionDto = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("booking_id", bookingId)
            .put("field_id", fieldId)
            .put("rating", rating)
            .put("comment", comment)

        val root = postJsonWithResponse("$baseUrl/api/user/reviews", token, body)
        val review = root.optJSONObject("review") ?: root.optJSONObject("data") ?: JSONObject()
        ReviewSubmissionDto(
            reviewId = review.optInt("review_id"),
            rating = review.optInt("rating", rating),
            comment = review.optString("comment", comment)
        )
    }

    suspend fun acceptMatchRequest(token: String, matchRequestId: Int) = withContext(Dispatchers.IO) {
        postJsonWithoutBody("$baseUrl/api/user/match-requests/$matchRequestId/accept", token)
    }

    suspend fun rejectMatchRequest(token: String, matchRequestId: Int) = withContext(Dispatchers.IO) {
        postJsonWithoutBody("$baseUrl/api/user/match-requests/$matchRequestId/reject", token)
    }

    suspend fun getConversations(token: String): List<ConversationListItemDto> = withContext(Dispatchers.IO) {
        val root = getJson("$baseUrl/api/user/conversations", token)
        val data = root.optJSONArray("data") ?: JSONArray()
        List(data.length()) { index ->
            val row = data.optJSONObject(index) ?: JSONObject()
            ConversationListItemDto(
                conversationId = row.optInt("conversationId"),
                fieldId = row.optIntOrNull("fieldId"),
                fieldName = row.optString("fieldName"),
                fieldAvatar = row.optString("fieldAvatar").takeIf { it.isNotBlank() },
                ownerName = row.optString("ownerName").takeIf { it.isNotBlank() },
                ownerPhone = row.optString("ownerPhone").takeIf { it.isNotBlank() },
                lastMessage = row.optString("lastMessage"),
                lastMessageTime = row.optString("lastMessageTime"),
                unreadCount = row.optInt("unreadCount", 0)
            )
        }
    }

    suspend fun createConversation(
        token: String,
        fieldId: Int?,
        bookingId: Int?,
        peerUserId: Int? = null
    ): CreateConversationResultDto = withContext(Dispatchers.IO) {
        val body = JSONObject()
        fieldId?.let { body.put("fieldId", it) }
        bookingId?.let { body.put("bookingId", it) }
        peerUserId?.let { body.put("peerUserId", it) }

        val root = postJsonWithResponse("$baseUrl/api/user/conversations", token, body)
        val data = root.optJSONObject("data") ?: JSONObject()
        CreateConversationResultDto(
            conversationId = data.optInt("conversationId"),
            fieldId = data.optIntOrNull("fieldId"),
            bookingId = data.optIntOrNull("bookingId")
        )
    }

    suspend fun getConversationMessages(token: String, conversationId: Int): ConversationThreadDto = withContext(Dispatchers.IO) {
        val root = getJson("$baseUrl/api/user/conversations/$conversationId/messages", token)
        val data = root.optJSONObject("data") ?: JSONObject()
        val conversation = data.optJSONObject("conversation") ?: JSONObject()
        val messages = data.optJSONArray("messages") ?: JSONArray()

        val mappedMessages = List(messages.length()) { idx ->
            val row = messages.optJSONObject(idx) ?: JSONObject()
            ConversationMessageDto(
                messageId = row.optInt("messageId"),
                content = row.optString("content"),
                createdAt = row.optString("createdAt"),
                isMine = row.optBoolean("isMine", false)
            )
        }

        ConversationThreadDto(
            conversationId = conversation.optInt("conversationId", conversationId),
            fieldName = conversation.optString("fieldName"),
            ownerPhone = conversation.optString("ownerPhone").takeIf { it.isNotBlank() },
            messages = mappedMessages
        )
    }

    suspend fun sendConversationMessage(token: String, conversationId: Int, content: String): ConversationMessageDto = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("messageType", "text")
            .put("content", content)

        val root = postJsonWithResponse("$baseUrl/api/user/conversations/$conversationId/messages", token, body)
        val data = root.optJSONObject("data") ?: JSONObject()
        ConversationMessageDto(
            messageId = data.optInt("messageId"),
            content = data.optString("content"),
            createdAt = data.optString("createdAt"),
            isMine = data.optBoolean("isMine", true)
        )
    }

    suspend fun markConversationRead(token: String, conversationId: Int) = withContext(Dispatchers.IO) {
        postJsonWithoutBody("$baseUrl/api/user/conversations/$conversationId/read", token)
    }

    private fun JSONObject.toNotificationDto(): NotificationDto {
        return NotificationDto(
            id = optInt("id"),
            type = optString("type"),
            section = optString("section"),
            title = optString("title"),
            subtitle = optString("subtitle"),
            content = optString("content"),
            time = optString("time"),
            isRead = optBoolean("isRead", false),
            targetType = optString("targetType").takeIf { it.isNotBlank() },
            targetId = optIntOrNull("targetId"),
            bookingId = optIntOrNull("bookingId"),
            fieldId = optIntOrNull("fieldId"),
            peerUserId = optMetadataInt("peerUserId")
        )
    }

    private fun JSONObject.optMetadataInt(key: String): Int? {
        val metadataValue = opt("metadata")
        val metadata = when (metadataValue) {
            is JSONObject -> metadataValue
            is String -> runCatching { JSONObject(metadataValue) }.getOrNull()
            else -> null
        } ?: return null
        return metadata.optIntOrNull(key)
    }

    private fun getJson(endpoint: String, token: String): JSONObject {
        val connection = createConnection(endpoint, "GET", token)
        return try {
            val responseCode = connection.responseCode
            val responseText = readResponseBody(connection, responseCode)
            if (responseCode !in 200..299) {
                throw IOException(readApiErrorMessage(responseCode, responseText))
            }
            JSONObject(responseText)
        } finally {
            connection.disconnect()
        }
    }

    private fun postJsonWithResponse(endpoint: String, token: String, body: JSONObject): JSONObject {
        val connection = createConnection(endpoint, "POST", token).apply { doOutput = true }
        return try {
            connection.outputStream.bufferedWriter().use { it.write(body.toString()) }
            val responseCode = connection.responseCode
            val responseText = readResponseBody(connection, responseCode)
            if (responseCode !in 200..299) {
                throw IOException(readApiErrorMessage(responseCode, responseText))
            }
            JSONObject(responseText)
        } finally {
            connection.disconnect()
        }
    }

    private fun patchJson(endpoint: String, token: String) {
        val connection = createConnection(endpoint, "PATCH", token)
        try {
            val responseCode = connection.responseCode
            val responseText = readResponseBody(connection, responseCode)
            if (responseCode !in 200..299) {
                throw IOException(readApiErrorMessage(responseCode, responseText))
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun postJsonWithoutBody(endpoint: String, token: String) {
        val connection = createConnection(endpoint, "POST", token).apply { doOutput = true }
        try {
            connection.outputStream.bufferedWriter().use { it.write("{}") }
            val responseCode = connection.responseCode
            val responseText = readResponseBody(connection, responseCode)
            if (responseCode !in 200..299) {
                throw IOException(readApiErrorMessage(responseCode, responseText))
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun createConnection(endpoint: String, method: String, token: String): HttpURLConnection {
        return (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 30_000
            readTimeout = 30_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
        }
    }

    private fun readResponseBody(connection: HttpURLConnection, responseCode: Int): String {
        return if (responseCode in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
        }
    }

    private fun readApiErrorMessage(responseCode: Int, responseText: String): String {
        val message = runCatching { JSONObject(responseText).optString("message") }.getOrNull().orEmpty()
        return message.ifBlank { "HTTP $responseCode: inbox request failed" }
    }

    companion object {
        private val BASE_URL = ApiConfig.BASE_URL
    }
}

private fun JSONObject.optIntOrNull(name: String): Int? =
    if (has(name) && !isNull(name)) optInt(name) else null
