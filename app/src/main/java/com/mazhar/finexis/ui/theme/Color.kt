package com.mazhar.finexis.ui.theme

import androidx.compose.ui.graphics.Color

// Finexis Dark Theme Color Palette
val FinexisBg = Color(0xFF070A13)         // Very deep dark blue/black background
val FinexisSurface = Color(0xFF101524)    // Slightly lighter dark color for cards & text fields
val FinexisPrimary = Color(0xFF00A86B)    // Emerald Green primary brand color
val FinexisOnPrimary = Color(0xFFFFFFFF)  // White text on primary buttons
val FinexisTextPrimary = Color(0xFFFFFFFF)// Main header & body text
val FinexisTextSecondary = Color(0xFF8E9CAE)// Muted text color for labels & subtitles
val FinexisBorder = Color(0xFF1E2640)     // Thin borders for input fields
val FinexisIncome = Color(0xFF2ECC71)     // Green for income amounts
val FinexisExpense = Color(0xFFE74C3C)    // Red for expense amounts

// Finexis Light Theme Color Palette
val FinexisLightBg = Color(0xFFF8FAFC)         // Slate 50 (Very light grey/white background)
val FinexisLightSurface = Color(0xFFFFFFFF)    // White card surface & text fields
val FinexisLightTextPrimary = Color(0xFF0F172A)// Slate 900 (Dark slate text)
val FinexisLightTextSecondary = Color(0xFF64748B)// Slate 500 (Muted grey text)
val FinexisLightBorder = Color(0xFFE2E8F0)     // Slate 200 (Light grey borders)

// Legacy colors (if needed by previews/templates, mapped to new brand colors)
val Purple80 = FinexisPrimary
val PurpleGrey80 = FinexisTextSecondary
val Pink80 = FinexisIncome

val Purple40 = FinexisPrimary
val PurpleGrey40 = FinexisTextSecondary
val Pink40 = FinexisExpense