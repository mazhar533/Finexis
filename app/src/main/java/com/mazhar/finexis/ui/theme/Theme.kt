package com.mazhar.finexis.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = FinexisPrimary,
    onPrimary = FinexisOnPrimary,
    background = FinexisBg,
    onBackground = FinexisTextPrimary,
    surface = FinexisSurface,
    onSurface = FinexisTextPrimary,
    secondary = FinexisTextSecondary,
    outline = FinexisBorder
)

private val LightColorScheme = lightColorScheme(
    primary = FinexisPrimary,
    onPrimary = FinexisOnPrimary,
    background = FinexisLightBg,
    onBackground = FinexisLightTextPrimary,
    surface = FinexisLightSurface,
    onSurface = FinexisLightTextPrimary,
    secondary = FinexisLightTextSecondary,
    outline = FinexisLightBorder
)

@Composable
fun FinexisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Follow system default or dynamic override
    dynamicColor: Boolean = false, // Disable dynamic colors to keep signature brand look
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}