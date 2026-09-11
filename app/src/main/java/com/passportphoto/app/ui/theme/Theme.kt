package com.passportphoto.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = SurfaceWhite,
    secondary = AccentTeal,
    background = BackgroundLight,
    surface = SurfaceWhite,
    onBackground = TextDark,
    onSurface = TextDark,
    error = ErrorRed
)

private val DarkColors = darkColorScheme(
    primary = AccentTeal,
    onPrimary = TextDark,
    secondary = PrimaryBlue,
    background = PrimaryBlueDark,
    surface = PrimaryBlueDark,
    error = ErrorRed
)

@Composable
fun PassportPhotoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
