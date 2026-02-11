package com.example.animedev20.ui.theme.data.remote

import android.os.Build
import java.util.Locale

object ApiConfig {
    private const val ANDROID_EMULATOR_BASE_URL = "http://10.0.2.2:3000"
    private const val GENYMOTION_BASE_URL = "http://10.0.3.2:3000"

    private const val DEVICE_BASE_URL = "http://192.168.1.100:3000"

    val baseUrl: String
        get() = when {
            isGenymotion() -> GENYMOTION_BASE_URL
            isEmulator() -> ANDROID_EMULATOR_BASE_URL
            else -> DEVICE_BASE_URL
        }

    private fun isGenymotion(): Boolean =
        Build.MANUFACTURER.contains("Genymotion", ignoreCase = true)

    private fun isEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT.lowercase(Locale.ROOT)
        val model = Build.MODEL.lowercase(Locale.ROOT)
        val product = Build.PRODUCT.lowercase(Locale.ROOT)
        val brand = Build.BRAND.lowercase(Locale.ROOT)
        val device = Build.DEVICE.lowercase(Locale.ROOT)
        val hardware = Build.HARDWARE.lowercase(Locale.ROOT)

        return fingerprint.startsWith("generic")
                || fingerprint.startsWith("unknown")
                || fingerprint.contains("emulator")
                || model.contains("emulator")
                || model.contains("android sdk built for")
                || product.contains("sdk")
                || product.contains("sdk_gphone")
                || product.contains("emulator")
                || product.contains("simulator")
                || hardware.contains("ranchu")
                || hardware.contains("goldfish")
                || (brand.startsWith("generic") && device.startsWith("generic"))
    }
}
