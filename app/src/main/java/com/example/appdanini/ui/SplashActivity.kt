package com.example.appdanini.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.appdanini.R
import com.example.appdanini.ui.auth.AuthActivity
import com.example.appdanini.util.TokenManager
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 🔥 FCM 토큰 가져오기
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("FCM Token", "FCM 토큰: $token")
                } else {
                    Log.e("FCM Token", "토큰 가져오기 실패: ${task.exception}")
                }
            }

        // 🔥 3초 후 초기 상태 분기
        Handler(Looper.getMainLooper()).postDelayed({
            val tokenManager = TokenManager(this)
            val intent = if (tokenManager.isInitialSetupDone()) {
                Intent(this, HomeActivity::class.java)
            } else {
                Intent(this, AuthActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, 3000)
    }
}

//
//FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//    if (task.isSuccessful) {
//        val fcmToken = task.result
//        Log.d("FCM Token", "기기 토큰: $fcmToken")
//
//        // TODO: 서버로 fcmToken 전송하는 함수 호출
//        authViewModel.registerDeviceToken(fcmToken)
//    }
//}

