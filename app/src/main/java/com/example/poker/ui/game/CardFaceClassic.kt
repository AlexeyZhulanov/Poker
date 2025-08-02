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
import com.example.poker.data.remote.dto.Card
import com.example.poker.domain.model.Rank
import com.example.poker.domain.model.Suit
import com.example.poker.ui.theme.GreenCard
import com.example.poker.ui.theme.OswaldFontFamily

@Composable
fun CardFaceClassic(card: Card, isFourColorMode: Boolean, modifier: Modifier) {
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
        modifier = modifier,
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
                BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(3.dp)) {
                    val boxWithConstraintsScope = this
                    val cornerFontSize = (boxWithConstraintsScope.maxHeight.value * 0.16f).sp
                    val standardIconSize = boxWithConstraintsScope.maxHeight / 6.66f
                    val aceIconSize = boxWithConstraintsScope.maxHeight / 5
                    val paddingValue = boxWithConstraintsScope.maxHeight / 12
                    val dpEqual28 = boxWithConstraintsScope.maxHeight / 4.28f
                    val dpEqual23 = boxWithConstraintsScope.maxHeight / 5.2f
                    val tenIconSize = boxWithConstraintsScope.maxHeight / 10

                    Column(Modifier.align(Alignment.TopStart).width(IntrinsicSize.Min), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = getCardNameClassic(rank), color = suitColor, fontSize = cornerFontSize, fontWeight = FontWeight.SemiBold, fontFamily = OswaldFontFamily,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false
                                )
                            )
                        )
                        Icon(imageVector = suitVector, contentDescription = null, tint = suitColor,
                            modifier = Modifier
                                .size(paddingValue))
                        //.offset(0.5.dp, 0.dp))
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
                                    modifier = Modifier.size(aceIconSize)
                                )
                            }
                        }
                        Rank.TWO -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(0.dp, paddingValue),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize).graphicsLayer(rotationZ = 180f)
                                )
                            }
                        }
                        Rank.THREE -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(0.dp, paddingValue),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize).graphicsLayer(rotationZ = 180f)
                                )
                            }
                        }
                        Rank.FOUR -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(0.dp, paddingValue),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(0.55f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize).graphicsLayer(rotationZ = 180f)
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(0.dp, paddingValue),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(-0.55f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize).graphicsLayer(rotationZ = 180f)
                                )
                            }
                        }
                        Rank.FIVE -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(0.dp, paddingValue),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(0.55f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize).graphicsLayer(rotationZ = 180f)
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(0.dp, paddingValue),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(-0.55f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize).graphicsLayer(rotationZ = 180f)
                                )
                            }
                            Box(
                                Modifier.align(Alignment.Center)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                            }
                        }
                        Rank.SIX -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(0.dp, paddingValue),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(0.55f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize).graphicsLayer(rotationZ = 180f)
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(0.dp, paddingValue),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(-0.55f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize).graphicsLayer(rotationZ = 180f)
                                )
                            }
                        }
                        Rank.SEVEN -> {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(0.dp, paddingValue),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(0.55f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize).graphicsLayer(rotationZ = 180f)
                                )
                            }
                            Column(
                                modifier = Modifier.fillMaxSize().padding(0.dp, paddingValue),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(-0.55f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize).graphicsLayer(rotationZ = 180f)
                                )
                            }
                            Box(
                                Modifier.align(BiasAlignment(0f, -0.4f))
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                            }
                        }
                        Rank.EIGHT -> {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(0.dp, paddingValue),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(0.55f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize).graphicsLayer(rotationZ = 180f)
                                )
                            }
                            Column(
                                modifier = Modifier.fillMaxSize().padding(0.dp, paddingValue),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(-0.55f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize).graphicsLayer(rotationZ = 180f)
                                )
                            }
                            Column(
                                modifier = Modifier.fillMaxSize().padding(0.dp, dpEqual28),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize).graphicsLayer(rotationZ = 180f)
                                )
                            }
                        }
                        Rank.NINE -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(0.dp, paddingValue / 2),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(0.55f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize).graphicsLayer(rotationZ = 180f)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize).graphicsLayer(rotationZ = 180f)
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(0.dp, paddingValue / 2),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(-0.55f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize).graphicsLayer(rotationZ = 180f)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize).graphicsLayer(rotationZ = 180f)
                                )
                            }
                            Box(
                                Modifier.align(Alignment.Center)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(standardIconSize)
                                )
                            }
                        }
                        Rank.TEN -> {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(0.dp, paddingValue),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(0.4f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(tenIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(tenIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f).size(tenIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f).size(tenIconSize)
                                )
                            }
                            Column(
                                modifier = Modifier.fillMaxSize().padding(0.dp, paddingValue),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = BiasAlignment.Horizontal(-0.4f)
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(tenIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(tenIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f).size(tenIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f).size(tenIconSize)
                                )
                            }
                            Column(
                                modifier = Modifier.fillMaxSize().padding(0.dp, dpEqual23),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.size(tenIconSize)
                                )
                                Icon(
                                    imageVector = suitVector,
                                    contentDescription = null,
                                    tint = suitColor,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f).size(tenIconSize)
                                )
                            }
                        }
                        else -> {}
                    }
                    Column(Modifier
                        .align(Alignment.BottomEnd).width(IntrinsicSize.Min)
                        .graphicsLayer(rotationZ = 180f),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = getCardNameClassic(rank), color = suitColor, fontSize = cornerFontSize, fontWeight = FontWeight.SemiBold, fontFamily = OswaldFontFamily,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false
                                )
                            ))
                        Icon(imageVector = suitVector, contentDescription = null, tint = suitColor,
                            modifier = Modifier.size(paddingValue))
                        //.offset(0.5.dp, 0.dp))
                    }
                }
            }
        }
    }
}

private fun getCardNameClassic(rank: Rank): String {
    return if(rank.value < 11) rank.value.toString() else "A"
}