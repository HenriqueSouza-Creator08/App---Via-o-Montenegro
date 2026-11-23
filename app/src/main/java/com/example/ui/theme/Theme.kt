package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

val PlusJakartaSansFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans, FontWeight.ExtraBold)
)

val InterFontFamily = FontFamily(
    Font(R.font.inter, FontWeight.Normal),
    Font(R.font.inter, FontWeight.Medium),
    Font(R.font.inter, FontWeight.SemiBold),
    Font(R.font.inter, FontWeight.Bold)
)

val JetBrainsMonoFontFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Bold)
)

object BrandPalette {
    val MontenegroDarkGreen = Color(0xFF2D5A27)
    val MontenegroOlive = Color(0xFF719E19)
    val MontenegroLime = Color(0xFF88B822)
    val MontenegroBrightLime = Color(0xFFB9EA6B)
    val MontenegroNavy = Color(0xFF14243A)
    val OperationalBlue = Color(0xFF2563EB)
    val OperationalEmerald = Color(0xFF10B981)
    val AlertRed = Color(0xFFEF4444)

    val Slate50 = Color(0xFFF8FAFC)
    val Slate100 = Color(0xFFF1F5F9)
    val Slate200 = Color(0xFFE2E8F0)
    val Slate400 = Color(0xFF94A3B8)
    val Slate500 = Color(0xFF64748B)
    val Slate700 = Color(0xFF334155)
    val Slate800 = Color(0xFF1E293B)
    val Slate900 = Color(0xFF0F172A)
}

@Immutable
data class MontenegroExtendedColors(
    val cardBackground: Color,
    val cardBorder: Color,
    val subtleSurface: Color,
    val mutedText: Color,
    val limePillBackground: Color,
    val limePillContent: Color,
    val blueAccent: Color,
    val emeraldAccent: Color,
    val dangerContainer: Color,
    val dangerContent: Color,
    val glassBackground: Color,
    val glassBorder: Color
)

val LocalMontenegroColors = staticCompositionLocalOf {
    MontenegroExtendedColors(
        cardBackground = Color.White,
        cardBorder = BrandPalette.Slate200,
        subtleSurface = BrandPalette.Slate50,
        mutedText = BrandPalette.Slate400,
        limePillBackground = Color(0xFFECFCCB),
        limePillContent = Color(0xFF3F6212),
        blueAccent = BrandPalette.OperationalBlue,
        emeraldAccent = BrandPalette.OperationalEmerald,
        dangerContainer = Color(0xFFFEF2F2),
        dangerContent = Color(0xFFDC2626),
        glassBackground = Color.White.copy(alpha = 0.85f),
        glassBorder = Color.White.copy(alpha = 0.9f)
    )
}

private val LightColorScheme = lightColorScheme(
    primary = BrandPalette.OperationalBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = BrandPalette.MontenegroOlive,
    onSecondary = Color.White,
    secondaryContainer = BrandPalette.MontenegroBrightLime,
    onSecondaryContainer = BrandPalette.Slate900,
    tertiary = BrandPalette.OperationalEmerald,
    onTertiary = Color.White,
    background = BrandPalette.Slate50,
    onBackground = BrandPalette.Slate800,
    surface = Color.White,
    onSurface = BrandPalette.Slate800,
    surfaceVariant = BrandPalette.Slate100,
    onSurfaceVariant = BrandPalette.Slate500,
    outline = BrandPalette.Slate200,
    error = BrandPalette.AlertRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF3B82F6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = BrandPalette.MontenegroBrightLime,
    onSecondary = BrandPalette.Slate900,
    secondaryContainer = Color(0xFF365314),
    onSecondaryContainer = Color(0xFFD9F99D),
    tertiary = BrandPalette.OperationalEmerald,
    onTertiary = Color.White,
    background = BrandPalette.Slate900,
    onBackground = BrandPalette.Slate100,
    surface = BrandPalette.Slate800,
    onSurface = BrandPalette.Slate100,
    surfaceVariant = BrandPalette.Slate700,
    onSurfaceVariant = BrandPalette.Slate400,
    outline = BrandPalette.Slate700,
    error = Color(0xFFF87171),
    onError = BrandPalette.Slate900
)

val AppTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 38.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    bodySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.6.sp
    )
)

val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MontenegroTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extended = if (darkTheme) {
        MontenegroExtendedColors(
            cardBackground = BrandPalette.Slate800,
            cardBorder = BrandPalette.Slate700,
            subtleSurface = BrandPalette.Slate900,
            mutedText = BrandPalette.Slate400,
            limePillBackground = Color(0xFF365314).copy(alpha = 0.45f),
            limePillContent = Color(0xFFD9F99D),
            blueAccent = Color(0xFF60A5FA),
            emeraldAccent = Color(0xFF34D399),
            dangerContainer = Color(0xFF450A0A).copy(alpha = 0.65f),
            dangerContent = Color(0xFFFCA5A5),
            glassBackground = BrandPalette.Slate800.copy(alpha = 0.88f),
            glassBorder = Color.White.copy(alpha = 0.10f)
        )
    } else {
        MontenegroExtendedColors(
            cardBackground = Color.White,
            cardBorder = BrandPalette.Slate200.copy(alpha = 0.85f),
            subtleSurface = BrandPalette.Slate50,
            mutedText = BrandPalette.Slate400,
            limePillBackground = Color(0xFFECFCCB),
            limePillContent = Color(0xFF3F6212),
            blueAccent = BrandPalette.OperationalBlue,
            emeraldAccent = BrandPalette.OperationalEmerald,
            dangerContainer = Color(0xFFFEF2F2),
            dangerContent = Color(0xFFDC2626),
            glassBackground = Color.White.copy(alpha = 0.86f),
            glassBorder = Color.White.copy(alpha = 0.90f)
        )
    }

    CompositionLocalProvider(LocalMontenegroColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
