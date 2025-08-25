package com.example.poker.ui.game

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.poker.R
import com.example.poker.data.remote.dto.Card
import com.example.poker.data.remote.dto.GameMode
import com.example.poker.data.remote.dto.GameStage
import com.example.poker.data.remote.dto.GameState
import com.example.poker.data.remote.dto.OutsInfo
import com.example.poker.data.remote.dto.Player
import com.example.poker.data.remote.dto.PlayerAction
import com.example.poker.data.remote.dto.PlayerState
import com.example.poker.data.remote.dto.PlayerStatus
import com.example.poker.domain.model.Suit
import com.example.poker.ui.theme.MerriWeatherFontFamily
import com.example.poker.util.calculateChipStack
import com.example.poker.util.calculatePlayerPosition
import com.example.poker.util.toBB
import com.example.poker.util.toBBFloat
import com.example.poker.util.toMinutesSeconds
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.random.Random

@Composable
fun GameScreen(viewModel: GameViewModel, onNavigateToLobby: () -> Unit) {
    val winnerId by viewModel.tournamentWinner.collectAsState()
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var lastBoardResult by remember { mutableLongStateOf(0L) }

    val lifecycleOwner = LocalLifecycleOwner.current
    // Следим за жизненным циклом экрана
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.connect()
            } else if (event == Lifecycle.Event.ON_STOP) {
                viewModel.disconnect()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    // Перехватываем нажатие "назад"
    BackHandler(enabled = true) {
        showExitDialog = true
    }
    if (showExitDialog) {
        ConfirmExitDialog(
            onConfirm = {
                showExitDialog = false
                onNavigateToLobby()
            },
            onDismiss = {
                showExitDialog = false
            }
        )
    }
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF003D33))) {
        val boxModifier = remember {
            Modifier
                .background(Color.Black)
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(60.dp)
        }
        Box(modifier = boxModifier)

        val boxModifier2 = remember {
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        }
        Box(
            modifier = boxModifier2,
            contentAlignment = Alignment.Center
        ) {
            val topBarModifier = remember {
                Modifier
                    .align(Alignment.TopCenter)
                    .height(30.dp)
                    .fillMaxWidth()
            }
            TopBar({ viewModel.gameMode.value }, { viewModel.tournamentInfo.value }, { viewModel.specsCount.value }, topBarModifier)
            val boxModifier3 = remember {
                Modifier
                    .padding(0.dp, 30.dp, 0.dp, 63.dp)
                    .background(Color(0xFF004D40))
                    .fillMaxSize()
            }
            Box(boxModifier3) {
                val settingsModifier = remember {
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(50.dp)
                        .clip(CircleShape)
                        .padding(0.dp, 0.dp, 5.dp, 5.dp)
                        .clickable(onClick = { showSettingsMenu = !showSettingsMenu })
                }
                // Кнопка настроек
                Icon(painter = painterResource(R.drawable.ic_settings), contentDescription = "Settings", tint = Color.Black, modifier = settingsModifier)

                PlayersLayout(
                    playersOnTableProvider = { viewModel.playersOnTable.value },
                    myUserIdProvider = { viewModel.myUserId.value },
                    boardResultProvider = { viewModel.boardResult.value },
                    gameStateProvider = { viewModel.gameState.value },
                    allInEquityProvider = { viewModel.allInEquity.value },
                    visibleActionIdsProvider = { viewModel.visibleActionIds.value },
                    scaleMultiplierProvider = { viewModel.scaleMultiplier.value },
                    stackDisplayModeProvider = { viewModel.stackDisplayMode.value },
                    onLastBoardResultChange = { amount -> lastBoardResult = amount }
                )
                val waitingModifier = remember {
                    Modifier
                        .align(Alignment.Center)
                        .background(Color(0xFF00695C), shape = RoundedCornerShape(percent = 50))
                        .border(4.dp, Color(0xFF004D40), shape = RoundedCornerShape(percent = 50))
                        .padding(32.dp, 16.dp)
                }
                BoardLayout(
                    gameStateProvider = { viewModel.gameState.value },
                    boardRunoutsProvider = { viewModel.boardRunouts.value },
                    staticCardsProvider = { viewModel.staticCommunityCards.value },
                    runCountProvider = { viewModel.runsCount.value },
                    stackDisplayModeProvider = { viewModel.stackDisplayMode.value },
                    specsCountProvider = { viewModel.specsCount.value },
                    multiboardModifier = Modifier.align(Alignment.CenterStart),
                    singleBoardModifier = Modifier.align(Alignment.Center),
                    waitingModifier = waitingModifier
                )
                // Выдвижное меню настроек
                SettingsMenu(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 60.dp, end = 16.dp),
                    showSettingsMenu = showSettingsMenu,
                    stackDisplayModeProvider = { viewModel.stackDisplayMode.value },
                    scaleMultiplierProvider = { viewModel.scaleMultiplier.value },
                    onToggleDisplayMode = { viewModel.toggleStackDisplayMode() },
                    onIncreaseScale = { viewModel.changeScale(0.05f) },
                    onDecreaseScale = { viewModel.changeScale(-0.05f) }
                )
            }
            BottomLayout(
                modifier = Modifier.align(Alignment.BottomCenter),
                playersOnTableProvider = { viewModel.playersOnTable.value },
                myUserIdProvider = { viewModel.myUserId.value },
                runItStateProvider = { viewModel.runItUiState.value },
                stackDisplayModeProvider = { viewModel.stackDisplayMode.value },
                gameStateProvider = { viewModel.gameState.value },
                gameModeProvider = { viewModel.gameMode.value },
                isActionPanelLockedProvider = { viewModel.isActionPanelLocked.value },
                allInEquityProvider = { viewModel.allInEquity.value },
                onSitAtTableClick = { viewModel.onSitAtTableClick() },
                onReadyClick = { viewModel.onReadyClick(it) },
                onFold = { viewModel.onFold() },
                onCheck = { viewModel.onCheck() },
                onCall = { viewModel.onCall() },
                onBet = { viewModel.onBet(it) },
                onChoice = { times -> viewModel.onRunItChoice(times) },
                onConfirm = { accepted -> viewModel.onRunItConfirmation(accepted) },
                onHideRunItState = { viewModel.hideRunItState() }
            )
            winnerId?.let {
                TournamentWinnerDialog(
                    playersOnTableProvider = { viewModel.playersOnTable.value },
                    winnerId = it,
                    lastBoardResult = lastBoardResult,
                    onReturnToLobby = onNavigateToLobby
                )
            }
        }
    }
}

