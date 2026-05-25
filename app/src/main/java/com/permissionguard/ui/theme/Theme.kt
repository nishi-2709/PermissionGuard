package com.permissionguard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PermissionGuardColorScheme = darkColorScheme(
    background = NavyBackground,
    surface = NavySurface,
    surfaceVariant = NavySurfaceLight,
    primary = CyanAccent,
    secondary = PurpleAccent,
    tertiary = TealAccent,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = RiskHigh
)

@Composable
fun PermissionGuardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PermissionGuardColorScheme,
        content = content
    )
}
