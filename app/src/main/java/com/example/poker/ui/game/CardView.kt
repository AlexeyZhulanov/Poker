package com.example.poker.ui.game

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poker.data.remote.dto.Card
import com.example.poker.domain.model.Rank
import com.example.poker.domain.model.Suit
import com.example.poker.ui.theme.OswaldFontFamily

@Composable
fun CardBack() {
    Card(
        modifier = Modifier
            .width(80.dp)
            .height(120.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.Black),
        colors = CardDefaults.cardColors(containerColor = Color.Blue)
    ) {
        // Здесь можно добавить узор для рубашки
    }
}

@Composable
fun CardFace(card: Card) {
    val rank = card.rank // "ACE", "KING", "TEN", "NINE"
    val suit = card.suit // "SPADES", "HEARTS"

    val suitVector = when(suit) {
        Suit.HEARTS -> CardSuits.Heart
        Suit.DIAMONDS -> CardSuits.Diamond
        Suit.CLUBS -> CardSuits.Club
        else -> CardSuits.Spade
    }
    val suitColor = if (suit == Suit.HEARTS || suit == Suit.DIAMONDS) Color.Red else Color.Black

    Card(
        modifier = Modifier
            .width(80.dp)
            .height(120.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.Gray),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        when (rank) {
            Rank.JACK, Rank.QUEEN, Rank.KING, Rank.ACE -> {
                // ПОКАЗЫВАЕМ ГОТОВУЮ КАРТИНКУ
                // Вам нужно будет создать логику для выбора правильного drawable
                // Например, по имени "face_card_${rank.lowercase()}_of_${suit.lowercase()}"
                // Image(painter = painterResource(id = R.drawable.your_face_card_image), ...)
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = rank.value.toString(), fontSize = 32.sp, color = suitColor)
                }
            }
            else -> {
                // РИСУЕМ ЦИФРОВУЮ КАРТУ
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)) {
                    // Уголки карты
                    Column(Modifier.align(Alignment.TopStart), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = rank.value.toString(), color = suitColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, fontFamily = OswaldFontFamily)
                        Icon(imageVector = suitVector, contentDescription = null, tint = suitColor,
                            modifier = Modifier
                                .size(10.dp)
                                .offset(0.5.dp, 0.dp))
                    }
                    when(rank) {
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
                        Text(text = rank.value.toString(), color = suitColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, fontFamily = OswaldFontFamily)
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

@Composable
@Preview
fun Test() {
    CardFace(Card(Rank.TEN, Suit.DIAMONDS))
}

// Главный Composable, который решает, что показать
@Composable
fun PokerCard(card: Card, isFaceUp: Boolean) {
    if (isFaceUp) {
        CardFace(card = card)
    } else {
        CardBack()
    }
}