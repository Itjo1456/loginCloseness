package com.example.appdanini.data.model.remote.api

import com.example.appdanini.data.model.request.invite.AcceptGroupRequest
import com.example.appdanini.data.model.request.invite.InviteCodeRequest
import com.example.appdanini.data.model.request.invite.InviteCodeResponse
import com.example.appdanini.data.model.request.auth.LoginRequest
import com.example.appdanini.data.model.request.auth.SignupRequest
import com.example.appdanini.data.model.request.invite.TransferInviteRequest
import com.example.appdanini.data.model.request.auth.EmailCheckRequest
import com.example.appdanini.data.model.request.auth.EmailCheckResponse
import com.example.appdanini.data.model.request.auth.LoginResponse
import com.example.appdanini.data.model.request.invite.AcceptGroupResponse
import com.example.appdanini.data.model.request.invite.FcmTokenRequest
import com.example.appdanini.data.model.request.invite.TransferInviteResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface AuthApi {
        // #1 회원가입 (인증 불필요)
        // 확인
        @POST("/api/v1/users/signup")
        suspend fun signup(@Body req: SignupRequest): Response<Void>

        // #2 로그인 (인증 불필요)
        // 확인, 이때 받는 이름과 이메일은 저장
        // 그룹 아이디는 널값으로 받을 수 있음을 인지하기
        @POST("/api/v1/users/login")
        suspend fun login(@Body req: LoginRequest): Response<LoginResponse>


        // #3 이메일 중복 확인 (인증 불필요)
        // # 확인
        @POST("/api/v1/users/check-email")
        suspend fun checkEmail(@Body request: EmailCheckRequest): Response<EmailCheckResponse>


        // #4 토큰 리프레시
        // # 확인
        @POST("/api/v1/auth/refresh")
        suspend fun refreshAccessToken(
                @Header("Authorization") refreshToken: String
        ): Response<Unit>


        // ========== 인증 필요한 API에는 모두 Need-Auth 추가 ==========

        //#5 초대코드 생성
        //# 확인
        @Headers("Need-Auth: true")
        @POST("/api/v1/groups")
        suspend fun requestInviteCode(@Body req : InviteCodeRequest) : Response<InviteCodeResponse>

        // #6 초대 코드 전송
        // # 확인
        @Headers("Need-Auth: true")
        @POST("/api/v1/groups/join-request/me")
        suspend fun transferInviteCode(@Body req: TransferInviteRequest): Response<TransferInviteResponse>


        // #7 초대 요청 상태 체크(피공유자, 폴링)
        // # 수정 필요
        @Headers("Need-Auth: true")
        @GET("/api/v1/groups/request_state")
        suspend fun checkInviteStatus(): Response<TransferInviteResponse>

        // fcm 메세지를 이용하면 request_id를 받고, 보내주는 것도 가능함.

        // #8 공유자 가입 수락
        @Headers("Need-Auth: true")
        @POST("/api/v1/groups/accept/{requestId}")
        suspend fun acceptGroup(
                @Path("request_id") requestId: Int,
                @Body request: AcceptGroupRequest
        ): Response<AcceptGroupResponse>

        // #9 디바이스 토큰 등록
        @Headers("Need-Auth: true")
        @POST("/api/v1/users/device-token")
        suspend fun registerDeviceToken(@Body request: FcmTokenRequest): Response<Unit>
}