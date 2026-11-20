package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val MsrDarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = PitchBlack,
    primaryContainer = DarkSurfaceElevated,
    onPrimaryContainer = TextPrimary,
    secondary = SubtlePurple,
    onSecondary = TextPrimary,
    secondaryContainer = DeepPurpleAccent,
    onSecondaryContainer = TextPrimary,
    tertiary = GoldAccent,
    onTertiary = PitchBlack,
    background = DeepNavyBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder,
    error = ErrorRed,
    onError = TextPrimary
)

val MsrShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp)
)

@Composable
fun MsrTopUpTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MsrDarkColorScheme,
        typography = MsrTypography,
        shapes = MsrShapes,
        content = content
    )
}
