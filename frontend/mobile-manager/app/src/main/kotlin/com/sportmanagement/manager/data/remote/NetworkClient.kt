package com.sportmanagement.manager.data.remote

import android.os.Build
import com.sportmanagement.manager.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {

    private const val EMULATOR_BASE_URL = "http://10.0.2.2:5000/"
    private const val USB_REVERSE_BASE_URL = "http://127.0.0.1:5000/"

    // Emulator uses 10.0.2.2. A real device connected over USB should hit the host
    // through `adb reverse tcp:5000 tcp:5000`, so localhost is the correct base URL.
    val BASE_URL: String
        get() = if (isEmulator()) EMULATOR_BASE_URL else BuildConfig.DEV_SERVER_URL.ifBlank {
            USB_REVERSE_BASE_URL
        }

    private fun isEmulator(): Boolean =
        Build.FINGERPRINT.startsWith("generic") ||
        Build.FINGERPRINT.startsWith("unknown") ||
        Build.MODEL.contains("google_sdk") ||
        Build.MODEL.contains("Emulator") ||
        Build.MODEL.contains("Android SDK built for x86") ||
        Build.MANUFACTURER.contains("Genymotion") ||
        (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
        Build.PRODUCT.contains("sdk_gphone") ||
        Build.PRODUCT.contains("sdk")

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    fun buildOkHttpClient(tokenProvider: () -> String?): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = tokenProvider()
                val request = if (token != null) {
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else {
                    chain.request()
                }
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun <T> createService(serviceClass: Class<T>, okHttpClient: OkHttpClient): T {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(serviceClass)
    }
}
