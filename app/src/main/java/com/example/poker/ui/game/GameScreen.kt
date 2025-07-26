package com.example.poker.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poker.data.remote.dto.GameState
import com.example.poker.data.remote.dto.PlayerState

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val gameState by viewModel.gameState.collectAsState()
    val myUserId by viewModel.myUserId.collectAsState()
    val playersOnTable by viewModel.playersOnTable.collectAsState()

    val myPlayerState = gameState?.playerStates?.find { it.player.userId == myUserId }
    val activePlayerId = gameState?.playerStates?.getOrNull(gameState!!.activePlayerPosition)?.player?.userId

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val boxWithConstraintsScope = this
        val width = boxWithConstraintsScope.maxWidth
        val height = boxWithConstraintsScope.maxHeight

        ///////
        Box(Modifier.align(Alignment.TopCenter).background(Color.Gray).height(30.dp).fillMaxWidth()) {
            Text("TopBar 30 dp", textAlign = TextAlign.Center, modifier = Modifier.fillMaxSize())
        }
        ////////

        // todo нормально паддинги вписать как константы
        Box(Modifier.padding(0.dp, 30.dp, 0.dp, 63.dp).background(Color(0xFF004D40)).fillMaxSize()) {
            val reorderedPlayers = remember(playersOnTable, myUserId) {
                val myPlayerIndex = playersOnTable.indexOfFirst { it.player.userId == myUserId }
                if (myPlayerIndex != -1) {
                    // Создаем новый список, начиная с нашего игрока
                    playersOnTable.subList(myPlayerIndex, playersOnTable.size) + playersOnTable.subList(0, myPlayerIndex)
                } else {
                    playersOnTable
                }
            }
            val alignments  = calculatePlayerPosition(reorderedPlayers.size)
            reorderedPlayers.forEachIndexed { index, playerState ->
                PlayerDisplay(
                    modifier = Modifier.align(alignments[index]).padding(3.dp),
                    playerState = playerState,
                    isMyPlayer = playerState.player.userId == myUserId,
                    isFourColorMode = true,
                    isGameStarted = gameState != null,
                    scaleMultiplier = 1.2f // todo добавить кастомизацию
                )
            }
            gameState?.let {
                CardsLayout(it, width, Modifier.align(Alignment.Center))
            } ?: Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(
                Alignment.Center)) {
                val myPlayer = playersOnTable.find { it.player.userId == myUserId }?.player
                Text("Waiting for players...", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                if (myPlayer != null) {
                    Button(
                        onClick = { viewModel.onReadyClick(!myPlayer.isReady) }, // Отправляем противоположный статус
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (myPlayer.isReady) Color.Gray else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (myPlayer.isReady) "Cancel Ready" else "I'm Ready")
                    }
                }
            }
        }
        // Отображаем стол
        //PokerTable() // todo можно будет красиво сделать и вернуть

        ActionPanel(
            viewModel = viewModel,
            isMyTurn = gameState != null && activePlayerId == myUserId,
            playerState = myPlayerState,
            gameState = gameState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

private fun calculatePlayerPosition(playersCount: Int): List<BiasAlignment> {
    // 0.68 - центр
    val list = mutableListOf(BiasAlignment(0f, 1f)) // first
    when(playersCount) {
        1 -> return list
        2 -> list.add(BiasAlignment(0f, -1f))
        3 -> {
            list.add(BiasAlignment(-1f, -0.9f))
            list.add(BiasAlignment(1f, -0.9f))
        }
        4 -> {
            list.add(BiasAlignment(-1f, -0.5f))
            list.add(BiasAlignment(0f, -1f))
            list.add(BiasAlignment(1f, -0.5f))
        }
        5 -> {
            list.add(BiasAlignment(-1f, 0.5f))
            list.add(BiasAlignment(-1f, -0.9f))
            list.add(BiasAlignment(1f, -0.9f))
            list.add(BiasAlignment(1f, 0.5f))
        }
        6 -> {
            list.add(BiasAlignment(-1f, 0.5f))
            list.add(BiasAlignment(-1f, -0.5f))
            list.add(BiasAlignment(0f, -1f))
            list.add(BiasAlignment(1f, -0.5f))
            list.add(BiasAlignment(1f, 0.5f))
        }
        7 -> {
            list.add(BiasAlignment(-1f, 0.5f))
            list.add(BiasAlignment(-1f, -0.5f))
            list.add(BiasAlignment(-0.5f, -1f))
            list.add(BiasAlignment(0.5f, -1f))
            list.add(BiasAlignment(1f, -0.5f))
            list.add(BiasAlignment(1f, 0.5f))
        }
        8 -> {
            list.add(BiasAlignment(-1f, 0.5f))
            list.add(BiasAlignment(-1f, -0.4f))
            list.add(BiasAlignment(-1f, -0.85f))
            list.add(BiasAlignment(0f, -1f))
            list.add(BiasAlignment(1f, -0.85f))
            list.add(BiasAlignment(1f, -0.4f))
            list.add(BiasAlignment(1f, 0.5f))
        }
        9 -> {
            list.add(BiasAlignment(-1f, 0.85f))
            list.add(BiasAlignment(-1f, 0.4f))
            list.add(BiasAlignment(-1f, -0.5f))
            list.add(BiasAlignment(-0.5f, -1f))
            list.add(BiasAlignment(0.5f, -1f))
            list.add(BiasAlignment(1f, -0.5f))
            list.add(BiasAlignment(1f, 0.4f))
            list.add(BiasAlignment(1f, 0.85f))
        }
        else -> return listOf()
    }
    return list
}

@Composable
fun CardsLayout(state: GameState, w: Dp, modifier: Modifier) {
    val cardWidth = w / 5
    val cardHeight = cardWidth * 1.5f
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text("Pot: ${state.pot}", color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            // Отображаем 5 общих карт, даже если их еще нет
            (0 until 5).forEach { index ->
                val card = state.communityCards.getOrNull(index)
                PokerCard(
                    card = card,
                    isFourColorMode = true,
                    modifier = Modifier.width(cardWidth).height(cardHeight)
                )
            }
        }
    }
}

