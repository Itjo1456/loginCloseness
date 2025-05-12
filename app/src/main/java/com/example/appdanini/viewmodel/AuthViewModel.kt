package com.example.appdanini.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdanini.util.TokenManager
import com.example.appdanini.data.model.repository.AuthRepository
import com.example.appdanini.data.model.request.invite.AcceptGroupRequest
import com.example.appdanini.data.model.request.invite.TransferInviteResponse
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private val _inviteStatus = MutableLiveData<TransferInviteResponse?>()
    val inviteStatus: LiveData<TransferInviteResponse?> get() = _inviteStatus
    init {
        _shouldForceLogout.value = authRepository.shouldForceLogout()
    }

    private var monitoringJob: Job? = null

    // ✅ 로그인
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val success = authRepository.loginAndGetResponse(email, password)
                _loginResult.value = success

                if (success) {
                    // 로그인 성공 후 FCM 토큰 전송
                    FirebaseMessaging.getInstance().token
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val token = task.result
                                registerDeviceToken(token)
                            } else {
                                Log.e("AuthViewModel", "FCM 토큰 가져오기 실패: ${task.exception}")
                            }
                        }
                }

            } catch (e: Exception) {
                Log.e("AuthViewModel", "login exception: ${e.localizedMessage}", e)
                _loginResult.value = false
            }
        }
    }


    // ✅ 회원가입
    fun signup(name: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val success = authRepository.signupAndGetResponse(name, email, password)
                _signupResult.value = success
            } catch (e: Exception) {
                Log.e("AuthViewModel", "signup exception: ${e.localizedMessage}", e)
                _signupResult.value = false
            }
        }
    }

    // ✅ 이메일 중복 확인
    fun checkEmailDuplication(email: String) {
        viewModelScope.launch {
            try {
                val result = authRepository.checkEmailDuplication(email)
                _isEmailDuplicated.value = result
            } catch (e: Exception) {
                Log.e("AuthViewModel", "checkEmailDuplication exception: ${e.localizedMessage}", e)
                _isEmailDuplicated.value = false // 예외 발생 시 중복 아님으로 처리
            }
        }
    }

    suspend fun refreshAccessToken(refreshToken: String) =
        try {
            authRepository.refreshAccessToken(refreshToken)
        } catch (e: Exception) {
            null
        }

    // ✅ 공유자가 초대 코드 요청 후 저장
    fun requestInviteCode(familyName: String) {
        viewModelScope.launch {
            try {
                val response = authRepository.requestInviteCode(familyName)
                val code = response?.inviteCode
                _inviteCode.value = code
            } catch (e: Exception) {
                Log.e("AuthViewModel", "초대코드 요청 실패: ${e.localizedMessage}", e)
                _inviteCode.value = null  // 실패 시 명확하게 null 처리
            }
        }
    }

    // ✅ 피공유자가 초대 코드 전송
    fun transferInviteCode(inviteCode: String) {
        viewModelScope.launch {
            try {
                val result = authRepository.transferInviteCode(inviteCode)
                _transferInviteCodeResult.value = result
            } catch (e: Exception) {
                Log.e("AuthViewModel", "초대 코드 전송 실패: ${e.localizedMessage}", e)
                _transferInviteCodeResult.value = false
            }
        }
    }

    // ✅ 피공유자가 수락 여부 확인 (user 정보는 토큰으로 자동 처리됨)
    suspend fun checkInviteStatus(): TransferInviteResponse? {
        return try {
            val result = authRepository.checkInviteStatus()
            _inviteStatus.value = result
            result
        } catch (e: Exception) {
            _inviteStatus.value = null
            null
        }
    }


    // ✅ 공유자가 수락/거절
    suspend fun acceptGroup(isAccepted: Boolean, requestId: Int): Boolean {
        return authRepository.acceptGroup(requestId, isAccepted)
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

    fun monitorForceLogout() {
        if (monitoringJob?.isActive == true) return

        monitoringJob = viewModelScope.launch {
            while (true) {
                if (authRepository.shouldForceLogout()) {
                    _shouldForceLogout.postValue(true)
                    break
                }
                delay(2000) // 2초마다 감시
            }
        }
    }

}