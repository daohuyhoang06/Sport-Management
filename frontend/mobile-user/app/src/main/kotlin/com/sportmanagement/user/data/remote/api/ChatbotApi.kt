package com.sportmanagement.user.data.remote.api

import com.sportmanagement.user.domain.model.ChatbotMessage
import com.sportmanagement.user.domain.model.ChatbotSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object ChatbotApi {
    private const val BASE_URL = "http://10.0.2.2:5000"

    suspend fun sendMessage(
        message: String,
        conversationHistory: List<ChatbotMessage>
    ): String = withContext(Dispatchers.IO) {
        val connection = (URL("$BASE_URL/api/ai/chat").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 30_000
            readTimeout = 30_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }

        try {
            val historyPayload = JSONArray().apply {
                conversationHistory.forEach { chatMessage ->
                    put(
                        JSONObject()
                            .put(
                                "role",
                                if (chatMessage.sender == ChatbotSender.USER) "user" else "assistant"
                            )
                            .put("message", chatMessage.text)
                    )
                }
            }

            val requestBody = JSONObject()
                .put("message", message)
                .put("conversationHistory", historyPayload)
                .toString()

            connection.outputStream.use { stream ->
                stream.write(requestBody.toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            val responseText = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }

            val responseJson = responseText.takeIf { it.isNotBlank() }?.let(::JSONObject)
                ?: JSONObject()

            if (responseCode !in 200..299) {
                throw IOException(responseJson.optString("message", "HTTP $responseCode"))
            }

            if (!responseJson.optBoolean("success", false)) {
                throw IOException(responseJson.optString("message", "Chatbot tạm thời không khả dụng"))
            }

            val reply = responseJson.optString("message").trim()
            if (reply.isBlank()) {
                throw IOException("Chatbot không trả về nội dung")
            }

            reply
        } finally {
            connection.disconnect()
        }
    }
}
