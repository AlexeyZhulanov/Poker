package com.example.poker.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poker.R
import com.example.poker.shared.model.Card
import com.example.poker.shared.model.Rank
import com.example.poker.shared.model.Suit
import com.example.poker.ui.theme.GreenCard
import com.example.poker.ui.theme.OswaldFontFamily

@Composable
fun CardFaceClassic(card: Card, isFourColorMode: Boolean, modifier: Modifier) {
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
        }
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, Color.Gray),
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
            BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(3.dp)) {
                val sizes = remember(maxHeight) {
                    object {
                        val cornerFontSize = (maxHeight.value * 0.16f).sp
                        val standardIconSize = maxHeight / 6.66f
                        val aceIconSize = maxHeight / 5f
                        val paddingValue = maxHeight / 12f
                        val tenIconSize = maxHeight / 10f
                        val dpEqual28 = maxHeight / 4.28f
                        val dpEqual23 = maxHeight / 5.2f
                    }
                }
                Column(Modifier.align(Alignment.TopStart).width(IntrinsicSize.Min), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = getCardNameClassic(cardData.rank), color = cardData.color, fontSize = sizes.cornerFontSize, fontWeight = FontWeight.SemiBold, fontFamily = OswaldFontFamily,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        )
                    )
                    Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color,
                        modifier = Modifier.size(sizes.paddingValue))
                }
                when(cardData.rank) {
                    Rank.ACE -> {
                        Box(Modifier.align(Alignment.Center)) {
                            Icon(
                                imageVector = cardData.vector,
                                contentDescription = null,
                                tint = cardData.color,
                                modifier = Modifier.size(sizes.aceIconSize)
                            )
                        }
                    }
                    Rank.TWO -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(vertical = sizes.paddingValue),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize).graphicsLayer(rotationZ = 180f))
                        }
                    }
                    Rank.THREE -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = sizes.paddingValue),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize).graphicsLayer(rotationZ = 180f))
                        }
                    }
                    Rank.FOUR -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(vertical = sizes.paddingValue),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = BiasAlignment.Horizontal(0.55f)
                        ) {
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize).graphicsLayer(rotationZ = 180f))
                        }
                        Column(
                            modifier = Modifier.fillMaxSize().padding(vertical = sizes.paddingValue),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = BiasAlignment.Horizontal(-0.55f)
                        ) {
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize).graphicsLayer(rotationZ = 180f))
                        }
                    }
                    Rank.FIVE -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(vertical = sizes.paddingValue),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = BiasAlignment.Horizontal(0.55f)
                        ) {
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize).graphicsLayer(rotationZ = 180f))
                        }
                        Column(
                            modifier = Modifier.fillMaxSize().padding(vertical = sizes.paddingValue),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = BiasAlignment.Horizontal(-0.55f)
                        ) {
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize).graphicsLayer(rotationZ = 180f))
                        }
                        Box(
                            Modifier.align(Alignment.Center)
                        ) {
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                        }
                    }
                    Rank.SIX -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(vertical = sizes.paddingValue),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = BiasAlignment.Horizontal(0.55f)
                        ) {
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize).graphicsLayer(rotationZ = 180f))
                        }
                        Column(
                            modifier = Modifier.fillMaxSize().padding(vertical = sizes.paddingValue),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = BiasAlignment.Horizontal(-0.55f)
                        ) {
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize).graphicsLayer(rotationZ = 180f))
                        }
                    }
                    Rank.SEVEN -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(vertical = sizes.paddingValue),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = BiasAlignment.Horizontal(0.55f)
                        ) {
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize).graphicsLayer(rotationZ = 180f))
                        }
                        Column(
                            modifier = Modifier.fillMaxSize().padding(vertical = sizes.paddingValue),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = BiasAlignment.Horizontal(-0.55f)
                        ) {
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize).graphicsLayer(rotationZ = 180f))
                        }
                        Box(
                            Modifier.align(BiasAlignment(0f, -0.4f))
                        ) {
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                        }
                    }
                    Rank.EIGHT -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(vertical = sizes.paddingValue),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = BiasAlignment.Horizontal(0.55f)
                        ) {
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize).graphicsLayer(rotationZ = 180f))
                        }
                        Column(
                            modifier = Modifier.fillMaxSize().padding(vertical = sizes.paddingValue),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = BiasAlignment.Horizontal(-0.55f)
                        ) {
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize).graphicsLayer(rotationZ = 180f))
                        }
                        Column(
                            modifier = Modifier.fillMaxSize().padding(vertical = sizes.dpEqual28),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize).graphicsLayer(rotationZ = 180f))
                        }
                    }
                    Rank.NINE -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(vertical = sizes.paddingValue / 2),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = BiasAlignment.Horizontal(0.55f)
                        ) {
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize).graphicsLayer(rotationZ = 180f))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize).graphicsLayer(rotationZ = 180f))
                        }
                        Column(
                            modifier = Modifier.fillMaxSize().padding(vertical = sizes.paddingValue / 2),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = BiasAlignment.Horizontal(-0.55f)
                        ) {
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize).graphicsLayer(rotationZ = 180f))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize).graphicsLayer(rotationZ = 180f))
                        }
                        Box(Modifier.align(Alignment.Center)) {
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.standardIconSize))
                        }
                    }
                    Rank.TEN -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(vertical = sizes.paddingValue),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = BiasAlignment.Horizontal(0.4f)
                        ) {
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.tenIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.tenIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.graphicsLayer(rotationZ = 180f).size(sizes.tenIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.graphicsLayer(rotationZ = 180f).size(sizes.tenIconSize))
                        }
                        Column(
                            modifier = Modifier.fillMaxSize().padding(vertical = sizes.paddingValue),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = BiasAlignment.Horizontal(-0.4f)
                        ) {
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.tenIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.tenIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.graphicsLayer(rotationZ = 180f).size(sizes.tenIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.graphicsLayer(rotationZ = 180f).size(sizes.tenIconSize))
                        }
                        Column(
                            modifier = Modifier.fillMaxSize().padding(vertical = sizes.dpEqual23),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.tenIconSize))
                            Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.graphicsLayer(rotationZ = 180f).size(sizes.tenIconSize))
                        }
                    }
                    else -> {}
                }
                Column(Modifier.align(Alignment.BottomEnd).width(IntrinsicSize.Min).graphicsLayer(rotationZ = 180f),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = getCardNameClassic(cardData.rank), color = cardData.color, fontSize = sizes.cornerFontSize, fontWeight = FontWeight.SemiBold, fontFamily = OswaldFontFamily,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        ))
                    Icon(imageVector = cardData.vector, contentDescription = null, tint = cardData.color, modifier = Modifier.size(sizes.paddingValue))
                }
            }
        }
    }
}

private fun getCardNameClassic(rank: Rank): String {
    return if(rank.value < 11) rank.value.toString() else "A"
}