package com.example.poker.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WinnerAnimationOverlay(
    isWinner: Boolean,
    scaleMultiplier: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val strokeWidthPx = 3.dp.value * scaleMultiplier * LocalDensity.current.density
    val targetOffset = with(density) { 40.dp.toPx() } * scaleMultiplier
    val goldColor = Color(0xFFFFD700)

    // === СОСТОЯНИЯ АНИМАЦИИ ===
    val sweepAngle = remember { Animatable(0f) }
    val rotZ = remember { Animatable(-90f) }
    val rotX1 = remember { Animatable(-35f) }
    val rotX2 = remember { Animatable(35f) }
    val ringsOffset = remember { Animatable(0f) }
    var isTextVisible by remember { mutableStateOf(false) }

    // === ОРКЕСТРАТОР (ХОРЕОГРАФИЯ ЭТАПОВ) ===
    LaunchedEffect(isWinner) {
        if (isWinner) {
            // Сброс
            sweepAngle.snapTo(0f)
            rotZ.snapTo(-90f)
            rotX1.snapTo(-35f)
            rotX2.snapTo(35f)
            ringsOffset.snapTo(0f)
            isTextVisible = false

            // ЭТАП 1: Вырастают половинки колец (до 120 градусов)
            sweepAngle.animateTo(120f, tween(600, easing = FastOutSlowInEasing))

            // ЭТАП 2: Кольца бешенно вращаются по Z (5 оборотов = 1800 градусов)
            coroutineScope {
                launch { rotZ.animateTo(-180f + 1800f, tween(1500, easing = LinearOutSlowInEasing)) }
                launch { ringsOffset.animateTo(targetOffset, tween(750, easing = LinearOutSlowInEasing, delayMillis = 750)) }
            }

            // ЭТАП 3: Поворот по X, доращивание колец до 360, старт текста, размер колец через offset
            isTextVisible = true
            coroutineScope {
                launch { rotX1.animateTo(-100f, tween(800, easing = FastOutSlowInEasing)) }
                launch { rotX2.animateTo(100f, tween(800, easing = FastOutSlowInEasing)) }
                launch { sweepAngle.animateTo(360f, tween(800, easing = FastOutSlowInEasing, delayMillis = 900)) }
            }
        } else {
            // Сброс
            sweepAngle.snapTo(0f)
            rotZ.snapTo(-90f)
            rotX1.snapTo(-35f)
            rotX2.snapTo(35f)
            ringsOffset.snapTo(0f)
            isTextVisible = false
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // === КОЛЬЦО 1 ===
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationX = rotX1.value
                    rotationZ = rotZ.value
                }
        ) {
            drawRingWithSpark(
                sweepAngle = sweepAngle.value,
                startAngle = 0f, // Стартует справа
                color = goldColor,
                strokeWidth = strokeWidthPx,
                ringOffset = ringsOffset.value
            )
        }

        // === КОЛЬЦО 2 ===
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationX = rotX2.value
                    rotationZ = rotZ.value
                }
        ) {
            drawRingWithSpark(
                sweepAngle = sweepAngle.value,
                startAngle = 180f, // Стартует слева
                color = goldColor,
                strokeWidth = strokeWidthPx,
                ringOffset = -ringsOffset.value
            )
        }

        // === СЛОЙ С БУКВАМИ (Появляется на 3-м этапе) ===
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp * scaleMultiplier),
            modifier = Modifier.offset(y = (-5).dp * scaleMultiplier)
        ) {
            VolumetricTextWithStaggeredReveal(
                text = "W", index = 0, volumeOffsetX = 0.6.dp, volumeOffsetY = 0.dp,
                scaleMultiplier = scaleMultiplier, isParentVisible = isTextVisible
            )
            // Буква "I" - Двусторонний объем (volumeOffsetX размножится в обе стороны)
            VolumetricTextWithStaggeredReveal(
                text = "I", index = 1, volumeOffsetX = 0.6.dp, volumeOffsetY = 0.dp,
                scaleMultiplier = scaleMultiplier, isParentVisible = isTextVisible,
                isBidirectional = true
            )
            VolumetricTextWithStaggeredReveal(
                text = "N", index = 2, volumeOffsetX = (-0.6).dp, volumeOffsetY = 0.dp,
                scaleMultiplier = scaleMultiplier, isParentVisible = isTextVisible
            )
        }
    }
}

