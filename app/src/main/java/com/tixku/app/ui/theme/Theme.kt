package com.tixku.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TixkuRed = Color(0xFFE53935)
val TixkuRedDark = Color(0xFFB71C1C)
val TixkuAmber = Color(0xFFFFC107)

private val LightColors = lightColorScheme(
    primary = TixkuRed,
    onPrimary = Color.White,
    secondary = TixkuRedDark,
    tertiary = TixkuAmber,
    background = Color(0xFFFAFAFA),
    surface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = TixkuRed,
    onPrimary = Color.White,
    secondary = TixkuAmber,
    tertiary = TixkuAmber,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
)

@Composable
fun TixkuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography,
        content = content,
    )
}