@Composable
fun AnimatedCommunityCards(
    cards: List<Card>,
    modifier: Modifier = Modifier,
    staticCardsSize: Int = 0,
    isMultiboard: Boolean = false
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val boxWithConstraintsScope = this
        val cardWidth = boxWithConstraintsScope.maxWidth / 5
        // todo Здесь много расчетов, при этом можно всё вычислить буквально один раз и больше не считать, нужно подумать как
        // 1. Создаем анимируемые состояния для каждой из 5 карт
        val cardOffsetsX = remember { List(5) { Animatable(0f) } }
        val cardOffsetsY = remember { List(5) { Animatable(0f) } }
        val cardAlphas = remember { List(5) { Animatable(0f) } }
        val cardRotations = remember { List(5) { Animatable(0f) } }

        var isReadyForAnimation by remember { mutableStateOf(isMultiboard) }

        // 2. LaunchedEffect - "мозг" анимации. Запускается, когда меняется список карт
        LaunchedEffect(cards) {
            // Рассчитываем целевые X-позиции для карт
            val flopTarget1X = 0f
            val flopTarget2X = cardWidth.value
            val flopTarget3X = (cardWidth * 2).value
            val turnTargetX = (cardWidth * 3).value
            val riverTargetX = (cardWidth * 4).value

            // Начальная Y-позиция "вылета"
            val startX = boxWithConstraintsScope.maxWidth.value * 0.5f
            val startY = boxWithConstraintsScope.maxHeight.value * 0.75f

            if(cards.isEmpty()) {
                // Перед началом новой анимации сбрасываем все значения в 0
                isReadyForAnimation = false
                coroutineScope {
                    (0..4).forEach { i ->
                       launch { cardOffsetsX[i].snapTo(0f) }
                        launch { cardOffsetsY[i].snapTo(0f) }
                        launch { cardAlphas[i].snapTo(0f) }
                        launch { cardRotations[i].snapTo(0f) }
                    }
                }
            }

            when (cards.size) {
                3 -> {
                    // 1. "Телепортируем" все 3 карты в ЦЕНТРАЛЬНУЮ позицию за пределами экрана
                    (0..2).forEach { i ->
                        cardOffsetsX[i].snapTo(startX) // Все начинают с центра
                        cardOffsetsY[i].snapTo(startY)
                        cardAlphas[i].snapTo(0f)
                    }
                }
                4 -> {
                    cardOffsetsX[3].snapTo(turnTargetX)
                    cardOffsetsY[3].snapTo(startY)
                }
                5 -> {
                    cardOffsetsX[4].snapTo(riverTargetX)
                    cardOffsetsY[4].snapTo(startY)
                }
            }

            when (cards.size) {
                3 -> { // Флоп
                    // Теперь, когда все карты на стартовых позициях, разрешаем их показать
                    isReadyForAnimation = true

                    // 2. Анимируем их "вылет" в центр стола
                    (0..2).forEach { i -> launch { cardAlphas[i].animateTo(1f, tween(100)) } }
                    (0..2).forEach { i -> launch { cardOffsetsX[i].animateTo(flopTarget1X, tween((i+1) * 300)) } }
                    (0..2).forEach { i -> launch { cardOffsetsY[i].animateTo(0f, tween((i+1) * 300)) } }
                    delay(950) // Ждем, пока они прилетят
                    // 3. Анимируем "разъезжание" крайних карт из центра
                    launch { cardOffsetsX[1].animateTo(flopTarget2X, spring(stiffness = Spring.StiffnessLow)) }
                    launch { cardOffsetsX[2].animateTo(flopTarget3X, spring(stiffness = Spring.StiffnessLow)) }
                }
                4 -> { // Терн
                    launch { cardAlphas[3].animateTo(1f, tween(200)) }
                    launch { cardRotations[3].animateTo(360f, tween(600)) }
                    cardOffsetsY[3].animateTo(0f, tween(500))
                }
                5 -> { // Ривер
                    launch { cardAlphas[4].animateTo(1f, tween(200)) }
                    launch { cardRotations[4].animateTo(360f, tween(600)) }
                    cardOffsetsY[4].animateTo(0f, tween(500))
                }
            }
        }
        if(isReadyForAnimation) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height((cardWidth - 1.dp) * 1.5f)) {
                (staticCardsSize..4).forEach { i ->
                    val card = cards.getOrNull(i)
                    card?.let {
                        PokerCard(
                            card = it,
                            modifier = Modifier
                                .width(cardWidth - 1.dp)
                                .alpha(cardAlphas[i].value)
                                .graphicsLayer { rotationZ = cardRotations[i].value }
                                .offset(
                                    x = cardOffsetsX[i].value.dp,
                                    y = cardOffsetsY[i].value.dp
                                )
                        )
                    }
                }
            }
        } else Box(Modifier.height((cardWidth - 1.dp) * 1.5f))
    }
}

@Composable
fun SingleBoardLayout(
    pot: Long,
    bigBlindAmount: Long,
    communityCards: ImmutableList<Card>,
    displayMode: StackDisplayMode,
    modifier: Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        val text = if(displayMode == StackDisplayMode.CHIPS) pot.toString() else pot.toBB(bigBlindAmount) + " BB"
        Text("Pot: $text", color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        AnimatedCommunityCards(cards = communityCards)
    }
}

