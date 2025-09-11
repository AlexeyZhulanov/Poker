package com.example.poker.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poker.shared.model.Card
import com.example.poker.shared.model.Rank
import com.example.poker.shared.model.Suit
import com.example.poker.ui.theme.CardCharactersFontFamily
import com.example.poker.util.getCardName

@Composable
fun CardFaceAlternative(card: Card, modifier: Modifier, scaleMultiplier: Float) {
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
        val borderColor = when(suit) {
            Suit.HEARTS -> Color(0xFF972828)
            Suit.SPADES -> Color(0xFF584B4C)
            Suit.DIAMONDS -> Color(0xFF256189)
            Suit.CLUBS -> Color(0xFF448335)
        }
        object {
            val vector = suitVector
            val color = suitColor
            val border = borderColor
            val brush = Brush.linearGradient(colors = listOf(border, color))
        }
    }
    val scaleData = remember(scaleMultiplier) {
        object {
            val shape = RoundedCornerShape(8.dp * scaleMultiplier)
            val borderWidth = 2.dp * scaleMultiplier
            val paddingAll = 3.dp * scaleMultiplier
        }
    }
    val cardName = remember(rank) { getCardName(rank) }
    val isTen = rank == Rank.TEN
    Box(
        modifier = modifier.background(
            brush = suitData.brush,
            shape = scaleData.shape
        ).border(scaleData.borderWidth, suitData.border, scaleData.shape)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(scaleData.paddingAll)) {
            val sizes = remember(isTen, maxWidth, maxHeight) {
                object {
                    val topTextSize = if(isTen) (maxHeight.value / 5).sp else (maxHeight.value / 4).sp
                    val topIconSize = maxHeight / 5.5f
                    val bottomTextSize = if(isTen) (maxHeight.value * 0.65f * 0.8f).sp else (maxHeight.value * 0.65f).sp
                    val topSpacing = (maxWidth.value * -0.02f).sp
                    val bottomSpacing = (maxWidth.value * -0.08f).sp
                }
            }
            Column(Modifier.align(Alignment.TopStart), horizontalAlignment = Alignment.Start) {
                Text(text = cardName, color = Color.White, fontSize = sizes.topTextSize, fontWeight = FontWeight.SemiBold, fontFamily = CardCharactersFontFamily,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        ),
                        lineHeight = TextUnit.Unspecified
                    ),
                    softWrap = false,
                    letterSpacing = if (isTen) sizes.topSpacing else TextUnit.Unspecified
                )
                Icon(imageVector = suitData.vector, contentDescription = null, tint = Color.White,
                    modifier = Modifier.size(sizes.topIconSize).align(Alignment.CenterHorizontally))
            }
            Text(text = cardName, color = Color.White, fontSize = sizes.bottomTextSize, fontWeight = FontWeight.SemiBold, fontFamily = CardCharactersFontFamily,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    ),
                    lineHeight = TextUnit.Unspecified
                ),
                softWrap = false,
                overflow = TextOverflow.Visible,
                modifier = Modifier.align(BiasAlignment(0.7f, 1f)),
                letterSpacing = if (isTen) sizes.bottomSpacing else TextUnit.Unspecified
            )
        }
    }
}