@Composable
fun ActionPanel(
    viewModel: GameViewModel,
    isMyTurn: Boolean,
    playerState: PlayerState?, // Состояние текущего игрока
    gameState: GameState?, // Общее состояние игры
    modifier: Modifier
) {
    // Состояние для отображения/скрытия ползунка
    var showBetSlider by remember { mutableStateOf(false) }

    // Используем Box для наложения ползунка поверх панели
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .height(63.dp)
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(Color.Black)
    ) {
        // --- ОСНОВНАЯ ПАНЕЛЬ С ТРЕМЯ КНОПКАМИ ---
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Кнопка FOLD
            BottomButton(onClick = { viewModel.onFold() }, enabled = isMyTurn, text = "Fold", modifier = Modifier.weight(1f))

            // 2. Динамическая кнопка CHECK / CALL
            val amountToCall = gameState?.amountToCall ?: 0L
            val myCurrentBet = playerState?.currentBet ?: 0L

            if (amountToCall == 0L || amountToCall == myCurrentBet) {
                // Если ставить не нужно, показываем CHECK
                BottomButton(onClick = { viewModel.onCheck() }, enabled = isMyTurn, text = "Check", modifier = Modifier.weight(1f))
            } else {
                // Если нужно коллировать, показываем CALL с суммой
                val callValue = minOf(playerState?.player?.stack ?: 0L, amountToCall - myCurrentBet)
                BottomButton(onClick = { viewModel.onCall() }, enabled = isMyTurn, text = "Call $callValue", modifier = Modifier.weight(1f))
            }

            // 3. Кнопка BET / RAISE
            BottomButton(onClick = { showBetSlider = true }, enabled = isMyTurn, text = "Bet", modifier = Modifier.weight(1f))
        }
    }
    // --- ПОЛЗУНОК ДЛЯ СТАВКИ (появляется по условию) ---
    if (showBetSlider && playerState != null && gameState != null) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            BetControls(
                minBet = (gameState.amountToCall - playerState.currentBet) + gameState.lastRaiseAmount,
                maxBet = playerState.player.stack,
                initialBet = (gameState.amountToCall - playerState.currentBet) + gameState.lastRaiseAmount,
                onBetConfirmed = { betAmount ->
                    viewModel.onBet(playerState.currentBet + betAmount)
                    showBetSlider = false // Скрываем ползунок после подтверждения
                },
                onDismiss = { showBetSlider = false }
            )
        }
    }
}

@Composable
fun BetControls(
    minBet: Long,
    maxBet: Long, // Стек игрока
    initialBet: Long,
    onBetConfirmed: (Long) -> Unit,
    onDismiss: () -> Unit // Функция для закрытия
) {
    // Используем String для TextField, но Float для Slider
    var sliderValue by remember { mutableFloatStateOf(initialBet.toFloat()) }
    var textValue by remember { mutableStateOf(initialBet.toString()) }

    // Синхронизация: если меняется слайдер, обновляем текст
    LaunchedEffect(sliderValue) {
        textValue = sliderValue.toLong().toString()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 70.dp, start = 16.dp, end = 16.dp), // Располагаем над основной панелью
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close bet controls",
            tint = Color.White,
            modifier = Modifier.align(Alignment.End)
                .size(30.dp)
                .clip(CircleShape)
                .background(Color.DarkGray)
                .border(1.dp, Color.White, shape = CircleShape)
                .clickable(onClick = onDismiss)
        )
        Column(
            modifier = Modifier.padding(16.dp, 3.dp, 16.dp, 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                valueRange = minBet.toFloat()..maxBet.toFloat()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        // Синхронизация: если меняется текст, обновляем слайдер
                        sliderValue = it.toFloatOrNull() ?: 0f
                    },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onBetConfirmed(textValue.toLongOrNull() ?: 0) }) {
                    Text("Confirm")
                }
            }
        }
    }
}

