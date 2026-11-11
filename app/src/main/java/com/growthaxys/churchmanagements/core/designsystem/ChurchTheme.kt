package com.growthaxys.churchmanagements.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Background = Color(0xFF0B0D12)
val Surface = Color(0xFF1A1D24)
val Cyan = Color(0xFF00E5FF)
val Purple = Color(0xFF7C5CFF)
val Muted = Color(0xFF9AA4B2)
val Border = Color(0x24FFFFFF)

private val ChurchColors = darkColorScheme(
    primary = Cyan,
    onPrimary = Background,
    secondary = Purple,
    background = Background,
    onBackground = Color.White,
    surface = Surface,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF242832),
    onSurfaceVariant = Muted,
    outline = Border,
    error = Color(0xFFFF5D6C)
)

@Composable
fun ChurchTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ChurchColors, content = content)
}
