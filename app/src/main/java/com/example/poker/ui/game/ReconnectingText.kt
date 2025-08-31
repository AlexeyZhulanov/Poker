package com.example.poker.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun ReconnectingText(modifier: Modifier = Modifier, textSize: TextUnit = 18.sp) {
    val dotCount = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        dotCount.animateTo(
            targetValue = 4f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }
    val animatedDots = ".".repeat(dotCount.value.toInt().coerceIn(1, 3))

    Text(
        text = "Соединение$animatedDots",
        color = Color.White,
        fontSize = textSize,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Start,
        modifier = modifier
    )
}