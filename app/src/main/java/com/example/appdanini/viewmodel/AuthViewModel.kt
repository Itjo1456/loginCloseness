package com.example.appdanini.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdanini.util.TokenManager
import com.example.appdanini.data.model.repository.AuthRepository
import com.example.appdanini.data.model.request.invite.AcceptGroupRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    // ✅ 로그인 결과
    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    // ✅ 회원가입 결과
    private val _signupResult = MutableLiveData<Boolean>()
    val signupResult: LiveData<Boolean> = _signupResult

    // ✅ 이메일 중복 결과
    private val _isEmailDuplicated = MutableLiveData<Boolean?>()
    val isEmailDuplicated: LiveData<Boolean?> = _isEmailDuplicated

    // ✅ 강제 로그아웃 상태
    private val _shouldForceLogout = MutableLiveData<Boolean>()
    val shouldForceLogout: LiveData<Boolean> = _shouldForceLogout

    // (기존 초대코드 관련은 유지)
    private val _inviteCode = MutableLiveData<String?>()
    val inviteCode: LiveData<String?> = _inviteCode

    private val _transferInviteCodeResult = MutableLiveData<Boolean>()
    val transferInviteCodeResult: LiveData<Boolean> = _transferInviteCodeResult

    init {
        _shouldForceLogout.value = authRepository.shouldForceLogout()
    }

    // ✅ 로그인
    fun login(email: String, password: String) {
        viewModelScope.launch {
            val success = authRepository.loginAndGetResponse(email, password)
            _loginResult.value = success
        }
    }

    // ✅ 회원가입
    fun signup(name: String, email: String, password: String) {
        viewModelScope.launch {
            val success = authRepository.signupAndGetResponse(name, email, password)
            _signupResult.value = success
        }
    }

    // ✅ 이메일 중복 확인
    fun checkEmailDuplication(email: String) {
        viewModelScope.launch {
            val result = authRepository.checkEmailDuplication(email)
            _isEmailDuplicated.value = result
        }
    }

    // ✅ 공유자가 초대 코드 요청 후 저장
    fun requestInviteCode(familyName: String) {
        viewModelScope.launch {
            val response = authRepository.requestInviteCode(familyName)
            _inviteCode.value = response?.inviteCode
        }
    }

    // ✅ 피공유자가 초대 코드 전송
    fun transferInviteCode(inviteCode: String) {
        viewModelScope.launch {
            val result = authRepository.transferInviteCode(inviteCode)
            _transferInviteCodeResult.value = result
        }
    }

    // ✅ 피공유자가 수락 여부 확인 (user 정보는 토큰으로 자동 처리됨)
    suspend fun checkInviteStatus(): CheckInviteStatusResponse? {
        return try {
            val inviteCode = tokenManager.getInviteCode() ?: return null
            val request = CheckInviteStatusRequest(inviteCode)
            authRepository.checkInviteStatus(request)
        } catch (e: Exception) {
            Log.e("InviteViewModel", "checkInviteStatus error", e)
            null
        }
    }

    // ✅ 공유자가 수락/거절
    suspend fun acceptGroup(inviteId: String, isAccepted: Boolean): Boolean {
        return try {
            val request = AcceptGroupRequest(inviteId.toString(), isAccepted)
            authRepository.acceptGroup(request)
        } catch (e: Exception) {
            Log.e("InviteViewModel", "acceptGroup error", e)
            false
        }
    }

    fun clearForceLogoutFlag() {
        authRepository.clearForceLogoutFlag()
    }



fun registerDeviceToken(fcmToken: String) {
    viewModelScope.launch {
        try {
            authRepository.registerDeviceToken(fcmToken)
        } catch (e: Exception) {
            Log.e("FCM", "기기 토큰 전송 실패: ${e.message}")
        }
    }
}

}