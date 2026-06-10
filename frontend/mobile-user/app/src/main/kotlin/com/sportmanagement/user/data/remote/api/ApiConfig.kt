package com.sportmanagement.user.data.remote.api

import com.sportmanagement.user.BuildConfig

object ApiConfig {
    // Dùng cấu hình build-time để hỗ trợ adb reverse trên máy thật.
    val BASE_URL: String = BuildConfig.API_BASE_URL
}