@Composable
fun MultiBoardLayout(
    staticCardsProvider: () -> ImmutableList<Card>,
    runouts: ImmutableList<ImmutableList<Card>>,
    runsProvider: () -> Int,
    pot: Long,
    displayMode: StackDisplayMode,
    bigBlind: Long,
    modifier: Modifier = Modifier
) {
    val staticCards = staticCardsProvider()
    val runs = runsProvider()
    BoxWithConstraints(modifier = modifier) { // todo тут тоже куча расчетов со всегда одинаковыми результатами
        val boxWithConstraintsScope = this
        val cardWidth = boxWithConstraintsScope.maxWidth / 5
        val cardHeight = (cardWidth - 1.dp) * 1.5f

        val offset = if(runs == 2) -(cardHeight / 2) -(cardHeight / 2.5f) else -cardHeight -(cardHeight / 5)
        val text = if(displayMode == StackDisplayMode.CHIPS) pot.toString() else pot.toBB(bigBlind) + " BB"
        Text("Pot: $text", color = Color.White, modifier = Modifier
            .align(Alignment.Center)
            .offset(0.dp, offset))

        // 1. Рисуем статичные карты
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.CenterStart)) {
            staticCards.forEach { card ->
                PokerCard(
                    card = card,
                    modifier = Modifier
                        .width(cardWidth - 1.dp)
                        .height(cardHeight)
                )
                Spacer(Modifier.width(1.dp))
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy((-(cardHeight.value)).dp), // Отрицательный отступ для наложения
            horizontalAlignment = Alignment.End
        ) {
            for ((index, runout) in runouts.withIndex()) {
                val target = when(runs) {
                    2 -> when(index) {
                        0 -> -(cardHeight / 4)
                        1 -> cardHeight / 4
                        else -> 0.dp
                    }
                    3 -> when(index) {
                        0 -> -(cardHeight / 2)
                        1 -> 0.dp
                        2 -> cardHeight / 2
                        else -> 0.dp
                    }
                    else -> 0.dp
                }
                // Анимируем смещение вверх для каждой предыдущей доски
                val yOffset by animateDpAsState(
                    // Смещаем каждую доску, кроме последней, на половину высоты карты
                    targetValue = target,
                    label = "boardOffset$index"
                )

                // Показываем доску с анимацией появления
                AnimatedVisibility(
                    visible = true, // Управляется самим списком runouts
                    enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = 200))
                ) {
                    Row(
                        modifier = Modifier.offset(y = yOffset)
                    ) {
                        // 2. Рисуем карты этого прогона
                        AnimatedCommunityCards(
                            cards = staticCards + runout,
                            modifier = Modifier,
                            staticCardsSize = staticCards.size,
                            isMultiboard = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActionPanel(
    myPlayer: Player?,
    myUserId: String?,
    isActionPanelLockedProvider: () -> Boolean,
    allInEquityProvider: () -> AllInEquity?,
    gameStateProvider: () -> GameState?, // Общее состояние игры
    displayMode: StackDisplayMode,
    modifier: Modifier,
    isTournament: Boolean,
    onSitAtTableClick: () -> Unit,
    onReadyClick: (Boolean) -> Unit,
    onFold: () -> Unit,
    onCheck: () -> Unit,
    onCall: () -> Unit,
    onBet: (Long) -> Unit
) {
    // Состояние для отображения/скрытия ползунка
    var showBetSlider by remember { mutableStateOf(false) }

    val gameState = gameStateProvider()
    // todo как-то remember сделать можно
    val playerState = gameState?.playerStates?.find { it.player.userId == myUserId }

    // Используем Box для наложения ползунка поверх панели
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .height(63.dp)
            .fillMaxWidth()
            .background(Color.Black)
    ) {
        when {
            myPlayer?.status == PlayerStatus.SPECTATING && (!isTournament || gameState == null)  -> {
                BottomButton(onClick = { onSitAtTableClick }, text = "Sit at Table", modifier = Modifier.fillMaxWidth())
            }
            gameState == null -> {
                if (myPlayer != null) {
                    val text = if (myPlayer.isReady) "Cancel Ready" else "I'm Ready"
                    BottomButton(onClick = { onReadyClick(!myPlayer.isReady) }, text = text, modifier = Modifier.fillMaxWidth())
                }
            }
            else -> { // todo сделать check/fold по удержанию кнопки и call any также
                // --- ОСНОВНАЯ ПАНЕЛЬ С ТРЕМЯ КНОПКАМИ ---
                val isActionPanelLocked = isActionPanelLockedProvider()
                val allInEquity = allInEquityProvider()
                val isMyTurn by remember {
                    derivedStateOf {
                        val activeId = gameState.playerStates.getOrNull(gameState.activePlayerPosition)?.player?.userId
                        activeId == myUserId && !isActionPanelLocked && allInEquity == null && gameState.stage != GameStage.SHOWDOWN
                    }
                }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Кнопка FOLD
                    BottomButton(onClick = { onFold() }, enabled = isMyTurn, text = "Fold", modifier = Modifier.weight(1f))

                    // 2. Динамическая кнопка CHECK / CALL
                    val amountToCall = gameState.amountToCall
                    val myCurrentBet = playerState?.currentBet ?: 0L

                    if (amountToCall == 0L || amountToCall == myCurrentBet) {
                        // Если ставить не нужно, показываем CHECK
                        BottomButton(onClick = { onCheck() }, enabled = isMyTurn, text = "Check", modifier = Modifier.weight(1f))
                    } else {
                        // Если нужно коллировать, показываем CALL с суммой
                        val callValue = minOf(playerState?.player?.stack ?: 0L, amountToCall - myCurrentBet)
                        val callText = if (displayMode == StackDisplayMode.BIG_BLINDS) {
                            "Call ${callValue.toBB(gameState.bigBlindAmount)} BB"
                        } else "Call $callValue"
                        BottomButton(onClick = { onCall() }, enabled = isMyTurn, text = callText, modifier = Modifier.weight(1f))
                    }

                    // 3. Кнопка BET / RAISE
                    val canRaise = (playerState?.player?.stack ?: 0L) > amountToCall
                    BottomButton(onClick = { showBetSlider = true }, enabled = isMyTurn && canRaise, text = "Bet", modifier = Modifier.weight(1f))
                }
            }
        }
    }
    // --- ПОЛЗУНОК ДЛЯ СТАВКИ (появляется по условию) ---
    if (showBetSlider && playerState != null) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = modifier.fillMaxWidth()
        ) {
            BetControls(
                minBet = minOf(gameState.amountToCall + gameState.lastRaiseAmount, playerState.player.stack),
                maxBet = playerState.player.stack + playerState.currentBet,
                amountToCall = gameState.amountToCall,
                displayMode = displayMode,
                bigBlind = gameState.bigBlindAmount,
                onBetConfirmed = { betAmount ->
                    onBet(betAmount)
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
    maxBet: Long,
    amountToCall: Long,
    displayMode: StackDisplayMode,
    bigBlind: Long,
    onBetConfirmed: (Long) -> Unit,
    onDismiss: () -> Unit // Функция для закрытия
) {
    // Используем String для TextField, но Float для Slider
    var betAmountInChips by remember(minBet) { mutableLongStateOf(minBet) }
    var textValue by remember { mutableStateOf("") }
    val isBetValid = betAmountInChips in minBet..maxBet

    val (presetX2, presetX3, presetX4) = if(amountToCall > 0) Triple(amountToCall * 2, amountToCall * 3, amountToCall * 4)
     else Triple(minBet * 2, minBet * 3, minBet * 4)

    // Синхронизация: запускается, когда меняется betAmountInChips или режим
    LaunchedEffect(betAmountInChips, displayMode) {
        textValue = if (displayMode == StackDisplayMode.BIG_BLINDS) {
            betAmountInChips.toBB(bigBlind)
        } else {
            betAmountInChips.toString()
        }
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
            modifier = Modifier
                .align(Alignment.End)
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
                value = betAmountInChips.toFloat(),
                onValueChange = { betAmountInChips = it.toLong() },
                valueRange = minBet.toFloat()..maxBet.toFloat()
            )
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier.align(Alignment.Start),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
            ) {
                // Функция для создания кнопок, чтобы не дублировать код
                @Composable
                fun PresetButton(amount: Long, label: String, isEnabled: Boolean = true) {
                    Button(
                        modifier = Modifier.size(30.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RectangleShape,
                        enabled = isEnabled,
                        onClick = { betAmountInChips = amount }
                    ) { Text(label) }
                }
                PresetButton(amount = minBet, label = "Min")
                PresetButton(amount = presetX2, label = "x2", isEnabled = presetX2 in minBet..maxBet)
                PresetButton(amount = presetX3, label = "x3", isEnabled = presetX3 in minBet..maxBet)
                PresetButton(amount = presetX4, label = "x4", isEnabled = presetX4 in minBet..maxBet)
                PresetButton(amount = maxBet, label = "Max")
            }
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { newText ->
                        textValue = newText
                        // Синхронизация: если меняется текст, обновляем слайдер
                        if (displayMode == StackDisplayMode.BIG_BLINDS) {
                            val bbValue = newText.trim().toDoubleOrNull()
                            if (bbValue != null) {
                                betAmountInChips = (bbValue * bigBlind).toLong()
                            }
                        } else {
                            betAmountInChips = newText.toLongOrNull() ?: 0L
                        }
                    },
                    label = {
                        val text = if (displayMode == StackDisplayMode.BIG_BLINDS) "Amount in BB" else "Amount"
                        Text(text)
                    },
                    isError = !isBetValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onBetConfirmed(betAmountInChips) }, enabled = isBetValid) {
                    Text("Confirm")
                }
            }
        }
    }
}

