package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val VariDarkColorScheme = darkColorScheme(
    primary = NeonVioletPrimary,
    onPrimary = OnNeonViolet,
    primaryContainer = NeonVioletContainer,
    onPrimaryContainer = OnNeonVioletContainer,
    secondary = ElectricNeonBlue,
    onSecondary = OnElectricNeonBlue,
    secondaryContainer = ElectricBlueContainer,
    onSecondaryContainer = OnElectricBlueContainer,
    tertiary = NeonMagentaTertiary,
    onTertiary = OnNeonMagenta,
    tertiaryContainer = NeonMagentaContainer,
    onTertiaryContainer = OnNeonMagentaContainer,
    background = ObsidianBackground,
    onBackground = WarmParchment,
    surface = VelvetSurface,
    onSurface = WarmParchment,
    surfaceVariant = CharcoalContainer,
    onSurfaceVariant = MutedSand,
    surfaceContainerLowest = ObsidianBackground,
    surfaceContainerLow = VelvetSurface,
    surfaceContainer = CharcoalContainer,
    surfaceContainerHigh = ElevatedContainer,
    surfaceContainerHighest = ElevatedContainer,
    outline = SubtleOutline,
    outlineVariant = SubtleOutlineVariant,
    error = RoseError,
    onError = OnRoseError,
    errorContainer = RoseErrorContainer,
    onErrorContainer = OnRoseErrorContainer
)

val VariShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun VariTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VariDarkColorScheme,
        typography = VariTypography,
        shapes = VariShapes,
        content = content
    )
}
