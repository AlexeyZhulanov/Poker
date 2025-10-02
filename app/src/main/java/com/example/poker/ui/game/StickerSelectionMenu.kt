package com.example.poker.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.poker.util.getStickerResource

sealed class StickerGridItem {
    data class Sticker(val id: String) : StickerGridItem()
    data object BackButton : StickerGridItem()
    data object NextButton : StickerGridItem()
    data object EmptySlot : StickerGridItem()
}

@Composable
fun StickerSelectionMenu(
    onStickerSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pages = remember {
        listOf(
            listOf("durak1", "durak2", "durak3", "durak4", "durak5", "durak6", "durak7", "durak8", "durak9", "durak10", "durak11", "durak12", "durak13", "durak14", "durak15", "durak16", "durak17", "durak18", "durak19", "durak20", "durak21", "durak22", "durak23", "durak24"),
            listOf("snake1", "snake2", "snake3", "snake4", "snake5", "snake6", "snake7", "snake8", "snake9", "snake10", "snake11", "snake12", "snake13", "snake14", "snake15", "snake16", "snake17", "snake18", "snake19", "snake20", "snake21", "snake22", "snake23"),
            listOf("sponge1", "sponge2", "sponge3", "sponge4", "sponge5", "sponge6", "sponge7", "sponge8", "sponge9", "sponge10", "sponge11", "sponge12", "sponge13", "sponge14", "sponge15", "sponge16")
        )
    }
    var currentPage by remember { mutableIntStateOf(0) }
    val totalPages = pages.size

    val itemsForCurrentPage = remember(currentPage) {
        val pageStickers = pages[currentPage]
        val gridItems = MutableList<StickerGridItem>(25) { StickerGridItem.EmptySlot }
        var stickerIndex = 0

        for (i in 0 until 25) {
            // Правило для кнопки "Назад" (слот 21, индекс 20)
            if (currentPage > 0 && i == 20) {
                gridItems[i] = StickerGridItem.BackButton
                continue
            }
            // Правило для кнопки "Вперед" (слот 25, индекс 24)
            if (currentPage < totalPages - 1 && i == 24) {
                gridItems[i] = StickerGridItem.NextButton
                continue
            }

            // Если это не слот для кнопки, пытаемся вставить стикер
            if (stickerIndex < pageStickers.size) {
                gridItems[i] = StickerGridItem.Sticker(pageStickers[stickerIndex])
                stickerIndex++
            }
        }
        gridItems
    }
    Card(
        modifier = modifier.size(250.dp).padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(top = 28.dp, start = 8.dp, end = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(5) { rowIndex ->
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(5) { colIndex ->
                            val item = itemsForCurrentPage[rowIndex * 5 + colIndex]

                            when (item) {
                                is StickerGridItem.Sticker -> IconButton(
                                    onClick = { onStickerSelected(item.id) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Image(
                                        painter = painterResource(id = getStickerResource(item.id)),
                                        contentDescription = "Sticker",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                StickerGridItem.BackButton -> IconButton(
                                    onClick = { currentPage-- },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "BackArrow", tint = Color.Gray)
                                }
                                StickerGridItem.NextButton -> IconButton(
                                    onClick = { currentPage++ },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "ForwardArrow", tint = Color.Gray)
                                }
                                StickerGridItem.EmptySlot -> Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).offset(x = 10.dp, y = (-10).dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
            }
        }
    }
}