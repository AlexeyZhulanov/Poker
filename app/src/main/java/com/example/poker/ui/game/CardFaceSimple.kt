package com.example.poker.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.poker.data.remote.dto.Card
import com.example.poker.domain.model.Rank
import com.example.poker.domain.model.Suit
import com.example.poker.ui.theme.CardCharactersFontFamily

@Composable
fun CardFaceSimple(card: Card, modifier: Modifier) {
    val rank = card.rank
    val suit = card.suit
    val suitData = remember(suit) {
        val suitVector = when(suit) {
            Suit.HEARTS -> CardSuits.Heart
            Suit.DIAMONDS -> CardSuits.Diamond
            Suit.CLUBS -> CardSuits.Club
            else -> CardSuits.Spade
        }
        val suitColor = when(suit) {
            Suit.HEARTS -> Color(0xFF821013)
            Suit.SPADES -> Color(0xFF232322)
            Suit.DIAMONDS -> Color(0xFF104886)
            Suit.CLUBS -> Color(0xFF0C7618)
        }
        object {
            val vector = suitVector
            val color = suitColor
        }
    }
    val (cardName, borderColor) = remember(rank) {
        getCardName(rank) to if(isBroadway(rank)) Color(0xFFFFBF00) else Color.Gray
    }
    val isTen = rank == Rank.TEN

    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.background(
            color = suitData.color,
            shape = RoundedCornerShape(8.dp)
        ).border(1.dp, borderColor, RoundedCornerShape(8.dp))
    ) {
        BoxWithConstraints(Modifier.weight(1.5f).fillMaxWidth(),
            contentAlignment = Alignment.TopCenter) {
            val fontSize = with(LocalDensity.current) {
                if(isTen) (maxHeight * 0.7f).toSp() else (maxHeight * 0.85f).toSp()
            }
            Text(text = cardName, color = Color.White, fontWeight = FontWeight.SemiBold, fontFamily = CardCharactersFontFamily,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    ),
                    lineHeight = TextUnit.Unspecified
                ),
                softWrap = false,
                fontSize = fontSize
            )
        }
        Icon(
            imageVector = suitData.vector,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.weight(1f).fillMaxWidth(fraction = 0.5f).padding(bottom = 5.dp)
        )
    }
}

fun isBroadway(rank: Rank): Boolean = rank.value >= 11

@Composable
@Preview
fun TestSimpleCard() {
    CardFaceSimple(Card(Rank.ACE, Suit.SPADES), Modifier.width(80.dp).height(120.dp))
}