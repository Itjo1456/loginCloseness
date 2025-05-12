package com.example.appdanini.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.example.appdanini.R
import com.example.appdanini.databinding.ActivityAuthBinding
import com.example.appdanini.util.TokenManager
import com.example.appdanini.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ force logout 감시
        observeForceLogout()

        // 🔥 start_destination 처리
        val startDest = intent.getIntExtra("start_destination", -1)
        if (startDest != -1) {
            val navHost = supportFragmentManager.findFragmentById(R.id.container_auth) as? NavHostFragment
            navHost?.navController?.navigate(startDest)
        }
    }

    private fun observeForceLogout() {
        authViewModel.shouldForceLogout.observe(this, Observer { shouldLogout ->
            if (shouldLogout) {
                Toast.makeText(this, "세션이 만료되었습니다. 다시 로그인해주세요.", Toast.LENGTH_LONG).show()
                tokenManager.clearTokens()
                authViewModel.clearForceLogoutFlag()

                val intent = Intent(this, AuthActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
        })
    }
}
