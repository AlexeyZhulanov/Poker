package com.example.poker.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poker.domain.model.Chip
import kotlin.math.cos
import kotlin.math.sin


//@OptIn(ExperimentalTextApi::class)
//@Composable
//fun PerspectiveChip(
//    chip: Chip,
//    modifier: Modifier = Modifier
//) {
//    val density = LocalDensity.current.density
//    val textMeasurer = rememberTextMeasurer()
//
//    Box(
//        modifier = modifier
//            .size(80.dp)
//            .graphicsLayer {
//                rotationX = -40f
//                cameraDistance = 8 * density
//            }.offset(5.dp, 0.dp)
//    ) {
//        Canvas(modifier = Modifier.fillMaxSize()) {
//            val thickness = size.height * 0.2f
//            val topOvalRect = Rect(0f, 0f, size.width, size.height - thickness)
//            val bottomOvalRect = Rect(0f, thickness, size.width, size.height)
//
//            // --- 1. Рисуем "боковую" часть фишки ---
//            val sidePath = Path().apply {
//                // Левая вертикальная грань
//                moveTo(topOvalRect.left, topOvalRect.center.y)
//                lineTo(bottomOvalRect.left, bottomOvalRect.center.y)
//                // Нижняя дуга
//                arcTo(bottomOvalRect, 180f, -180f, false)
//                // Правая вертикальная грань
//                lineTo(topOvalRect.right, topOvalRect.center.y)
//                // Верхняя дуга (для замыкания, хоть и невидима)
//                arcTo(topOvalRect, 0f, 180f, false)
//                close()
//            }
//            drawPath(path = sidePath, color = chip.baseColor.darken())
//            drawPath(sidePath, color = Color.Black, style = Stroke(width = 0.8.dp.toPx()))
//
//            // --- 2. Рисуем "верхнюю" часть (эллипс) ---
//            drawOval(color = chip.baseColor, topLeft = topOvalRect.topLeft, size = topOvalRect.size)
//            drawOval(color = Color.Black, topLeft = topOvalRect.topLeft, size = topOvalRect.size, style = Stroke(width = 0.8.dp.toPx()))
//
//            // --- 3. Рисуем узор (в правильном порядке) ---
//
//            // 3.1 Четыре радиальные полосы
//            for (i in 0..3) {
//                val angle = (45 + i * 90).toFloat()
//                val stripeWidthDegrees = 20f // Ширина полосы в градусах
//                val angleRad = Math.toRadians(angle.toDouble())
//                val startAngle = angle - stripeWidthDegrees / 2
//
//                // --- Полоса на боковой части ---
//                if (sin(angleRad) > 0) {
//                    val sideStripePath = Path().apply {
//                        // Рисуем дугу по нижнему краю
//                        arcTo(bottomOvalRect, angle - stripeWidthDegrees / 2, stripeWidthDegrees, false)
//                        // Соединяем с верхним краем
//                        val topRightRad = Math.toRadians((angle + stripeWidthDegrees / 2).toDouble())
//                        lineTo(
//                            topOvalRect.center.x + topOvalRect.width / 2 * cos(topRightRad).toFloat(),
//                            topOvalRect.center.y + topOvalRect.height / 2 * sin(topRightRad).toFloat()
//                        )
//                        // Рисуем дугу по верхнему краю в обратном направлении
//                        arcTo(topOvalRect, angle + stripeWidthDegrees / 2, -stripeWidthDegrees, false)
//                        close()
//                    }
//                    drawPath(sideStripePath, color = chip.accentColor.darken())
//                    drawPath(sideStripePath, color = Color.Black, style = Stroke(width = 1.dp.toPx()))
//                }
//
//                // --- Полоса на верхней части ---
//                // Сначала рисуем заливку
//                drawArc(
//                    color = chip.accentColor,
//                    startAngle = startAngle,
//                    sweepAngle = stripeWidthDegrees,
//                    useCenter = true,
//                    topLeft = topOvalRect.topLeft,
//                    size = topOvalRect.size
//                )
//                // --- Поверх рисуем обводку ---
//                drawArc(
//                    color = Color.Black,
//                    startAngle = startAngle,
//                    sweepAngle = stripeWidthDegrees,
//                    useCenter = true,
//                    topLeft = topOvalRect.topLeft,
//                    size = topOvalRect.size,
//                    style = Stroke(width = 1.dp.toPx()) // Задаем стиль обводки
//                )
//            }
//
//            // 3.2 Центральный круг (рисуется ПОВЕРХ полос)
//            val centerCircleRadius = topOvalRect.width * 0.3f
//            // Сначала заливка
//            drawCircle(color = chip.accentColor, radius = centerCircleRadius, center = topOvalRect.center)
//            // Затем обводка
//            drawCircle(color = Color.Black, radius = centerCircleRadius, center = topOvalRect.center, style = Stroke(width = 1.dp.toPx()))
//
//            // 3.3 Текст с номиналом (рисуется ПОВЕРХ центрального круга)
//            val textToDraw = when {
//                chip.value >= 1000 -> "${chip.value / 1000}K"
//                else -> chip.value.toString()
//            }
//            val textStyle = TextStyle(color = chip.baseColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
//            val textLayoutResult = textMeasurer.measure(AnnotatedString(textToDraw), style = textStyle)
//            drawText(
//                textLayoutResult = textLayoutResult,
//                topLeft = Offset(
//                    x = center.x - textLayoutResult.size.width / 2,
//                    y = topOvalRect.center.y - textLayoutResult.size.height / 2
//                )
//            )
//        }
//    }
//}

