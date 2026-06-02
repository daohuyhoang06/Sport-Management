package com.sportmanagement.user.data.remote.api
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.json.JSONArray
import java.io.DataOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class AuthUserDto(
    val id: Int?,
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val status: String,
    val birthday: String?,
    val gender: String?,
    val address: String?,
    val membership: String?,
    val avatarUrl: String?,
    val favoriteSportIds: List<Int>,
    val favoriteSportKeys: Set<String>
)

data class AuthSessionDto(
    val user: AuthUserDto,
    val token: String,
    val refreshToken: String,
    val firebaseToken: String?,
    val firebaseRefreshToken: String?
)

data class RegisterRequestDto(
    val name: String,
    val email: String,
    val password: String,
    val phone: String?,
    val birthday: String?,
    val address: String?,
    val favoriteSportKeys: Set<String> = emptySet()
)

data class UpdateProfileRequestDto(
    val name: String?,
    val phone: String?,
    val birthday: String?,
    val gender: String?,
    val address: String?,
    val favoriteSportKeys: Set<String>? = null
)

class AuthApi(
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    suspend fun login(identifier: String, password: String): AuthSessionDto =
        withContext(Dispatchers.IO) {
            postAuthRequest(
                endpoint = "$baseUrl/api/auth/login",
                body = JSONObject()
                    .put("identifier", identifier)
                    .put("password", password)
            )
        }

    suspend fun register(request: RegisterRequestDto): AuthSessionDto =
        withContext(Dispatchers.IO) {
            val body = JSONObject()
                .put("name", request.name)
                .put("email", request.email)
                .put("password", request.password)

            request.phone?.takeIf { it.isNotBlank() }?.let { body.put("phone", it) }
            request.birthday?.takeIf { it.isNotBlank() }?.let { body.put("birthday", it) }
            request.address?.takeIf { it.isNotBlank() }?.let { body.put("address", it) }
            if (request.favoriteSportKeys.isNotEmpty()) {
                body.put("favoriteSportKeys", JSONArray(request.favoriteSportKeys.toList()))
            }

            postAuthRequest(
                endpoint = "$baseUrl/api/auth/register",
                body = body
            )
        }

    suspend fun loginWithGoogle(idToken: String): AuthSessionDto =
        withContext(Dispatchers.IO) {
            postAuthRequest(
                endpoint = "$baseUrl/api/auth/social-login",
                body = JSONObject()
                    .put("provider", "google")
                    .put("idToken", idToken)
            )
        }

    suspend fun getMe(token: String): AuthUserDto = withContext(Dispatchers.IO) {
        val connection = createJsonConnection(
            endpoint = "$baseUrl/api/auth/me",
            method = "GET",
            token = token
        )

        try {
            val responseCode = connection.responseCode
            val responseText = readResponseBody(connection, responseCode)
            if (responseCode !in 200..299) {
                throw IOException(readApiErrorMessage(responseCode, responseText))
            }

            val root = JSONObject(responseText)
            val payload = root.optJSONObject("data")
                ?: throw IOException("Auth response missing data")
            parseAuthUser(payload)
        } finally {
            connection.disconnect()
        }
    }

    suspend fun updateMe(token: String, request: UpdateProfileRequestDto): AuthUserDto =
        withContext(Dispatchers.IO) {
            val body = JSONObject()
            request.name?.let { body.put("name", it) }
            request.phone?.let { body.put("phone", it) }
            request.birthday?.let { body.put("birthday", it) }
            request.gender?.let { body.put("sex", it) }
            request.address?.let { body.put("address", it) }
            request.favoriteSportKeys?.let {
                body.put("favoriteSportKeys", JSONArray(it.toList()))
            }

            val connection = createJsonConnection(
                endpoint = "$baseUrl/api/auth/me",
                method = "PUT",
                token = token
            ).apply {
                doOutput = true
            }

            try {
                connection.outputStream.bufferedWriter().use { writer ->
                    writer.write(body.toString())
                }
                val responseCode = connection.responseCode
                val responseText = readResponseBody(connection, responseCode)
                if (responseCode !in 200..299) {
                    throw IOException(readApiErrorMessage(responseCode, responseText))
                }

                val root = JSONObject(responseText)
                val payload = root.optJSONObject("data")
                    ?: throw IOException("Auth response missing data")
                parseAuthUser(payload)
            } finally {
                connection.disconnect()
            }
        }

    suspend fun uploadAvatar(
        token: String,
        imageBytes: ByteArray,
        fileName: String,
        mimeType: String
    ): AuthUserDto = withContext(Dispatchers.IO) {
        val boundary = "----SportManagementBoundary${System.currentTimeMillis()}"
        val connection = createJsonConnection(
            endpoint = "$baseUrl/api/auth/me/avatar",
            method = "POST",
            token = token
        ).apply {
            doOutput = true
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        }

        try {
            DataOutputStream(connection.outputStream).use { output ->
                output.writeBytes("--$boundary\r\n")
                output.writeBytes(
                    "Content-Disposition: form-data; name=\"avatar\"; filename=\"$fileName\"\r\n"
                )
                output.writeBytes("Content-Type: $mimeType\r\n\r\n")
                output.write(imageBytes)
                output.writeBytes("\r\n")
                output.writeBytes("--$boundary--\r\n")
                output.flush()
            }

            val responseCode = connection.responseCode
            val responseText = readResponseBody(connection, responseCode)
            if (responseCode !in 200..299) {
                throw IOException(readApiErrorMessage(responseCode, responseText))
            }

            val root = JSONObject(responseText)
            val payload = root.optJSONObject("data")
                ?: throw IOException("Auth response missing data")
            parseAuthUser(payload)
        } finally {
            connection.disconnect()
        }
    }

    private fun postAuthRequest(
        endpoint: String,
        body: JSONObject
    ): AuthSessionDto {
        val connection = createJsonConnection(
            endpoint = endpoint,
            method = "POST",
            token = null
        ).apply {
            doOutput = true
        }

        return try {
            connection.outputStream.bufferedWriter().use { writer ->
                writer.write(body.toString())
            }

            val responseCode = connection.responseCode
            val responseText = readResponseBody(connection, responseCode)
            if (responseCode !in 200..299) {
                throw IOException(readApiErrorMessage(responseCode, responseText))
            }

            parseAuthSession(JSONObject(responseText))
        } finally {
            connection.disconnect()
        }
    }

    private fun parseAuthSession(response: JSONObject): AuthSessionDto {
        val data = response.optJSONObject("data")
            ?: throw IOException("Auth response missing data")
        val user = data.optJSONObject("user")
            ?: throw IOException("Auth response missing user")

        return AuthSessionDto(
            user = parseAuthUser(user),
            token = data.optString("token"),
            refreshToken = data.optString("refreshToken"),
            firebaseToken = data.optString("firebaseToken").takeIf { it.isNotBlank() },
            firebaseRefreshToken = data.optString("firebaseRefreshToken").takeIf { it.isNotBlank() }
        )
    }

    private fun parseAuthUser(user: JSONObject): AuthUserDto {
        return AuthUserDto(
            id = if (user.has("person_id") && !user.isNull("person_id")) user.optInt("person_id") else null,
            name = user.optSanitizedString("name"),
            email = user.optSanitizedString("email"),
            phone = user.optSanitizedString("phone"),
            role = user.optSanitizedString("role"),
            status = user.optSanitizedString("status"),
            birthday = user.optSanitizedStringOrNull("birthday"),
            gender = user.optSanitizedStringOrNull("sex")
                ?: user.optSanitizedStringOrNull("gender"),
            address = user.optSanitizedStringOrNull("address"),
            membership = user.optSanitizedStringOrNull("membership")
                ?: user.optSanitizedStringOrNull("membership_level"),
            avatarUrl = user.optSanitizedStringOrNull("avatarUrl")
                ?: user.optSanitizedStringOrNull("avatar_url"),
            favoriteSportIds = user.optIntList("favoriteSportIds"),
            favoriteSportKeys = user.optStringSet("favoriteSportKeys")
        )
    }

    private fun createJsonConnection(
        endpoint: String,
        method: String,
        token: String?
    ): HttpURLConnection {
        return (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 30_000
            readTimeout = 30_000
            setRequestProperty("Accept", "application/json")
            if (method != "GET") {
                setRequestProperty("Content-Type", "application/json")
            }
            token?.takeIf { it.isNotBlank() }?.let {
                setRequestProperty("Authorization", "Bearer $it")
            }
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
        val message = runCatching {
            JSONObject(responseText).optString("message")
        }.getOrNull().orEmpty()
        return message.ifBlank { "HTTP $responseCode: auth request failed" }
    }

    companion object {
        private const val BASE_URL = "http://10.0.2.2:5000"
    }
}

private fun JSONObject.optSanitizedString(name: String): String {
    val raw = optString(name, "")
    return if (raw.equals("null", ignoreCase = true)) "" else raw
}

private fun JSONObject.optSanitizedStringOrNull(name: String): String? {
    val value = optSanitizedString(name).trim()
    return value.takeIf { it.isNotEmpty() }
}

private fun JSONObject.optIntList(name: String): List<Int> {
    val array = optJSONArray(name) ?: return emptyList()
    return buildList {
        for (index in 0 until array.length()) {
            val value = array.optInt(index, Int.MIN_VALUE)
            if (value > 0) {
                add(value)
            }
        }
    }
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

