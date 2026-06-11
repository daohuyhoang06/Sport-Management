package com.sportmanagement.user.push

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.sportmanagement.user.BuildConfig
import com.sportmanagement.user.data.remote.api.ApiConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

object PushNotificationRegistrar {
    private const val TAG = "PushRegistrar"
    private const val CACHE_PREFS = "user_repository_cache"
    private const val AUTH_TOKEN_KEY = "auth_token"

    fun registerCurrentToken(context: Context) {
        val appContext = context.applicationContext
        if (readAuthToken(appContext).isNullOrBlank()) return

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                if (token.isNotBlank()) {
                    registerToken(appContext, token)
                }
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "Unable to get FCM token", error)
            }
    }

    fun registerToken(context: Context, fcmToken: String) {
        val appContext = context.applicationContext
        val authToken = readAuthToken(appContext) ?: return

        thread(name = "register-fcm-token", isDaemon = true) {
            runCatching {
                val body = JSONObject()
                    .put("token", fcmToken)
                    .put("platform", "android")
                    .put("appVersion", BuildConfig.VERSION_NAME)
                    .put("deviceId", readDeviceId(appContext))

                val connection = (URL("${ApiConfig.BASE_URL}/api/user/device-tokens")
                    .openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 15_000
                    readTimeout = 15_000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Authorization", "Bearer $authToken")
                }

                try {
                    connection.outputStream.use { output ->
                        output.write(body.toString().toByteArray(Charsets.UTF_8))
                    }

                    if (connection.responseCode !in 200..299) {
                        Log.w(TAG, "Register FCM token failed: ${connection.responseCode}")
                    }
                } finally {
                    connection.disconnect()
                }
            }.onFailure { error ->
                Log.w(TAG, "Register FCM token error", error)
            }
        }
    }

    private fun readAuthToken(context: Context): String? =
        context.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE)
            .getString(AUTH_TOKEN_KEY, null)
            ?.takeIf { it.isNotBlank() }

    private fun readDeviceId(context: Context): String? =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?.takeIf { it.isNotBlank() }
}