// Вспомогательная функция для затемнения цвета
private fun Color.darken(factor: Float = 0.7f): Color {
    return Color(
        red = this.red * factor,
        green = this.green * factor,
        blue = this.blue * factor,
        alpha = this.alpha
    )
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun PerspectiveChipStack(
    chips: List<Chip>,
    modifier: Modifier = Modifier,
    chipSize: Dp = 80.dp,
    stackOffset: Float = -0.8f // Насколько сильно фишки "выглядывают" друг из-под друга
) {
    if (chips.isEmpty()) return

    val density = LocalDensity.current.density
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .size(chipSize)
            .graphicsLayer {
                rotationX = -50f // Более сильный наклон для лучшего вида стопки
                cameraDistance = 5 * density
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val thickness = size.height * 0.2f
            val verticalShift = thickness * stackOffset

            val totalVisualHeight = (size.height - ((chips.size - 1) * verticalShift)) * 0.8f
            val scale = minOf(1f, size.height / totalVisualHeight)

            withTransform({
                // Сначала сжимаем все, что будет нарисовано, относительно центра
                scale(scale, scale, center)
                // Затем сдвигаем холст, чтобы отцентровать стопку
                val visualCenterY = (size.height + (chips.size - 1) * verticalShift) / 2f
                translate(top = center.y - visualCenterY)
            }) {
                chips.reversed().forEachIndexed { index, chip ->
                    val yOffset = index * verticalShift

                    // --- Горизонтальный отступ ---
                    val horizontalPadding = size.width * 0.02f
                    val paddedWidth = size.width - (2 * horizontalPadding)

                    val topOvalRect = Rect(horizontalPadding, yOffset, paddedWidth + horizontalPadding, size.height - thickness + yOffset)
                    val bottomOvalRect = Rect(horizontalPadding, thickness + yOffset, paddedWidth + horizontalPadding, size.height + yOffset)
                    // 1. Рисуем боковую грань для КАЖДОЙ фишки
                    val sidePath = Path().apply {
                        moveTo(topOvalRect.left, topOvalRect.center.y)
                        lineTo(bottomOvalRect.left, bottomOvalRect.center.y)
                        arcTo(bottomOvalRect, 180f, -180f, false)
                        lineTo(topOvalRect.right, topOvalRect.center.y)
                        arcTo(topOvalRect, 0f, 180f, false)
                        close()
                    }
                    drawPath(path = sidePath, color = chip.baseColor.darken())
                    drawPath(sidePath, color = Color.Black, style = Stroke(width = 0.8.dp.toPx()))

                    // 2. Рисуем верхнюю грань (эллипс) для КАЖДОЙ фишки
                    drawOval(color = chip.baseColor, topLeft = topOvalRect.topLeft, size = topOvalRect.size)

                    for (i in 0..3) {
                        val angle = (45 + i * 90).toFloat()
                        val stripeWidthDegrees = 20f // Ширина полосы в градусах
                        val angleRad = Math.toRadians(angle.toDouble())
                        val startAngle = angle - stripeWidthDegrees / 2

                        // --- Полоса на боковой части ---
                        if (sin(angleRad) > 0) {
                            val sideStripePath = Path().apply {
                                // Рисуем дугу по нижнему краю
                                arcTo(bottomOvalRect, angle - stripeWidthDegrees / 2, stripeWidthDegrees, false)
                                // Соединяем с верхним краем
                                val topRightRad = Math.toRadians((angle + stripeWidthDegrees / 2).toDouble())
                                lineTo(
                                    topOvalRect.center.x + topOvalRect.width / 2 * cos(topRightRad).toFloat(),
                                    topOvalRect.center.y + topOvalRect.height / 2 * sin(topRightRad).toFloat()
                                )
                                // Рисуем дугу по верхнему краю в обратном направлении
                                arcTo(topOvalRect, angle + stripeWidthDegrees / 2, -stripeWidthDegrees, false)
                                close()
                            }
                            drawPath(sideStripePath, color = chip.accentColor.darken())
                            drawPath(sideStripePath, color = Color.Black, style = Stroke(width = 1.dp.toPx()))
                        }
                        if (index == chips.size - 1) {
                            // --- Полоса на верхней части ---
                            // Сначала рисуем заливку
                            drawArc(
                                color = chip.accentColor,
                                startAngle = startAngle,
                                sweepAngle = stripeWidthDegrees,
                                useCenter = true,
                                topLeft = topOvalRect.topLeft,
                                size = topOvalRect.size
                            )
                            // --- Поверх рисуем обводку ---
                            drawArc(
                                color = Color.Black,
                                startAngle = startAngle,
                                sweepAngle = stripeWidthDegrees,
                                useCenter = true,
                                topLeft = topOvalRect.topLeft,
                                size = topOvalRect.size,
                                style = Stroke(width = 1.dp.toPx()) // Задаем стиль обводки
                            )
                        }
                    }

                    // 3. Узор рисуем ТОЛЬКО для самой верхней фишки
                    if (index == chips.size - 1) {
                        // 3.2 Центральный круг (рисуется ПОВЕРХ полос)
                        val centerCircleRadius = topOvalRect.width * 0.3f
                        // Сначала заливка
                        drawCircle(color = chip.accentColor, radius = centerCircleRadius, center = topOvalRect.center)
                        // Затем обводка
                        drawCircle(color = Color.Black, radius = centerCircleRadius, center = topOvalRect.center, style = Stroke(width = 1.dp.toPx()))

                        // 3.3 Текст с номиналом (рисуется ПОВЕРХ центрального круга)
                        val textToDraw = when {
                            chip.value >= 1000 -> "${chip.value / 1000}K"
                            else -> chip.value.toString()
                        }
                        val fontSize = if(chip.value < 100) (chipSize / 3).toSp() else (chipSize / 4).toSp()
                        val textStyle = TextStyle(
                            color = chip.baseColor, // Основной цвет (например, белый)
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold
                        )
                        val textLayoutResult = textMeasurer.measure(AnnotatedString(textToDraw), style = textStyle)

                        // Центр для трансформаций и позиционирования
                        val textCenter = Offset(center.x, topOvalRect.center.y)
                        val textTopLeft = Offset(
                            x = textCenter.x - textLayoutResult.size.width / 2,
                            y = textCenter.y - textLayoutResult.size.height / 2
                        )
                        // Смещение для тени
                        val shadowOffset = Offset(0.5.dp.toPx(), 0.5.dp.toPx())

                        // Применяем трансформацию сжатия ко всему, что рисуется внутри
                        withTransform({
                            scale(scaleX = 1f, scaleY = 0.85f, pivot = textCenter)
                        }) {
                            // Сначала рисуем тень
                            drawText(
                                textLayoutResult = textLayoutResult,
                                color = Color.Black.copy(alpha = 0.6f),
                                topLeft = textTopLeft + shadowOffset
                            )
                            // Затем основной текст
                            drawText(
                                textLayoutResult = textLayoutResult,
                                color = chip.baseColor,
                                topLeft = textTopLeft
                            )
                        }
                    }
                }
            }
        }
//        val textToDraw = when {
//            chips.first().value >= 1000 -> "${chips.first().value / 1000}K"
//            else -> chips.first().value.toString()
//        }
//        Text(
//            text = textToDraw,
//            color = chips.first().baseColor,
//            fontSize = 14.sp,
//            fontWeight = FontWeight.Bold,
//            style = TextStyle(
//                shadow = Shadow(
//                    Color.Black.copy(alpha = 0.6f),
//                    offset = Offset(1f, 1f),
//                    blurRadius = 2f
//                )
//            ),
//            modifier = Modifier
//                .graphicsLayer {
//                    // Применяем тот же наклон, что и к фишке, чтобы они лежали в одной плоскости
//                    rotationX = -50f
//                    cameraDistance = 5 * density
//
//                    // Можно немного сжать по вертикали для усиления эффекта
//                    scaleY = 0.9f
//                }.offset(0.dp, (-47).dp).align(Alignment.Center)
//        )
    }
}

//@Preview
//@Composable
//fun ChipPreview() {
//    Box(modifier = Modifier.size(100.dp)) {
//        PerspectiveChip(chip = standardChipSet.first { it.value == 100L })
//    }
//}

@Composable
@Preview
fun TestPerspectiveChips() {
    val chipsForBet = calculateChipStack(10)
    PerspectiveChipStack(
        chips = chipsForBet
    )
}