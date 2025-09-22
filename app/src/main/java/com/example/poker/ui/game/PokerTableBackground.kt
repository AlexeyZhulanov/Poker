package com.example.poker.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Определяем цвета стола
        val feltColor = Color(0xFF00693E) // Темно-зеленый
        val woodColorDark = Color(0xFF5C3A21)
        val woodColorLight = Color(0xFF8B5A2B)
        val lineColor = Color.Yellow.copy(alpha = 0.6f)

        // 1. Рисуем деревянный бортик (с градиентом для объема)
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(woodColorLight, woodColorDark),
                center = center,
                radius = canvasWidth / 2
            ),
            size = Size(canvasWidth, canvasHeight)
        )

        // 2. Рисуем зеленое сукно стола (чуть меньше бортика)
        drawOval(
            color = feltColor,
            topLeft = Offset(20f, 20f),
            size = Size(canvasWidth - 40f, canvasHeight - 40f)
        )

        // 3. Рисуем желтую линию разметки
        drawOval(
            color = lineColor,
            topLeft = Offset(40f, 40f),
            size = Size(canvasWidth - 80f, canvasHeight - 80f),
            style = Stroke(width = 5f) // Рисуем только контур
        )
    }
}

@Composable
@Preview
fun TestPokerTable() {
    PokerTableBackground()
}