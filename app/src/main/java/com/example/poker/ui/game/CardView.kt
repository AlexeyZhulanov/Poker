package com.example.poker.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poker.shared.model.Card
import com.example.poker.shared.model.Rank
import com.example.poker.shared.model.Suit
import com.example.poker.ui.theme.CardCharactersFontFamily
import com.example.poker.util.getCardName

@Composable
fun CardBack(modifier: Modifier = Modifier, scaleMultiplier: Float, minMultiplier: Float = 1f) {
    val backColor = Color(0xFF1D4ED8)
    val patternColor = Color(0xFF3B82F6)
    val scaleData = remember(scaleMultiplier) {
        object {
            val shape = RoundedCornerShape(8.dp * scaleMultiplier * minMultiplier)
            val borderStroke = BorderStroke(4.dp * scaleMultiplier * minMultiplier, Color.White)
            val boxModifier = Modifier.fillMaxSize().background(backColor).padding(4.dp * scaleMultiplier * minMultiplier)
            val strokeWidth = 2.dp * scaleMultiplier * minMultiplier
            val step = 10.dp * scaleMultiplier * minMultiplier
        }
    }

    Card(
        modifier = modifier,
        shape = scaleData.shape,
        border = scaleData.borderStroke,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = scaleData.boxModifier
            .drawWithCache {
                // Этот блок кода выполнится только один раз (или при изменении размера).
                // Все вычисления и создание объектов кешируются.
                val strokeWidth = scaleData.strokeWidth.toPx()
                val step = scaleData.step.toPx()
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
fun PokerCard(card: Card?, modifier: Modifier, scaleMultiplier: Float = 1f) {
    card?.let {
        CardFaceAlternative(it, modifier, scaleMultiplier)
    } ?: CardBack(modifier, scaleMultiplier)
}

@Composable
fun ClassicPokerCard(card: Card?, isFourColorMode: Boolean, modifier: Modifier, scaleMultiplier: Float = 1f) {
    card?.let {
        CardFaceClassic(it, isFourColorMode, modifier, scaleMultiplier)
    } ?: CardBack(modifier, scaleMultiplier)
}

@Composable
fun ClassicPlayerPokerCard(card: Card?, isFourColorMode: Boolean, scaleMultiplier: Float) {
    val cardModifier = remember(scaleMultiplier) {
        Modifier.width(30.dp * scaleMultiplier).height(45.dp * scaleMultiplier)
    }
    card?.let {
        CardFaceClassic(it, isFourColorMode, cardModifier, scaleMultiplier)
    } ?: CardBack(cardModifier, scaleMultiplier, minMultiplier = 0.6f)
}

@Composable
fun SimplePokerCard(card: Card?, scaleMultiplier: Float) {
    val cardModifier = remember(scaleMultiplier) {
        Modifier.width(30.dp * scaleMultiplier).height(45.dp * scaleMultiplier)
    }
    card?.let {
        CardFaceSimple(it, cardModifier, scaleMultiplier, true)
    } ?: CardBack(cardModifier, scaleMultiplier, minMultiplier = 0.6f)
}

enum class FlipDirection {
    CLOCKWISE, // По часовой стрелке
    COUNTER_CLOCKWISE // Против часовой стрелки
}

@Composable
fun FlippingPokerCard(card: Card?, flipDirection: FlipDirection = FlipDirection.CLOCKWISE, scaleMultiplier: Float, rotation: Float, isClassicFace: Boolean, isFourColorMode: Boolean = true) {
    val density = LocalDensity.current.density
    val isFaceUp = card != null

    // 1. Анимируем единый "прогресс" переворота от 0.0 (рубашка) до 1.0 (лицо)
    val animationProgress by animateFloatAsState(
        targetValue = if (isFaceUp) 1f else 0f,
        animationSpec = if(isFaceUp) tween(durationMillis = 800) else snap(),
        label = "flip_progress"
    )
    val (jumpHeight, modifier) = remember(scaleMultiplier) {
        20.dp * scaleMultiplier to
                Modifier
                    .width(40.dp * scaleMultiplier)
                    .height(60.dp * scaleMultiplier)
                    .graphicsLayer { rotationZ = rotation }
    }
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
            translationY = tossHeight * jumpHeight.toPx() // 20.dp - максимальная высота "подскока"
        }
    if (animationProgress <= 0.5f) {
        CardBack(modifier = cardModifier, scaleMultiplier, 0.8f)
    } else {
        // Поворачиваем лицо карты обратно, чтобы оно не было зеркальным
        if (card != null) {
            if(isClassicFace) {
                CardFaceClassic(
                    card = card,
                    isFourColorMode = isFourColorMode,
                    modifier = cardModifier.graphicsLayer { rotationY = 180f },
                    scaleMultiplier = scaleMultiplier
                )
            } else {
                CardFaceAlternative(
                    card = card,
                    modifier = cardModifier.graphicsLayer { rotationY = 180f },
                    scaleMultiplier
                )
            }
        } else CardBack(modifier = cardModifier.graphicsLayer { rotationY = 180f }, scaleMultiplier, 0.8f)
    }
}

@Composable
fun SuitGroupCard(suit: Suit, modifier: Modifier = Modifier) {
    val suitData = remember(suit) {
        val suitVector = when(suit) {
            Suit.HEARTS -> CardSuits.Heart
            Suit.DIAMONDS -> CardSuits.Diamond
            Suit.CLUBS -> CardSuits.Club
            else -> CardSuits.Spade
        }
        val suitColor = when(suit) {
            Suit.HEARTS -> Color(0xFF821013)
            Suit.SPADES -> Color(0xFF232322)
            Suit.DIAMONDS -> Color(0xFF104886)
            Suit.CLUBS -> Color(0xFF0C7618)
        }
        object {
            val vector = suitVector
            val color = suitColor
        }
    }
    Box(contentAlignment = Alignment.Center, modifier = modifier.background(color = suitData.color, shape = RoundedCornerShape(4.dp)).padding(horizontal = 2.dp)) {
        Icon(
            imageVector = suitData.vector,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun RankGroupCard(rank: Rank, modifier: Modifier = Modifier) {
    BoxWithConstraints(contentAlignment = Alignment.Center, modifier = modifier) {
        val fontSize = with(LocalDensity.current) {
            if(rank == Rank.TEN) (maxHeight * 0.7f).toSp() else (maxHeight * 0.85f).toSp()
        }
        val cardName = remember(rank) { getCardName(rank) }
        Text(text = cardName, color = Color.White, fontWeight = FontWeight.SemiBold, fontFamily = CardCharactersFontFamily,
            style = TextStyle(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                ),
                lineHeight = TextUnit.Unspecified,
                letterSpacing = if(rank == Rank.TEN) -(5).sp else TextUnit.Unspecified
            ),
            softWrap = false,
            fontSize = fontSize,
        )
    }
}

//@Composable
//@Preview
//fun TestSimpleCard2() {
//    Box(modifier = Modifier.width(30.dp).aspectRatio(0.8f), contentAlignment = Alignment.Center) {
//        SuitGroupCard(Suit.SPADES)
//    }
//}