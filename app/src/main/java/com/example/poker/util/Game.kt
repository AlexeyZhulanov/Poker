package com.example.poker.util

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.example.poker.R
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

@DrawableRes
fun getStickerResource(stickerId: String): Int {
    return when (stickerId) {
        "durak1" -> R.drawable.sticker_durak1
        "durak2" -> R.drawable.sticker_durak2
        "durak3" -> R.drawable.sticker_durak3
        "durak4" -> R.drawable.sticker_durak4
        "durak5" -> R.drawable.sticker_durak5
        "durak6" -> R.drawable.sticker_durak6
        "durak7" -> R.drawable.sticker_durak7
        "durak8" -> R.drawable.sticker_durak8
        "durak9" -> R.drawable.sticker_durak9
        "durak10" -> R.drawable.sticker_durak10
        "durak11" -> R.drawable.sticker_durak11
        "durak12" -> R.drawable.sticker_durak12
        "durak13" -> R.drawable.sticker_durak13
        "durak14" -> R.drawable.sticker_durak14
        "durak15" -> R.drawable.sticker_durak15
        "durak16" -> R.drawable.sticker_durak16
        "durak17" -> R.drawable.sticker_durak17
        "durak18" -> R.drawable.sticker_durak18
        "durak19" -> R.drawable.sticker_durak19
        "durak20" -> R.drawable.sticker_durak20
        "durak21" -> R.drawable.sticker_durak21
        "durak22" -> R.drawable.sticker_durak22
        "durak23" -> R.drawable.sticker_durak23
        "durak24" -> R.drawable.sticker_durak24

        "snake1" -> R.drawable.sticker_snake1
        "snake2" -> R.drawable.sticker_snake2
        "snake3" -> R.drawable.sticker_snake3
        "snake4" -> R.drawable.sticker_snake4
        "snake5" -> R.drawable.sticker_snake5
        "snake6" -> R.drawable.sticker_snake6
        "snake7" -> R.drawable.sticker_snake7
        "snake8" -> R.drawable.sticker_snake8
        "snake9" -> R.drawable.sticker_snake9
        "snake10" -> R.drawable.sticker_snake10
        "snake11" -> R.drawable.sticker_snake11
        "snake12" -> R.drawable.sticker_snake12
        "snake13" -> R.drawable.sticker_snake13
        "snake14" -> R.drawable.sticker_snake14
        "snake15" -> R.drawable.sticker_snake15
        "snake16" -> R.drawable.sticker_snake16
        "snake17" -> R.drawable.sticker_snake17
        "snake18" -> R.drawable.sticker_snake18
        "snake19" -> R.drawable.sticker_snake19
        "snake20" -> R.drawable.sticker_snake20
        "snake21" -> R.drawable.sticker_snake21
        "snake22" -> R.drawable.sticker_snake22
        "snake23" -> R.drawable.sticker_snake23

        "sponge1" -> R.drawable.sticker_sponge1
        "sponge2" -> R.drawable.sticker_sponge2
        "sponge3" -> R.drawable.sticker_sponge3
        "sponge4" -> R.drawable.sticker_sponge4
        "sponge5" -> R.drawable.sticker_sponge5
        "sponge6" -> R.drawable.sticker_sponge6
        "sponge7" -> R.drawable.sticker_sponge7
        "sponge8" -> R.drawable.sticker_sponge8
        "sponge9" -> R.drawable.sticker_sponge9
        "sponge10" -> R.drawable.sticker_sponge10
        "sponge11" -> R.drawable.sticker_sponge11
        "sponge12" -> R.drawable.sticker_sponge12
        "sponge13" -> R.drawable.sticker_sponge13
        "sponge14" -> R.drawable.sticker_sponge14
        "sponge15" -> R.drawable.sticker_sponge15
        "sponge16" -> R.drawable.sticker_sponge16

        else -> R.drawable.sticker_durak1
    }
}