package com.kamal.creditliability.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF245B4A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3EBDD),
    onPrimaryContainer = Color(0xFF10251E),
    secondary = Color(0xFF52645B),
    background = Color(0xFFF7F8F5),
    surface = Color(0xFFF7F8F5)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9AD0B6),
    primaryContainer = Color(0xFF244B3B)
)

@Composable
fun CreditLiabilityTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
