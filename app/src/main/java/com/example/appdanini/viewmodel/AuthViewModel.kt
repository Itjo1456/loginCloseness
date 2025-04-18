package com.example.appdanini.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appdanini.data.model.repository.AuthRepository
import com.example.appdanini.data.model.request.LoginResponse
import com.example.appdanini.data.model.request.SignupResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    private val _signupResult = MutableLiveData<Boolean>()
    val signupResult: LiveData<Boolean> = _signupResult

    private val _shouldForceLogout = MutableLiveData<Boolean>()
    val shouldForceLogout: LiveData<Boolean> = _shouldForceLogout

    private val _loginResponse = MutableLiveData<LoginResponse?>()
    val loginResponse: LiveData<LoginResponse?> = _loginResponse

    private val _signResponse = MutableLiveData<SignupResponse?>()
    val signResponse: LiveData<SignupResponse?> = _signResponse

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val response = authRepository.loginAndGetResponse(email, password)
            _loginResponse.value = response
            _loginResult.value = response != null
        }
    }

    fun signup(username: String, email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.signup(username, email, password)
            _signupResult.value = result
        }
    }

    fun checkForceLogout() {
        val isLogoutRequired = authRepository.shouldForceLogout()
        _shouldForceLogout.value = isLogoutRequired
        if (isLogoutRequired) {
            authRepository.clearTokens()
            authRepository.clearForceLogoutFlag()
        }
    }
}