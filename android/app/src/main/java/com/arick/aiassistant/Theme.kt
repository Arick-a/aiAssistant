package com.arick.aiassistant

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val InkBlack = Color(0xFF111111)
internal val InkSurface = Color(0xFF1A1A1A)
internal val InkSurfaceRaised = Color(0xFF222222)
internal val InkBorder = Color(0xFF2E2E2E)
internal val InkMuted = Color(0xFFB8B9B6)
internal val InkSubtle = Color(0xFF666666)
internal val AssistantOrange = Color(0xFFFF8400)
internal val AssistantSuccess = Color(0xFFB6FFCE)
internal val AssistantViolet = Color(0xFFB2B2FF)

private val AssistantColors = darkColorScheme(
    primary = AssistantOrange,
    onPrimary = InkBlack,
    primaryContainer = Color(0xFF291C0F),
    onPrimaryContainer = AssistantOrange,
    secondary = InkBorder,
    onSecondary = Color.White,
    background = InkBlack,
    onBackground = Color.White,
    surface = InkBlack,
    onSurface = Color.White,
    surfaceVariant = InkSurface,
    onSurfaceVariant = InkMuted,
    outline = InkBorder,
    outlineVariant = InkBorder,
    error = Color(0xFFFF5C33),
    onError = InkBlack,
)

private val AssistantTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        color = Color.White,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        color = Color.White,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = Color.White,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = Color.White,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = Color.White,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = InkMuted,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        color = AssistantOrange,
    ),
)

private val AssistantShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp),
)

@Composable
fun AiAssistantTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = AssistantColors,
        typography = AssistantTypography,
        shapes = AssistantShapes,
        content = content,
    )
}
