package com.example.poker.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poker.data.remote.dto.Card
import com.example.poker.data.remote.dto.GameState
import com.example.poker.data.remote.dto.OutsInfo
import com.example.poker.data.remote.dto.Player
import com.example.poker.data.remote.dto.PlayerState
import com.example.poker.data.remote.dto.PlayerStatus
import com.example.poker.domain.model.Suit
import com.example.poker.ui.theme.MerriWeatherFontFamily
import kotlin.random.Random

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val gameState by viewModel.gameState.collectAsState()
    val myUserId by viewModel.myUserId.collectAsState()
    val playersOnTable by viewModel.playersOnTable.collectAsState()
    val runItState by viewModel.runItUiState.collectAsState()
    val isActionPanelLocked by viewModel.isActionPanelLocked.collectAsState()
    val allInEquity by viewModel.allInEquity.collectAsState()
    val staticCards by viewModel.staticCommunityCards.collectAsState()
    val boardRunouts by viewModel.boardRunouts.collectAsState()

    val myPlayerState = gameState?.playerStates?.find { it.player.userId == myUserId }
    val activePlayerId = gameState?.playerStates?.getOrNull(gameState!!.activePlayerPosition)?.player?.userId

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val boxWithConstraintsScope = this
        val width = boxWithConstraintsScope.maxWidth
        //val height = boxWithConstraintsScope.maxHeight

        val specsCount = playersOnTable.filter { it.player.status == PlayerStatus.SPECTATING }.size
        val animatedCount by animateIntAsState(
            targetValue = specsCount,
            animationSpec = tween(durationMillis = 300)
        )

        ///////
        Box(Modifier.align(Alignment.TopCenter).background(Color.Gray).height(30.dp).fillMaxWidth()) {
            Text("TopBar 30 dp", textAlign = TextAlign.Center, modifier = Modifier.fillMaxSize())
            if (animatedCount != 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.CenterEnd).padding(3.dp, 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Visibility,
                        modifier = Modifier.size(20.dp),
                        contentDescription = null,
                        tint = Color.White)

                    Text(
                        text = animatedCount.toString(),
                        color = Color.White,
                        fontSize = 16.sp)
                }
            }
        }
        ////////

        // todo нормально паддинги вписать как константы
        Box(Modifier.padding(0.dp, 30.dp, 0.dp, 63.dp).background(Color(0xFF004D40)).fillMaxSize()) {
            val reorderedPlayers = remember(playersOnTable, myUserId) {
                val visiblePlayers = playersOnTable.filter { it.player.status != PlayerStatus.SPECTATING }
                val myPlayerIndex = visiblePlayers.indexOfFirst { it.player.userId == myUserId }
                if (myPlayerIndex != -1) {
                    // Создаем новый список, начиная с нашего игрока
                    visiblePlayers.subList(myPlayerIndex, visiblePlayers.size) + visiblePlayers.subList(0, myPlayerIndex)
                } else {
                    visiblePlayers
                }
            }
            val (alignments, equityPositions) = calculatePlayerPosition(reorderedPlayers.size)
            reorderedPlayers.forEachIndexed { index, playerState ->
                val isRightTailDirection = equityPositions[index]
                val (offset, tailDirection) = if(isRightTailDirection) (-80).dp to TailDirection.RIGHT else 80.dp to TailDirection.LEFT
                val scaleMultiplier = 1.2f // todo добавить кастомизацию
                val isMyPlayer = playerState.player.userId == myUserId
                val verticalOffset = if(isMyPlayer) (-25).dp else 15.dp

                allInEquity?.let { (equities, outs, _) ->
                    val equity = equities[playerState.player.userId]
                    val out = outs[playerState.player.userId]
                    if(out == null && equity != null) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.align(alignments[index]).offset(offset * scaleMultiplier * 0.8f, verticalOffset * scaleMultiplier)) {
                            EquityBubble(equity, tailDirection, scaleMultiplier)
                        }
                    } else if(out != null) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.align(alignments[index]).offset(offset * scaleMultiplier, 0.dp)) {
                            OutsBubble(equity, out, scaleMultiplier)
                        }
                    }
                }

                PlayerDisplay(
                    modifier = Modifier.align(alignments[index]).padding(3.dp),
                    playerState = playerState,
                    isMyPlayer = isMyPlayer,
                    isFourColorMode = true,
                    isGameStarted = gameState != null,
                    scaleMultiplier = scaleMultiplier
                )
            }
            if (boardRunouts.isNotEmpty()) {
                MultiBoardLayout(staticCards = staticCards, runouts = boardRunouts, modifier = Modifier.align(Alignment.CenterStart))
            } else {
                gameState?.let {
                    SingleBoardLayout(it, width, Modifier.align(Alignment.Center))
                } ?: Column(modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color(0xFF00695C), shape = RoundedCornerShape(percent = 50))
                    .border(4.dp, Color(0xFF004D40), shape = RoundedCornerShape(percent = 50))
                    .padding(32.dp, 16.dp)
                ) {
                    Text("Waiting for players...", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                    if(animatedCount != 0) {
                        Spacer(Modifier.size(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Visibility,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp).padding(2.dp, 0.dp))

                            Text(
                                text = animatedCount.toString(),
                                color = Color.White,
                                fontSize = 20.sp)
                        }
                    }
                }
            }
        }
        // Отображаем стол
        //PokerTable() // todo можно будет красиво сделать и вернуть

        val myPlayer = playersOnTable.find { it.player.userId == myUserId }?.player

        when (val state = runItState) {
            is RunItUiState.Hidden -> {
                ActionPanel(
                    viewModel = viewModel,
                    isMyTurn = gameState != null && activePlayerId == myUserId && !isActionPanelLocked,
                    myPlayer = myPlayer,
                    playerState = myPlayerState,
                    gameState = gameState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
            is RunItUiState.AwaitingUnderdogChoice -> {
                UnderdogChoiceUi(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onChoice = { times -> viewModel.onRunItChoice(times) }
                )
            }
            is RunItUiState.AwaitingFavoriteConfirmation -> {
                val underdogName = playersOnTable.find { it.player.userId == state.underdogId }?.player?.username
                FavoriteConfirmationUi(
                    underdogName = underdogName ?: state.underdogId,
                    times = state.times,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onConfirm = { accepted -> viewModel.onRunItConfirmation(accepted) }
                )
            }
        }
    }
}

