package com.example.poker.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poker.ui.theme.OswaldFontFamily
import kotlinx.coroutines.delay

@Composable
fun TurnTimer(
    expiresAt: Long,
    modifier: Modifier = Modifier,
    scaleMultiplier: Float = 1f
) {
    var remainingSeconds by remember { mutableLongStateOf(0L) }

    // LaunchedEffect будет перезапускаться каждый раз, когда меняется expiresAt
    LaunchedEffect(key1 = expiresAt) {
        while (true) {
            val remaining = expiresAt - System.currentTimeMillis()
            if (remaining <= 0) {
                remainingSeconds = 0
                break
            }
            remainingSeconds = (remaining + 999) / 1000 // Округление вверх
            delay(200L) // Обновляем 5 раз в секунду для плавности
        }
    }

    val textColor = if (remainingSeconds <= 5) {
        Color(0xFFEF4444) // Ярко-красный для предупреждения
    } else {
        Color.White
    }

    Box(modifier = modifier
            .size(22.dp * scaleMultiplier)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.7f))
            .border(1.dp * scaleMultiplier, Color.White.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = remainingSeconds.toString(),
            color = textColor,
            fontSize = 11.sp * scaleMultiplier,
            fontWeight = FontWeight.Bold,
            fontFamily = OswaldFontFamily,
            style = TextStyle(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                ),
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both
                )
            )
        )
    }
}