package com.example.poker.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poker.R
import com.example.poker.shared.model.Card
import com.example.poker.shared.model.Rank
import com.example.poker.shared.model.Suit
import com.example.poker.ui.theme.GreenCard
import com.example.poker.ui.theme.OswaldFontFamily

@Composable
fun CardFaceClassic(card: Card, isFourColorMode: Boolean, modifier: Modifier, scaleMultiplier: Float = 1f) {
    val cardData = remember(card, isFourColorMode) {
        val suitVector = when (card.suit) {
            Suit.HEARTS -> CardSuits.Heart
            Suit.DIAMONDS -> CardSuits.Diamond
            Suit.CLUBS -> CardSuits.Club
            else -> CardSuits.Spade
        }
        val suitColor = if (isFourColorMode) {
            when (card.suit) {
                Suit.HEARTS -> Color.Red
                Suit.SPADES -> Color.Black
                Suit.DIAMONDS -> Color.Blue
                Suit.CLUBS -> GreenCard // #00aa00
            }
        } else {
            if (card.suit == Suit.HEARTS || card.suit == Suit.DIAMONDS) Color.Red else Color.Black
        }
        var drawableId: Int? = null
        var contentDescription: String? = null
        if (card.rank in listOf(Rank.JACK, Rank.QUEEN, Rank.KING)) {
            drawableId = when (card.rank) {
                Rank.JACK -> when (card.suit) {
                    Suit.DIAMONDS -> if (isFourColorMode) R.drawable.jack_diamonds_fc else R.drawable.jack_diamonds
                    Suit.HEARTS -> R.drawable.jack_hearts
                    Suit.CLUBS -> if (isFourColorMode) R.drawable.jack_clubs_fc else R.drawable.jack_clubs
                    Suit.SPADES -> R.drawable.jack_spades
                }
                Rank.QUEEN -> when (card.suit) {
                    Suit.DIAMONDS -> if (isFourColorMode) R.drawable.queen_diamonds_fc else R.drawable.queen_diamonds
                    Suit.HEARTS -> R.drawable.queen_hearts
                    Suit.CLUBS -> if (isFourColorMode) R.drawable.queen_clubs_fc else R.drawable.queen_clubs
                    Suit.SPADES -> R.drawable.queen_spades
                }
                else -> when (card.suit) { // KING
                    Suit.DIAMONDS -> if (isFourColorMode) R.drawable.king_diamonds_fc else R.drawable.king_diamonds
                    Suit.HEARTS -> R.drawable.king_hearts
                    Suit.CLUBS -> if (isFourColorMode) R.drawable.king_clubs_fc else R.drawable.king_clubs
                    Suit.SPADES -> R.drawable.king_spades
                }
            }
            contentDescription = "Card ${card.rank.name.lowercase().replaceFirstChar { it.titlecase() }}"
        }
        // Возвращаем объект со всеми данными
        object {
            val rank = card.rank
            val vector = suitVector
            val color = suitColor
            val imageId = drawableId
            val description = contentDescription
            val isTen = card.rank.value == 10
        }
    }
    val (shapeSize, borderSize) = if(scaleMultiplier < 1f) 6.dp * scaleMultiplier to 1.dp * scaleMultiplier else 6.dp to 1.dp
    val padding = if(scaleMultiplier < 1f) 3.dp * scaleMultiplier else 3.dp
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(shapeSize),
        border = BorderStroke(borderSize, Color.Gray),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        if (cardData.imageId != null) {
            Image(
                painter = painterResource(id = cardData.imageId),
                contentDescription = cardData.description,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        } else {
            BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
                val sizes = remember(maxHeight) {
                    object {
                        val cornerFontSize = (maxHeight.value * 0.55f).sp
                        val cornerIconSize = maxHeight * 0.4f
                        val cornerOffset = -maxHeight * 0.15f
                        val cornerSpace = -maxHeight * 0.25f
                        val topSpacing = (maxWidth.value * -0.05f).sp
                        val offsetAce = maxWidth * 0.54f
                        val paddingValue = maxHeight / 12f
                        val spacingValue = maxHeight / 15f
                    }
                }
                val fraction = if(cardData.isTen) 0.6f else 0.5f
                Column(Modifier.align(Alignment.TopStart).fillMaxWidth(fraction = fraction), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(sizes.cornerSpace)) {
                    val (spacing, fontWeight) = if(cardData.isTen) sizes.topSpacing to FontWeight.Normal else TextUnit.Unspecified to FontWeight.SemiBold
                    Text(text = getCardNameClassic(cardData.rank), color = cardData.color, fontSize = sizes.cornerFontSize, fontWeight = fontWeight, fontFamily = OswaldFontFamily,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            ),
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.Both
                            ),
                            letterSpacing = spacing
                        ),
                        modifier = Modifier.offset(y = sizes.cornerOffset)
                    )
                    Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color,
                        modifier = Modifier.size(sizes.cornerIconSize))
                }
                val (count, totalFraction) = if(cardData.rank.value == 14) 1 to 1f else cardData.rank.value to 1f - fraction
                val size = when(cardData.rank.value) {
                    14 -> maxHeight
                    in 1..4 -> maxHeight * 0.6f / 4
                    5 -> maxHeight * 0.6f / 5
                    in 6..9 -> maxHeight * 0.6f / 6
                    else -> maxHeight * 0.6f / 8
                }
                Box(Modifier.fillMaxHeight().fillMaxWidth(fraction = totalFraction).align(Alignment.CenterEnd).padding(vertical = sizes.paddingValue)) {
                    when(count) {
                        1 -> {
                            Icon(
                                imageVector = cardData.vector,
                                contentDescription = null,
                                tint = cardData.color,
                                modifier = Modifier.size(size).offset(x = sizes.offsetAce)
                            )
                        }
                        in 2..4 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(sizes.spacingValue, Alignment.CenterVertically), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
                                repeat(count) {
                                    Icon(
                                        imageVector = cardData.vector,
                                        contentDescription = null,
                                        tint = cardData.color,
                                        modifier = Modifier.size(size)
                                    )
                                }
                            }
                        }
                        else -> {
                            val bigHalf = if(count % 2 == 0) count / 2 else count / 2 + 1
                            val smallHalf = count / 2
                            Row(horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
                                Column(verticalArrangement = Arrangement.spacedBy(sizes.spacingValue, Alignment.CenterVertically), modifier = Modifier.fillMaxHeight()) {
                                    repeat(smallHalf) {
                                        Icon(
                                            imageVector = cardData.vector,
                                            contentDescription = null,
                                            tint = cardData.color,
                                            modifier = Modifier.size(size)
                                        )
                                    }
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(sizes.spacingValue, Alignment.CenterVertically), modifier = Modifier.fillMaxHeight()) {
                                    repeat(bigHalf) {
                                        Icon(
                                            imageVector = cardData.vector,
                                            contentDescription = null,
                                            tint = cardData.color,
                                            modifier = Modifier.size(size)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun TestClassicFace() {
    CardFaceClassic(Card(Rank.SEVEN, Suit.SPADES), true, Modifier.width(80.dp).height(120.dp))
}

private fun getCardNameClassic(rank: Rank): String {
    return when(rank.value) {
        14 -> "A"
        10 -> "l0"
        else -> rank.value.toString()
    }
}