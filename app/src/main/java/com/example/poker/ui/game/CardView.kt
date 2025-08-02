package com.example.poker.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.poker.data.remote.dto.Card
import com.example.poker.domain.model.Rank
import com.example.poker.domain.model.Suit

@Composable
fun CardBack(modifier: Modifier) {
    val backColor = Color(0xFF1D4ED8) // Насыщенный синий
    val patternColor = Color(0xFF3B82F6) // Более светлый синий для узора

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(4.dp, Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backColor)
                .padding(4.dp) // Внутренний отступ, чтобы узор не заходил на рамку
        ) {
            // Узор
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 2.dp.toPx()
                val step = 10.dp.toPx()

                // Диагональные линии слева направо
                for (i in -size.height.toInt()..size.width.toInt() step step.toInt()) {
                    drawLine(
                        color = patternColor,
                        start = Offset(x = i.toFloat(), y = 0f),
                        end = Offset(x = i.toFloat() + size.height, y = size.height),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }

                // Диагональные линии справа налево
                for (i in 0..(size.width.toInt() + size.height.toInt()) step step.toInt()) {
                    drawLine(
                        color = patternColor,
                        start = Offset(x = i.toFloat(), y = 0f),
                        end = Offset(x = i.toFloat() - size.height, y = size.height),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun Test() {
    CardFaceAlternative(Card(Rank.JACK, Suit.DIAMONDS), Modifier.width(200.dp).height(300.dp))
    //CardBack(Modifier)
}

@Composable
fun PokerCard(card: Card?, isFourColorMode: Boolean, modifier: Modifier) {
    card?.let {
        CardFaceAlternative(it, modifier)
    } ?: CardBack(modifier)
}