@Composable
fun BottomButton(onClick: () -> Unit, enabled: Boolean = true, text: String, modifier: Modifier) {
    val color1 = if(enabled) Color.Red else Color(0xFF640D14)
    val color2 = if(enabled) Color(0xFF640D14) else Color.Black
    FilledTonalButton( // todo можно в теории присвоить это в remember от enabled и текста
        modifier = modifier
            .height(60.dp)
            .padding(1.dp, 0.dp),
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
//fun TestPlayerWithEquity() {
//    PlayerWithEquity(null, TailDirection.RIGHT, PlayerState(Player("id", "test12", 1000L), lastAction = PlayerAction.Call(1L, "123"), cards = persistentListOf(Card(
//        Rank.KING, Suit.DIAMONDS), Card(Rank.FIVE, Suit.SPADES))), true, true, 15L, Modifier, true, setOf(), false,
//        StackDisplayMode.CHIPS, 20, 1.2f)
//}

@Composable
fun PlayerWithEquity(
    allInEquityProvider: () -> AllInEquity?,
    tailDirection: TailDirection,
    playerState: PlayerState,
    myUserId: String?,
    isActivePlayer: Boolean,
    turnExpiresAt: Long?,
    modifier: Modifier = Modifier,
    isGameStarted: Boolean,
    visibleActionIdsProvider: () -> ImmutableSet<String>,
    isWinner: Boolean = false,
    displayMode: StackDisplayMode,
    bigBlind: Long,
    scaleMultiplier: Float
) {
    val allInEquity = allInEquityProvider()
    val equity = allInEquity?.equities?.get(playerState.player.userId)
    val out = allInEquity?.outs?.get(playerState.player.userId)

    val requiredWidth = remember(equity != null, out != null) {
        when {
            out != null -> 255.dp * scaleMultiplier
            equity != null -> 190.dp * scaleMultiplier
            else -> 70.dp * scaleMultiplier
        }
    }

    Box(modifier = modifier.width(requiredWidth), contentAlignment = Alignment.Center) {
        PlayerDisplay(
            modifier = Modifier,
            playerState = playerState,
            myUserId = myUserId,
            isActivePlayer = isActivePlayer,
            turnExpiresAt = turnExpiresAt,
            isGameStarted = isGameStarted,
            visibleActionIdsProvider = { visibleActionIdsProvider() },
            scaleMultiplier = scaleMultiplier,
            displayMode = displayMode,
            isWinner = isWinner,
            bigBlind = bigBlind
        )
        val mod = if(tailDirection == TailDirection.RIGHT) Modifier.align(Alignment.CenterStart) else Modifier.align(Alignment.CenterEnd)

        if(out == null && equity != null) {
            Box(contentAlignment = Alignment.Center, modifier = mod) {
                EquityBubble(equity, tailDirection, scaleMultiplier)
            }
        } else if(out != null) {
            Box(contentAlignment = Alignment.Center, modifier = mod) {
                OutsBubble(equity, out, scaleMultiplier)
            }
        }
    }
}

//@Composable
//@Preview
//fun TestPlayerDisplay() {
//    PlayerDisplay(PlayerState(Player("", "test12", 1000L), lastAction = PlayerAction.Call(1L, "123"), cards = persistentListOf(Card(
//        Rank.KING, Suit.DIAMONDS), Card(Rank.FIVE, Suit.SPADES))), true, true, 15L, Modifier, true, setOf(), false,
//        StackDisplayMode.CHIPS, 20, 1.2f)
//}

@Composable
fun PlayerDisplay(
    playerState: PlayerState,
    isActivePlayer: Boolean,
    turnExpiresAt: Long?,
    myUserId: String?,
    modifier: Modifier = Modifier,
    isGameStarted: Boolean,
    visibleActionIdsProvider: () -> ImmutableSet<String>,
    isWinner: Boolean = false,
    displayMode: StackDisplayMode,
    bigBlind: Long,
    scaleMultiplier: Float
) {
    val visibleActionIds = visibleActionIdsProvider()
    Box(modifier = modifier
        .width(70.dp * scaleMultiplier)
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

        if (isGameStarted) {
            val (card1, card2) = if (playerState.player.userId == myUserId || playerState.cards.isNotEmpty()) {
                playerState.cards.getOrNull(0) to playerState.cards.getOrNull(1)
            } else null to null
            AnimatedVisibility(
                modifier = Modifier.align(Alignment.Center),
                visible = !playerState.hasFolded,
                enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = slideOutVertically(targetOffsetY = { it / 6 }) + fadeOut(animationSpec = tween(durationMillis = 500))
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy((-20).dp * scaleMultiplier)) {
                    FlippingPokerCard(
                        card = card1,
                        flipDirection = FlipDirection.COUNTER_CLOCKWISE,
                        modifier = Modifier
                            .width(40.dp * scaleMultiplier)
                            .height(60.dp * scaleMultiplier)
                            .graphicsLayer { rotationZ = -10f }
                    )
                    FlippingPokerCard(
                        card = card2,
                        flipDirection = FlipDirection.CLOCKWISE,
                        modifier = Modifier
                            .width(40.dp * scaleMultiplier)
                            .height(60.dp * scaleMultiplier)
                            .graphicsLayer { rotationZ = 10f }
                    )
                }
            }
        } else {
            if(playerState.player.isReady) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Is ready",
                    tint = Color.Green,
                    modifier = Modifier.size(60.dp * scaleMultiplier)
                )
            }
        }
        Column(modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .clip(TrapezoidShape(10f))
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Black)) {
            val (s, pd) = remember(playerState.player.username.length) {
                when(playerState.player.username.length) {
                    in 0..8 -> 10.sp to 0.dp
                    in 9..10 -> 9.sp to 1.dp
                    in 11..13 -> 8.sp to 2.dp
                    else -> 7.sp to 3.dp
                }
            }
            Text(
                text = playerState.player.username,
                color = Color.White,
                fontWeight = FontWeight.Normal,
                fontSize = s * scaleMultiplier,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(3.dp, 1.dp + pd),
                style = TextStyle(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    ),
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.Both
                    )
                ),
                maxLines = 1
            )
            HorizontalDivider()
            val (stackText, textColor) = if(playerState.player.stack != 0L) {
                if(displayMode == StackDisplayMode.BIG_BLINDS) playerState.player.stack.toBB(bigBlind) + " BB" to Color.White
                else playerState.player.stack.toString() to Color.White
            } else "All-In" to Color.Red
            Text(
                text = stackText,
                color = textColor,
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
                ),
                maxLines = 1
            )
        }
        AnimatedVisibility(
            modifier = Modifier.align(Alignment.Center),
            visible = playerState.lastAction?.id in visibleActionIds,
            enter = fadeIn(animationSpec = tween(300)) + slideInVertically(),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            playerState.lastAction?.let {
                PlayerAction(it, scaleMultiplier)
            }
        }
        if (isActivePlayer && turnExpiresAt != null) {
            TurnTimer(
                expiresAt = turnExpiresAt,
                modifier = Modifier.align(Alignment.TopEnd),
                scaleMultiplier = scaleMultiplier
            )
        }
        if (!playerState.player.isConnected) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(40.dp * scaleMultiplier)
                    .clip(RoundedCornerShape(12.dp * scaleMultiplier))
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                PulsingConnectionLostIcon(modifier = Modifier.size(32.dp * scaleMultiplier))
            }
        }
        AnimatedVisibility(
            modifier = modifier.fillMaxSize(),
            visible = isWinner,
            enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(animationSpec = tween(1000), initialOffsetY = { -it }),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box {
                RadiantGlowEffectEnhanced(
                    Modifier
                        .size(52.dp * scaleMultiplier)
                        .align(Alignment.Center),
                    color = Color(0xFFFDFFD8),
                    rayCount = 32,
                    innerRadiusRatio = 0.2f
                )
                Image(
                    painter = painterResource(R.drawable.winner_cup),
                    contentDescription = "Winner",
                    modifier = Modifier
                        .width(27.dp * scaleMultiplier)
                        .height(39.dp * scaleMultiplier)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

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
            modifier = Modifier
                .width(60.dp * scaleMultiplier)
                .height(36.dp * scaleMultiplier),
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
            modifier = Modifier
                .width(100.dp * scaleMultiplier)
                .height(80.dp * scaleMultiplier),
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
        modifier = modifier
            .padding(16.dp, 0.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Gray),
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
    expiresAt: Long,
    onChoice: (Int) -> Unit,
    onHideRunItState: () -> Unit
) {
    var remainingTime by remember { mutableLongStateOf(expiresAt - System.currentTimeMillis()) }
    LaunchedEffect(expiresAt) {
        while (remainingTime > 0) {
            remainingTime = expiresAt - System.currentTimeMillis()
            delay(50L) // Обновляем часто для плавной анимации
        }
        onHideRunItState
    }
    val totalDurationMillis = 15000f
    val progress = (remainingTime / totalDurationMillis).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .height(75.dp)
            .background(Color.Black)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CustomLinearProgressBar(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = Color.Green,
                trackColor = Color.Black,
            )
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
}

@Composable
fun FavoriteConfirmationUi(
    underdogName: String,
    times: Int,
    expiresAt: Long,
    modifier: Modifier,
    onConfirm: (Boolean) -> Unit,
    onHideRunItState: () -> Unit
) {
    var remainingTime by remember { mutableLongStateOf(expiresAt - System.currentTimeMillis()) }
    LaunchedEffect(expiresAt) {
        while (remainingTime > 0) {
            remainingTime = expiresAt - System.currentTimeMillis()
            delay(50L) // Обновляем часто для плавной анимации
        }
        onHideRunItState
    }
    val totalDurationMillis = 15000f
    val progress = (remainingTime / totalDurationMillis).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .height(95.dp)
            .background(Color.Black)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CustomLinearProgressBar(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = Color.Green,
                trackColor = Color.Black,
            )
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

@Composable
fun PlayerAction(action: PlayerAction, scaleMultiplier: Float) {
    val (text, color, brightColor) = when(action) {
        is PlayerAction.Fold -> Triple("Fold", Color(0xFF983036), Color(0xFFE84952))
        is PlayerAction.Check -> Triple("Check", Color(0xFFB29A3B), Color(0xFFF2D250))
        is PlayerAction.Call -> Triple("Call", Color(0xFFDBA656), Color(0xFFF2B85F))
        is PlayerAction.Bet -> Triple("Bet", Color(0xFF3C9FC5), Color(0xFF48BFED))
        is PlayerAction.Raise -> Triple("Raise", Color(0xFF3396AE), Color(0xFF46CCED))
        is PlayerAction.AllIn -> Triple("All-In", Color(0xFFAF6832), Color(0xFFF08E44))
    }
    val width = 50.dp * scaleMultiplier
    val height = 20.dp * scaleMultiplier
    Box(contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(3.dp * scaleMultiplier))
            .background(Color.Black.copy(alpha = 0.8f))
            .border(
                BorderStroke(
                    1.dp * scaleMultiplier,
                    Brush.radialGradient(
                        listOf(color.copy(alpha = 0.6f), brightColor),
                        radius = 60f * scaleMultiplier
                    )
                ), RoundedCornerShape(3.dp * scaleMultiplier)
            )
    ) {
        Text(text = text, color = brightColor, fontSize = 12.sp * scaleMultiplier)
    }
}

@Composable
fun CustomLinearProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    // Внешний Box - это фон (трек)
    Box(
        modifier = modifier
            .background(trackColor)
            .fillMaxWidth()
    ) {
        // Внутренний Box - это сама полоска прогресса
        Box(
            modifier = Modifier
                .background(color)
                .fillMaxHeight()
                // Ключевой момент: ширина внутреннего блока - это доля от ширины внешнего
                .fillMaxWidth(fraction = progress)
        )
    }
}

@Composable
fun TournamentWinnerDialog(
    playersOnTableProvider: () -> ImmutableList<PlayerState>,
    winnerId: String?,
    lastBoardResult: Long,
    onReturnToLobby: () -> Unit
) {
    val playersOnTable = playersOnTableProvider()
    val playerState = playersOnTable.find { it.player.userId == winnerId }
    if(playerState != null) {
        val player = playerState.player
        val winner = player.copy(isReady = false, stack = player.stack + lastBoardResult)
        // Полупрозрачный фон, который затемняет игру
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA000))))
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Иконка кубка
                    Image(
                        painter = painterResource(R.drawable.winner_cup),
                        contentDescription = "Winner Trophy",
                        modifier = Modifier.size(64.dp)
                    )

                    Text(
                        text = "TOURNAMENT WINNER!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // Аватарка победителя со свечением
                    Box(contentAlignment = Alignment.Center) {
                        RadiantGlowEffectEnhanced(modifier = Modifier.size(120.dp), rayCount = 32, color = Color(0xFFFFB506))
                        PlayerDisplay(
                            playerState = PlayerState(player = winner),
                            myUserId = "", // Неважно, просто для отображения
                            isActivePlayer = false,
                            turnExpiresAt = null,
                            isGameStarted = false,
                            visibleActionIdsProvider = { persistentSetOf() },
                            displayMode = StackDisplayMode.CHIPS,
                            bigBlind = 0L,
                            scaleMultiplier = 1.2f
                        )
                    }

                    Button(onClick = onReturnToLobby) {
                        Text("Return to Lobby")
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmExitDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss, // Сработает при клике мимо окна
        title = { Text("Confirm Exit") },
        text = { Text("Are you sure you want to leave the game? Your hand will be folded.") },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Leave") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun TopBar(
    gameModeProvider: () -> GameMode?,
    tournamentInfoProvider: () -> TournamentInfo?,
    specsCountProvider: () -> Int,
    modifier: Modifier
) {
    val tournamentInfo = tournamentInfoProvider()
    val gameMode = gameModeProvider()
    val animatedCount by animateIntAsState(
        targetValue = specsCountProvider(),
        animationSpec = tween(durationMillis = 300)
    )
    var levelSeconds by remember { mutableLongStateOf(0L) }

    val topBarText = if(gameMode == GameMode.CASH) "Cash 10 / 20"
    else {
        tournamentInfo?.let {
            val ante = if(it.ante == 0L) "-" else it.ante
            "sb ${it.sb}/bb ${it.bb}/ante $ante"
        } ?: "Tournament"
    }
    val leftText = if(gameMode == GameMode.CASH) null else {
        LaunchedEffect(key1 = tournamentInfo?.levelTime) {
            tournamentInfo?.levelTime?.let {
                while (true) {
                    val remaining = it - System.currentTimeMillis()
                    if (remaining <= 0) {
                        levelSeconds = 0
                        break
                    }
                    levelSeconds = remaining
                    delay(1000L)
                }
            }
        }
        tournamentInfo?.let { "level${it.level} | ${levelSeconds.toMinutesSeconds()}" }
    }
    Box(modifier) {
        if(gameMode == GameMode.CASH) {
            Text(topBarText, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center), color = Color.White)
        } else {
            val textSize = when(topBarText.length) {
                in 0..20 -> 14.sp
                in 21..24 -> 12.sp
                else -> 11.sp
            }
            leftText?.let { Text(it, textAlign = TextAlign.Start, modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(3.dp, 0.dp), color = Color.White, fontSize = textSize) }
            Text(topBarText, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center), color = Color.White, fontSize = textSize)
        }
        if (animatedCount != 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(3.dp, 0.dp)
                    .align(Alignment.CenterEnd)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_visibility),
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
}

