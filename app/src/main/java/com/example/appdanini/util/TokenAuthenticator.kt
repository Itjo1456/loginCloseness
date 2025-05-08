package com.example.appdanini.util

import com.example.appdanini.data.model.remote.api.AuthApi
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

import dagger.Lazy

class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApi: Lazy<AuthApi> // ✅ Lazy 주입 // 이게 곧 authapi 인터페이스를 구현한 레트로핏 객체
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = tokenManager.getRefreshToken() ?: return null

        val newAccessToken = runBlocking {
            try {
                val result = authApi.get().refreshAccessToken("Bearer $refreshToken")
                if (result.isSuccessful) {
                    val newToken = result.headers()["Authorization"]?.removePrefix("Bearer ")
                        ?: return@runBlocking null
                    tokenManager.saveTokensOnly(newToken, refreshToken)
                    return@runBlocking newToken
                } else {
                    tokenManager.setForceLogout(true)
                    null
                }
            } catch (e: Exception) {
                tokenManager.setForceLogout(true)
                null
            }
        } ?: return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
    }
}
//서버에 401 응답이 오면 → TokenAuthenticator.authenticate() 호출
//이미 Authorization 헤더 있으면 → 중지
//RefreshToken으로 새 AccessToken 요청
//성공 시 → 새 토큰 저장하고, 요청 다시 만들기
//실패 시 → null 반환 (로그아웃 처리 등을 고려)