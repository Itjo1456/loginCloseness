package com.example.appdanini.ui
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.appdanini.R
import com.example.appdanini.ui.auth.AuthActivity
import com.example.appdanini.ui.invite.FamilyNameFragmentDirections
import com.example.appdanini.util.TokenManager
import com.example.appdanini.viewmodel.AuthViewModel
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    @Inject lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        lifecycleScope.launch {
            val sessionValid = tryRefreshIfNeeded()

            if (!sessionValid) {
                tokenManager.clearSession()  // ✅ 전체 초기화
                goToLogin()
                return@launch
            }

            if (tokenManager.isInitialSetupDone()) {
                goToHome()
            } else {
                handleInitialSetupFlow()
            }
        }
    }

    private suspend fun tryRefreshIfNeeded(): Boolean {
        if (tokenManager.shouldForceLogout()) return false

        val expired = tokenManager.isAccessTokenExpired()
        val refreshToken = tokenManager.getRefreshToken()

        if (!expired) return true
        if (refreshToken.isNullOrEmpty()) return false

        return try {
            val response = authViewModel.refreshAccessToken(refreshToken)
            val newToken = response?.headers()?.get("Authorization")?.removePrefix("Bearer ")

            if (!newToken.isNullOrBlank()) {
                tokenManager.saveTokensOnly(newToken, refreshToken)
                tokenManager.clearForceLogoutFlag()
                true
            } else {
                tokenManager.setForceLogout(true)
                false
            }
        } catch (e: Exception) {
            tokenManager.setForceLogout(true)
            false
        }
    }

    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun goToLogin() {
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }

    private fun goToClosenessIntro() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.putExtra("start_destination", R.id.closenessFragment)
        startActivity(intent)
        finish()
    }

    private fun goToFamilyInvitePage() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.putExtra("start_destination", R.id.inviteFragment)
        startActivity(intent)
        finish()
    }

    private fun goToWaitingPage() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.putExtra("start_destination", R.id.waitInviteFragment)
        startActivity(intent)
        finish()
    }
    private fun handleInitialSetupFlow() {
        val hasFamilyName = !tokenManager.getFamilyName().isNullOrBlank()
        val hasInviteCode = !tokenManager.getInviteCode().isNullOrBlank()

        when {
            hasFamilyName && hasInviteCode -> {
                goToClosenessIntro()
            }

            hasInviteCode && !hasFamilyName -> {
                lifecycleScope.launch {
                    val inviteStatus = authViewModel.checkInviteStatus()
                    when (inviteStatus?.status) {
                        "APPROVED" -> goToHome()
                        "WAITING", null -> goToWaitingPage()
                        "REJECTED" -> {
                            tokenManager.clearSession()
                            goToFamilyInvitePage()
                        }
                    }
                }
            }

            else -> {
                goToFamilyInvitePage()
            }
        }
    }
}