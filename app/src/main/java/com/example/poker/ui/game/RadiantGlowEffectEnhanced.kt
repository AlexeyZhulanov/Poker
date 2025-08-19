package com.example.poker.ui.game

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadiantGlowEffectEnhanced(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFFD700),
    rayCount: Int = 16,
    innerRadiusRatio: Float = 0.3f
) {
    val infiniteTransition = rememberInfiniteTransition()

    // Анимация длины лучей
    val rayLength by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "ray_length"
    )

    // Анимация прозрачности
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow_alpha"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing)
        )
    )

    Canvas(modifier = modifier.graphicsLayer {
        rotationZ = rotation
    }) {
        val center = center
        val outerRadius = size.minDimension / 2
        val innerRadius = outerRadius * innerRadiusRatio
        val rayWidth = outerRadius * 0.08f

        repeat(rayCount) { i ->
            val angle = 2f * PI.toFloat() * i / rayCount
            val start = Offset(
                center.x + innerRadius * cos(angle),
                center.y + innerRadius * sin(angle)
            )
            val end = Offset(
                center.x + outerRadius * rayLength * cos(angle),
                center.y + outerRadius * rayLength * sin(angle)
            )

            // Градиент от прозрачного к цвету
            drawLine(
                brush = Brush.linearGradient(
                    0f to color.copy(alpha = 0f),
                    0.3f to color.copy(alpha = alpha),
                    1f to color.copy(alpha = 0f)
                ),
                start = start,
                end = end,
                strokeWidth = rayWidth,
                cap = StrokeCap.Round,
                blendMode = BlendMode.Screen
            )
        }
    }
}