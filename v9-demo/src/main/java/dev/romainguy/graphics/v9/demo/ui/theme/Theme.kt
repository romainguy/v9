package dev.romainguy.graphics.v9.demo.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorPalette = lightColors(
    primary = Color(0xFF9DECFC),
    primaryVariant = Color(0xFFC4C4C4),
    secondary = Color(0xFF9DECFC),
    secondaryVariant = Color(0xFFC4C4C4),

    background = Color(0xFF3E4757),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    onPrimary = Color(0xFF1A1B1E),
    onSecondary = Color(0xFF000000),
)

@Composable
fun V9Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = LightColorPalette,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}