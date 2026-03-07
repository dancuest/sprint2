package com.example.animedev20.ui.theme.data.remote

import com.example.animedev20.ui.theme.domain.model.UserProfile
import retrofit2.http.GET

@Deprecated("Use AuthApiPlain + UsersApi for backend integration")
interface AuthApi {
    @GET("auth/me")
    suspend fun getMe(): ApiResponse<UserProfile>
}
