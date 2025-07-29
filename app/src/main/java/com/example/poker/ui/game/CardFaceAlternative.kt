package com.example.poker.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poker.data.remote.dto.Card
import com.example.poker.domain.model.Rank
import com.example.poker.domain.model.Suit
import com.example.poker.ui.theme.CardCharactersFontFamily

@Composable
fun CardFaceAlternative(card: Card, modifier: Modifier) {
    val rank = card.rank
    val suit = card.suit
    val cardName = getCardName(rank)
    val isTen = rank == Rank.TEN

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
    val borderColor = when(suit) {
        Suit.HEARTS -> Color(0xFF972828)
        Suit.SPADES -> Color(0xFF584B4C)
        Suit.DIAMONDS -> Color(0xFF256189)
        Suit.CLUBS -> Color(0xFF448335)
    }
    val gradientBrush = Brush.linearGradient(
        colors = listOf(borderColor, suitColor),
        start = Offset.Zero,
        end = Offset.Infinite
    )
    Box(
        modifier = modifier.background(
            brush = gradientBrush,
            shape = RoundedCornerShape(8.dp)
        ).border(2.dp, borderColor, RoundedCornerShape(8.dp))
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(3.dp)) {
            val boxWithConstraintsScope = this
            val topTextSize = if(isTen) (boxWithConstraintsScope.maxHeight.value / 5).sp else (boxWithConstraintsScope.maxHeight.value / 4).sp
            val topIconSize = boxWithConstraintsScope.maxHeight / 5
            val bottomTextSize = if(isTen) (boxWithConstraintsScope.maxHeight.value * 0.65f * 0.8f).sp else (boxWithConstraintsScope.maxHeight.value * 0.65f).sp
            val topSpacing = (boxWithConstraintsScope.maxWidth.value * -0.02f).sp
            val bottomSpacing = (boxWithConstraintsScope.maxWidth.value * -0.08f).sp
            Column(Modifier.align(Alignment.TopStart).width(IntrinsicSize.Min), horizontalAlignment = Alignment.Start) {
                Text(text = cardName, color = Color.White, fontSize = topTextSize, fontWeight = FontWeight.SemiBold, fontFamily = CardCharactersFontFamily,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        ),
                        lineHeight = TextUnit.Unspecified
                    ),
                    softWrap = false,
                    letterSpacing = if (isTen) topSpacing else TextUnit.Unspecified
                )
                Icon(imageVector = suitVector, contentDescription = null, tint = Color.White,
                    modifier = Modifier.size(topIconSize))
            }
            Text(text = cardName, color = Color.White, fontSize = bottomTextSize, fontWeight = FontWeight.SemiBold, fontFamily = CardCharactersFontFamily,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    ),
                    lineHeight = TextUnit.Unspecified
                ),
                softWrap = false,
                overflow = TextOverflow.Visible,
                modifier = Modifier.align(BiasAlignment(0.7f, 1f)),
                letterSpacing = if (isTen) bottomSpacing else TextUnit.Unspecified
            )
        }
    }
}

private fun getCardName(rank: Rank): String {
    return if(rank.value < 11) rank.value.toString() else {
        when(rank) {
            Rank.JACK -> "J"
            Rank.QUEEN -> "Q"
            Rank.KING -> "K"
            Rank.ACE -> "A"
            else -> ""
        }
    }
}