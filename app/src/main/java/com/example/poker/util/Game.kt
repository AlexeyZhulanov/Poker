package com.example.poker.util

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.example.poker.domain.model.Chip
import com.example.poker.domain.model.standardChipSet
import com.example.poker.shared.model.Card
import com.example.poker.shared.model.Rank
import com.example.poker.shared.model.Suit
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

const val serverUrl = "https://poker.amessenger.ru"
const val serverSocketUrl = "wss://poker.amessenger.ru"
const val ROOM_ID = "offline_room"

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
    val list = mutableListOf<BiasAlignment>()
    val equityList = mutableListOf<Boolean>() // right = false, left = true
    when(playersCount) {
        1 -> { list.add(BiasAlignment(0f, 1f)); equityList.add(true) }
        2 -> {
            list.add(BiasAlignment(0f, 1f)); equityList.add(true)
            list.add(BiasAlignment(0f, -1f)); equityList.add(false)
        }
        3 -> {
            list.add(BiasAlignment(0f, 1f)); equityList.add(true)
            list.add(BiasAlignment(-1f, -1f)); equityList.add(false)
            list.add(BiasAlignment(1f, -1f)); equityList.add(true)
        }
        4 -> {
            list.add(BiasAlignment(1f, 1f)); equityList.add(true)
            list.add(BiasAlignment(-1f, 1f)); equityList.add(false)
            list.add(BiasAlignment(-1f, -1f)); equityList.add(false)
            list.add(BiasAlignment(1f, -1f)); equityList.add(true)
        }
        5 -> {
            list.add(BiasAlignment(0f, 1f)); equityList.add(true)
            list.add(BiasAlignment(-1f, 0.7f)); equityList.add(false)
            list.add(BiasAlignment(-1f, -1f)); equityList.add(false)
            list.add(BiasAlignment(1f, -1f)); equityList.add(true)
            list.add(BiasAlignment(1f, 0.7f)); equityList.add(true)
        }
        6 -> {
            list.add(BiasAlignment(0f, 1f)); equityList.add(true)
            list.add(BiasAlignment(-1f, 0.7f)); equityList.add(false)
            list.add(BiasAlignment(-1f, -0.7f)); equityList.add(false)
            list.add(BiasAlignment(0f, -1f)); equityList.add(false)
            list.add(BiasAlignment(1f, -0.7f)); equityList.add(true)
            list.add(BiasAlignment(1f, 0.7f)); equityList.add(true)
        }
        7 -> {
            list.add(BiasAlignment(0f, 1f)); equityList.add(true)
            list.add(BiasAlignment(-1f, 0.7f)); equityList.add(false)
            list.add(BiasAlignment(-1f, -0.7f)); equityList.add(false)
            list.add(BiasAlignment(-0.3f, -1f)); equityList.add(true)
            list.add(BiasAlignment(0.3f, -1f)); equityList.add(false)
            list.add(BiasAlignment(1f, -0.7f)); equityList.add(true)
            list.add(BiasAlignment(1f, 0.7f)); equityList.add(true)
        }
        8 -> {
            list.add(BiasAlignment(0.3f, 1f)); equityList.add(false)
            list.add(BiasAlignment(-0.3f, 1f)); equityList.add(true)
            list.add(BiasAlignment(-1f, 0.7f)); equityList.add(false)
            list.add(BiasAlignment(-1f, -0.7f)); equityList.add(false)
            list.add(BiasAlignment(-0.3f, -1f)); equityList.add(true)
            list.add(BiasAlignment(0.3f, -1f)); equityList.add(false)
            list.add(BiasAlignment(1f, -0.7f)); equityList.add(true)
            list.add(BiasAlignment(1f, 0.7f)); equityList.add(true)
        }
        9 -> {
            list.add(BiasAlignment(0f, 0.85f)); equityList.add(true)
            list.add(BiasAlignment(-1f, 1f)); equityList.add(false)
            list.add(BiasAlignment(-1f, 0.7f)); equityList.add(false)
            list.add(BiasAlignment(-1f, -0.7f)); equityList.add(false)
            list.add(BiasAlignment(-0.3f, -1f)); equityList.add(true)
            list.add(BiasAlignment(0.3f, -1f)); equityList.add(false)
            list.add(BiasAlignment(1f, -0.7f)); equityList.add(true)
            list.add(BiasAlignment(1f, 0.7f)); equityList.add(true)
            list.add(BiasAlignment(1f, 1f)); equityList.add(true)
        }
        else -> return Pair(listOf(), listOf())
    }
    return list to equityList
}