private fun calculatePlayerPosition(playersCount: Int): Pair<List<BiasAlignment>, List<Boolean>> {
    // 0.68 - центр
    val list = mutableListOf(BiasAlignment(0f, 1f)) // first
    val equityList = mutableListOf(true) // right = true, left = false
    when(playersCount) {
        1 -> return list to equityList
        2 -> { list.add(BiasAlignment(0f, -1f)); equityList.add(false) }
        3 -> {
            list.add(BiasAlignment(-1f, -0.9f))
            equityList.add(true)
            list.add(BiasAlignment(1f, -0.9f))
            equityList.add(false)
        }
        4 -> {
            list.add(BiasAlignment(-1f, -0.5f))
            equityList.add(true)
            list.add(BiasAlignment(0f, -1f))
            equityList.add(false)
            list.add(BiasAlignment(1f, -0.5f))
            equityList.add(false)
        }
        5 -> {
            list.add(BiasAlignment(-1f, 0.5f))
            equityList.add(true)
            list.add(BiasAlignment(-1f, -0.9f))
            equityList.add(true)
            list.add(BiasAlignment(1f, -0.9f))
            equityList.add(false)
            list.add(BiasAlignment(1f, 0.5f))
            equityList.add(false)
        }
        6 -> {
            list.add(BiasAlignment(-1f, 0.5f))
            equityList.add(true)
            list.add(BiasAlignment(-1f, -0.5f))
            equityList.add(true)
            list.add(BiasAlignment(0f, -1f))
            equityList.add(false)
            list.add(BiasAlignment(1f, -0.5f))
            equityList.add(false)
            list.add(BiasAlignment(1f, 0.5f))
            equityList.add(false)
        }
        7 -> {
            list.add(BiasAlignment(-1f, 0.5f))
            equityList.add(true)
            list.add(BiasAlignment(-1f, -0.5f))
            equityList.add(true)
            list.add(BiasAlignment(-0.5f, -1f))
            equityList.add(true)
            list.add(BiasAlignment(0.5f, -1f))
            equityList.add(false)
            list.add(BiasAlignment(1f, -0.5f))
            equityList.add(false)
            list.add(BiasAlignment(1f, 0.5f))
            equityList.add(false)
        }
        8 -> {
            list.add(BiasAlignment(-1f, 0.5f))
            equityList.add(true)
            list.add(BiasAlignment(-1f, -0.4f))
            equityList.add(true)
            list.add(BiasAlignment(-1f, -0.85f))
            equityList.add(true)
            list.add(BiasAlignment(0f, -1f))
            equityList.add(false)
            list.add(BiasAlignment(1f, -0.85f))
            equityList.add(false)
            list.add(BiasAlignment(1f, -0.4f))
            equityList.add(false)
            list.add(BiasAlignment(1f, 0.5f))
            equityList.add(false)
        }
        9 -> {
            list.add(BiasAlignment(-1f, 0.85f))
            equityList.add(true)
            list.add(BiasAlignment(-1f, 0.4f))
            equityList.add(true)
            list.add(BiasAlignment(-1f, -0.5f))
            equityList.add(true)
            list.add(BiasAlignment(-0.5f, -1f))
            equityList.add(true)
            list.add(BiasAlignment(0.5f, -1f))
            equityList.add(false)
            list.add(BiasAlignment(1f, -0.5f))
            equityList.add(false)
            list.add(BiasAlignment(1f, 0.4f))
            equityList.add(false)
            list.add(BiasAlignment(1f, 0.85f))
            equityList.add(false)
        }
        else -> return Pair(listOf(), listOf())
    }
    return list to equityList
}

