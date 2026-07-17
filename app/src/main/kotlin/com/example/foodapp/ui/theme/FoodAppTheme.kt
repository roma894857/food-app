package com.example.foodapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import android.os.Build
import android.view.View

// Color scheme definitions
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF1F1F1F),
    surface = Color(0xFF1F1F1F),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFF2F2F2),
    surface = Color(0xFFF2F2F2),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF111111),
    onSurface = Color(0xFF111111),
)

@Composable
fun FoodAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicColorScheme(context, darkTheme) else dynamicColorScheme(context, darkTheme)
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

@Composable
private fun isSystemInDarkTheme() = false // TODO: Implement actual system theme detection

private fun dynamicColorScheme(context: Context, darkTheme: Boolean) = if (darkTheme) DarkColorScheme else LightColorScheme

// Color constants
object AppColors {
    val Purple40 = Color(0xFF6750A4)
    val Purple80 = Color(0xFFd0bcff)
    val PurpleGrey40 = Color(0xFF625b71)
    val PurpleGrey80 = Color(0xFFccbedd)
    val Pink40 = Color(0xFF7d5260)
    val Pink80 = Color(0xFFffb8b4)

    // App-specific colors
    val PrimaryGreen = Color(0xFF4CAF50)
    val ErrorRed = Color(0xFFDC143C)
    val BackgroundLight = Color(0xFFFFFFFF)
    val SurfaceLight = Color(0xFFF5F5F5)
    val BackgroundDark = Color(0xFF121212)
    val SurfaceDark = Color(0xFF1E1E1E)
}

// Typography
object Typography {
    // Add custom typography here if needed from font resources
}