// === ФУНКЦИЯ ДЛЯ ОТРИСОВКИ КОЛЬЦА И ИСКРЫ ===
private fun DrawScope.drawRingWithSpark(
    sweepAngle: Float,
    startAngle: Float,
    color: Color,
    strokeWidth: Float,
    ringOffset: Float
) {
    if (sweepAngle <= 0f) return

    // 1. Рисуем саму дугу
    val radius = size.minDimension / 2f
    val centerOffset = Offset(size.width / 2f, size.height / 2f)

    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius + ringOffset),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )

    // 2. Вычисляем точные координаты конца дуги (для искры)
    // Угол переводим в радианы для математики
    val currentAngleRad = Math.toRadians((startAngle + sweepAngle).toDouble())
    val sparkX = centerOffset.x + radius * kotlin.math.cos(currentAngleRad).toFloat()
    val sparkY = centerOffset.y + radius * kotlin.math.sin(currentAngleRad).toFloat() + ringOffset

    // 3. Рисуем свечение искры (Радиальный градиент)
    val sparkRadius = strokeWidth * 3f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, Color.Transparent),
            center = Offset(sparkX, sparkY),
            radius = sparkRadius
        ),
        radius = sparkRadius,
        center = Offset(sparkX, sparkY)
    )

    // 4. Рисуем ядро искры (Яркая белая точка)
    drawCircle(
        color = Color.White,
        radius = strokeWidth / 1.5f,
        center = Offset(sparkX, sparkY)
    )
}

// Помощник для поочередного появления букв
@Composable
fun VolumetricTextWithStaggeredReveal(
    text: String,
    index: Int,
    volumeOffsetX: Dp,
    volumeOffsetY: Dp,
    scaleMultiplier: Float,
    isParentVisible: Boolean,
    isBidirectional: Boolean = false
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(isParentVisible) {
        if (isParentVisible) {
            delay(index * 120L) // Поочередная задержка (staggered delay)
            visible = true
        } else {
            visible = false
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it }, // Вылет снизу
            animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium)
        ) + fadeIn() + scaleIn(initialScale = 0.3f)
    ) {
        VolumetricText(
            text = text,
            volumeOffsetX = volumeOffsetX,
            volumeOffsetY = volumeOffsetY,
            scaleMultiplier = scaleMultiplier,
            isBidirectional = isBidirectional
        )
    }
}

@Composable
fun VolumetricText(
    text: String,
    modifier: Modifier = Modifier,
    scaleMultiplier: Float,
    volumeOffsetX: Dp = 0.dp,
    volumeOffsetY: Dp = 0.dp,
    isBidirectional: Boolean = false
) {
    val fontSize = 28.sp * scaleMultiplier
    val fontWeight = FontWeight.Black

    val faceGradient = remember {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFFFE082), Color(0xFFFFD700), Color(0xFFFFA000))
        )
    }

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        val layersCount = if (isBidirectional) 2 else 6 // Для "I" хватит по 2 слоя с каждой стороны

        for (i in layersCount downTo 1) {
            val shadowColor = Color(0xFFB8860B).copy(alpha = 1f - (i.toFloat() / (layersCount + 1)))
            val textStyle = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))

            if (isBidirectional) {
                // Рисуем слой СЛЕВА
                Text(
                    text = text, color = shadowColor, fontSize = fontSize, fontWeight = fontWeight,
                    modifier = Modifier.offset(x = (-volumeOffsetX * i) * scaleMultiplier),
                    style = textStyle
                )
                // Рисуем слой СПРАВА
                Text(
                    text = text, color = shadowColor, fontSize = fontSize, fontWeight = fontWeight,
                    modifier = Modifier.offset(x = (volumeOffsetX * i) * scaleMultiplier),
                    style = textStyle
                )
            } else {
                // Стандартное смещение в одну сторону
                Text(
                    text = text, color = shadowColor, fontSize = fontSize, fontWeight = fontWeight,
                    modifier = Modifier.offset(
                        x = (volumeOffsetX * i) * scaleMultiplier,
                        y = (volumeOffsetY * i) * scaleMultiplier
                    ),
                    style = textStyle
                )
            }
        }

        // Лицевой слой
        Text(
            text = text,
            style = TextStyle(
                brush = faceGradient, fontSize = fontSize, fontWeight = fontWeight,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                shadow = Shadow(color = Color.White.copy(alpha = 0.7f), blurRadius = 10f * scaleMultiplier)
            )
        )
    }
}