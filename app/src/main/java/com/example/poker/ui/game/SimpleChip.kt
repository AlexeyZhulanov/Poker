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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.poker.domain.model.Chip

@Composable
fun SimpleChip(
    chips: List<Chip>,
    modifier: Modifier = Modifier,
    chipSize: Dp = 80.dp
) {
    if (chips.isEmpty()) return

    // Берем только самую верхнюю фишку для отрисовки
    val topChip = chips.first()
    val textMeasurer = rememberTextMeasurer()
    Box(modifier = modifier.size(chipSize)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2
            val center = this.center

            // 1. Рисуем основное тело фишки (круг)
            drawCircle(color = topChip.baseColor, radius = radius, center = center)
            // Рисуем черную обводку по краю
            drawCircle(color = Color.Black, radius = radius, center = center, style = Stroke(width = 1.dp.toPx()))

            // 2. Рисуем 4 полосы по краям
            for (i in 0..3) {
                val angle = (45 + i * 90).toFloat()
                val stripeWidthDegrees = 20f
                val startAngle = angle - stripeWidthDegrees / 2
                val arcRect = Rect(center = center, radius = radius)

                // Заливка полосы
                drawArc(
                    color = topChip.accentColor,
                    startAngle = startAngle,
                    sweepAngle = stripeWidthDegrees,
                    useCenter = true,
                    topLeft = arcRect.topLeft,
                    size = arcRect.size
                )
                // Обводка полосы
                drawArc(
                    color = Color.Black,
                    startAngle = startAngle,
                    sweepAngle = stripeWidthDegrees,
                    useCenter = true,
                    topLeft = arcRect.topLeft,
                    size = arcRect.size,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // 3. Рисуем центральный круг
            val centerCircleRadius = radius * 0.6f // 60% от основного радиуса
            drawCircle(color = topChip.accentColor, radius = centerCircleRadius, center = center)
            drawCircle(color = Color.Black, radius = centerCircleRadius, center = center, style = Stroke(width = 1.dp.toPx()))

            // 4. Рисуем текст с номиналом
            val textToDraw = when {
                topChip.value >= 1000 -> "${topChip.value / 1000}K"
                else -> topChip.value.toString()
            }
            val fontSize = if(topChip.value < 100) (chipSize / 3).toSp() else (chipSize / 4).toSp()
            val textStyle = TextStyle(
                color = topChip.baseColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            val textLayoutResult = textMeasurer.measure(
                text = AnnotatedString(textToDraw),
                style = textStyle,
                constraints = Constraints.fixedWidth((size.minDimension * 0.6f).toInt()) // Ширина центрального круга
            )

            val textTopLeft = Offset(
                x = center.x - textLayoutResult.size.width / 2,
                y = center.y - textLayoutResult.size.height / 2
            )
            val shadowOffset = Offset(0.25.dp.toPx(), 0.25.dp.toPx())

            // Сначала рисуем тень
            drawText(
                textLayoutResult = textLayoutResult,
                color = Color.Black.copy(alpha = 0.6f),
                topLeft = textTopLeft + shadowOffset
            )
            // Затем основной текст
            drawText(
                textLayoutResult = textLayoutResult,
                color = topChip.baseColor,
                topLeft = textTopLeft - shadowOffset
            )
        }
    }
}

//@Composable
//@Preview
//fun TestSimpleChip() {
//    val chip = standardChipSet[4]
//    Box(modifier = Modifier.fillMaxSize().graphicsLayer {
//        translationX = -20f
//        translationY = -20f
//    }, contentAlignment = Alignment.Center) {
//        SimpleChip(listOf(chip), chipSize = 20.dp * 1.3f)
//    }
//}