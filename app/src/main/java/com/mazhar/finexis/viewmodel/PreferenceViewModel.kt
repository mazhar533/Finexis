package com.mazhar.finexis.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreferenceViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPrefs = try {
        application.getSharedPreferences("finexis_prefs", Context.MODE_PRIVATE)
    } catch (_: Exception) {
        null
    }

    private val _isDarkMode = MutableStateFlow(sharedPrefs?.getBoolean("dark_mode", true) ?: true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    private val _currency = MutableStateFlow(sharedPrefs?.getString("currency", "PKR") ?: "PKR")
    val currency: StateFlow<String> = _currency

    private val _isBiometricEnabled = MutableStateFlow(sharedPrefs?.getBoolean("biometric_enabled", false) ?: false)
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled

    private val _isOnboardingCompleted = MutableStateFlow(sharedPrefs?.getBoolean("onboarding_completed", false) ?: false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted

    private val _globalToast = MutableStateFlow<Pair<String, Boolean>?>(null)
    val globalToast: StateFlow<Pair<String, Boolean>?> = _globalToast

    fun showToast(message: String, isError: Boolean) {
        _globalToast.value = Pair(message, isError)
    }

    fun clearToast() {
        _globalToast.value = null
    }

    init {
        loadRatesFromCache()
        fetchExchangeRates()
    }

    @SuppressLint("UseKtx")
    fun toggleTheme() {
        val newValue = !_isDarkMode.value
        _isDarkMode.value = newValue
        sharedPrefs?.edit()?.putBoolean("dark_mode", newValue)?.apply()
    }

    @SuppressLint("UseKtx")
    fun setCurrency(currencyCode: String) {
        _currency.value = currencyCode
        sharedPrefs?.edit()?.putString("currency", currencyCode)?.apply()
    }

    @SuppressLint("UseKtx")
    fun toggleBiometric() {
        val newValue = !_isBiometricEnabled.value
        _isBiometricEnabled.value = newValue
        sharedPrefs?.edit()?.putBoolean("biometric_enabled", newValue)?.apply()
        if (!newValue) {
            clearCachedCredentials()
        }
    }

    fun saveCachedCredentials(email: String, pass: String) {
        sharedPrefs?.edit()?.apply {
            putString("cached_email", email)
            putString("cached_pass", pass)
            apply()
        }
    }

    fun getCachedCredentials(): Pair<String, String>? {
        val email = sharedPrefs?.getString("cached_email", null)
        val pass = sharedPrefs?.getString("cached_pass", null)
        if (email != null && pass != null) {
            return Pair(email, pass)
        }
        return null
    }

    fun clearCachedCredentials() {
        sharedPrefs?.edit()?.apply {
            remove("cached_email")
            remove("cached_pass")
            apply()
        }
    }

    @SuppressLint("UseKtx")
    fun completeOnboarding() {
        _isOnboardingCompleted.value = true
        sharedPrefs?.edit()?.putBoolean("onboarding_completed", true)?.apply()
    }

    private fun loadRatesFromCache() {
        val map = mutableMapOf<String, Double>()
        val keys = listOf("pkr", "usd", "eur", "gbp", "inr", "sar", "aed")
        keys.forEach { key ->
            val rate = sharedPrefs?.getFloat("rate_$key", 0.0f)?.toDouble() ?: 0.0
            if (rate > 0.0) {
                map[key] = rate
            }
        }
        if (map.isNotEmpty()) {
            com.mazhar.finexis.ui.utils.CurrencyHelper.updateRates(map)
        }
    }

    private fun saveRatesToCache(rates: Map<String, Double>) {
        sharedPrefs?.edit()?.apply {
            rates.forEach { (cur, rate) ->
                putFloat("rate_$cur", rate.toFloat())
            }
            apply()
        }
    }

    private fun fetchExchangeRates() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = java.net.URL("https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/usd.json")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                
                if (connection.responseCode == 200) {
                    val text = connection.inputStream.bufferedReader().use { it.readText() }
                    val rates = parseRatesFromJson(text)
                    if (rates.isNotEmpty()) {
                        com.mazhar.finexis.ui.utils.CurrencyHelper.updateRates(rates)
                        saveRatesToCache(rates)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun parseRatesFromJson(jsonStr: String): Map<String, Double> {
        val map = mutableMapOf<String, Double>()
        try {
            val jsonObject = org.json.JSONObject(jsonStr)
            if (jsonObject.has("usd")) {
                val usdObject = jsonObject.getJSONObject("usd")
                val keys = listOf("pkr", "usd", "eur", "gbp", "inr", "sar", "aed")
                for (key in keys) {
                    if (usdObject.has(key)) {
                        map[key] = usdObject.getDouble(key)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return map
    }
}
