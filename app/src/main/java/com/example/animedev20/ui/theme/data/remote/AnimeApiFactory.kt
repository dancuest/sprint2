package com.example.animedev20.ui.theme.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AnimeApiFactory {
    fun createRetrofit(baseUrl: String, tokenStore: AuthTokenStore): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = okhttp3.Interceptor { chain ->
            val request = chain.request()
            val path = request.url.encodedPath
            val shouldSkipAuth = path.contains("/auth/device")
            val token = tokenStore.getToken()

            val authenticatedRequest = if (!shouldSkipAuth && !token.isNullOrBlank()) {
                request.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else {
                request
            }

            chain.proceed(authenticatedRequest)
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

        val normalizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        return Retrofit.Builder()
            .baseUrl(normalizedBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun create(baseUrl: String, tokenStore: AuthTokenStore): AnimeApi {
        return createRetrofit(baseUrl, tokenStore).create(AnimeApi::class.java)
    }
}
