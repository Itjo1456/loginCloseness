package com.example.appdanini.data.model.remote

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val FORCE_LOGOUT_KEY = "force_logout"
    }

    // "auth_prefs"라는 이름의 전용 저장소(SharedPreferences)를 가져와서, 이후 prefs 변수로 토큰을 읽고 쓰도록 준비하는 것
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveLoginData(token: String, refreshToken: String, userName: String, userId: Long) {
        prefs.edit()
            .putString("access_token", token)
            .putString("refresh_token", refreshToken)
            .putString("user_name", userName)
            .putLong("user_id", userId)
            .apply()
    }

    fun getUserName(): String? = prefs.getString("user_name", null)

    fun getUserId(): Long = prefs.getLong("user_id", -1)

    fun getAccessToken(): String? = prefs.getString(ACCESS_TOKEN_KEY, null)

    fun getRefreshToken(): String? = prefs.getString(REFRESH_TOKEN_KEY, null)

    fun clearTokens() {
        prefs.edit().clear().apply()
    }
    fun setForceLogout(value: Boolean) {
        prefs.edit().putBoolean(FORCE_LOGOUT_KEY, value).apply()
    }

    fun shouldForceLogout(): Boolean {
        return prefs.getBoolean(FORCE_LOGOUT_KEY, false)
    }

    fun clearForceLogoutFlag() {
        prefs.edit().remove(FORCE_LOGOUT_KEY).apply()
    }

    fun saveTokensOnly(token: String, refreshToken: String) {
        prefs.edit()
            .putString("access_token", token)
            .putString("refresh_token", refreshToken)
            .apply()
    }
}

