package com.example.appdanini.util

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val FORCE_LOGOUT_KEY = "force_logout"
        private const val FAMILY_NAME_KEY = "family_name"
        private const val INVITE_CODE_KEY = "inviteCode"
        private const val REQUEST_CODE_KEY = "request_id"

        private const val INITIAL_SETUP_DONE_KEY = "initial_setup_done"
        private const val INVITE_CODE_SUBMITTED_KEY = "invite_code_submitted"
        private const val GROUP_SCORE = "group_score"
        private const val PERSONAL_SCORE = "personal_score"
        private const val GROUP_ID = "group_id"
        private const val EMOTION_SUBMIT_DATE_KEY = "emotion_submit_date"
        private const val EMOTION_SUBMIT_TIMESTAMP_KEY = "emotion_submit_timestamp"
        private const val EMOTION_COOLDOWN_HOURS = 12L

        // 추가: 테스트 완료 플래그
        private const val TEST_COMPLETED_KEY = "test_completed"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // 기존 메서드들...
    fun saveTokensOnly(token: String, refreshToken: String) {
        prefs.edit()
            .putString(ACCESS_TOKEN_KEY, token)
            .putString(REFRESH_TOKEN_KEY, refreshToken)
            .apply()
    }

    fun saveFamilyname(family_name: String) {
        prefs.edit()
            .putString(FAMILY_NAME_KEY, family_name)
            .apply()
    }

    fun saveGroupId(group_id: Int) {
        prefs.edit()
            .putInt(GROUP_ID, group_id)
            .apply()
    }

    fun saveInviteCode(inviteCode: String) {
        prefs.edit()
            .putString(INVITE_CODE_KEY, inviteCode)
            .apply()
    }

    fun saveRequestCode(request_id: String) {
        prefs.edit()
            .putString(REQUEST_CODE_KEY, request_id)
            .apply()
    }

    fun isAccessTokenExpired(): Boolean {
        val token = getAccessToken() ?: return true
        val parts = token.split(".")
        if (parts.size < 2) return true

        return try {
            val payloadJson =
                String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
            val exp = JSONObject(payloadJson).optLong("exp", 0L)
            val now = System.currentTimeMillis() / 1000
            now > exp
        } catch (e: Exception) {
            true
        }
    }

    fun saveScore(group_score: Int, personal_score: Int) {
        prefs.edit()
            .putInt(GROUP_SCORE, group_score)
            .putInt(PERSONAL_SCORE, personal_score)
            .apply()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun shouldShowEmotionPopup(): Boolean {
        val today = LocalDate.now().toString()
        val submittedDate = prefs.getString(EMOTION_SUBMIT_DATE_KEY, null)
        if (submittedDate == today) return false

        val now = LocalDateTime.now()
        val nineAmToday = now.toLocalDate().atTime(LocalTime.of(6, 0))
        return now.isAfter(nineAmToday)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun markEmotionSubmittedNow() {
        val now = System.currentTimeMillis()
        val today = LocalDate.now().toString()
        prefs.edit()
            .putLong(EMOTION_SUBMIT_TIMESTAMP_KEY, now)
            .putString(EMOTION_SUBMIT_DATE_KEY, today)
            .apply()
    }

    // 기존 getter 메서드들...
    fun getUserName(): String? = prefs.getString("name", null)
    fun getUserEmail(): String? = prefs.getString("email", null)
    fun getUserId(): String? = prefs.getString("userId", null)
    fun getAccessToken(): String? = prefs.getString(ACCESS_TOKEN_KEY, null)
    fun getRefreshToken(): String? = prefs.getString(REFRESH_TOKEN_KEY, null)
    fun getFamilyName(): String? = prefs.getString(FAMILY_NAME_KEY, null)
    fun getInviteCode(): String? = prefs.getString(INVITE_CODE_KEY, null)
    fun getGroupScore(): Int = prefs.getInt(GROUP_SCORE, 0)
    fun getGroupId(): Int = prefs.getInt(GROUP_ID, 0)
    fun getPersonalScore(): Int = prefs.getInt(PERSONAL_SCORE, 0)

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

    fun markInitialSetupDone() {
        prefs.edit().putBoolean(INITIAL_SETUP_DONE_KEY, true).apply()
    }

    fun isInitialSetupDone(): Boolean {
        return prefs.getBoolean(INITIAL_SETUP_DONE_KEY, false)
    }

    fun markInviteCodeSubmitted() {
        prefs.edit().putBoolean(INVITE_CODE_SUBMITTED_KEY, true).apply()
    }

    fun isInviteCodeSubmitted(): Boolean {
        return prefs.getBoolean(INVITE_CODE_SUBMITTED_KEY, false)
    }

    // 추가된 테스트 완료 메서드
    /** 테스트가 완료되었음을 표시합니다. */
    fun markTestCompleted() {
        prefs.edit()
            .putBoolean(TEST_COMPLETED_KEY, true)
            .apply()
    }

    /** 테스트 완료 여부를 반환합니다. */
    fun isTestCompleted(): Boolean =
        prefs.getBoolean(TEST_COMPLETED_KEY, false)

    /** 테스트 완료 플래그를 초기화합니다. */
    fun clearTestCompleted() {
        prefs.edit().remove(TEST_COMPLETED_KEY).apply()
    }


    fun clearSession() {
        prefs.edit()
            .remove("access_token")
            .remove("refresh_token")
            .remove("force_logout")
            .remove("family_name")
            .remove("inviteCode")
            .remove("request_id")
            .remove("initial_setup_done")
            .remove("invite_code_submitted")
            .remove("group_id")
            .remove("group_score")
            .remove("personal_score")
            .apply()
    }
}