@Composable
fun SingleBoardLayout(state: GameState, w: Dp, modifier: Modifier) {
    //val cardWidth = w / 5
    //val cardHeight = cardWidth * 1.5f
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text("Pot: ${state.pot}", color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            // Отображаем 5 общих карт, даже если их еще нет
            (0 until 5).forEach { index ->
                val card = state.communityCards.getOrNull(index)
                val mod = if(card == null) Modifier.weight(1f).aspectRatio(0.6667f).alpha(0.2f)
                else Modifier.weight(1f).aspectRatio(0.6667f)
                PokerCard(
                    card = card,
                    isFourColorMode = true,
                    modifier = mod
                )
            }
        }
    }
}

@Composable
fun MultiBoardLayout(
    staticCards: List<Card>,
    runouts: List<List<Card>>,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val boxWithConstraintsScope = this
        val cardWidth = boxWithConstraintsScope.maxWidth / 5
        val cardHeight = cardWidth * 1.5f

        // 1. Рисуем статичные карты
        Row(verticalAlignment = Alignment.CenterVertically) {
            staticCards.forEach { card ->
                PokerCard(
                    card = card,
                    isFourColorMode = true,
                    modifier = Modifier.width(cardWidth).height(cardHeight)
                )
            }
        Column(
            verticalArrangement = Arrangement.spacedBy((-(cardHeight.value / 2)).dp), // Отрицательный отступ для наложения
            horizontalAlignment = Alignment.End
        ) {
            for ((index, runout) in runouts.withIndex()) {
                // Анимируем смещение вверх для каждой предыдущей доски
                val yOffset by animateDpAsState(
                    // Смещаем каждую доску, кроме последней, на половину высоты карты
                    targetValue = if(runouts.size == 3 || index < runouts.size - 1) -(cardHeight / 2) else 0.dp,
                    label = "boardOffset$index"
                )

                // Показываем доску с анимацией появления
                AnimatedVisibility(
                    visible = true, // Управляется самим списком runouts
                    enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = 200))
                ) {
                    Row(
                        modifier = Modifier.offset(y = yOffset)
                            .width(cardWidth * (5 - staticCards.size))
                    ) {
                        // 2. Рисуем карты этого прогона
                        runout.forEach { card ->
                            PokerCard(
                                card = card,
                                isFourColorMode = true,
                                modifier = Modifier.weight(1f).aspectRatio(0.6667f)
                            )
                        }
                        // 3. Добавляем плейсхолдеры до 5 карт
                        repeat(5 - staticCards.size - runout.size) {
                            CardBack(
                                modifier = Modifier.weight(1f).aspectRatio(0.6667f).alpha(0.2f)
                            )
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
fun ActionPanel(
    viewModel: GameViewModel,
    isMyTurn: Boolean,
    myPlayer: Player?,
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
        when {
            myPlayer?.status == PlayerStatus.SPECTATING -> {
                // todo 1000 поменять
                BottomButton(onClick = { viewModel.onSitAtTableClick(buyIn = 1000L) }, text = "Sit at Table", modifier = Modifier.fillMaxWidth())
            }
            gameState == null -> {
                if (myPlayer != null) {
                    val text = if (myPlayer.isReady) "Cancel Ready" else "I'm Ready"
                    BottomButton(onClick = { viewModel.onReadyClick(!myPlayer.isReady) }, text = text, modifier = Modifier.fillMaxWidth())
                }
            }
            else -> { // todo сделать check/fold по удержанию кнопки и call any также
                // --- ОСНОВНАЯ ПАНЕЛЬ С ТРЕМЯ КНОПКАМИ ---
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Кнопка FOLD
                    BottomButton(onClick = { viewModel.onFold() }, enabled = isMyTurn, text = "Fold", modifier = Modifier.weight(1f))

                    // 2. Динамическая кнопка CHECK / CALL
                    val amountToCall = gameState.amountToCall
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
//@Composable
//fun PokerTable() {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth(0.95f)
//            .aspectRatio(0.6f)
//            .background(Color(0xFF00695C), shape = RoundedCornerShape(percent = 50)) // Зеленый фон и овальная форма
//            .border(4.dp, Color(0xFF004D40), shape = RoundedCornerShape(percent = 50)) // Темно-зеленая рамка
//    )
//}

@Composable
fun BottomButton(onClick: () -> Unit, enabled: Boolean = true, text: String, modifier: Modifier) {
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

//@Composable
//@Preview
//fun TestPlayerDisplay() {
//    PlayerDisplay(PlayerState(Player("", "test", 1000L)), true, true, Modifier, true, true, null,1.2f)
//}

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

//@Composable
//@Preview
//fun TextEquityBox() {
//    Box(contentAlignment = Alignment.Center, modifier = Modifier.width(100.dp).height(80.dp)) {
//        //EquityBubble(74.18, TailDirection.LEFT)
//        //OutsBubble(63.6, OutsInfo.DrawingDead)
//    }
//
//}

@Composable
fun EquityBubble(
    equity: Double,
    tailDirection: TailDirection,
    scaleMultiplier: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = OvalWithTailShape(
            tailDirection = tailDirection
        ),
        color = Color.Black.copy(alpha = 0.6f),
        border = BorderStroke(1.5.dp * scaleMultiplier, Color(0xFF216625))
    ) {
        Box(
            modifier = Modifier.width(60.dp * scaleMultiplier).height(36.dp * scaleMultiplier),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$equity%",
                color = Color(0xFF62CF1E),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp * scaleMultiplier
            )
        }
    }
}

@Composable
fun OutsBubble(
    equity: Double?,
    outsInfo: OutsInfo,
    scaleMultiplier: Float,
    modifier: Modifier = Modifier
) {
    val randomSeed = remember { Random.nextInt() }

    Surface(
        modifier = modifier.graphicsLayer { clip = false },
        shape = JaggedOvalShape(seed = randomSeed),
        color = Color.Black.copy(alpha = 0.6f),
        border = BorderStroke(1.dp * scaleMultiplier, Color(0xFF610C18))
    ) {
        Box(
            modifier = Modifier.width(100.dp * scaleMultiplier).height(80.dp * scaleMultiplier),
            contentAlignment = Alignment.Center
        ) {
            when (outsInfo) {
                is OutsInfo.DirectOuts -> {
                    val outs = outsInfo.cards
                    if(outs.size > 18) {
                        Text("${outs.size} Outs", color = Color.LightGray, fontSize = 14.sp * scaleMultiplier, fontFamily = MerriWeatherFontFamily)
                    } else {
                        OutsText(outs, true, scaleMultiplier, Modifier.align(BiasAlignment(0f, -0.2f)))
                    }
                    Text(
                        text = "$equity%",
                        color = Color(0xFFE61D2A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp * scaleMultiplier,
                        modifier = Modifier.align(BiasAlignment(0f, 0.6f))
                    )
                }
                is OutsInfo.RunnerRunner -> {
                    Text("Runner\nRunner", color = Color.LightGray, fontSize = 14.sp * scaleMultiplier, fontFamily = MerriWeatherFontFamily)
                }
                is OutsInfo.DrawingDead -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Drawing",
                            color = Color.LightGray,
                            fontSize = 14.sp * scaleMultiplier,
                            fontFamily = MerriWeatherFontFamily
                        )
                        Text(
                            "Dead",
                            color = Color.LightGray,
                            fontSize = 14.sp * scaleMultiplier,
                            fontFamily = MerriWeatherFontFamily
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OutsText(outs: List<Card>, isFourColorMode: Boolean, scaleMultiplier: Float, modifier: Modifier) {
    Text(
        buildAnnotatedString {
            outs.forEach { card ->
                // Определяем цвет для этой карты
                val suitColor = if (isFourColorMode) {
                    when (card.suit) {
                        Suit.HEARTS -> Color.Red
                        Suit.DIAMONDS -> Color.Blue
                        Suit.CLUBS -> Color.Green
                        Suit.SPADES -> Color.Black
                    }
                } else {
                    if (card.suit == Suit.HEARTS || card.suit == Suit.DIAMONDS) Color.Red else Color.Black
                }

                // Определяем символ масти
                val suitSymbol = when (card.suit) {
                    Suit.HEARTS -> "\u2661"
                    Suit.DIAMONDS -> "\u2662"
                    Suit.CLUBS -> "\u2667"
                    Suit.SPADES -> "\u2664"
                }

                // Добавляем ранг и масть с нужным цветом
                withStyle(style = SpanStyle(color = suitColor)) {
                    append("${getCardName(card.rank)}$suitSymbol")
                }
            }
        },
        fontSize = 8.sp * scaleMultiplier,
        maxLines = 3,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(16.dp, 0.dp).clip(RoundedCornerShape(4.dp)).background(Color.Gray),
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

@Composable
fun UnderdogChoiceUi(
    modifier: Modifier,
    onChoice: (Int) -> Unit
) {
    Box(
        modifier = modifier
            .height(63.dp)
            .navigationBarsPadding()
            .background(Color.Black)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { t ->
                val times = t + 1
                val text = when(times) {
                    2 -> "Two times"
                    3 -> "Three times"
                    else -> "Once"
                }
                BottomButton(onClick = { onChoice(times) }, text = text, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun FavoriteConfirmationUi(
    underdogName: String,
    times: Int,
    modifier: Modifier,
    onConfirm: (Boolean) -> Unit
) {
    Box(
        modifier = modifier
            .height(83.dp)
            .navigationBarsPadding()
            .background(Color.Black)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("$underdogName wants to run it $times times", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomButton(onClick = { onConfirm(true) }, text = "Accept", modifier = Modifier.weight(1f))
                BottomButton(onClick = { onConfirm(false) }, text = "Decline", modifier = Modifier.weight(1f))
            }
        }
    }
}

// todo использовать в главном экране
//val configuration = LocalConfiguration.current
//when (configuration.orientation) {
//    Configuration.ORIENTATION_LANDSCAPE -> { /* Логика для ландшафта */ }
//    else -> { /* Логика для портрета */ }
//}