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
import com.example.appdanini.data.model.request.invite.TransferInviteResponse
import retrofit2.Response
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

    suspend fun refreshAccessToken(refreshToken: String): Response<Unit>? {
        return try {
            authApi.refreshAccessToken("Bearer $refreshToken")
        } catch (e: Exception) {
            Log.e("AuthRepository", "토큰 갱신 실패: ${e.localizedMessage}")
            null
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

    // AuthRepository.kt 내부에 추가
    // 최초 생성자의 가족 group_id와 초대 코드가 여기서 저장
    suspend fun requestInviteCode(groupName: String): InviteCodeResponse? {
        return try {
            val response = authApi.requestInviteCode(InviteCodeRequest(groupName))
            if (response.isSuccessful) {
                response.body()?.let { result ->
                    // 서버가 내려준 group_id는 생성자 측에서만 저장
                    tokenManager.saveGroupId(result.group_id)
                    tokenManager.saveInviteCode(result.inviteCode)
                    result
                }
            } else {
                Log.e(
                    "AuthRepository",
                    "requestInviteCode failed: code=${response.code()} message=${response.message()}"
                )
                null
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "requestInviteCode exception: ${e.localizedMessage}")
            null
        }
    }



    // 실제 API 호출 + 성공 여부 판단
    suspend fun transferInviteCode(inviteCode: String): Boolean {
        return try {
            val response = authApi.transferInviteCode(TransferInviteRequest(inviteCode))
            // 1) HTTP 상태 코드 검사
            if (!response.isSuccessful) {
                Log.e(
                    "AuthRepository",
                    "transferInviteCode failed: code=${response.code()} message=${response.message()}"
                )
                return false
            }
            // 2) 응답 바디가 있을 때만 request_id 저장하고 true 반환
            // 일단 저장은 해두자. 보내 주는 이유가 있을 거임.
            response.body()?.let { result ->
                true
            } ?: run {
                Log.e("AuthRepository", "transferInviteCode: response body is null")
                false
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "transferInviteCode exception: ${e.localizedMessage}", e)
            false
        }
    }

    // 폴링 상태 체크
    // 상태 체크 후
    suspend fun checkInviteStatus(): TransferInviteResponse? {
        return try {
            val response = authApi.checkInviteStatus()
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(
                    "AuthRepository",
                    "checkInviteStatus failed: code=${response.code()} message=${response.message()}"
                )
                null
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "checkInviteStatus error", e)
            null
        }
    }


    suspend fun acceptGroup(requestId: Int, isAccepted: Boolean): Boolean {
        return try {
            val status = if (isAccepted) "ACCEPT" else "REJECT"
            val request = AcceptGroupRequest(status)
            val response = authApi.acceptGroup(requestId, request)

            if (response.isSuccessful) {
                val body = response.body()
                Log.d("AuthRepository", "수락 결과: request_id=${body?.request_id}, 시간=${body?.updated_at}")
                true
            } else {
                Log.e("AuthRepository", "초대 응답 실패: ${response.code()} ${response.message()}")
                false
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "초대 응답 예외: ${e.localizedMessage}", e)
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
