package com.example.animedev20.ui.theme.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

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

        /**
         * El detalle del anime puede tardar más porque el backend traduce
         * la sinopsis antes de responder. Sin estos timeouts, OkHttp puede
         * cortar la llamada antes de que llegue la respuesta traducida.
         */
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
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