@Composable
fun WaitingPlayersLayout(
    modifier: Modifier,
    specsCountProvider: () -> Int
) {
    val animatedCount by animateIntAsState(
        targetValue = specsCountProvider(),
        animationSpec = tween(durationMillis = 300)
    )
    Column(modifier = modifier) {
        Text("Waiting for players...", color = Color.White, style = MaterialTheme.typography.headlineSmall)
        if(animatedCount != 0) {
            Spacer(Modifier.size(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_visibility),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(2.dp, 0.dp))

                Text(
                    text = animatedCount.toString(),
                    color = Color.White,
                    fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun SettingsMenu(
    modifier: Modifier,
    showSettingsMenu: Boolean,
    stackDisplayModeProvider: () -> StackDisplayMode,
    scaleMultiplierProvider: () -> Float,
    onToggleDisplayMode: () -> Unit,
    onIncreaseScale: () -> Unit,
    onDecreaseScale: () -> Unit
) {
    val scaleMultiplier = scaleMultiplierProvider()
    val stackDisplayMode = stackDisplayModeProvider()
    AnimatedVisibility(
        visible = showSettingsMenu,
        modifier = modifier,
        enter = slideInHorizontally { it },
        exit = slideOutHorizontally { it }
    ) {
        Card(elevation = CardDefaults.cardElevation(8.dp)) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Show stack in BB")
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = stackDisplayMode == StackDisplayMode.BIG_BLINDS,
                    onCheckedChange = { onToggleDisplayMode }
                )
            }
            Text("UI Scale", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(onClick = { onDecreaseScale }) {
                    Icon(painter = painterResource(R.drawable.ic_remove), "Decrease")
                }

                Text(
                    text = "${(scaleMultiplier * 100).toInt() + 1}%",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { onIncreaseScale }) {
                    Icon(Icons.Default.Add, "Increase")
                }
            }
        }
    }
}

@Composable
fun PlayersLayout(
    playersOnTableProvider: () -> ImmutableList<PlayerState>,
    myUserIdProvider: () -> String?,
    boardResultProvider: () ->  ImmutableList<Pair<String, Long>>?,
    gameStateProvider: () -> GameState?,
    allInEquityProvider: () -> AllInEquity?,
    visibleActionIdsProvider: () -> ImmutableSet<String>,
    scaleMultiplierProvider: () -> Float,
    stackDisplayModeProvider: () -> StackDisplayMode,
    onLastBoardResultChange: (Long) -> Unit
) {
    val playersOnTable = playersOnTableProvider()
    val myUserId = myUserIdProvider()
    val boardResult = boardResultProvider()
    val gameState = gameStateProvider()
    val scaleMultiplier = scaleMultiplierProvider()
    val stackDisplayMode = stackDisplayModeProvider()
    val winnerIds = remember(boardResult) {
        boardResult?.map { it.first }?.toImmutableSet() ?: persistentSetOf()
    }
    val activePlayerId = remember(gameState?.activePlayerPosition) {
        gameState?.playerStates?.getOrNull(gameState.activePlayerPosition)?.player?.userId
    }
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
    val (alignments, equityPositions) = remember(reorderedPlayers.size) {
        calculatePlayerPosition(reorderedPlayers.size)
    }
    Box(Modifier.fillMaxSize()) {
        reorderedPlayers.forEachIndexed { index, playerState ->
            key(playerState.player.userId) {
                val tailDirection = if(equityPositions[index]) TailDirection.RIGHT else TailDirection.LEFT
                val isActivePlayer = playerState.player.userId == activePlayerId

                if(playerState.currentBet > 0) {
                    val (h, v) = alignments[index]
                    val (bet, textBet) = if(stackDisplayMode == StackDisplayMode.CHIPS) {
                        playerState.currentBet.toFloat() to playerState.currentBet.toString()
                    }
                    else {
                        playerState.currentBet.toBBFloat(gameState?.bigBlindAmount ?: 0L) to playerState.currentBet.toBB(gameState?.bigBlindAmount ?: 0L) + " BB"
                    }

                    val animatedAlpha = remember { Animatable(0f) }
                    val animatedH = remember { Animatable(h * 0.8f) }
                    val animatedV = remember { Animatable(v * 0.8f) }

                    val animatedAlignment = BiasAlignment(animatedH.value, animatedV.value)

                    LaunchedEffect(key1 = playerState.currentBet) {
                        launch { animatedAlpha.animateTo(1f, animationSpec = tween(200)) }
                        launch { animatedH.animateTo(h * 0.6f, animationSpec = tween(400)) }
                        launch { animatedV.animateTo(v * 0.6f, animationSpec = tween(400)) }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy((-3).dp * scaleMultiplier),
                        modifier = Modifier
                            .align(animatedAlignment)
                            .alpha(animatedAlpha.value)) {
                        val chips = remember(bet) { calculateChipStack(ceil(bet).toLong()) }
                        PerspectiveChipStack(
                            chips = chips,
                            chipSize = 30.dp * scaleMultiplier
                        )
                        Text(
                            text = textBet,
                            fontSize = 10.sp * scaleMultiplier,
                            color = Color.White.copy(alpha = 0.8f),
                            fontFamily = MerriWeatherFontFamily,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false
                                )
                            )
                        )
                    }
                }
                if(playerState.player.userId in winnerIds) {
                    val amount = boardResult?.find { it.first == playerState.player.userId }?.second ?: 0L
                    if(amount > 0) {
                        //lastBoardResult = amount
                        onLastBoardResultChange(amount)
                        val (h, v) = alignments[index]
                        val (bet, textBet) = if(stackDisplayMode == StackDisplayMode.CHIPS) {
                            amount.toFloat() to amount.toString()
                        }
                        else {
                            amount.toBBFloat(gameState?.bigBlindAmount ?: 0L) to amount.toBB(gameState?.bigBlindAmount ?: 0L) + " BB"
                        }
                        val animatedAlpha = remember { Animatable(0f) }
                        val animatedH = remember { Animatable(0f) }
                        val animatedV = remember { Animatable(0f) }

                        val animatedAlignment = BiasAlignment(animatedH.value, animatedV.value)
                        LaunchedEffect(key1 = amount) {
                            launch {
                                animatedAlpha.animateTo(1f, animationSpec = tween(200))
                                animatedAlpha.animateTo(0f, animationSpec = tween(500, delayMillis = 1500))
                            }
                            launch { animatedH.animateTo(h * 0.65f, animationSpec = tween(2000)) }
                            launch { animatedV.animateTo(v * 0.65f, animationSpec = tween(2000)) }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy((-3).dp * scaleMultiplier),
                            modifier = Modifier
                                .align(animatedAlignment)
                                .alpha(animatedAlpha.value)) {
                            val chips = remember(bet) { calculateChipStack(ceil(bet).toLong()) }
                            PerspectiveChipStack(
                                chips = chips,
                                chipSize = 30.dp * scaleMultiplier
                            )
                            Text(
                                text = textBet,
                                fontSize = 10.sp * scaleMultiplier,
                                color = Color.White.copy(alpha = 0.8f),
                                fontFamily = MerriWeatherFontFamily,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                )
                            )
                        }
                    }
                }
                PlayerWithEquity(
                    allInEquityProvider = { allInEquityProvider() },
                    tailDirection = tailDirection,
                    modifier = Modifier
                        .align(alignments[index])
                        .padding(3.dp),
                    playerState = playerState,
                    myUserId = myUserId,
                    isActivePlayer = isActivePlayer,
                    turnExpiresAt = gameState?.turnExpiresAt,
                    isGameStarted = gameState != null,
                    visibleActionIdsProvider = { visibleActionIdsProvider() },
                    scaleMultiplier = scaleMultiplier,
                    displayMode = stackDisplayMode,
                    isWinner = playerState.player.userId in winnerIds,
                    bigBlind = gameState?.bigBlindAmount ?: 0L
                )
            }
        }
    }
}

