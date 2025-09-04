package com.example.poker.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.poker.data.remote.dto.Card
import com.example.poker.domain.model.Rank
import com.example.poker.domain.model.Suit

@Composable
fun CardBack(modifier: Modifier = Modifier) {
    val backColor = Color(0xFF1D4ED8)
    val patternColor = Color(0xFF3B82F6)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(4.dp, Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        val boxModifier = remember {
            Modifier
                .fillMaxSize()
                .background(backColor)
                .padding(4.dp)
        }
        Box(modifier = boxModifier
            .drawWithCache {
                // Этот блок кода выполнится только один раз (или при изменении размера).
                // Все вычисления и создание объектов кешируются.
                val strokeWidth = 2.dp.toPx()
                val step = 10.dp.toPx()
                onDrawBehind {
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
        )
    }
}

@Composable
@Preview
fun Test() {
    CardFaceAlternative(Card(Rank.TEN, Suit.HEARTS), Modifier.width(200.dp).height(300.dp))
    //CardBack(Modifier)
}

@Composable
fun PokerCard(card: Card?, modifier: Modifier) {
    card?.let {
        CardFaceAlternative(it, modifier)
    } ?: CardBack(modifier)
}

@Composable
fun ClassicPokerCard(card: Card?, isFourColorMode: Boolean,  modifier: Modifier) {
    card?.let {
        CardFaceClassic(it, isFourColorMode, modifier)
    } ?: CardBack(modifier)
}

@Composable
fun SimplePokerCard(card: Card?, modifier: Modifier) {
    card?.let {
        CardFaceSimple(it, modifier)
    } ?: CardBack(modifier)
}

enum class FlipDirection {
    CLOCKWISE, // По часовой стрелке
    COUNTER_CLOCKWISE // Против часовой стрелки
}

@Composable
fun FlippingPokerCard(card: Card?, modifier: Modifier, flipDirection: FlipDirection = FlipDirection.CLOCKWISE) {
    val density = LocalDensity.current.density
    val isFaceUp = card != null

    // 1. Анимируем единый "прогресс" переворота от 0.0 (рубашка) до 1.0 (лицо)
    val animationProgress by animateFloatAsState(
        targetValue = if (isFaceUp) 1f else 0f,
        animationSpec = if(isFaceUp) tween(durationMillis = 800) else snap(),
        label = "flip_progress"
    )
    // 2. Вычисляем и применяем все трансформации в graphicsLayer
    val cardModifier = modifier
        .graphicsLayer {
            // --- Логика переворота (rotationY) ---
            val direction = if (flipDirection == FlipDirection.CLOCKWISE) 1 else -1
            rotationY = (animationProgress * 180f) * direction

            // Устанавливаем камеру для создания 3D-эффекта
            cameraDistance = 12 * density

            // --- Логика "подбрасывания" (translationY) ---
            // Используем формулу параболы: пик анимации будет в середине (когда progress = 0.5)
            val tossHeight = -4 * (animationProgress - animationProgress * animationProgress)
            translationY = tossHeight * 20.dp.toPx() // 20.dp - максимальная высота "подскока"
        }
    if (animationProgress <= 0.5f) {
        CardBack(modifier = cardModifier)
    } else {
        // Поворачиваем лицо карты обратно, чтобы оно не было зеркальным
        if (card != null) {
            CardFaceAlternative(
                card = card,
                modifier = cardModifier.graphicsLayer { rotationY = 180f }
            )
        } else CardBack(modifier = cardModifier.graphicsLayer { rotationY = 180f })
    }
}