// todo возможно не будет использоваться
@Composable
fun PokerTable() {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .aspectRatio(0.6f)
            .background(Color(0xFF00695C), shape = RoundedCornerShape(percent = 50)) // Зеленый фон и овальная форма
            .border(4.dp, Color(0xFF004D40), shape = RoundedCornerShape(percent = 50)) // Темно-зеленая рамка
    )
}

@Composable
fun BottomButton(onClick: () -> Unit, enabled: Boolean, text: String, modifier: Modifier) {
    val color1 = if(enabled) Color.Red else Color(0xFF640D14)
    val color2 = if(enabled) Color(0xFF640D14) else Color.Black
    FilledTonalButton(
        modifier = modifier.height(60.dp).padding(1.dp, 0.dp),
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonColors(Color.Red.copy(alpha = 0.7f), Color.Black, Color(0xFF38070B), Color.Black),
        border = BorderStroke(5.dp, Brush.radialGradient(listOf(color1, color2), radius = 170f))
    ) {
        Text(text, fontSize = 18.sp, textAlign = TextAlign.Center)
    }
}

class TrapezoidShape(private val slantWidth: Float = 20f) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(0f, 0f) // Верхний левый угол
            lineTo(size.width, 0f) // Верхний правый угол
            lineTo(size.width - slantWidth, size.height) // Нижний правый угол (смещенный)
            lineTo(slantWidth, size.height) // Нижний левый угол (смещенный)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun PlayerDisplay(
    playerState: PlayerState,
    isMyPlayer: Boolean,
    isFourColorMode: Boolean,
    modifier: Modifier = Modifier,
    isGameStarted: Boolean,
    scaleMultiplier: Float
) {
    Box(modifier = modifier
        .width(IntrinsicSize.Max)
        .height(80.dp * scaleMultiplier)
        .padding(5.dp * scaleMultiplier, 0.dp)) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Player Avatar",
            tint = Color.White,
            modifier = Modifier
                .size(60.dp * scaleMultiplier)
                .clip(CircleShape)
                .background(Color.DarkGray)
                .border(1.dp, Color.White, shape = CircleShape)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy((-20).dp * scaleMultiplier),
            modifier = Modifier.align(Alignment.Center)
        ) {
            if (isGameStarted) {
                val (card1, card2, isShow) = if (isMyPlayer || playerState.cards.isNotEmpty()) {
                    Triple(playerState.cards.getOrNull(0), playerState.cards.getOrNull(1),true)
                } else {
                    if(playerState.hasFolded) {
                        Triple(null, null, false)
                    } else Triple(null, null, true)
                }
                if(isShow) {
                    PokerCard(
                        card = card1,
                        isFourColorMode = isFourColorMode,
                        modifier = Modifier
                            .width(40.dp * scaleMultiplier)
                            .height(60.dp * scaleMultiplier)
                            .graphicsLayer { rotationZ = -10f }
                    )
                    PokerCard(
                        card = card2,
                        isFourColorMode = isFourColorMode,
                        modifier = Modifier
                            .width(40.dp * scaleMultiplier)
                            .height(60.dp * scaleMultiplier)
                            .graphicsLayer { rotationZ = 10f }
                    )
                }
            } else {
                if(playerState.player.isReady) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Is ready",
                        tint = Color.Green,
                        modifier = Modifier.size(60.dp * scaleMultiplier).offset(0.dp, (-10).dp * scaleMultiplier)
                    )
                }
            }
        }
        Column(modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .clip(TrapezoidShape(10f))
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Black)) {
            Text(
                text = playerState.player.username,
                color = Color.White,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp * scaleMultiplier,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(0.dp, 1.dp),
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    ),
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.Both
                    )
                )
            )
            HorizontalDivider()
            Text(
                text = playerState.player.stack.toString(),
                color = Color.White,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp * scaleMultiplier,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(0.dp, 1.dp),
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    ),
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.Both
                    )
                )
            )
        }
    }
}

// todo использовать в главном экране
//val configuration = LocalConfiguration.current
//when (configuration.orientation) {
//    Configuration.ORIENTATION_LANDSCAPE -> { /* Логика для ландшафта */ }
//    else -> { /* Логика для портрета */ }
//}