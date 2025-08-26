package com.example.poker.util

import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.example.poker.domain.model.Chip
import com.example.poker.domain.model.standardChipSet

fun calculateChipStack(amount: Long): List<Chip> {
    if (amount <= 0) return emptyList()

    val result = mutableListOf<Chip>()
    var remainingAmount = amount

    // Проходим по нашему набору фишек от самой дорогой к самой дешевой
    for (chip in standardChipSet) {
        // Вычисляем, сколько фишек этого номинала "влезает" в остаток
        val count = remainingAmount / chip.value
        if (count > 0) {
            // Добавляем нужное количество фишек в результат
            repeat(count.toInt()) {
                result.add(chip)
            }
            // Уменьшаем остаток
            remainingAmount %= chip.value
        }
    }
    return result
}

fun calculatePlayerPosition(playersCount: Int): Pair<List<BiasAlignment>, List<Boolean>> {
    // 0.68 - центр
    val list = mutableListOf(BiasAlignment(0f, 1f)) // first
    val equityList = mutableListOf(true) // right = false, left = true
    when(playersCount) {
        1 -> return list to equityList
        2 -> { list.add(BiasAlignment(0f, -1f)); equityList.add(false) }
        3 -> {
            list.add(BiasAlignment(-1f, -0.9f))
            equityList.add(false)
            list.add(BiasAlignment(1f, -0.9f))
            equityList.add(true)
        }
        4 -> {
            list.add(BiasAlignment(-1f, -0.5f))
            equityList.add(false)
            list.add(BiasAlignment(0f, -1f))
            equityList.add(false)
            list.add(BiasAlignment(1f, -0.5f))
            equityList.add(true)
        }
        5 -> {
            list.add(BiasAlignment(-1f, 0.5f))
            equityList.add(false)
            list.add(BiasAlignment(-1f, -0.9f))
            equityList.add(false)
            list.add(BiasAlignment(1f, -0.9f))
            equityList.add(true)
            list.add(BiasAlignment(1f, 0.5f))
            equityList.add(true)
        }
        6 -> {
            list.add(BiasAlignment(-1f, 0.5f))
            equityList.add(false)
            list.add(BiasAlignment(-1f, -0.5f))
            equityList.add(false)
            list.add(BiasAlignment(0f, -1f))
            equityList.add(false)
            list.add(BiasAlignment(1f, -0.5f))
            equityList.add(true)
            list.add(BiasAlignment(1f, 0.5f))
            equityList.add(true)
        }
        7 -> {
            list.add(BiasAlignment(-1f, 0.5f))
            equityList.add(false)
            list.add(BiasAlignment(-1f, -0.5f))
            equityList.add(false)
            list.add(BiasAlignment(-0.5f, -1f))
            equityList.add(false)
            list.add(BiasAlignment(0.5f, -1f))
            equityList.add(true)
            list.add(BiasAlignment(1f, -0.5f))
            equityList.add(true)
            list.add(BiasAlignment(1f, 0.5f))
            equityList.add(true)
        }
        8 -> {
            list.add(BiasAlignment(-1f, 0.5f))
            equityList.add(false)
            list.add(BiasAlignment(-1f, -0.4f))
            equityList.add(false)
            list.add(BiasAlignment(-1f, -0.85f))
            equityList.add(false)
            list.add(BiasAlignment(0f, -1f))
            equityList.add(false)
            list.add(BiasAlignment(1f, -0.85f))
            equityList.add(true)
            list.add(BiasAlignment(1f, -0.4f))
            equityList.add(true)
            list.add(BiasAlignment(1f, 0.5f))
            equityList.add(true)
        }
        9 -> {
            list.add(BiasAlignment(-1f, 0.85f))
            equityList.add(false)
            list.add(BiasAlignment(-1f, 0.4f))
            equityList.add(false)
            list.add(BiasAlignment(-1f, -0.5f))
            equityList.add(false)
            list.add(BiasAlignment(-0.5f, -1f))
            equityList.add(false)
            list.add(BiasAlignment(0.5f, -1f))
            equityList.add(true)
            list.add(BiasAlignment(1f, -0.5f))
            equityList.add(true)
            list.add(BiasAlignment(1f, 0.4f))
            equityList.add(true)
            list.add(BiasAlignment(1f, 0.85f))
            equityList.add(true)
        }
        else -> return Pair(listOf(), listOf())
    }
    return list to equityList
}

fun calculateOffset(
    startAlignment: Alignment,
    endAlignment: Alignment,
    parentWidthPx: Float,
    parentHeightPx: Float
) : Pair<IntOffset, IntOffset> {
    val startOffset = startAlignment.align(IntSize.Zero, IntSize(parentWidthPx.toInt(), parentHeightPx.toInt()), LayoutDirection.Ltr)
    val endOffset = endAlignment.align(IntSize.Zero, IntSize(parentWidthPx.toInt(), parentHeightPx.toInt()), LayoutDirection.Ltr)
    return startOffset to endOffset
}