package com.mazhar.finexis.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreferenceViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPrefs = application.getSharedPreferences("finexis_prefs", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(sharedPrefs.getBoolean("dark_mode", true))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    private val _currency = MutableStateFlow(sharedPrefs.getString("currency", "PKR") ?: "PKR")
    val currency: StateFlow<String> = _currency

    private val _isBiometricEnabled = MutableStateFlow(sharedPrefs.getBoolean("biometric_enabled", false))
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled

    fun toggleTheme() {
        val newValue = !_isDarkMode.value
        _isDarkMode.value = newValue
        sharedPrefs.edit().putBoolean("dark_mode", newValue).apply()
    }

    fun setCurrency(currencyCode: String) {
        _currency.value = currencyCode
        sharedPrefs.edit().putString("currency", currencyCode).apply()
    }

    fun toggleBiometric() {
        val newValue = !_isBiometricEnabled.value
        _isBiometricEnabled.value = newValue
        sharedPrefs.edit().putBoolean("biometric_enabled", newValue).apply()
    }
}
