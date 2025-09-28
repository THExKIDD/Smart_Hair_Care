package com.example.smarthaircare.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun SmartHairCareTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF4CAF50),
            primaryContainer = Color(0xFFE8F5E8),
            secondary = Color(0xFF2196F3),
            surface = Color(0xFFF8F9FA),
            background = Color(0xFFFFFFFF)
        ),
        content = content
    )
}
