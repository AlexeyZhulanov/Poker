package com.example.poker.util

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import com.example.poker.R
import com.example.poker.domain.model.Chip
import com.example.poker.domain.model.standardChipSet
import com.example.poker.shared.model.Card
import com.example.poker.shared.model.Rank
import com.example.poker.shared.model.Suit
import com.example.poker.ui.game.StackDisplayMode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.roundToInt

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

data class NormalizedPosition(
    val x: Float,
    val y: Float,
    val isLeftEquity: Boolean
)

fun calculatePlayerPosition(playersCount: Int): List<NormalizedPosition> {
    return when(playersCount) {
        1 -> listOf(NormalizedPosition(0.5f, 1f, isLeftEquity = true))
        2 -> listOf(
            NormalizedPosition(0.5f, 1f, isLeftEquity = true),
            NormalizedPosition(0.5f, 0f, isLeftEquity = false)
        )
        3 -> listOf(
            NormalizedPosition(0.5f, 1.0f, isLeftEquity = true),
            NormalizedPosition(0.0f, 0.0f, isLeftEquity = false),
            NormalizedPosition(1.0f, 0.0f, isLeftEquity = true)
        )
        4 -> listOf(
            NormalizedPosition(1.0f, 1.0f, isLeftEquity = true),
            NormalizedPosition(0.0f, 1.0f, isLeftEquity = false),
            NormalizedPosition(0.0f, 0.0f, isLeftEquity = false),
            NormalizedPosition(1.0f, 0.0f, isLeftEquity = true)
        )
        5 -> listOf(
            NormalizedPosition(0.5f, 1.0f, isLeftEquity = true),
            NormalizedPosition(0.0f, 0.85f, isLeftEquity = false),
            NormalizedPosition(0.0f, 0.0f, isLeftEquity = false),
            NormalizedPosition(1.0f, 0.0f, isLeftEquity = true),
            NormalizedPosition(1.0f, 0.85f, isLeftEquity = true)
        )
        6 -> listOf(
            NormalizedPosition(0.5f, 1.0f, isLeftEquity = true),
            NormalizedPosition(0.0f, 0.85f, isLeftEquity = false),
            NormalizedPosition(0.0f, 0.15f, isLeftEquity = false),
            NormalizedPosition(0.5f, 0.0f, isLeftEquity = false),
            NormalizedPosition(1.0f, 0.15f, isLeftEquity = true),
            NormalizedPosition(1.0f, 0.85f, isLeftEquity = true)
        )
        7 -> listOf(
            NormalizedPosition(0.5f, 1.0f, isLeftEquity = true),
            NormalizedPosition(0.0f, 0.85f, isLeftEquity = false),
            NormalizedPosition(0.0f, 0.15f, isLeftEquity = false),
            NormalizedPosition(0.35f, 0.0f, isLeftEquity = true),
            NormalizedPosition(0.65f, 0.0f, isLeftEquity = false),
            NormalizedPosition(1.0f, 0.15f, isLeftEquity = true),
            NormalizedPosition(1.0f, 0.85f, isLeftEquity = true)
        )
        8 -> listOf(
            NormalizedPosition(0.65f, 1.0f, isLeftEquity = false),
            NormalizedPosition(0.35f, 1.0f, isLeftEquity = true),
            NormalizedPosition(0.0f, 0.85f, isLeftEquity = false),
            NormalizedPosition(0.0f, 0.15f, isLeftEquity = false),
            NormalizedPosition(0.35f, 0.0f, isLeftEquity = true),
            NormalizedPosition(0.65f, 0.0f, isLeftEquity = false),
            NormalizedPosition(1.0f, 0.15f, isLeftEquity = true),
            NormalizedPosition(1.0f, 0.85f, isLeftEquity = true)
        )
        9 -> listOf(
            NormalizedPosition(0.5f, 0.925f, isLeftEquity = true),
            NormalizedPosition(0.0f, 1.0f, isLeftEquity = false),
            NormalizedPosition(0.0f, 0.85f, isLeftEquity = false),
            NormalizedPosition(0.0f, 0.15f, isLeftEquity = false),
            NormalizedPosition(0.35f, 0.0f, isLeftEquity = true),
            NormalizedPosition(0.65f, 0.0f, isLeftEquity = false),
            NormalizedPosition(1.0f, 0.15f, isLeftEquity = true),
            NormalizedPosition(1.0f, 0.85f, isLeftEquity = true),
            NormalizedPosition(1.0f, 1.0f, isLeftEquity = true)
        )
        else -> emptyList()
    }
}

