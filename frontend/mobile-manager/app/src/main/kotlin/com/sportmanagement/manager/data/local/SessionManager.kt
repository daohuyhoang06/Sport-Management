package com.sportmanagement.manager.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveSession(
        token: String,
        refreshToken: String,
        userId: Int,
        name: String,
        role: String,
        avatarUrl: String?
    ) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putInt(KEY_USER_ID, userId)
            putString(KEY_NAME, name)
            putString(KEY_ROLE, role)
            putString(KEY_AVATAR_URL, avatarUrl)
            apply()
        }
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)
    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)
    fun getUserName(): String? = prefs.getString(KEY_NAME, null)
    fun getRole(): String? = prefs.getString(KEY_ROLE, null)
    fun getAvatarUrl(): String? = prefs.getString(KEY_AVATAR_URL, null)

    fun isLoggedIn(): Boolean = getToken() != null

    fun clearSession() = prefs.edit().clear().apply()

    companion object {
        private const val PREFS_NAME = "manager_session"
        private const val KEY_TOKEN = "token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_NAME = "name"
        private const val KEY_ROLE = "role"
        private const val KEY_AVATAR_URL = "avatar_url"
    }
}
