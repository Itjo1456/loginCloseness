package com.example.appdanini

import android.app.Application
import com.example.appdanini.data.model.remote.TokenManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp // 앱의 시작점 선언
class App : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
