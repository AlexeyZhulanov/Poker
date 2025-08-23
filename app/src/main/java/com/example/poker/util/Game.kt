package com.example.poker.util

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