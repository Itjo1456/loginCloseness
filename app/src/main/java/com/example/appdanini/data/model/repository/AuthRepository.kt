package com.example.appdanini.data.model.repository

import android.util.Log
import com.example.appdanini.util.TokenManager
import com.example.appdanini.data.model.remote.api.AuthApi
import com.example.appdanini.data.model.request.invite.AcceptGroupRequest
import com.example.appdanini.data.model.request.invite.InviteCodeRequest
import com.example.appdanini.data.model.request.auth.LoginRequest
import com.example.appdanini.data.model.request.auth.SignupRequest
import com.example.appdanini.data.model.request.invite.TransferInviteRequest
import com.example.appdanini.data.model.request.auth.EmailCheckRequest
import com.example.appdanini.data.model.request.invite.FcmTokenRequest
import com.example.appdanini.data.model.request.invite.InviteCodeResponse
import javax.inject.Inject


class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {

    // ✅ 로그인 요청 후 헤더에서 토큰만 추출하는 방식
    // 서버와 직접 통신하는 레포지토리
    suspend fun loginAndGetResponse(email: String, password: String): Boolean {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            Log.d("AuthRepository", "서버 응답 코드: ${response.code()}")

            if (response.isSuccessful) {
                // 서버에서 토큰을 주는 키 이름이 아래와 같은지 체크하기
                val accessToken = response.headers()["Authorization"]?.removePrefix("Bearer ")
                val refreshToken = response.headers()["Refresh-Token"]

                if (!accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty()) {
                    tokenManager.saveTokensOnly(accessToken, refreshToken)
                    Log.d("AuthRepository", "헤더에서 토큰 저장 완료: $accessToken")
                    true
                } else {
                    Log.e("AuthRepository", "헤더에 토큰 없음")
                    false
                }
            } else {
                Log.e("AuthRepository", "Login failed: ${response.code()} ${response.message()}")
                false
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login exception: ${e.localizedMessage}")
            false
        }
    }

    suspend fun signupAndGetResponse(name: String, email: String, password: String): Boolean {
        return try {
            val response = authApi.signup(SignupRequest(name, email, password))
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("AuthRepository", "회원가입 예외: ${e.localizedMessage}")
            false
        }
    }

    suspend fun checkEmailDuplication(email: String): Boolean? {
        return try {
            val response = authApi.checkEmail(EmailCheckRequest(email))
            if (response.isSuccessful) {
                response.body()?.duplicated
            } else {
                Log.e("AuthRepository", "이메일 중복 확인 실패: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "이메일 중복 확인 예외: ${e.localizedMessage}")
            null
        }
    }

    fun checkForceLogout(): Boolean {
        val accessToken = tokenManager.getAccessToken()
        val forceLogoutFlag = tokenManager.shouldForceLogout()

        val result = accessToken.isNullOrEmpty() || forceLogoutFlag
        Log.d("AuthRepository", "checkForceLogout 결과: $result")
        return result
    }


    // AuthRepository.kt 내부에 추가
    // 최초 생성자의 가족 group_id와 초대 코드가 여기서 저장
    suspend fun requestInviteCode(group_name: String): InviteCodeResponse? {
        return try {
            val response = authApi.requestInviteCode(InviteCodeRequest(group_name))
            if (response.isSuccessful) {
                response.body()?.also { result ->
                    tokenManager.saveGroupId(result.group_id) // 수정됨
                    tokenManager.saveInviteCode(result.inviteCode)
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }


    }


    // 실제 API 호출 + 성공 여부 판단
    suspend fun transferInviteCode(inviteCode: String): Boolean {
        return try {
            val response = authApi.transferInviteCode(TransferInviteRequest(inviteCode))
            response.isSuccessful // 결과를 Boolean으로
        } catch (e: Exception) {
            false
        }
    }

    suspend fun checkInviteStatus(request: CheckInviteStatusRequest): CheckInviteStatusResponse? {
        return try {
            val response = authApi.checkInviteStatus(request)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e("AuthRepository", "checkInviteStatus error", e)
            null
        }
    }

    suspend fun acceptGroup(request: AcceptGroupRequest): Boolean {
        return try {
            val response = authApi.acceptGroup(request)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun registerDeviceToken(fcmToken: String) {
        try {
            val response = authApi.registerDeviceToken(FcmTokenRequest(fcmToken))
            if (response.isSuccessful) {
                Log.d("FCM", "토큰 등록 성공")
            } else {
                Log.e("FCM", "토큰 등록 실패: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("FCM", "토큰 등록 예외: ${e.localizedMessage}")
        }
    }


    fun getAccessToken(): String? = tokenManager.getAccessToken()
    fun getRefreshToken(): String? = tokenManager.getRefreshToken()
    fun clearTokens() = tokenManager.clearTokens()
    fun shouldForceLogout(): Boolean = tokenManager.shouldForceLogout()
    fun setForceLogout(value: Boolean) = tokenManager.setForceLogout(value)
    fun clearForceLogoutFlag() = tokenManager.clearForceLogoutFlag()

}
