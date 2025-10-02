package com.example.poker.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import com.example.poker.shared.model.Card
import com.example.poker.shared.model.Rank
import com.example.poker.shared.model.Suit
import com.example.poker.ui.theme.CardCharactersFontFamily
import com.example.poker.ui.theme.OswaldFontFamily
import com.example.poker.util.getCardName

@Composable
fun CardFaceSimple(card: Card, modifier: Modifier, scaleMultiplier: Float, isNeedBorder: Boolean, minMultiplier: Float = 1f) {
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
    val scaleData = remember(scaleMultiplier) {
        object {
            val shape = RoundedCornerShape(8.dp * scaleMultiplier * minMultiplier)
            val borderWidth = 1.dp * scaleMultiplier * minMultiplier
            val bottomPadding = 5.dp * scaleMultiplier * minMultiplier
        }
    }
    val cardName = remember(rank) { getCardName(rank) }
    val isTen = rank == Rank.TEN
    val modInitial = modifier.background(color = suitData.color, shape = scaleData.shape)
    val modFinal = if(isNeedBorder) modInitial.border(scaleData.borderWidth, Color.Gray, scaleData.shape) else modInitial

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modFinal) {
        BoxWithConstraints(Modifier.weight(1.5f).fillMaxWidth(),
            contentAlignment = Alignment.TopCenter) {
            val fontSize = with(LocalDensity.current) { if(isTen) (maxHeight * 0.83f).toSp() else (maxHeight * 0.85f).toSp() }
            val (fontFamily, fontWeight) = if(isTen) OswaldFontFamily to FontWeight.Normal else CardCharactersFontFamily to FontWeight.SemiBold
            val modifier = if(isTen) Modifier.offset(x = maxWidth * 0.04f, y = maxHeight * -0.15f) else Modifier
            Text(text = cardName, color = Color.White, fontWeight = fontWeight, fontFamily = fontFamily,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    ),
                    lineHeight = TextUnit.Unspecified
                ),
                softWrap = false,
                fontSize = fontSize,
                modifier = modifier
            )
        }
        Icon(
            imageVector = suitData.vector,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.weight(1f).fillMaxWidth(fraction = 0.5f).padding(bottom = scaleData.bottomPadding)
        )
    }
}

@Composable
@Preview
fun TestSimpleCard() {
    CardFaceSimple(Card(Rank.TEN, Suit.SPADES), Modifier.width(30.dp).height(45.dp), 1f, false)
}