fun calculatePlayerPositionLandscape(playersCount: Int): List<NormalizedPosition> {
    return when(playersCount) {
        1 -> listOf(
            NormalizedPosition(0.5f, 1f, isLeftEquity = true)
        )
        2 -> listOf(
            NormalizedPosition(0.5f, 1f, isLeftEquity = true),
            NormalizedPosition(0.5f, 0f, isLeftEquity = false)
        )
        3 -> listOf(
            NormalizedPosition(0.5f, 1.0f, isLeftEquity = true),
            NormalizedPosition(0.0f, 0.5f, isLeftEquity = false),
            NormalizedPosition(1.0f, 0.5f, isLeftEquity = true)
        )
        4 -> listOf(
            NormalizedPosition(0.5f, 1.0f, isLeftEquity = true),
            NormalizedPosition(0.0f, 0.5f, isLeftEquity = false),
            NormalizedPosition(0.5f, 0.0f, isLeftEquity = false),
            NormalizedPosition(1.0f, 0.5f, isLeftEquity = true)
        )
        5 -> listOf(
            NormalizedPosition(0.5f, 1.0f, isLeftEquity = true),
            NormalizedPosition(0.1f, 1.0f, isLeftEquity = false),
            NormalizedPosition(0.1f, 0.0f, isLeftEquity = false),
            NormalizedPosition(0.9f, 0.0f, isLeftEquity = true),
            NormalizedPosition(0.9f, 1.0f, isLeftEquity = true)
        )
        6 -> listOf(
            NormalizedPosition(0.5f, 1.0f, isLeftEquity = true),
            NormalizedPosition(0.1f, 1.0f, isLeftEquity = false),
            NormalizedPosition(0.1f, 0.0f, isLeftEquity = false),
            NormalizedPosition(0.5f, 0.0f, isLeftEquity = false),
            NormalizedPosition(0.9f, 0.0f, isLeftEquity = true),
            NormalizedPosition(0.9f, 1.0f, isLeftEquity = true)
        )
        7 -> listOf(
            NormalizedPosition(0.5f, 1.0f, isLeftEquity = true),
            NormalizedPosition(0.1f, 1.0f, isLeftEquity = false),
            NormalizedPosition(0.1f, 0.0f, isLeftEquity = false),
            NormalizedPosition(0.35f, 0.0f, isLeftEquity = false),
            NormalizedPosition(0.65f, 0.0f, isLeftEquity = true),
            NormalizedPosition(0.9f, 0.0f, isLeftEquity = true),
            NormalizedPosition(0.9f, 1.0f, isLeftEquity = true)
        )
        8 -> listOf(
            NormalizedPosition(0.5f, 1.0f, isLeftEquity = true),
            NormalizedPosition(0.1f, 1.0f, isLeftEquity = false),
            NormalizedPosition(0.0f, 0.5f, isLeftEquity = false),
            NormalizedPosition(0.1f, 0.0f, isLeftEquity = false),
            NormalizedPosition(0.5f, 0.0f, isLeftEquity = false),
            NormalizedPosition(0.9f, 0.0f, isLeftEquity = true),
            NormalizedPosition(1.0f, 0.5f, isLeftEquity = true),
            NormalizedPosition(0.9f, 1.0f, isLeftEquity = true)
        )
        9 -> listOf(
            NormalizedPosition(0.5f, 1.0f, isLeftEquity = true),
            NormalizedPosition(0.1f, 1.0f, isLeftEquity = false),
            NormalizedPosition(0.0f, 0.5f, isLeftEquity = false),
            NormalizedPosition(0.1f, 0.0f, isLeftEquity = false),
            NormalizedPosition(0.35f, 0.0f, isLeftEquity = false),
            NormalizedPosition(0.65f, 0.0f, isLeftEquity = true),
            NormalizedPosition(0.9f, 0.0f, isLeftEquity = true),
            NormalizedPosition(1.0f, 0.5f, isLeftEquity = true),
            NormalizedPosition(0.9f, 1.0f, isLeftEquity = true)
        )
        else -> emptyList()
    }
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

@DrawableRes
fun getThrowItemResource(stickerId: String, isSecondStage: Boolean): Int {
    return if(isSecondStage) {
        when(stickerId) {
            "egg" -> R.drawable.throw_egg2
            "tomato" -> R.drawable.throw_tomato2
            "poop" -> R.drawable.throw_poop2
            else -> R.drawable.throw_tomato2
        }
    } else {
        when(stickerId) {
            "egg" -> R.drawable.throw_egg1
            "tomato" -> R.drawable.throw_tomato1
            "poop" -> R.drawable.throw_poop1
            else -> R.drawable.throw_tomato1
        }
    }
}

fun formatBet(chips: Long, mode: StackDisplayMode, bb: Long): String {
    return if (mode == StackDisplayMode.BIG_BLINDS) chips.toBB(bb) else chips.toString()
}

fun parseBet(text: String, mode: StackDisplayMode, bb: Long): Long? {
    return if (mode == StackDisplayMode.BIG_BLINDS) {
        text.trim().toDoubleOrNull()?.let { (it * bb).toLong() }
    } else {
        text.trim().toLongOrNull()
    }
}

fun Modifier.centerAt(centerX: Float, centerY: Float) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    // Устанавливаем размер компонента таким, какой он есть
    layout(placeable.width, placeable.height) {
        // Сдвигаем его на заданные координаты, вычитая половину ширины и высоты
        val x = centerX.roundToInt() - (placeable.width / 2)
        val y = centerY.roundToInt() - (placeable.height / 2)
        placeable.placeRelative(x, y)
    }
}