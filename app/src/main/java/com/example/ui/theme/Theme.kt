package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.data.preferences.AppThemeMode

private val LightColorScheme = lightColorScheme(
    primary = SaffronPrimary,
    onPrimary = ChandanWhite,
    primaryContainer = SacredCreamSurfaceVariant,
    onPrimaryContainer = SaffronPrimaryDark,
    secondary = SacredMaroon,
    onSecondary = ChandanWhite,
    secondaryContainer = SacredCreamSurfaceVariant,
    onSecondaryContainer = SacredSoftMaroon,
    tertiary = SacredGold,
    onTertiary = SacredTextPrimary,
    background = SacredCreamBackground,
    onBackground = SacredTextPrimary,
    surface = SacredCreamSurface,
    onSurface = SacredTextPrimary,
    surfaceVariant = SacredCreamSurfaceVariant,
    onSurfaceVariant = SacredTextSecondary,
    outline = SacredDivider
)

private val DarkColorScheme = darkColorScheme(
    primary = SaffronPrimaryLight,
    onPrimary = SacredDarkBackground,
    primaryContainer = SacredDarkSurfaceVariant,
    onPrimaryContainer = ChandanWhite,
    secondary = SacredSoftMaroon,
    onSecondary = ChandanWhite,
    secondaryContainer = SacredDarkSurfaceVariant,
    onSecondaryContainer = SacredDarkTextPrimary,
    tertiary = HaldiYellow,
    onTertiary = SacredDarkBackground,
    background = SacredDarkBackground,
    onBackground = SacredDarkTextPrimary,
    surface = SacredDarkSurface,
    onSurface = SacredDarkTextPrimary,
    surfaceVariant = SacredDarkSurfaceVariant,
    onSurfaceVariant = SacredDarkTextSecondary,
    outline = SacredDarkDivider
)

@Composable
fun MantraJapTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
