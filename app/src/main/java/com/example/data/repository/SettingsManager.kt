package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettings(
    val geminiApiKey: String,
    val geminiModel: String = "gemini-2.5-flash",
    val googleMapsApiKey: String = "",
    val defaultSearchRadiusKm: Int = 10,
    val defaultResultLimit: Int = 20,
    val highPotentialThreshold: Int = 80,
    val isDemoMode: Boolean = false
)

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("leadfinder_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        val defaultKey = try {
            if (BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY") BuildConfig.GEMINI_API_KEY else ""
        } catch (_: Exception) {
            ""
        }
        val savedKey = prefs.getString("gemini_api_key", defaultKey) ?: defaultKey
        val model = prefs.getString("gemini_model", "gemini-2.5-flash") ?: "gemini-2.5-flash"
        val mapsKey = prefs.getString("google_maps_api_key", "") ?: ""
        val radius = prefs.getInt("default_search_radius", 10)
        val limit = prefs.getInt("default_result_limit", 20)
        val threshold = prefs.getInt("high_potential_threshold", 80)
        // If no api key is available, default demo mode to true so user immediately sees live capabilities
        val demo = prefs.getBoolean("is_demo_mode", savedKey.isBlank() && mapsKey.isBlank())

        return AppSettings(
            geminiApiKey = savedKey,
            geminiModel = model,
            googleMapsApiKey = mapsKey,
            defaultSearchRadiusKm = radius,
            defaultResultLimit = limit,
            highPotentialThreshold = threshold,
            isDemoMode = demo
        )
    }

    fun updateSettings(
        geminiApiKey: String? = null,
        geminiModel: String? = null,
        googleMapsApiKey: String? = null,
        defaultRadius: Int? = null,
        defaultLimit: Int? = null,
        highPotentialThreshold: Int? = null,
        isDemoMode: Boolean? = null
    ) {
        val editor = prefs.edit()
        val current = _settings.value

        geminiApiKey?.let { editor.putString("gemini_api_key", it) }
        geminiModel?.let { editor.putString("gemini_model", it) }
        googleMapsApiKey?.let { editor.putString("google_maps_api_key", it) }
        defaultRadius?.let { editor.putInt("default_search_radius", it) }
        defaultLimit?.let { editor.putInt("default_result_limit", it) }
        highPotentialThreshold?.let { editor.putInt("high_potential_threshold", it) }
        isDemoMode?.let { editor.putBoolean("is_demo_mode", it) }

        editor.apply()

        _settings.value = AppSettings(
            geminiApiKey = geminiApiKey ?: current.geminiApiKey,
            geminiModel = geminiModel ?: current.geminiModel,
            googleMapsApiKey = googleMapsApiKey ?: current.googleMapsApiKey,
            defaultSearchRadiusKm = defaultRadius ?: current.defaultSearchRadiusKm,
            defaultResultLimit = defaultLimit ?: current.defaultResultLimit,
            highPotentialThreshold = highPotentialThreshold ?: current.highPotentialThreshold,
            isDemoMode = isDemoMode ?: current.isDemoMode
        )
    }

    fun getEffectiveGeminiKey(): String {
        val key = _settings.value.geminiApiKey.trim()
        if (key.isNotEmpty() && key != "MY_GEMINI_API_KEY") return key
        return try {
            if (BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY") BuildConfig.GEMINI_API_KEY else ""
        } catch (_: Exception) {
            ""
        }
    }
}