@Composable
fun BottomLayout(
    modifier: Modifier,
    playersOnTableProvider: () -> ImmutableList<PlayerState>,
    myUserIdProvider: () -> String?,
    runItStateProvider: () -> RunItUiState,
    stackDisplayModeProvider: () -> StackDisplayMode,
    gameStateProvider: () -> GameState?,
    gameModeProvider: () -> GameMode?,
    isActionPanelLockedProvider: () -> Boolean,
    allInEquityProvider: () -> AllInEquity?,
    onSitAtTableClick: () -> Unit,
    onReadyClick: (Boolean) -> Unit,
    onFold: () -> Unit,
    onCheck: () -> Unit,
    onCall: () -> Unit,
    onBet: (Long) -> Unit,
    onChoice: (Int) -> Unit,
    onConfirm: (Boolean) -> Unit,
    onHideRunItState: () -> Unit
) {
    val playersOnTable = playersOnTableProvider()
    val runItState = runItStateProvider()

    when (val state = runItState) {
        is RunItUiState.Hidden -> {
            val myUserId = myUserIdProvider()
            val stackDisplayMode = stackDisplayModeProvider()
            val gameMode = gameModeProvider()
            val myPlayer = playersOnTable.find { it.player.userId == myUserId }?.player
            ActionPanel(
                myPlayer = myPlayer,
                myUserId = myUserId,
                gameStateProvider = { gameStateProvider() },
                isActionPanelLockedProvider = { isActionPanelLockedProvider() },
                allInEquityProvider = { allInEquityProvider() },
                displayMode = stackDisplayMode,
                modifier = modifier,
                isTournament = gameMode == GameMode.TOURNAMENT,
                onSitAtTableClick = { onSitAtTableClick() },
                onReadyClick = { onReadyClick(it) },
                onFold = { onFold() },
                onCheck = { onCheck() },
                onCall = { onCall() },
                onBet = { onBet(it) }
            )
        }
        is RunItUiState.AwaitingUnderdogChoice -> {
            UnderdogChoiceUi(
                modifier = modifier,
                expiresAt = state.expiresAt,
                onChoice = { onChoice(it) },
                onHideRunItState = { onHideRunItState }
            )
        }
        is RunItUiState.AwaitingFavoriteConfirmation -> {
            val underdogName = playersOnTable.find { it.player.userId == state.underdogId }?.player?.username
            FavoriteConfirmationUi(
                underdogName = underdogName ?: state.underdogId,
                times = state.times,
                expiresAt = state.expiresAt,
                modifier = modifier,
                onConfirm = { onConfirm(it) },
                onHideRunItState = { onHideRunItState }
            )
        }
    }
}

