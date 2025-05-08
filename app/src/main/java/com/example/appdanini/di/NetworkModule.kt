package com.example.appdanini.di

import com.example.appdanini.util.TokenManager
import com.example.appdanini.data.model.remote.api.AuthApi
import com.example.appdanini.data.model.remote.api.ClosenessApi
import com.example.appdanini.util.AuthInterceptor
import com.example.appdanini.util.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

// #2 네트워크 모듈
@Module
@InstallIn(SingletonComponent::class) //@InstallIn(SingletonComponent::class)는 앱 전체에서 공유되는 싱글톤 객체로 등록, //기서 제공하는 Retrofit, OkHttpClient 등은 앱이 실행될 때 한 번만 만들어지고, 필요한 곳에서 계속 재사용돼.
object NetworkModule {

    private const val BASE_URL = "https://ce935491-363e-4a9e-9d36-d95c076c9aef.mock.pstmn.io/"

    @Provides
    @Singleton //네트워크 요청/응답 로그를 확인할 수 있게 해주는 로그 인터셉터.
    fun provideLogginInterceptor(): HttpLoggingInterceptor{
        return HttpLoggingInterceptor().apply{
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    @Provides
    @Singleton //AuthInterceptor 클래스는 요청 헤더에 JWT access token 자동 첨부하는 역할.
    fun provideAuthInterceptor(tokenManager: TokenManager): AuthInterceptor {
        return AuthInterceptor(tokenManager)
    }
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideClosenessApi(retrofit: Retrofit): ClosenessApi { // ✅ 추가
        return retrofit.create(ClosenessApi::class.java)
    }

}



