package com.sportmanagement.user.data.remote.api
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class MomoPaymentResponse(
    val paymentId: Int?,
    val orderId: String,
    val requestId: String,
    val amount: Int,
    val resultCode: Int?,
    val message: String?,
    val payUrl: String?,
    val deeplink: String?,
    val qrCodeUrl: String?
)

data class MomoPaymentStatusResponse(
    val orderId: String,
    val paymentStatus: String?,
    val failureReason: String?,
    val transactionId: String?
)

object MomoPaymentApi {
    suspend fun createPayment(
        token: String,
        bookingIds: List<Int>,
        orderInfo: String,
        redirectUrl: String? = null
    ): MomoPaymentResponse = withContext(Dispatchers.IO) {
        val normalizedBookingIds = bookingIds.distinct().filter { it > 0 }
        require(normalizedBookingIds.isNotEmpty()) { "bookingIds must not be empty" }

        val url = URL("${ApiConfig.BASE_URL}/api/payments/momo/create")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 30_000
            readTimeout = 30_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
        }

        val bookingIdsArray = org.json.JSONArray()
        normalizedBookingIds.forEach { bookingIdsArray.put(it) }

        val body = JSONObject()
            .put("booking_id", normalizedBookingIds.first())
            .put("booking_ids", bookingIdsArray)
            .put("orderInfo", orderInfo)
            .put("redirectUrl", redirectUrl)
            .toString()

        connection.outputStream.use { stream ->
            stream.write(body.toByteArray(Charsets.UTF_8))
        }

        val responseCode = connection.responseCode
        val responseText = if (responseCode in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
        }

        val json = responseText.takeIf { it.isNotBlank() }?.let(::JSONObject)
            ?: JSONObject()

        if (responseCode !in 200..299) {
            throw IOException(json.optString("message", "HTTP $responseCode"))
        }

        MomoPaymentResponse(
            paymentId = json.optIntOrNull("payment_id"),
            orderId = json.optString("order_id"),
            requestId = json.optString("request_id"),
            amount = json.optInt("amount"),
            resultCode = json.optIntOrNull("resultCode"),
            message = json.optStringOrNull("message"),
            payUrl = json.optStringOrNull("payUrl"),
            deeplink = json.optStringOrNull("deeplink"),
            qrCodeUrl = json.optStringOrNull("qrCodeUrl")
        )
    }

    suspend fun createDemoPayment(
        amount: Int,
        orderInfo: String,
        redirectUrl: String? = null
    ): MomoPaymentResponse = withContext(Dispatchers.IO) {
        val url = URL("${ApiConfig.BASE_URL}/api/payments/momo/create")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 30_000
            readTimeout = 30_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }

        val body = JSONObject()
            .put("demo", true)
            .put("amount", amount)
            .put("orderInfo", orderInfo)
            .put("redirectUrl", redirectUrl)
            .toString()

        connection.outputStream.use { stream ->
            stream.write(body.toByteArray(Charsets.UTF_8))
        }

        val responseCode = connection.responseCode
        val responseText = if (responseCode in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
        }

        val json = responseText.takeIf { it.isNotBlank() }?.let(::JSONObject)
            ?: JSONObject()

        if (responseCode !in 200..299) {
            throw IOException(json.optString("message", "HTTP $responseCode"))
        }

        MomoPaymentResponse(
            paymentId = json.optIntOrNull("payment_id"),
            orderId = json.optString("order_id"),
            requestId = json.optString("request_id"),
            amount = json.optInt("amount"),
            resultCode = json.optIntOrNull("resultCode"),
            message = json.optStringOrNull("message"),
            payUrl = json.optStringOrNull("payUrl"),
            deeplink = json.optStringOrNull("deeplink"),
            qrCodeUrl = json.optStringOrNull("qrCodeUrl")
        )
    }

    suspend fun confirmClientPaymentResult(
        token: String,
        orderId: String,
        requestId: String?,
        resultCode: Int,
        message: String? = null
    ) = withContext(Dispatchers.IO) {
        val url = URL("${ApiConfig.BASE_URL}/api/payments/momo/client-confirm")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 30_000
            readTimeout = 30_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
        }

        val body = JSONObject()
            .put("orderId", orderId)
            .put("resultCode", resultCode)
            .put("message", message)
        requestId?.takeIf { it.isNotBlank() }?.let { body.put("requestId", it) }

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
            val json = responseText.takeIf { it.isNotBlank() }?.let(::JSONObject)
            throw IOException(json?.optString("message", "HTTP $responseCode") ?: "HTTP $responseCode")
        }
    }

    suspend fun getPaymentByOrderId(orderId: String): MomoPaymentStatusResponse =
        withContext(Dispatchers.IO) {
            val connection = (URL("${ApiConfig.BASE_URL}/api/payments/order/$orderId").openConnection() as HttpURLConnection).apply {
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

                val json = responseText.takeIf { it.isNotBlank() }?.let(::JSONObject)
                    ?: JSONObject()

                if (responseCode !in 200..299) {
                    throw IOException(json.optString("message", "HTTP $responseCode"))
                }

                MomoPaymentStatusResponse(
                    orderId = json.optString("order_id"),
                    paymentStatus = json.optStringOrNull("payment_status"),
                    failureReason = json.optStringOrNull("failure_reason"),
                    transactionId = json.optStringOrNull("transaction_id")
                )
            } finally {
                connection.disconnect()
            }
        }
}

private fun JSONObject.optStringOrNull(name: String): String? =
    optString(name).takeIf { it.isNotBlank() && it != "null" }

private fun JSONObject.optIntOrNull(name: String): Int? =
    if (has(name) && !isNull(name)) optInt(name) else null

