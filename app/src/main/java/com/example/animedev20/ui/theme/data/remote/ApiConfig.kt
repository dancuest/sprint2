package com.example.animedev20.ui.theme.data.remote

import android.os.Build

object ApiConfig {
    private const val EMULATOR_BASE_URL = "http://10.0.2.2:3000"
    private const val DEVICE_BASE_URL = "http://localhost:3000"

    val baseUrl: String
        get() = if (isEmulator()) EMULATOR_BASE_URL else DEVICE_BASE_URL

    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
            || "google_sdk" == Build.PRODUCT)
    }
}
