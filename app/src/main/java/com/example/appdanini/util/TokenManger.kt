package com.example.appdanini.util

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
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
        private const val INITIAL_SETUP_DONE_KEY = "initial_setup_done" // 🔥 추가
        private const val INVITE_CODE_SUBMITTED_KEY = "invite_code_submitted" // 🔥 추가
        private const val GROUP_SCORE = "group_score"
        private const val PERSONAL_SCORE = "personal_score"
        private const val GROUP_ID = "group_id"
        private const val EMOTION_SUBMIT_DATE_KEY = "emotion_submit_date" // 날짜 기준
        private const val EMOTION_SUBMIT_TIMESTAMP_KEY = "emotion_submit_timestamp" // 시간 기준
        private const val EMOTION_COOLDOWN_HOURS = 12L // 제출 후 다시 뜨게 하려면 몇 시간?
    }

    // "auth_prefs"라는 이름의 전용 저장소(SharedPreferences)를 가져와서, 이후 prefs 변수로 토큰을 읽고 쓰도록 준비하는 것
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // #1 로그인 이후 해더에서 추출된 토큰값을 저장하는 함수
    fun saveTokensOnly(token: String, refreshToken: String) {
        prefs.edit()
            .putString("access_token", token)
            .putString("refresh_token", refreshToken)
            .apply()
    }

    // #2 가족명 저장
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

    // 초대 코드 저장
    fun saveInviteCode(inviteCode: String) {
        prefs.edit()
            .putString(INVITE_CODE_KEY, inviteCode)
            .apply()
    }

    // 점수 저장
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

        // 이미 오늘 제출했으면 안 띄움
        if (submittedDate == today) return false

        // 현재 시간이 오전 9시 이후인지 확인
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
        } //주로 토큰 갱신(refresh) 실패 시(예: 서버에서 401을 받았거나 네트워크 예외) 호출해서, 앱 전역에 “더 이상 이 계정으로 API 호출을 해서는 안 된다”는 신호를 보내는 용도입니다. ​

        fun shouldForceLogout(): Boolean {
            return prefs.getBoolean(FORCE_LOGOUT_KEY, false)
        }

        fun clearForceLogoutFlag() {
            prefs.edit().remove(FORCE_LOGOUT_KEY).apply()
        }

        // 🔥 초기 설정 완료 저장
        fun markInitialSetupDone() {
            prefs.edit().putBoolean(INITIAL_SETUP_DONE_KEY, true).apply()
        }

        fun isInitialSetupDone(): Boolean {
            return prefs.getBoolean(INITIAL_SETUP_DONE_KEY, false)
        }

        // 🔥 초대코드 제출 완료 저장
        fun markInviteCodeSubmitted() {
            prefs.edit().putBoolean(INVITE_CODE_SUBMITTED_KEY, true).apply()
        }

        fun isInviteCodeSubmitted(): Boolean {
            return prefs.getBoolean(INVITE_CODE_SUBMITTED_KEY, false)
        }
    }
