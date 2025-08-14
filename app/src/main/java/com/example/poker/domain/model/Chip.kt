package com.example.poker.domain.model

import androidx.compose.ui.graphics.Color

data class Chip(
    val value: Long,
    val baseColor: Color,
    val accentColor: Color
)

val standardChipSet: List<Chip> = listOf(
    Chip(1000, Color(0xFF424242), Color(0xFFF57F17)), // Темно-серый с оранжевым
    Chip(500, Color(0xFF6A1B9A), Color(0xFFE0E0E0)), // Фиолетовый с серым
    Chip(100, Color(0xFFB71C1C), Color(0xFFFFFFFF)), // Красный с белым
    Chip(25, Color(0xFF00695C), Color(0xFFFFFFFF)),  // Зеленый с белым
    Chip(10, Color(0xFF1565C0), Color(0xFFFFFFFF)),  // Синий с белым
    Chip(5, Color(0xFFFF8F00), Color(0xFF000000)),   // Желтый с черным
    Chip(1, Color(0xFFFAFAFA), Color(0xFFD32F2F))   // Белый с красным
)