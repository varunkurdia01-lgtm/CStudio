package com.example.ui.theme

import android.os.Build
import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = GlassPrimary,
    onPrimary = Color.White,
    primaryContainer = GlassPrimary.copy(alpha = 0.1f),
    onPrimaryContainer = GlassPrimary,
    secondary = GlassAccent,
    onSecondary = Color.White,
    background = GlassBackground,
    onBackground = GlassText,
    surface = GlassSurface,
    onSurface = GlassText,
    surfaceVariant = GlassBackground,
    onSurfaceVariant = GlassTextSecondary,
    error = ErrorRed,
    outline = Slate300
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF62D9FF),
    onPrimary = Color(0xFF0B0D12),
    primaryContainer = Color(0xFF62D9FF).copy(alpha = 0.1f),
    onPrimaryContainer = Color(0xFF62D9FF),
    secondary = Color(0xFF9B9CFF),
    onSecondary = Color(0xFF0B0D12),
    background = Color(0xFF0B0D12),
    onBackground = Color(0xFFF4F6FA),
    surface = Color(0xFF11141B),
    onSurface = Color(0xFFF4F6FA),
    surfaceVariant = Color(0xFF171B23),
    onSurfaceVariant = Color(0xFFB7BDC9),
    error = Color(0xFFFF6B7A),
    outline = Color(0xFF2A303B)
)

private val AmoledColorScheme = darkColorScheme(
    primary = Color(0xFF62D9FF),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF62D9FF).copy(alpha = 0.1f),
    onPrimaryContainer = Color(0xFF62D9FF),
    secondary = Color(0xFF9B9CFF),
    onSecondary = Color.Black,
    background = Color.Black,
    onBackground = Color(0xFFF4F6FA),
    surface = Color(0xFF080808),
    onSurface = Color(0xFFF4F6FA),
    surfaceVariant = Color(0xFF101010),
    onSurfaceVariant = Color(0xFFB7BDC9),
    error = Color(0xFFFF6B7A),
    outline = Color(0xFF1C1C1C)
)

private val MidnightColorScheme = darkColorScheme(
    primary = Color(0xFF62D9FF),
    onPrimary = Color(0xFF051024),
    primaryContainer = Color(0xFF62D9FF).copy(alpha = 0.1f),
    onPrimaryContainer = Color(0xFF62D9FF),
    secondary = Color(0xFF9B9CFF),
    onSecondary = Color(0xFF051024),
    background = Color(0xFF051024),
    onBackground = Color(0xFFF4F6FA),
    surface = Color(0xFF0A1931),
    onSurface = Color(0xFFF4F6FA),
    surfaceVariant = Color(0xFF112543),
    onSurfaceVariant = Color(0xFFB7BDC9),
    error = Color(0xFFFF6B7A),
    outline = Color(0xFF1C345C)
)

@Composable
fun MyApplicationTheme(
    themePreference: String = "System Default",
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    
    val colorScheme = when (themePreference) {
        "Light Theme" -> LightColorScheme
        "Dark Mode" -> DarkColorScheme
        "AMOLED Black" -> AmoledColorScheme
        "Midnight Blue" -> MidnightColorScheme
        else -> if (systemDark) DarkColorScheme else LightColorScheme
    }

    val isDark = colorScheme != LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

val androidx.compose.material3.ColorScheme.success: Color
    get() = if (this.background.red < 0.5f) Color(0xFF5FE0A0) else SuccessGreen

