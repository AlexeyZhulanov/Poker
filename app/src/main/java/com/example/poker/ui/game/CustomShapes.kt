package com.example.poker.ui.game

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class TrapezoidShape(private val slantWidth: Float = 20f) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(0f, 0f) // Верхний левый угол
            lineTo(size.width, 0f) // Верхний правый угол
            lineTo(size.width - slantWidth, size.height) // Нижний правый угол (смещенный)
            lineTo(slantWidth, size.height) // Нижний левый угол (смещенный)
            close()
        }
        return Outline.Generic(path)
    }
}

enum class TailDirection {
    LEFT, RIGHT
}

class OvalWithTailShape(private val tailDirection: TailDirection) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(Path().apply {
            val ovalRect = Rect(0f, 0f, size.width, size.height)
            addOval(ovalRect)

            val angleStart = if (tailDirection == TailDirection.LEFT) 140f else 40f
            val angleEnd = if (tailDirection == TailDirection.LEFT) 130f else 50f

            val radiusX = ovalRect.width / 2f
            val radiusY = ovalRect.height / 2f

            val pointOnOvalXStart = ovalRect.center.x + (radiusX * cos(Math.toRadians(angleStart.toDouble()))).toFloat()
            val pointOnOvalYStart = ovalRect.center.y + (radiusY * sin(Math.toRadians(angleStart.toDouble()))).toFloat()

            val pointOnOvalXEnd = ovalRect.center.x + (radiusX * cos(Math.toRadians(angleEnd.toDouble()))).toFloat()
            val pointOnOvalYEnd = ovalRect.center.y + (radiusY * sin(Math.toRadians(angleEnd.toDouble()))).toFloat()

            // 3. Определяем острый кончик хвоста
            val tipX = if (tailDirection == TailDirection.LEFT) size.width * 0.03f else size.width * 0.97f
            val tipY = size.height * 0.98f

            // 4. Рисуем хвост
            moveTo(pointOnOvalXStart, pointOnOvalYStart)
            // Используем одну кривую, чтобы создать плавный изгиб к кончику
            quadraticTo(
                (pointOnOvalXStart + tipX) / 2, // Контрольная точка по X
                tipY,                          // Контрольная точка по Y
                tipX,                          // Конечная точка X (кончик)
                tipY                           // Конечная точка Y (кончик)
            )
            // Второй кривой возвращаемся к конечной точке на овале
            quadraticTo(
                (pointOnOvalXEnd + tipX) / 2,
                tipY,
                pointOnOvalXEnd,
                pointOnOvalYEnd
            )
            close()
        })
    }
}

class JaggedOvalShape(private val seed: Int) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val random = Random(seed)
        // 1. Создаем основной овал
        val basePath = Path().apply {
            addOval(Rect(0f, 0f, size.width, size.height))
        }

        val minBiteRadius = size.height / 9
        val maxBiteRadius = size.height / 7.5f

        val bitePath = Path()
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radiusX = size.width / 2f
        val radiusY = size.height / 2f

        // 2. Используем while, чтобы обойти весь периметр (2 * PI радиан)
        var currentAngle = 0f
        while (currentAngle < 2 * PI) {
            // 3. Генерируем случайный радиус для этого "укуса"
            val biteRadius = minBiteRadius + random.nextFloat() * (maxBiteRadius - minBiteRadius)

            // 4. Вычисляем, какой угол займет этот "укус" на окружности, чтобы они шли встык
            // Используем геометрию, чтобы найти угол, соответствующий дуге
            val angleStep = (asin(biteRadius / (radiusX + radiusY) * 2) * 2f) * 0.8f // 0.8f для небольшого наложения

            // 5. Вычисляем центр "укуса" на контуре
            val biteCenterX = centerX + (radiusX * cos(currentAngle))
            val biteCenterY = centerY + (radiusY * sin(currentAngle))

            // 6. Создаем круглый "укус"
            bitePath.reset()
            bitePath.addOval(
                Rect(
                    left = biteCenterX - biteRadius,
                    top = biteCenterY - biteRadius,
                    right = biteCenterX + biteRadius,
                    bottom = biteCenterY + biteRadius,
                )
            )

            // 7. Вычитаем "укус" из основной фигуры
            basePath.op(basePath, bitePath, PathOperation.Difference)

            // 8. Продвигаемся по контуру на ширину сделанного "укуса"
            currentAngle += angleStep
        }

        return Outline.Generic(basePath)
    }
}