fun calculatePlayerPositionLandscape(playersCount: Int): Pair<List<BiasAlignment>, List<Boolean>> {
    val list = mutableListOf(BiasAlignment(0f, 1f)) // first
    val equityList = mutableListOf(true) // right = false, left = true
    when(playersCount) {
        1 -> return list to equityList
        2 -> { list.add(BiasAlignment(0f, -1f)); equityList.add(false) }
        3 -> {
            list.add(BiasAlignment(-1f, 0f)); equityList.add(false)
            list.add(BiasAlignment(1f, 0f)); equityList.add(true)
        }
        4 -> {
            list.add(BiasAlignment(-1f, 0f)); equityList.add(false)
            list.add(BiasAlignment(0f, -1f)); equityList.add(false)
            list.add(BiasAlignment(1f, 0f)); equityList.add(true)
        }
        5 -> {
            list.add(BiasAlignment(-0.8f, 1f)); equityList.add(false)
            list.add(BiasAlignment(-0.8f, -1f)); equityList.add(false)
            list.add(BiasAlignment(0.8f, -1f)); equityList.add(true)
            list.add(BiasAlignment(0.8f, 1f)); equityList.add(true)
        }
        6 -> {
            list.add(BiasAlignment(-0.8f, 1f)); equityList.add(false)
            list.add(BiasAlignment(-0.8f, -1f)); equityList.add(false)
            list.add(BiasAlignment(0f, -1f)); equityList.add(false)
            list.add(BiasAlignment(0.8f, -1f)); equityList.add(true)
            list.add(BiasAlignment(0.8f, 1f)); equityList.add(true)
        }
        7 -> {
            list.add(BiasAlignment(-0.8f, 1f)); equityList.add(false)
            list.add(BiasAlignment(-0.8f, -1f)); equityList.add(false)
            list.add(BiasAlignment(-0.3f, -1f)); equityList.add(false)
            list.add(BiasAlignment(0.3f, -1f)); equityList.add(true)
            list.add(BiasAlignment(0.8f, -1f)); equityList.add(true)
            list.add(BiasAlignment(0.8f, 1f)); equityList.add(true)
        }
        8 -> {
            list.add(BiasAlignment(-0.8f, 1f)); equityList.add(false)
            list.add(BiasAlignment(-1f, 0f)); equityList.add(false)
            list.add(BiasAlignment(-0.8f, -1f)); equityList.add(false)
            list.add(BiasAlignment(0f, -1f)); equityList.add(false)
            list.add(BiasAlignment(0.8f, -1f)); equityList.add(true)
            list.add(BiasAlignment(1f, 0f)); equityList.add(true)
            list.add(BiasAlignment(0.8f, 1f)); equityList.add(true)
        }
        9 -> {
            list.add(BiasAlignment(-0.8f, 1f)); equityList.add(false)
            list.add(BiasAlignment(-1f, 0f)); equityList.add(false)
            list.add(BiasAlignment(-0.8f, -1f)); equityList.add(false)
            list.add(BiasAlignment(-0.3f, -1f)); equityList.add(false)
            list.add(BiasAlignment(0.3f, -1f)); equityList.add(true)
            list.add(BiasAlignment(0.8f, -1f)); equityList.add(true)
            list.add(BiasAlignment(1f, 0f)); equityList.add(true)
            list.add(BiasAlignment(0.8f, 1f)); equityList.add(true)
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

fun getCardName(rank: Rank): String {
    return if(rank.value < 10) rank.value.toString() else {
        when(rank) {
            Rank.TEN -> "l0"
            Rank.JACK -> "J"
            Rank.QUEEN -> "Q"
            Rank.KING -> "K"
            Rank.ACE -> "A"
            else -> ""
        }
    }
}

@Immutable
sealed interface OutDisplayItem {
    @Immutable
    data class FullCard(val card: Card) : OutDisplayItem
    @Immutable
    data class RankGroup(val rank: Rank) : OutDisplayItem
    @Immutable
    data class SuitGroup(val suit: Suit) : OutDisplayItem
}

/**
 * Подготавливает список элементов для отображения, группируя ауты.
 * @param outs Список карт-аутов.
 * @return Список элементов для UI.
 */
fun prepareOutDisplayItems(outs: List<Card>): List<OutDisplayItem> {
    if (outs.isEmpty()) return emptyList()

    val displayItems = mutableListOf<OutDisplayItem>()
    val remainingOuts = outs.toMutableList()

    // 1. Ауты на флэш
    val outsBySuit = remainingOuts.groupBy { it.suit }
    val maxSize = outsBySuit.values.maxOfOrNull { it.size } ?: 0

    if (maxSize >= 5) { // Условный порог по количеству аутов для флеша
        val potentialFlushes = outsBySuit.filter { it.value.size == maxSize }
        // Убеждаемся, что такая группа только одна, иначе это не флэш
        if (potentialFlushes.size == 1) {
            val flushGroup = potentialFlushes.entries.first()

            displayItems.add(OutDisplayItem.SuitGroup(flushGroup.key))
            remainingOuts.removeAll(flushGroup.value.toSet())
        }
    }

    // 2. Ауты 3-4 карты одинакового ранга
    val rankGroups = remainingOuts.groupBy { it.rank }.filter { it.value.size >= 3 }

    rankGroups.forEach { (rank, cards) ->
        displayItems.add(OutDisplayItem.RankGroup(rank))
        remainingOuts.removeAll(cards.toSet())
    }

    // 3. Все отдельные оставшиеся ауты
    remainingOuts.sortByDescending { it.rank } // Сортируем для красоты
    remainingOuts.forEach { card ->
        displayItems.add(OutDisplayItem.FullCard(card))
    }

    return displayItems
}

val CardListSaver = Saver<ImmutableList<Card>, List<Card>>(
    save = { it.toList() },
    restore = { it.toImmutableList() }
)