@Composable
fun BoardLayout(
    gameStateProvider: () -> GameState?,
    boardRunoutsProvider: () -> ImmutableList<ImmutableList<Card>>,
    staticCardsProvider: () -> ImmutableList<Card>,
    runCountProvider: () -> Int,
    stackDisplayModeProvider: () -> StackDisplayMode,
    specsCountProvider: () -> Int,
    @SuppressLint("ModifierParameter") multiboardModifier: Modifier,
    singleBoardModifier: Modifier,
    waitingModifier: Modifier
) {
    val gameState = gameStateProvider()
    val boardRunouts = boardRunoutsProvider()
    val stackDisplayMode = stackDisplayModeProvider()
    if (gameState != null) {
        if(boardRunouts.isNotEmpty()) {
            MultiBoardLayout(staticCardsProvider = { staticCardsProvider() }, runouts = boardRunouts, runsProvider = { runCountProvider() }, pot = gameState.pot,
                displayMode = stackDisplayMode, bigBlind = gameState.bigBlindAmount,
                modifier = multiboardModifier)
        } else {
            SingleBoardLayout(
                gameState.pot,
                gameState.bigBlindAmount,
                gameState.communityCards,
                stackDisplayMode,
                singleBoardModifier
            )
        }
    } else {
        WaitingPlayersLayout(modifier = waitingModifier,
            specsCountProvider = { specsCountProvider() }
        )
    }
}

// todo использовать в главном экране
//val configuration = LocalConfiguration.current
//when (configuration.orientation) {
//    Configuration.ORIENTATION_LANDSCAPE -> { /* Логика для ландшафта */ }
//    else -> { /* Логика для портрета */ }
//}