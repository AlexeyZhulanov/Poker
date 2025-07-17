package com.example.poker.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poker.R
import com.example.poker.data.remote.dto.Card
import com.example.poker.domain.model.Rank
import com.example.poker.domain.model.Suit
import com.example.poker.ui.theme.GreenCard
import com.example.poker.ui.theme.OswaldFontFamily

@Composable
fun CardBack() {
    val backColor = Color(0xFF1D4ED8) // Насыщенный синий
    val patternColor = Color(0xFF3B82F6) // Более светлый синий для узора

    Card(
        modifier = Modifier
            .width(80.dp)
            .height(120.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(4.dp, Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backColor)
                .padding(4.dp) // Внутренний отступ, чтобы узор не заходил на рамку
        ) {
            // Узор
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 2.dp.toPx()
                val step = 10.dp.toPx()

                // Диагональные линии слева направо
                for (i in -size.height.toInt()..size.width.toInt() step step.toInt()) {
                    drawLine(
                        color = patternColor,
                        start = Offset(x = i.toFloat(), y = 0f),
                        end = Offset(x = i.toFloat() + size.height, y = size.height),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }

                // Диагональные линии справа налево
                for (i in 0..(size.width.toInt() + size.height.toInt()) step step.toInt()) {
                    drawLine(
                        color = patternColor,
                        start = Offset(x = i.toFloat(), y = 0f),
                        end = Offset(x = i.toFloat() - size.height, y = size.height),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
fun CardFace(card: Card, isFourColorMode: Boolean) {
    val rank = card.rank
    val suit = card.suit

    val suitVector = when(suit) {
        Suit.HEARTS -> CardSuits.Heart
        Suit.DIAMONDS -> CardSuits.Diamond
        Suit.CLUBS -> CardSuits.Club
        else -> CardSuits.Spade
    }
    val suitColor = if(isFourColorMode) {
        when(suit) {
            Suit.HEARTS -> Color.Red
            Suit.SPADES -> Color.Black
            Suit.DIAMONDS -> Color.Blue
            Suit.CLUBS -> GreenCard // #00aa00
        }
    } else {
        if (suit == Suit.HEARTS || suit == Suit.DIAMONDS) Color.Red else Color.Black
    }

    Card(
        modifier = Modifier
            .width(80.dp)
            .height(120.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.Gray),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        when (rank) {
            Rank.JACK, Rank.QUEEN, Rank.KING -> {
                when (rank) {
                    Rank.JACK -> {
                        val drawableId = when(suit) {
                            Suit.DIAMONDS -> if(isFourColorMode) R.drawable.jack_diamonds_fc else R.drawable.jack_diamonds
                            Suit.HEARTS -> R.drawable.jack_hearts
                            Suit.CLUBS -> if(isFourColorMode) R.drawable.jack_clubs_fc else R.drawable.jack_clubs
                            Suit.SPADES -> R.drawable.jack_spades
                        }
                        Image(
                            painter = painterResource(id = drawableId),
                            contentDescription = "Card Jack",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    Rank.QUEEN -> {
                        val drawableId = when(suit) {
                            Suit.DIAMONDS -> if(isFourColorMode) R.drawable.queen_diamonds_fc else R.drawable.queen_diamonds
                            Suit.HEARTS -> R.drawable.queen_hearts
                            Suit.CLUBS -> if(isFourColorMode) R.drawable.queen_clubs_fc else R.drawable.queen_clubs
                            Suit.SPADES -> R.drawable.queen_spades
                        }
                        Image(
                            painter = painterResource(id = drawableId),
                            contentDescription = "Card Queen",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    Rank.KING -> {
                        val drawableId = when(suit) {
                            Suit.DIAMONDS -> if(isFourColorMode) R.drawable.king_diamonds_fc else R.drawable.king_diamonds
                            Suit.HEARTS -> R.drawable.king_hearts
                            Suit.CLUBS -> if(isFourColorMode) R.drawable.king_clubs_fc else R.drawable.king_clubs
                            Suit.SPADES -> R.drawable.king_spades
                        }
                        Image(
                            painter = painterResource(id = drawableId),
                            contentDescription = "Card King",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                    }
                    else -> {}
                }
            }
            else -> {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)) {
                    Column(Modifier.align(Alignment.TopStart), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = getCardName(rank), color = suitColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, fontFamily = OswaldFontFamily)
                        Icon(imageVector = suitVector, contentDescription = null, tint = suitColor,
                            modifier = Modifier
                                .size(10.dp)
                                .offset(0.5.dp, 0.dp))
                    }
                    when(rank) {
                        Rank.ACE -> {
                            Box(
                                Modifier.align(Alignment.Center)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Rank.TWO -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(0.dp, 10.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f)
                                )
                            }
                        }
                        Rank.THREE -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(0.dp, 10.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f)
                                )
                            }
                        }
                        Rank.FOUR -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(0.dp, 10.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(0.55f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f)
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(0.dp, 10.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(-0.55f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f)
                                )
                            }
                        }
                        Rank.FIVE -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(0.dp, 10.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(0.55f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f)
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(0.dp, 10.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(-0.55f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f)
                                )
                            }
                            Box(
                                Modifier.align(Alignment.Center)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                            }
                        }
                        Rank.SIX -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(0.dp, 10.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(0.55f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f)
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(0.dp, 10.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(-0.55f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f)
                                )
                            }
                        }
                        Rank.SEVEN -> {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(0.dp, 10.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(0.55f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f)
                                )
                            }
                            Column(
                                modifier = Modifier.fillMaxSize().padding(0.dp, 10.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(-0.55f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f)
                                )
                            }
                            Box(
                                Modifier.align(BiasAlignment(0f, -0.4f))
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                            }
                        }
                        Rank.EIGHT -> {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(0.dp, 10.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(0.55f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f)
                                )
                            }
                            Column(
                                modifier = Modifier.fillMaxSize().padding(0.dp, 10.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(-0.55f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f)
                                )
                            }
                            Column(
                                modifier = Modifier.fillMaxSize().padding(0.dp, 28.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f)
                                )
                            }
                        }
                        Rank.NINE -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(0.dp, 5.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(0.55f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f)
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(0.dp, 5.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(-0.55f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f)
                                )
                            }
                            Box(
                                Modifier.align(Alignment.Center)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor
                                )
                            }
                        }
                        Rank.TEN -> {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(0.dp, 10.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(0.4f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f).size(12.dp)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f).size(12.dp)
                                )
                            }
                            Column(
                                modifier = Modifier.fillMaxSize().padding(0.dp, 10.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(-0.4f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f).size(12.dp)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f).size(12.dp)
                                )
                            }
                            Column(
                                modifier = Modifier.fillMaxSize().padding(0.dp, 23.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f).size(12.dp)
                                )
                            }
                        }
                        else -> {}
                    }
                    Column(Modifier
                        .align(Alignment.BottomEnd)
                        .graphicsLayer(rotationZ = 180f),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = getCardName(rank), color = suitColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, fontFamily = OswaldFontFamily)
                        Icon(imageVector = suitVector, contentDescription = null, tint = suitColor,
                            modifier = Modifier
                                .size(10.dp)
                                .offset(0.5.dp, 0.dp))
                    }
                }
            }
        }
    }
}

private fun getCardName(rank: Rank): String {
    return if(rank.value < 11) rank.value.toString() else "A"
}

@Composable
@Preview
fun Test() {
    //CardFace(Card(Rank.SIX, Suit.CLUBS), true)
    CardBack()
}

@Composable
fun PokerCard(card: Card, isFaceUp: Boolean, isFourColorMode: Boolean) {
    if (isFaceUp) {
        CardFace(card = card, isFourColorMode)
    } else {
        CardBack()
    }
}