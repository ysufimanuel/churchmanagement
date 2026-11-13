package com.growthaxys.churchmanagements.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.ProvideTextStyle
import com.growthaxys.churchmanagements.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * Design tokens extracted from the web app (`tailwind.config.ts` + `globals.css`).
 * The web is dark-only (`color-scheme: dark`), so Android is dark-only too.
 */
object Tokens {
    // hsl vars from :root
    val Background = Color(0xFF0B0D12)         // body bg
    val Foreground = Color(0xFFFFFFFF)
    val Card = Color(0xFF1C202B)               // --card 222 24% 14%
    val Primary = Color(0xFF00E5FF)            // --primary 187 100% 50%
    val OnPrimary = Color(0xFF0D0F16)          // --primary-foreground 222 25% 7%
    val Secondary = Color(0xFF2D3343)          // --secondary 222 20% 22%
    val OnSecondary = Color(0xFFE2E8F0)        // --secondary-foreground 214 32% 91%
    val Muted = Color(0xFF282D3C)              // --muted 222 20% 20%
    val MutedForeground = Color(0xFFA3B0C2)    // --muted-foreground 214 20% 70%
    val Destructive = Color(0xFFDC2626)        // --destructive 0 72% 51%
    val Success = Color(0xFF10B981)

    // glass surfaces (white alpha over background)
    val Border = Color(0x24FFFFFF)             // rgb(255 255 255 / .14)
    val BorderSubtle = Color(0x1FFFFFFF)       // .12
    val InputBg = Color(0x0FFFFFFF)            // .06
    val InputBorder = Color(0x29FFFFFF)        // .16
    val GlassTop = Color(0x1AFFFFFF)           // .10
    val GlassBottom = Color(0x0AFFFFFF)        // .04
    val GlassStrongTop = Color(0x29FFFFFF)     // .16
    val GlassStrongBottom = Color(0x12FFFFFF)  // .07
    val GlassFallback = Color(0xD11A1D24)      // rgb(26 29 36 / .82)

    // status/accent colors used in web (tailwind palette)
    val Emerald400 = Color(0xFF34D399)
    val Red400 = Color(0xFFF87171)
    val Amber400 = Color(0xFFFBBF24)
    val Orange400 = Color(0xFFFB923C)
    val Blue400 = Color(0xFF60A5FA)
    val Purple400 = Color(0xFFC084FC)

    val ChartColors = listOf(
        Color(0xFF00E5FF), Color(0xFF3B82F6), Color(0xFF10B981),
        Color(0xFFF59E0B), Color(0xFF8B5CF6), Color(0xFFEF4444),
    )
    val ChartAxis = Color(0xFFA0AEC0)

    // radius scale: --radius 0.875rem = 14dp; chip 10, badge 12, card 16
    val RadiusLg = 14.dp
    val RadiusMd = 12.dp
    val RadiusSm = 10.dp
    val RadiusCard = 16.dp
    val CardShape = RoundedCornerShape(RadiusCard)
    val ControlShape = RoundedCornerShape(RadiusLg)
    val ChipShape = RoundedCornerShape(RadiusSm)
}

private val ColorScheme = darkColorScheme(
    primary = Tokens.Primary,
    onPrimary = Tokens.OnPrimary,
    secondary = Tokens.Secondary,
    onSecondary = Tokens.OnSecondary,
    background = Tokens.Background,
    onBackground = Tokens.Foreground,
    surface = Tokens.Card,
    onSurface = Tokens.Foreground,
    surfaceVariant = Tokens.Muted,
    onSurfaceVariant = Tokens.MutedForeground,
    error = Tokens.Destructive,
    onError = Color.White,
    outline = Tokens.Border,
)

/** Web `default-web.css`: "Plus Jakarta Sans" 400–800 (variable font bundled in res/font). */
val PlusJakartaSans = FontFamily(
    listOf(400, 500, 600, 700, 800).map { w ->
        Font(R.font.plus_jakarta_sans, FontWeight(w), variationSettings = FontVariation.Settings(FontVariation.weight(w)))
    }
)

private val Base = Typography(
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 18.sp),
    titleLarge = TextStyle(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    labelSmall = TextStyle(fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium),
)

private val AppTypography = with(Base) {
    copy(
        displayLarge = displayLarge.copy(fontFamily = PlusJakartaSans), displayMedium = displayMedium.copy(fontFamily = PlusJakartaSans),
        displaySmall = displaySmall.copy(fontFamily = PlusJakartaSans), headlineLarge = headlineLarge.copy(fontFamily = PlusJakartaSans),
        headlineMedium = headlineMedium.copy(fontFamily = PlusJakartaSans), headlineSmall = headlineSmall.copy(fontFamily = PlusJakartaSans),
        titleLarge = titleLarge.copy(fontFamily = PlusJakartaSans), titleMedium = titleMedium.copy(fontFamily = PlusJakartaSans),
        titleSmall = titleSmall.copy(fontFamily = PlusJakartaSans), bodyLarge = bodyLarge.copy(fontFamily = PlusJakartaSans),
        bodyMedium = bodyMedium.copy(fontFamily = PlusJakartaSans), bodySmall = bodySmall.copy(fontFamily = PlusJakartaSans),
        labelLarge = labelLarge.copy(fontFamily = PlusJakartaSans), labelMedium = labelMedium.copy(fontFamily = PlusJakartaSans),
        labelSmall = labelSmall.copy(fontFamily = PlusJakartaSans),
    )
}

@Composable
fun ChurchTheme(content: @Composable () -> Unit) {
    @Suppress("UNUSED_VARIABLE") val ignored = isSystemInDarkTheme() // web is dark-only
    MaterialTheme(colorScheme = ColorScheme, typography = AppTypography) {
        // Material `Text` falls back to LocalTextStyle; make every Text (incl. ad-hoc fontSize) use the web font.
        ProvideTextStyle(TextStyle(fontFamily = PlusJakartaSans, color = Color.White), content = content)
    }
}
