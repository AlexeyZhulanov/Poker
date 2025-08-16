package com.example.poker.ui.game

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.poker.R

@Composable
fun PulsingConnectionLostIcon(modifier: Modifier = Modifier) {
    // 1. Создаем бесконечную транзакцию
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    // 2. Анимируем размер (scale) от 100% до 115% и обратно
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse // <-- Заставляет анимацию идти туда-обратно
        ),
        label = "pulse_scale"
    )

    // 3. Анимируем прозрачность (alpha) от 70% до 100% и обратно
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Icon(
        painter = painterResource(R.drawable.ic_wifi_off),
        contentDescription = "Connection Lost",
        tint = Color.Red.copy(alpha = 0.8f),
        modifier = modifier
            .scale(scale) // Применяем анимированный размер
            .alpha(alpha)  // Применяем анимированную прозрачность
    )
}