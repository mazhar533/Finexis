package com.mazhar.finexis.ui.utils

import java.util.Locale

object CurrencyHelper {
    // Default fallback rates against 1 USD
    private val ratesMap = java.util.concurrent.ConcurrentHashMap<String, Double>().apply {
        put("pkr", 280.0)
        put("usd", 1.0)
        put("eur", 0.92)
        put("gbp", 0.78)
        put("inr", 83.5)
        put("sar", 3.75)
        put("aed", 3.67)
    }

    fun updateRates(newRates: Map<String, Double>) {
        newRates.forEach { (cur, rate) ->
            ratesMap[cur.lowercase()] = rate
        }
    }

    private fun getUsdToCurRate(currency: String): Double {
        return ratesMap[currency.lowercase()] ?: 1.0
    }

    private fun getUsdToPkrRate(): Double {
        return ratesMap["pkr"] ?: 280.0
    }

    /**
     * Convert from base currency (PKR) to the active selected currency.
     */
    fun convertPkrToActive(amount: Double, currency: String): Double {
        val usdToPkr = getUsdToPkrRate()
        val usdToCur = getUsdToCurRate(currency)
        if (usdToPkr == 0.0) return 0.0
        return amount * (usdToCur / usdToPkr)
    }

    /**
     * Convert from the active selected currency to the base currency (PKR).
     */
    fun convertActiveToPkr(amount: Double, currency: String): Double {
        val usdToPkr = getUsdToPkrRate()
        val usdToCur = getUsdToCurRate(currency)
        if (usdToCur == 0.0) return 0.0
        return amount * (usdToPkr / usdToCur)
    }

    /**
     * Get the currency symbol.
     */
    fun getSymbol(currency: String): String {
        return when (currency.uppercase()) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "INR" -> "₹"
            "SAR" -> "SR "
            "AED" -> "AED "
            else -> "Rs "
        }
    }

    /**
     * Format a base PKR amount into the active currency representation.
     */
    fun format(amount: Double, currency: String, showDecimal: Boolean = false): String {
        val converted = convertPkrToActive(amount, currency)
        val symbol = getSymbol(currency)
        val formatted = if (showDecimal) {
            String.format(Locale.US, "%.2f", converted)
        } else {
            String.format(Locale.US, "%.0f", converted)
        }
        return if (symbol.endsWith(" ")) "$symbol$formatted" else "$symbol$formatted"
    }

    /**
     * Format a signed base PKR amount (e.g. +$12 or -Rs 3400).
     */
    fun formatSigned(amount: Double, currency: String, isIncome: Boolean, showDecimal: Boolean = false): String {
        val sign = if (isIncome) "+" else "-"
        return "$sign${format(amount, currency, showDecimal)}"
    }
}
