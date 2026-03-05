package com.example.animedev20.ui.theme.data.remote

import com.example.animedev20.ui.theme.data.remote.session.AuthInterceptor
import com.example.animedev20.ui.theme.data.remote.session.AuthTokenStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object AnimeApiFactory {
    fun create(baseUrl: String): AnimeApi {
        return createRetrofit(baseUrl = baseUrl, tokenStore = null)
            .create(AnimeApi::class.java)
    }

    fun createRetrofit(
        tokenStore: AuthTokenStore,
        baseUrl: String = ApiConfig.baseUrl
    ): Retrofit {
        return createRetrofit(baseUrl = baseUrl, tokenStore = tokenStore)
    }

    private fun createRetrofit(
        baseUrl: String,
        tokenStore: AuthTokenStore?
    ): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpBuilder = OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)

        tokenStore?.let {
            okHttpBuilder.addInterceptor(AuthInterceptor(it))
        }

        val normalizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        return Retrofit.Builder()
            .baseUrl(normalizedBaseUrl)
            .client(okHttpBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
