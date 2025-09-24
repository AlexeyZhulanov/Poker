package com.example.poker.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun PokerTableBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val railHeight = size.height * 0.08f // Ширина бортика (8% от высоты стола)

        // --- Определяем цвета ---
        val feltColorDark = Color(0xFF004D2B)   // Основной цвет сукна
        val feltColorLight = Color(0xFF00693E)  // "Блик" света в центре

        val railColorDark = Color(0xFF4A2A14)   // Темная часть дерева
        val railColorMedium = Color(0xFF7B4A23) // Средняя часть дерева
        val railColorLight = Color(0xFF8B5A2B)  // Светлая часть дерева

        val innerShadowColor = Color.Black.copy(alpha = 0.4f)
        val bettingLineColor = Color(0xFFE0B314).copy(alpha = 0.8f)

        // --- Рисуем слои стола ---

        // 1. Рисуем внешний деревянный бортик "с фаской"
        // Используем толстый контур с градиентом для имитации объема
        drawOval(
            brush = Brush.linearGradient(
                colors = listOf(railColorLight, railColorDark, railColorMedium),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height)
            ),
            size = size,
            style = Stroke(width = railHeight * 2) // Ширина контура равна ширине бортика
        )

        // 2. Рисуем внутреннюю тень для эффекта глубины
        // Это заставляет сукно выглядеть "утопленным" в стол
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, innerShadowColor),
                center = center,
                radius = (size.height - railHeight) / 2
            ),
            topLeft = Offset(railHeight / 2, railHeight / 2),
            size = Size(size.width - railHeight, size.height - railHeight)
        )

        // 3. Рисуем зеленое сукно с эффектом освещения
        // Радиальный градиент от светлого в центре к темному по краям
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(feltColorLight, feltColorDark),
                center = center,
                radius = size.height / 2.5f
            ),
            topLeft = Offset(railHeight, railHeight),
            size = Size(size.width - railHeight * 2, size.height - railHeight * 2)
        )

        // 4. Рисуем желтую линию для ставок
        drawOval(
            color = bettingLineColor,
            topLeft = Offset(railHeight * 1.5f, railHeight * 1.5f),
            size = Size(size.width - railHeight * 3, size.height - railHeight * 3),
            style = Stroke(width = 4f)
        )
    }
}

@Composable
@Preview
fun TestPokerTable() {
    PokerTableBackground()
}