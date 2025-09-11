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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.poker.R
import com.example.poker.shared.dto.GameMode
import com.example.poker.shared.dto.GameStage
import com.example.poker.shared.dto.OutsInfo
import com.example.poker.shared.dto.Player
import com.example.poker.shared.dto.PlayerAction
import com.example.poker.shared.dto.PlayerStatus
import com.example.poker.shared.model.Card
import com.example.poker.ui.theme.MerriWeatherFontFamily
import com.example.poker.util.OutDisplayItem
import com.example.poker.util.calculateChipStack
import com.example.poker.util.calculateOffset
import com.example.poker.util.calculatePlayerPosition
import com.example.poker.util.prepareOutDisplayItems
import com.example.poker.util.toBB
import com.example.poker.util.toBBFloat
import com.example.poker.util.toMinutesSeconds
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun GameScreen(viewModel: GameViewModel, onNavigateToLobby: () -> Unit) {
    val winnerId by viewModel.tournamentWinner.collectAsState()
    val scaleMultiplier by viewModel.scaleMultiplier.collectAsState()
    val stackDisplayMode by viewModel.stackDisplayMode.collectAsState()
    val gameMode by viewModel.gameMode.collectAsState()
    val specsCount by viewModel.specsCount.collectAsState()
    val tournamentInfo by viewModel.tournamentInfo.collectAsState()
    val isReconnecting by viewModel.isReconnecting.collectAsState()
    val isPerformanceMode by viewModel.isPerformanceMode.collectAsState()
    val isClassicCardsEnabled by viewModel.isClassicCardsEnabled.collectAsState()
    val isFourColorMode by viewModel.isFourColorMode.collectAsState()
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var lastBoardResult by remember { mutableLongStateOf(0L) }

    val lifecycleOwner = LocalLifecycleOwner.current
    // Следим за жизненным циклом экрана
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.connect()
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
            TopBar(gameMode, tournamentInfo, specsCount, isReconnecting, topBarModifier)
            val boxModifier3 = remember {
                Modifier
                    .padding(top = 30.dp, bottom = 63.dp)
                    .background(Color(0xFF004D40))
                    .fillMaxSize()
            }
            Box(boxModifier3) {
                val settingsModifier = remember {
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(50.dp)
                        .clip(CircleShape)
                        .padding(end = 5.dp, bottom = 5.dp)
                        .clickable(onClick = { showSettingsMenu = !showSettingsMenu })
                }
                // Кнопка настроек
                Icon(painter = painterResource(R.drawable.ic_settings), contentDescription = "Settings", tint = Color.Black, modifier = settingsModifier)

                PlayersLayout(
                    viewModel = viewModel,
                    scaleMultiplier = scaleMultiplier,
                    stackDisplayMode = stackDisplayMode,
                    isPerformanceMode = isPerformanceMode,
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
                    viewModel = viewModel,
                    stackDisplayMode = stackDisplayMode,
                    specsCount = specsCount,
                    isClassicCardsEnabled = isClassicCardsEnabled,
                    isFourColorMode = isFourColorMode,
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
                    stackDisplayMode = stackDisplayMode,
                    isClassicCardsEnabled = isClassicCardsEnabled,
                    isFourColorMode = isFourColorMode,
                    scaleMultiplier = scaleMultiplier,
                    onToggleClassicCards = { viewModel.toggleClassicCardsEnabled() },
                    onToggleFourColor = { viewModel.toggleFourColorMode() },
                    onToggleDisplayMode = { viewModel.toggleStackDisplayMode() },
                    onIncreaseScale = { viewModel.changeScale(0.05f) },
                    onDecreaseScale = { viewModel.changeScale(-0.05f) }
                )
            }
            BottomLayout(
                viewModel = viewModel,
                isPerformanceMode = isPerformanceMode,
                modifier = Modifier.align(Alignment.BottomCenter),
                stackDisplayMode = stackDisplayMode,
                gameMode = gameMode
            )
            winnerId?.let {
                TournamentWinnerDialog(
                    playerStatesProvider = { viewModel.gameState.value?.playerStates },
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
    cards: ImmutableList<Card>,
    modifier: Modifier = Modifier,
    staticCardsSize: Int = 0,
    isMultiboard: Boolean = false,
    isClassicCardsEnabled: Boolean = false,
    isFourColorMode: Boolean = true
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val density = LocalDensity.current
        val targets = remember(maxWidth, maxHeight) {
            with(density) {
                val cardWidth = maxWidth / 5
                val cardWidthPx = cardWidth.toPx()
                val offsetPx = (2.5.dp).toPx() // 1/2 от нужного отступа
                object {
                    val drawCardWidth = cardWidth - 5.dp // нужный отступ
                    val drawCardHeight = drawCardWidth * 1.5f
                    // Начальная позиция "вылета"
                    val startX = (maxWidth * 0.5f).toPx()
                    val startY = (maxHeight * 0.75f).toPx()
                    // Рассчитываем целевые X-позиции для карт
                    val flopTarget1X = offsetPx // 0f + offsetPx
                    val flopTarget2X = cardWidthPx + offsetPx
                    val flopTarget3X = cardWidthPx * 2 + offsetPx
                    val turnTargetX = cardWidthPx * 3 + offsetPx
                    val riverTargetX = cardWidthPx * 4 + offsetPx
                }
            }
        }
        // 1. Создаем анимируемые состояния для каждой из 5 карт
        val cardOffsetsX = remember { List(5) { Animatable(0f) } }
        val cardOffsetsY = remember { List(5) { Animatable(0f) } }
        val cardAlphas = remember { List(5) { Animatable(0f) } }
        val cardRotations = remember { List(5) { Animatable(0f) } }

        var isReadyForAnimation by remember { mutableStateOf(isMultiboard) }

        // 2. LaunchedEffect - "мозг" анимации. Запускается, когда меняется список карт
        LaunchedEffect(cards) {
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
                        cardOffsetsX[i].snapTo(targets.startX)
                        cardOffsetsY[i].snapTo(targets.startY)
                        cardAlphas[i].snapTo(0f)
                    }
                }
                4 -> {
                    cardOffsetsX[3].snapTo(targets.turnTargetX)
                    cardOffsetsY[3].snapTo(targets.startY)
                }
                5 -> {
                    cardOffsetsX[4].snapTo(targets.riverTargetX)
                    cardOffsetsY[4].snapTo(targets.startY)
                }
            }
            when (cards.size) {
                3 -> { // Флоп
                    // Теперь, когда все карты на стартовых позициях, разрешаем их показать
                    isReadyForAnimation = true

                    // 2. Анимируем их "вылет" в центр стола
                    (0..2).forEach { i -> launch { cardAlphas[i].animateTo(1f, tween(100)) } }
                    (0..2).forEach { i -> launch { cardOffsetsX[i].animateTo(targets.flopTarget1X, tween((i+1) * 300)) } }
                    (0..2).forEach { i -> launch { cardOffsetsY[i].animateTo(0f, tween((i+1) * 300)) } }
                    delay(950) // Ждем, пока они прилетят
                    // 3. Анимируем "разъезжание" крайних карт из центра
                    launch { cardOffsetsX[1].animateTo(targets.flopTarget2X, spring(stiffness = Spring.StiffnessLow)) }
                    launch { cardOffsetsX[2].animateTo(targets.flopTarget3X, spring(stiffness = Spring.StiffnessLow)) }
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
                .height(targets.drawCardHeight)) {
                (staticCardsSize..4).forEach { i ->
                    val card = cards.getOrNull(i)
                    card?.let {
                        val mod = Modifier
                            .width(targets.drawCardWidth)
                            .graphicsLayer {
                                alpha = cardAlphas[i].value
                                rotationZ = cardRotations[i].value
                                translationX = cardOffsetsX[i].value
                                translationY = cardOffsetsY[i].value
                            }
                        if(isClassicCardsEnabled) {
                            ClassicPokerCard(card = it, isFourColorMode = isFourColorMode, modifier = mod)
                        } else {
                            PokerCard(card = it, modifier = mod)
                        }
                    }
                }
            }
        } else Box(Modifier.height(targets.drawCardHeight))
    }
}

@Composable
fun SingleBoardLayout(
    pot: Long,
    bigBlindAmount: Long,
    communityCards: ImmutableList<Card>,
    displayMode: StackDisplayMode,
    isClassicCardsEnabled: Boolean,
    isFourColorMode: Boolean,
    modifier: Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        val text = if(displayMode == StackDisplayMode.CHIPS) pot.toString() else pot.toBB(bigBlindAmount) + " BB"
        Text("Pot: $text", color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        AnimatedCommunityCards(cards = communityCards, isClassicCardsEnabled = isClassicCardsEnabled, isFourColorMode = isFourColorMode)
    }
}

@Composable
fun MultiBoardLayout(
    staticCards: ImmutableList<Card>,
    runouts: ImmutableList<ImmutableList<Card>>,
    runs: Int,
    pot: Long,
    displayMode: StackDisplayMode,
    isClassicCardsEnabled: Boolean,
    isFourColorMode: Boolean,
    bigBlind: Long,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val layoutData = remember(maxWidth, runs) {
            val cardWidth = maxWidth / 5
            val drawCardWidth = cardWidth - 5.dp
            val cardHeight = drawCardWidth * 1.5f

            // Вычисляем смещение для текста с банком
            val potTextOffset = if(runs == 2) -(cardHeight / 2) -(cardHeight / 2.5f)
            else -cardHeight -(cardHeight / 5)
            // Заранее вычисляем все целевые точки для анимации
            val runTargets = List(runs) { index ->
                when (runs) {
                    2 -> if (index == 0) -(cardHeight / 4) else cardHeight / 4
                    3 -> when (index) {
                        0 -> -(cardHeight / 2)
                        1 -> 0.dp
                        else -> cardHeight / 2
                    }
                    else -> 0.dp
                }
            }
            // Возвращаем объект со всеми вычисленными значениями
            object {
                val drawWidth = drawCardWidth
                val height = cardHeight
                val potOffset = potTextOffset
                val targets = runTargets
            }
        }
        val potText = remember(pot, displayMode, bigBlind) {
            if (displayMode == StackDisplayMode.CHIPS) pot.toString() else pot.toBB(bigBlind) + " BB"
        }
        Text("Pot: $potText", color = Color.White, modifier = Modifier
            .align(Alignment.Center)
            .offset(y = layoutData.potOffset))

        // 1. Рисуем статичные карты
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.CenterStart)) {
            Spacer(Modifier.width(2.5.dp))
            staticCards.forEach { card ->
                val mod = Modifier
                    .width(layoutData.drawWidth)
                    .height(layoutData.height)
                if(isClassicCardsEnabled) {
                    ClassicPokerCard(card, isFourColorMode, mod)
                } else {
                    PokerCard(card, mod)
                }
                Spacer(Modifier.width(5.dp))
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(-layoutData.height), // Отрицательный отступ для наложения
            horizontalAlignment = Alignment.End
        ) {
            for ((index, runout) in runouts.withIndex()) {
                key(index) {
                    // Анимируем смещение вверх для каждой предыдущей доски
                    val yOffset by animateDpAsState(
                        // Смещаем каждую доску, кроме последней, на половину высоты карты
                        targetValue = layoutData.targets.getOrElse(index) { 0.dp },
                        label = "boardOffset$index"
                    )
                    // Показываем доску с анимацией появления
                    AnimatedVisibility(
                        visible = true, // Управляется самим списком runouts
                        enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = 200))
                    ) {
                        Row(
                            modifier = Modifier.graphicsLayer { translationY = yOffset.toPx() }
                        ) {
                            // 2. Рисуем карты этого прогона
                            AnimatedCommunityCards(
                                cards = (staticCards + runout).toImmutableList(),
                                modifier = Modifier,
                                staticCardsSize = staticCards.size,
                                isMultiboard = true,
                                isClassicCardsEnabled = isClassicCardsEnabled,
                                isFourColorMode = isFourColorMode
                            )
                        }
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
    isActionPanelLocked: Boolean,
    allInEquity: AllInEquity?,
    gameState: GameState?,
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

    val playerState = remember(gameState?.playerStates, myUserId) {
        gameState?.playerStates?.find { it.player.userId == myUserId }
    }
    val boxModifier = remember(modifier) {
        modifier
            .height(63.dp)
            .fillMaxWidth()
            .background(Color.Black)
    }
    // Используем Box для наложения ползунка поверх панели
    Box(contentAlignment = Alignment.TopCenter, modifier = boxModifier) {
        when {
            myPlayer?.status == PlayerStatus.SPECTATING && (!isTournament || gameState == null)  -> {
                BottomButton(onClick = { onSitAtTableClick() }, text = "Sit at Table", modifier = Modifier.fillMaxWidth())
            }
            gameState == null -> {
                if (myPlayer != null) {
                    val text = if (myPlayer.isReady) "Cancel Ready" else "I'm Ready"
                    BottomButton(onClick = { onReadyClick(!myPlayer.isReady) }, text = text, modifier = Modifier.fillMaxWidth())
                }
            }
            else -> {
                // --- ОСНОВНАЯ ПАНЕЛЬ С ТРЕМЯ КНОПКАМИ ---
                val activeId = remember(gameState.activePlayerPosition) {
                    gameState.playerStates.getOrNull(gameState.activePlayerPosition)?.player?.userId
                }
                val isMyTurn = activeId == myUserId && !isActionPanelLocked && allInEquity == null && gameState.stage != GameStage.SHOWDOWN
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
                        val callText = remember(displayMode, callValue, gameState.bigBlindAmount) {
                            if (displayMode == StackDisplayMode.BIG_BLINDS) {
                                "Call ${callValue.toBB(gameState.bigBlindAmount)} BB"
                            } else "Call $callValue"
                        }
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
    if (showBetSlider && playerState != null && gameState != null) {
        val imeInsets = WindowInsets.ime
        val bottomPadding by animateDpAsState(
            targetValue = with(LocalDensity.current) {
                imeInsets.getBottom(LocalDensity.current).toDp()
            },
            label = "bottom_padding_animation"
        )
        val totalPadding = if(bottomPadding > 100.dp) bottomPadding - 100.dp else bottomPadding
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = modifier
                .padding(bottom = totalPadding)
                .fillMaxWidth()
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

    val (presetX2, presetX3, presetX4) = remember(amountToCall, minBet) {
        if(amountToCall > 0) Triple(amountToCall * 2, amountToCall * 3, amountToCall * 4)
        else Triple(minBet * 2, minBet * 3, minBet * 4)
    }
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
        val iconModifier = remember {
            Modifier
                .align(Alignment.End)
                .size(30.dp)
                .clip(CircleShape)
                .background(Color.DarkGray)
                .border(1.dp, Color.White, shape = CircleShape)
                .clickable(onClick = onDismiss)
        }
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close bet controls",
            tint = Color.White,
            modifier = iconModifier
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
                Column(modifier = Modifier.weight(1f)) {
                    val labelText = if (displayMode == StackDisplayMode.BIG_BLINDS) "Amount in BB" else "Amount"
                    Text(
                        text = labelText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

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
                        isError = !isBetValid,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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
    val border = remember(enabled) {
        val color1 = if (enabled) Color.Red else Color(0xFF640D14)
        val color2 = if (enabled) Color(0xFF640D14) else Color.Black
        BorderStroke(5.dp, Brush.radialGradient(listOf(color1, color2), radius = 170f))
    }
    val buttonColors = remember {
        ButtonColors(
            containerColor = Color.Red.copy(alpha = 0.7f),
            contentColor = Color.White.copy(alpha = 0.8f),
            disabledContainerColor = Color(0xFF38070B),
            disabledContentColor = Color.Black
        )
    }
    FilledTonalButton(
        modifier = modifier
            .height(60.dp)
            .padding(horizontal = 1.dp),
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = buttonColors,
        border = border
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
    allInEquity: AllInEquity?,
    tailDirection: TailDirection,
    playerState: PlayerState,
    myUserId: String?,
    isActivePlayer: Boolean,
    isPerformanceMode: Boolean,
    turnExpiresAt: Long?,
    modifier: Modifier = Modifier,
    isGameStarted: Boolean,
    isWinner: Boolean = false,
    displayMode: StackDisplayMode,
    bigBlind: Long,
    scaleMultiplier: Float,
    alignHValue: Float
) {
    val equity = allInEquity?.equities?.get(playerState.player.userId)
    val out = allInEquity?.outs?.get(playerState.player.userId)

    val (requiredWidth, offset) = remember(equity != null, out != null, scaleMultiplier, alignHValue != 0f) {
        if(alignHValue != 0f) {
            when {
                out != null -> 255.dp * scaleMultiplier to 92.5.dp * scaleMultiplier * alignHValue
                equity != null -> 190.dp * scaleMultiplier to 60.dp * scaleMultiplier * alignHValue
                else -> 70.dp * scaleMultiplier to 0.dp
            }
        } else {
            when {
                out != null -> 255.dp * scaleMultiplier to 0.dp
                equity != null -> 190.dp * scaleMultiplier to 0.dp
                else -> 70.dp * scaleMultiplier to 0.dp
            }
        }
    }
    Box(modifier = modifier
        .width(requiredWidth)
        .offset(offset), contentAlignment = Alignment.Center) {
        PlayerDisplay(
            modifier = Modifier,
            playerState = playerState,
            myUserId = myUserId,
            isActivePlayer = isActivePlayer,
            turnExpiresAt = turnExpiresAt,
            isGameStarted = isGameStarted,
            isPerformanceMode = isPerformanceMode,
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
    isPerformanceMode: Boolean,
    turnExpiresAt: Long?,
    myUserId: String?,
    modifier: Modifier = Modifier,
    isGameStarted: Boolean,
    isWinner: Boolean = false,
    displayMode: StackDisplayMode,
    bigBlind: Long,
    scaleMultiplier: Float
) {
    val boxModifier = remember(scaleMultiplier) {
        modifier
            .width(70.dp * scaleMultiplier)
            .height(80.dp * scaleMultiplier)
            .padding(horizontal = 5.dp * scaleMultiplier)
    }
    Box(modifier = boxModifier) {
        val iconModifier = remember(scaleMultiplier) {
            Modifier
                .padding(top = 5.dp * scaleMultiplier)
                .size(55.dp * scaleMultiplier)
                .align(Alignment.TopCenter)
                .clip(CircleShape)
                .background(Color.DarkGray)
                .border(1.dp, Color.White, shape = CircleShape)
        }
        if(isGameStarted) {
            if(playerState.hasFolded) {
                Icon(imageVector = Icons.Default.Person, contentDescription = "Player Avatar", tint = Color.White, modifier = iconModifier)
            }
        } else {
            Icon(imageVector = Icons.Default.Person, contentDescription = "Player Avatar", tint = Color.White, modifier = iconModifier)
            if(playerState.player.isReady) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Is ready",
                    tint = Color.Green,
                    modifier = Modifier.size(60.dp * scaleMultiplier)
                )
            }
        }
        val arrangement1 = if(isPerformanceMode) Arrangement.Center else  Arrangement.spacedBy((-15).dp * scaleMultiplier)
        Column(modifier = Modifier.align(Alignment.BottomCenter),
            verticalArrangement = arrangement1) {
            if (isGameStarted) {
                val (card1, card2) = if (playerState.player.userId == myUserId || playerState.cards.isNotEmpty()) {
                    playerState.cards.getOrNull(0) to playerState.cards.getOrNull(1)
                } else null to null
                AnimatedVisibility(
                    visible = !playerState.hasFolded,
                    enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                    exit = slideOutVertically(targetOffsetY = { it / 6 }) + fadeOut(animationSpec = tween(durationMillis = 500))
                ) {
                    val arrangement2 = if(isPerformanceMode) Arrangement.Center else Arrangement.spacedBy((-20).dp * scaleMultiplier)
                    Row(horizontalArrangement = arrangement2) {
                        if(isPerformanceMode) {
                            SimplePokerCard(card1, scaleMultiplier)
                            SimplePokerCard(card2, scaleMultiplier)
                        } else {
                            FlippingPokerCard(
                                card = card1,
                                flipDirection = FlipDirection.COUNTER_CLOCKWISE,
                                scaleMultiplier = scaleMultiplier,
                                rotation = -10f
                            )
                            FlippingPokerCard(
                                card = card2,
                                flipDirection = FlipDirection.CLOCKWISE,
                                scaleMultiplier = scaleMultiplier,
                                rotation = 10f
                            )
                        }
                    }
                }
            }
            PlayerInfoWithTimer(
                playerState = playerState,
                isActivePlayer = isActivePlayer,
                isPerformanceMode = isPerformanceMode,
                turnExpiresAt = turnExpiresAt,
                displayMode = displayMode,
                bigBlind = bigBlind,
                scaleMultiplier = scaleMultiplier,
                modifier = Modifier.fillMaxWidth()
            )
        }
        PlayerActionDisplay(playerState.lastAction, scaleMultiplier, Modifier.align(Alignment.Center))

        if (!playerState.player.isConnected) {
            val connectModifier = remember(scaleMultiplier) {
                Modifier
                    .align(Alignment.Center)
                    .size(40.dp * scaleMultiplier)
                    .clip(RoundedCornerShape(12.dp * scaleMultiplier))
                    .background(Color.Black.copy(alpha = 0.6f))
            }
            Box(
                modifier = connectModifier,
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
                        .size(40.dp * scaleMultiplier)
                        .align(Alignment.Center),
                    color = Color(0xFFFDFFD8),
                    rayCount = 32,
                    innerRadiusRatio = 0.2f
                )
                val imageModifier = remember(scaleMultiplier) {
                    Modifier
                        .width(20.dp * scaleMultiplier)
                        .height(30.dp * scaleMultiplier)
                        .align(Alignment.Center)
                }
                Image(
                    painter = painterResource(R.drawable.winner_cup),
                    contentDescription = "Winner",
                    modifier = imageModifier
                )
            }
        }
    }
}

@Composable
fun PlayerInfoWithTimer(
    playerState: PlayerState,
    isActivePlayer: Boolean,
    isPerformanceMode: Boolean,
    turnExpiresAt: Long?,
    modifier: Modifier = Modifier,
    displayMode: StackDisplayMode,
    bigBlind: Long,
    scaleMultiplier: Float
) {
    val totalTime = 15_000L // Общее время на ход
    var remainingTime by remember { mutableLongStateOf(totalTime) }

    // Этот эффект будет обновлять оставшееся время, пока игрок активен
    LaunchedEffect(isActivePlayer, turnExpiresAt, isPerformanceMode) {
        val time = if(isPerformanceMode) 1000L else 50L
        if (isActivePlayer && turnExpiresAt != null) {
            while (isActive) {
                val newRemaining = (turnExpiresAt - System.currentTimeMillis()).coerceAtLeast(0L)
                remainingTime = newRemaining
                if (newRemaining == 0L) break
                delay(time)
            }
        } else remainingTime = 0
    }
    // Рассчитываем прогресс от 1.0f (полный) до 0.0f (пустой)
    val progress = remember(remainingTime) {
        (remainingTime.toFloat() / totalTime).coerceIn(0f, 1f)
    }
    // Анимируем цвет от зеленого к красному
    val progressColor = lerp(Color.Red, Color(0xFF00C853), progress)
    val scaleData = remember(scaleMultiplier) {
        object {
            val shape = RoundedTrapezoidShape(cornerRadius = 4.dp * scaleMultiplier)
            val drawWidth = 3.dp * scaleMultiplier
            val columnModifier = Modifier
                .clip(shape)
                .background(Color.Black)
            val verticalPadding = 1.dp * scaleMultiplier
        }
    }

    Box(
        // Применяем модификатор, который будет рисовать контур
        modifier = modifier
            .drawWithContent {
                // Сначала рисуем основное содержимое
                drawContent()
                // Если это активный игрок и время еще есть, рисуем контур
                if (isActivePlayer && progress > 0f) {
                    // Получаем контур нашей фигуры
                    val outline = scaleData.shape.createOutline(size, layoutDirection, this)
                    if (outline is Outline.Generic) {
                        val path = outline.path
                        val pathMeasure = PathMeasure()
                        val segmentPath = Path()

                        pathMeasure.setPath(path, false)

                        // Получаем только часть контура, соответствующую прогрессу
                        pathMeasure.getSegment(
                            startDistance = 0f,
                            stopDistance = pathMeasure.length * progress,
                            destination = segmentPath,
                            startWithMoveTo = true
                        )

                        // Рисуем полученный сегмент
                        drawPath(
                            path = segmentPath,
                            color = progressColor,
                            style = Stroke(width = scaleData.drawWidth.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }
    ) {
        // Основной контент
        Column(modifier = scaleData.columnModifier) {
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
                    .padding(scaleData.drawWidth, scaleData.verticalPadding + pd),
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
                    .padding(vertical = scaleData.verticalPadding),
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
    }
}

@Composable
fun EquityBubble(
    equity: Double,
    tailDirection: TailDirection,
    scaleMultiplier: Float,
    modifier: Modifier = Modifier
) {
    val border = remember(scaleMultiplier) {
        BorderStroke(1.5.dp * scaleMultiplier, Color(0xFF216625))
    }
    Surface(
        modifier = modifier,
        shape = OvalWithTailShape(
            tailDirection = tailDirection
        ),
        color = Color.Black.copy(alpha = 0.6f),
        border = border
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
                fontSize = 12.sp * scaleMultiplier,
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

@Composable
fun ModernOutsBubble(
    displayItems: List<OutDisplayItem>,
    scaleMultiplier: Float,
    modifier: Modifier = Modifier
) {
    if (displayItems.isEmpty()) return
    val width = remember(displayItems.size) {
        when(displayItems.size) {
            in 1..3 -> 25.dp * scaleMultiplier
            in 4..8 -> 15.dp * scaleMultiplier
            else -> 12.dp * scaleMultiplier
        }
    }

    FlowRow(
        modifier = modifier.padding(horizontal = 8.dp * scaleMultiplier),
        horizontalArrangement = Arrangement.spacedBy(3.dp * scaleMultiplier, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(3.dp * scaleMultiplier),
        maxItemsInEachRow = 5
    ) {
        displayItems.forEach { item ->
            Box(
                modifier = Modifier
                    .width(width)
                    .aspectRatio(0.8f),
                contentAlignment = Alignment.Center
            ) {
                when (item) {
                    is OutDisplayItem.FullCard -> CardFaceSimple(card = item.card, Modifier, 1f, false, 0.5f)
                    is OutDisplayItem.RankGroup -> RankGroupCard(rank = item.rank)
                    is OutDisplayItem.SuitGroup -> SuitGroupCard(suit = item.suit)
                }
            }
        }
    }
}

//@Composable
//@Preview
//fun TestOutsBubble() {
//    OutsBubble(37.56, OutsInfo.DirectOuts(listOf(Card(Rank.TWO, Suit.SPADES), Card(Rank.TWO, Suit.DIAMONDS), Card(Rank.TWO, Suit.HEARTS), Card(Rank.QUEEN, Suit.SPADES), Card(Rank.QUEEN, Suit.DIAMONDS), Card(Rank.QUEEN, Suit.CLUBS))), 1f)
//}

@Composable
fun OutsBubble(
    equity: Double?,
    outsInfo: OutsInfo,
    scaleMultiplier: Float,
    modifier: Modifier = Modifier
) {
    val randomSeed = remember { Random.nextInt() }
    val border = remember(scaleMultiplier) {
        BorderStroke(1.dp * scaleMultiplier, Color(0xFF610C18))
    }
    Surface(
        modifier = modifier.graphicsLayer { clip = false },
        shape = JaggedOvalShape(seed = randomSeed),
        color = Color.Black.copy(alpha = 0.6f),
        border = border
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
                    val displayItems = prepareOutDisplayItems(outs)
                    if(displayItems.size > 10) {
                        Text("${outs.size} Outs", color = Color.LightGray, fontSize = 14.sp * scaleMultiplier, fontFamily = MerriWeatherFontFamily,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false
                                ),
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            ),
                            modifier = Modifier.align(BiasAlignment(0f, -0.2f))
                        )
                    } else {
                        ModernOutsBubble(displayItems, scaleMultiplier, Modifier.align(BiasAlignment(0f, -0.2f)))
                    }
                    Text(
                        text = "$equity%",
                        color = Color(0xFFE61D2A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp * scaleMultiplier,
                        modifier = Modifier.align(BiasAlignment(0f, 0.6f)),
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
                is OutsInfo.RunnerRunner -> {
                    Text("Runner\nRunner", color = Color.LightGray, fontSize = 14.sp * scaleMultiplier, fontFamily = MerriWeatherFontFamily, style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        ),
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.Both
                        )
                    ))
                }
                is OutsInfo.DrawingDead -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Drawing",
                            color = Color.LightGray,
                            fontSize = 14.sp * scaleMultiplier,
                            fontFamily = MerriWeatherFontFamily,
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
                        Text(
                            "Dead",
                            color = Color.LightGray,
                            fontSize = 14.sp * scaleMultiplier,
                            fontFamily = MerriWeatherFontFamily,
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
        }
    }
}

@Composable
fun UnderdogChoiceUi(
    modifier: Modifier,
    isPerformanceMode: Boolean,
    expiresAt: Long,
    onChoice: (Int) -> Unit,
    onHideRunItState: () -> Unit
) {
    var remainingTime by remember { mutableLongStateOf(expiresAt - System.currentTimeMillis()) }
    val time = if(isPerformanceMode) 1000L else 50L
    LaunchedEffect(expiresAt) {
        while (remainingTime > 0) {
            remainingTime = expiresAt - System.currentTimeMillis()
            delay(time)
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
            val barModifier = remember {
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
            }
            CustomLinearProgressBar(
                progress = progress,
                modifier = barModifier,
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
    isPerformanceMode: Boolean,
    times: Int,
    expiresAt: Long,
    modifier: Modifier,
    onConfirm: (Boolean) -> Unit,
    onHideRunItState: () -> Unit
) {
    var remainingTime by remember { mutableLongStateOf(expiresAt - System.currentTimeMillis()) }
    val time = if(isPerformanceMode) 1000L else 50L
    LaunchedEffect(expiresAt) {
        while (remainingTime > 0) {
            remainingTime = expiresAt - System.currentTimeMillis()
            delay(time)
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
            val barModifier = remember {
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
            }
            CustomLinearProgressBar(
                progress = progress,
                modifier = barModifier,
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
fun PlayerActionDisplay(
    action: PlayerAction?,
    scaleMultiplier: Float,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(action?.id) {
        if (action != null) {
            visible = true
            delay(2000L)
            visible = false
        }
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        action?.let {
            PlayerAction(action = it, scaleMultiplier = scaleMultiplier)
        }
    }
}

@Composable
fun PlayerAction(action: PlayerAction, scaleMultiplier: Float) {
    val (text, color, brightColor) = remember(action) {
        when(action) {
            is PlayerAction.Fold -> Triple("Fold", Color(0xFF983036), Color(0xFFE84952))
            is PlayerAction.Check -> Triple("Check", Color(0xFFB29A3B), Color(0xFFF2D250))
            is PlayerAction.Call -> Triple("Call", Color(0xFFDBA656), Color(0xFFF2B85F))
            is PlayerAction.Bet -> Triple("Bet", Color(0xFF3C9FC5), Color(0xFF48BFED))
            is PlayerAction.Raise -> Triple("Raise", Color(0xFF3396AE), Color(0xFF46CCED))
            is PlayerAction.AllIn -> Triple("All-In", Color(0xFFAF6832), Color(0xFFF08E44))
        }
    }
    val shape = remember(scaleMultiplier) { RoundedCornerShape(3.dp * scaleMultiplier) }
    val border = remember(color, brightColor, scaleMultiplier) {
        BorderStroke(
            1.dp * scaleMultiplier,
            Brush.radialGradient(
                listOf(color.copy(alpha = 0.6f), brightColor),
                radius = 60f * scaleMultiplier
            )
        )
    }
    val mainModifier = remember(scaleMultiplier) {
        Modifier
            .width(50.dp * scaleMultiplier)
            .height(20.dp * scaleMultiplier)
            .clip(shape)
            .background(Color.Black.copy(alpha = 0.8f))
    }
    Box(contentAlignment = Alignment.Center,
        modifier = mainModifier.border(border, shape)
    ) {
        Text(text = text, color = brightColor, fontSize = 12.sp * scaleMultiplier, style = TextStyle(
            platformStyle = PlatformTextStyle(
                includeFontPadding = false
            ),
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.Both
            )
        ))
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
    playerStatesProvider: () -> ImmutableList<PlayerState>?,
    winnerId: String?,
    lastBoardResult: Long,
    onReturnToLobby: () -> Unit
) {
    val playersOnTable = playerStatesProvider()
    val playerState = playersOnTable?.find { it.player.userId == winnerId }
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
                            isPerformanceMode = true,
                            turnExpiresAt = null,
                            isGameStarted = false,
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
    gameMode: GameMode?,
    tournamentInfo: TournamentInfo?,
    specsCount: Int,
    isReconnecting: Boolean,
    modifier: Modifier
) {
    val animatedCount by animateIntAsState(
        targetValue = specsCount,
        animationSpec = tween(durationMillis = 300)
    )
    var levelSeconds by remember { mutableLongStateOf(0L) }

    val topBarText = remember(gameMode, tournamentInfo) {
        if(gameMode == GameMode.CASH) "Cash 10 / 20"
        else {
            tournamentInfo?.let {
                val ante = if(it.ante == 0L) "-" else it.ante
                "sb ${it.sb}/bb ${it.bb}/ante $ante"
            } ?: "Tournament"
        }
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
            if(isReconnecting) {
                ReconnectingText(Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 10.dp), 18.sp)
            } else Text(topBarText, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center), color = Color.White)
        } else {
            if(isReconnecting) {
                ReconnectingText(Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 10.dp), 18.sp)
            } else {
                val textSize = when(topBarText.length) {
                    in 0..20 -> 14.sp
                    in 21..24 -> 12.sp
                    else -> 11.sp
                }
                leftText?.let { Text(it, textAlign = TextAlign.Start, modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 3.dp),
                    color = Color.White, fontSize = textSize) }
                Text(topBarText, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center), color = Color.White, fontSize = textSize)
            }
        }
        if (animatedCount != 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 3.dp)
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
    specsCount: Int
) {
    val animatedCount by animateIntAsState(
        targetValue = specsCount,
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
                        .padding(horizontal = 2.dp))

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
    stackDisplayMode: StackDisplayMode,
    isClassicCardsEnabled: Boolean,
    isFourColorMode: Boolean,
    scaleMultiplier: Float,
    onToggleClassicCards: () -> Unit,
    onToggleFourColor: () -> Unit,
    onToggleDisplayMode: () -> Unit,
    onIncreaseScale: () -> Unit,
    onDecreaseScale: () -> Unit
) {
    AnimatedVisibility(
        visible = showSettingsMenu,
        modifier = modifier,
        enter = slideInHorizontally { it },
        exit = slideOutHorizontally { it }
    ) {
        Card(elevation = CardDefaults.cardElevation(8.dp), modifier = Modifier.width(IntrinsicSize.Max)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Classic cards board")
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isClassicCardsEnabled,
                    onCheckedChange = { onToggleClassicCards() }
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Classic 4 color")
                Switch(
                    enabled = isClassicCardsEnabled,
                    checked = isFourColorMode,
                    onCheckedChange = { onToggleFourColor() }
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Show stack in BB")
                Switch(
                    checked = stackDisplayMode == StackDisplayMode.BIG_BLINDS,
                    onCheckedChange = { onToggleDisplayMode() }
                )
            }
            Text("UI Scale", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(onClick = { onDecreaseScale() }) {
                    Icon(painter = painterResource(R.drawable.ic_remove), "Decrease")
                }

                Text(
                    text = "${(scaleMultiplier * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { onIncreaseScale() }) {
                    Icon(Icons.Default.Add, "Increase")
                }
            }
        }
    }
}

@Composable
fun PlayersLayout(
    viewModel: GameViewModel,
    scaleMultiplier: Float,
    stackDisplayMode: StackDisplayMode,
    isPerformanceMode: Boolean,
    onLastBoardResultChange: (Long) -> Unit
) {
    val roomInfo by viewModel.roomInfo.collectAsState()
//    val roomInfo = GameRoom("", "", GameMode.CASH,
//        persistentListOf(
//
//            Player("2", "test2", 1000L, PlayerStatus.SITTING_OUT),
//            Player("3", "test3", 1000L, PlayerStatus.SITTING_OUT),
//            Player("4", "test4", 1000L, PlayerStatus.SITTING_OUT),
//            Player("5", "test5", 1000L, PlayerStatus.SITTING_OUT),
//            Player("6", "test6", 1000L, PlayerStatus.SITTING_OUT),
//            Player("7", "test7", 1000L, PlayerStatus.SITTING_OUT),
//            Player("8", "test8", 1000L, PlayerStatus.SITTING_OUT),
//            Player("9", "test9", 1000L, PlayerStatus.SITTING_OUT),
//        ), ownerId = "1", buyIn = 1000L)
    val gameState by viewModel.gameState.collectAsState()
//    val gameState = GameState("123", playerStates = persistentListOf(
//        PlayerState(Player("1", "test1", 1000L, PlayerStatus.SITTING_OUT)),
//        PlayerState(Player("2", "test2", 1000L, PlayerStatus.SITTING_OUT)),
//        PlayerState(Player("3", "test3", 1000L, PlayerStatus.SITTING_OUT)),
//        PlayerState(Player("4", "test4", 1000L, PlayerStatus.SITTING_OUT)),
//        PlayerState(Player("5", "test5", 1000L, PlayerStatus.SITTING_OUT)),
//        PlayerState(Player("6", "test6", 1000L, PlayerStatus.SITTING_OUT)),
//        PlayerState(Player("7", "test7", 1000L, PlayerStatus.SITTING_OUT)),
//        PlayerState(Player("8", "test8", 1000L, PlayerStatus.SITTING_OUT)),
//        PlayerState(Player("9", "test9", 1000L, PlayerStatus.SITTING_OUT)),
//    ))
    val myUserId by viewModel.myUserId.collectAsState()
    val boardResult by viewModel.boardResult.collectAsState()
    val allInEquity by viewModel.allInEquity.collectAsState()
//    val allInEquity = AllInEquity(persistentMapOf(
//        "1" to 0.0,
//        "2" to 0.0,
//        "3" to 0.0,
//        "4" to 0.0,
//        "5" to 0.0,
//        "6" to 0.0,
//        "7" to 0.0,
//        "8" to 0.0,
//        "9" to 0.0,
//        ),
//        persistentMapOf(
//            "1" to OutsInfo.RunnerRunner,
//            "2" to OutsInfo.RunnerRunner,
//            "3" to OutsInfo.RunnerRunner,
//            "4" to OutsInfo.RunnerRunner,
//            "5" to OutsInfo.RunnerRunner,
//            "6" to OutsInfo.RunnerRunner,
//            "7" to OutsInfo.RunnerRunner,
//            "8" to OutsInfo.RunnerRunner,
//            "9" to OutsInfo.RunnerRunner,
//        ), 1)
    val playersOnTable by remember {
        derivedStateOf {
            if (gameState != null) {
                gameState!!.playerStates
            } else {
                roomInfo?.players?.map { PlayerState(player = it) }?.toImmutableList() ?: persistentListOf()
            }
        }
    }
    val winnerIds = remember(boardResult) {
        boardResult?.map { it.first }?.toImmutableSet() ?: persistentSetOf()
    }
    val activePlayerId by remember {
        derivedStateOf {
            gameState?.playerStates?.getOrNull(gameState?.activePlayerPosition ?: -1)?.player?.userId
        }
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
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val (parentWidthPx, parentHeightPx) = with(density) { maxWidth.toPx() to maxHeight.toPx() }
        val (chipStackWidthFix, chipStackHeightFix) = remember(scaleMultiplier) {
            if(isPerformanceMode) {
                with(density) { (20.dp * scaleMultiplier).toPx() / 2 to (30.dp * scaleMultiplier).toPx() / 2 }
            } else {
                with(density) { (30.dp * scaleMultiplier).toPx() / 2 to (40.dp * scaleMultiplier).toPx() / 2 }
            }
        }
        reorderedPlayers.forEachIndexed { index, playerState ->
            key(playerState.player.userId) {
                val tailDirection = if(equityPositions[index]) TailDirection.RIGHT else TailDirection.LEFT
                val isActivePlayer = playerState.player.userId == activePlayerId

                if(playerState.currentBet > 0) {
                    val (bet, textBet) = remember(playerState.currentBet, stackDisplayMode, gameState?.bigBlindAmount) {
                        if(stackDisplayMode == StackDisplayMode.CHIPS) {
                            playerState.currentBet.toFloat() to playerState.currentBet.toString()
                        }
                        else {
                            playerState.currentBet.toBBFloat(gameState?.bigBlindAmount ?: 0L) to playerState.currentBet.toBB(gameState?.bigBlindAmount ?: 0L) + " BB"
                        }
                    }
                    val (startOffset, endOffset) = remember(alignments[index], parentWidthPx, parentHeightPx, scaleMultiplier) {
                        val (h, v) = alignments[index]
                        val maxDistance = 0.82f
                        val minDistance = 0.47f
                        val baseDistance = 0.64f
                        val endCorrection = if(scaleMultiplier <= 1) maxDistance + (baseDistance - maxDistance) * ((scaleMultiplier - 0.5f) / 0.5f)
                                else baseDistance + (minDistance - baseDistance) * ((scaleMultiplier - 1.0f) / 0.5f)
                        val startAlignment = BiasAlignment(h * 0.85f, v * 0.85f)
                        val endAlignment = BiasAlignment(h * endCorrection, v * endCorrection)
                        calculateOffset(startAlignment, endAlignment, parentWidthPx, parentHeightPx)
                    }

                    val animatedAlpha = remember { Animatable(0f) }
                    val animatedX = remember { Animatable(startOffset.x.toFloat()) }
                    val animatedY = remember { Animatable(startOffset.y.toFloat()) }

                    LaunchedEffect(key1 = playerState.currentBet) {
                        animatedX.snapTo(startOffset.x.toFloat())
                        animatedY.snapTo(startOffset.y.toFloat())
                        launch { animatedAlpha.animateTo(1f, animationSpec = tween(200)) }
                        launch { animatedX.animateTo(endOffset.x.toFloat(), animationSpec = tween(400)) }
                        launch { animatedY.animateTo(endOffset.y.toFloat(), animationSpec = tween(400)) }
                    }
                    val fixWidth = if(isPerformanceMode && stackDisplayMode == StackDisplayMode.BIG_BLINDS) {
                        if(textBet.length > 4) chipStackWidthFix * 1.5f else chipStackWidthFix
                    } else chipStackWidthFix
                    ChipStackAndText(
                        bet = bet,
                        textBet = textBet,
                        scaleMultiplier = scaleMultiplier,
                        isPerformanceMode = isPerformanceMode,
                        modifier = Modifier.graphicsLayer {
                            translationX = animatedX.value - fixWidth
                            translationY = animatedY.value - chipStackHeightFix
                            alpha = animatedAlpha.value
                        }
                    )
                }
                if(playerState.player.userId in winnerIds) {
                    val amount = boardResult?.find { it.first == playerState.player.userId }?.second ?: 0L
                    if(amount > 0) {
                        //lastBoardResult = amount
                        onLastBoardResultChange(amount)
                        val (bet, textBet) = remember(amount, stackDisplayMode, gameState?.bigBlindAmount) {
                            if(stackDisplayMode == StackDisplayMode.CHIPS) {
                                amount.toFloat() to amount.toString()
                            }
                            else {
                                amount.toBBFloat(gameState?.bigBlindAmount ?: 0L) to amount.toBB(gameState?.bigBlindAmount ?: 0L) + " BB"
                            }
                        }
                        val (startOffset, endOffset) = remember(alignments[index], parentWidthPx, parentHeightPx, scaleMultiplier) {
                            val (h, v) = alignments[index]
                            val maxDistance = 0.85f
                            val minDistance = 0.55f
                            val baseDistance = 0.7f
                            val endCorrection = if(scaleMultiplier <= 1) maxDistance + (baseDistance - maxDistance) * ((scaleMultiplier - 0.5f) / 0.5f)
                            else baseDistance + (minDistance - baseDistance) * ((scaleMultiplier - 1.0f) / 0.5f)
                            val startAlignment = BiasAlignment(0f, 0f)
                            val endAlignment = BiasAlignment(h * endCorrection, v * endCorrection)
                            calculateOffset(startAlignment, endAlignment, parentWidthPx, parentHeightPx)
                        }

                        val animatedAlpha = remember { Animatable(0f) }
                        val animatedX = remember { Animatable(startOffset.x.toFloat()) }
                        val animatedY = remember { Animatable(startOffset.y.toFloat()) }

                        LaunchedEffect(key1 = amount) {
                            animatedX.snapTo(startOffset.x.toFloat())
                            animatedY.snapTo(startOffset.y.toFloat())
                            launch {
                                animatedAlpha.animateTo(1f, animationSpec = tween(200))
                                animatedAlpha.animateTo(0f, animationSpec = tween(500, delayMillis = 1500))
                            }
                            launch { animatedX.animateTo(endOffset.x.toFloat(), animationSpec = tween(2000)) }
                            launch { animatedY.animateTo(endOffset.y.toFloat(), animationSpec = tween(2000)) }
                        }
                        val fixWidth = if(isPerformanceMode && stackDisplayMode == StackDisplayMode.BIG_BLINDS) {
                            if(textBet.length > 4) chipStackWidthFix * 1.5f else chipStackWidthFix
                        } else chipStackWidthFix
                        ChipStackAndText(
                            bet = bet,
                            textBet = textBet,
                            scaleMultiplier = scaleMultiplier,
                            isPerformanceMode = isPerformanceMode,
                            modifier = Modifier.graphicsLayer {
                                translationX = animatedX.value - fixWidth
                                translationY = animatedY.value - chipStackHeightFix
                                alpha = animatedAlpha.value
                            }
                        )
                    }
                }
                PlayerWithEquity(
                    allInEquity = allInEquity,
                    tailDirection = tailDirection,
                    modifier = Modifier
                        .align(alignments[index])
                        .padding(3.dp),
                    playerState = playerState,
                    myUserId = myUserId,
                    isActivePlayer = isActivePlayer,
                    isPerformanceMode = isPerformanceMode,
                    turnExpiresAt = gameState?.turnExpiresAt,
                    isGameStarted = gameState != null,
                    scaleMultiplier = scaleMultiplier,
                    displayMode = stackDisplayMode,
                    isWinner = playerState.player.userId in winnerIds,
                    bigBlind = gameState?.bigBlindAmount ?: 0L,
                    alignHValue = alignments[index].horizontalBias
                )
            }
        }
    }
}

@Composable
fun ChipStackAndText(
    bet: Float,
    textBet: String,
    scaleMultiplier: Float,
    isPerformanceMode: Boolean,
    modifier: Modifier
) {
    val arrangement = if(isPerformanceMode) Arrangement.Center else Arrangement.spacedBy((-3).dp * scaleMultiplier)
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = arrangement,
        modifier = modifier) {
        val chips = remember(bet) { calculateChipStack(ceil(bet).toLong()) }
        if(isPerformanceMode) {
            SimpleChip(chips = chips, chipSize = 20.dp * scaleMultiplier)
        } else {
            PerspectiveChipStack(
                chips = chips,
                chipSize = 30.dp * scaleMultiplier
            )
        }
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

@Composable
fun BottomLayout(
    viewModel: GameViewModel,
    isPerformanceMode: Boolean,
    modifier: Modifier,
    stackDisplayMode: StackDisplayMode,
    gameMode: GameMode?
) {
    val roomInfo by viewModel.roomInfo.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val runItState by viewModel.runItUiState.collectAsState()
    val myUserId by viewModel.myUserId.collectAsState()
    val isActionPanelLocked by viewModel.isActionPanelLocked.collectAsState()
    val allInEquity by viewModel.allInEquity.collectAsState()

    when (val state = runItState) {
        is RunItUiState.Hidden -> {
            val myPlayer by remember {
                derivedStateOf {
                    if(gameState != null) {
                        gameState?.playerStates?.find { it.player.userId == myUserId }?.player
                            ?: roomInfo?.players?.find { it.userId == myUserId }
                    } else {
                        roomInfo?.players?.find { it.userId == myUserId }
                    }
                }
            }
            ActionPanel(
                myPlayer = myPlayer,
                myUserId = myUserId,
                gameState = gameState,
                isActionPanelLocked = isActionPanelLocked,
                allInEquity = allInEquity,
                displayMode = stackDisplayMode,
                modifier = modifier,
                isTournament = gameMode == GameMode.TOURNAMENT,
                onSitAtTableClick = { viewModel.onSitAtTableClick() },
                onReadyClick = { viewModel.onReadyClick(it) },
                onFold = { viewModel.onFold() },
                onCheck = { viewModel.onCheck() },
                onCall = { viewModel.onCall() },
                onBet = { viewModel.onBet(it) }
            )
        }
        is RunItUiState.AwaitingUnderdogChoice -> {
            UnderdogChoiceUi(
                modifier = modifier,
                isPerformanceMode = isPerformanceMode,
                expiresAt = state.expiresAt,
                onChoice = { times -> viewModel.onRunItChoice(times) },
                onHideRunItState = { viewModel.hideRunItState() }
            )
        }
        is RunItUiState.AwaitingFavoriteConfirmation -> {
            val underdogName = gameState?.playerStates?.find { it.player.userId == state.underdogId }?.player?.username
            FavoriteConfirmationUi(
                underdogName = underdogName ?: state.underdogId,
                isPerformanceMode = isPerformanceMode,
                times = state.times,
                expiresAt = state.expiresAt,
                modifier = modifier,
                onConfirm = { accepted -> viewModel.onRunItConfirmation(accepted) },
                onHideRunItState = { viewModel.hideRunItState() }
            )
        }
    }
}

@Composable
fun BoardLayout(
    viewModel: GameViewModel,
    stackDisplayMode: StackDisplayMode,
    specsCount: Int,
    isClassicCardsEnabled: Boolean,
    isFourColorMode: Boolean,
    @SuppressLint("ModifierParameter") multiboardModifier: Modifier,
    singleBoardModifier: Modifier,
    waitingModifier: Modifier
) {
    val gameState by viewModel.gameState.collectAsState()
    val boardRunouts by viewModel.boardRunouts.collectAsState()
    val staticCards by viewModel.staticCommunityCards.collectAsState()
    val runsCount by viewModel.runsCount.collectAsState()
    gameState?.let {
        if(boardRunouts.isNotEmpty()) {
            MultiBoardLayout(staticCards = staticCards, runouts = boardRunouts, runs = runsCount, pot = it.pot,
                displayMode = stackDisplayMode, isClassicCardsEnabled = isClassicCardsEnabled, bigBlind = it.bigBlindAmount,
                modifier = multiboardModifier, isFourColorMode = isFourColorMode)
        } else {
            SingleBoardLayout(
                pot = it.pot,
                bigBlindAmount = it.bigBlindAmount,
                communityCards = it.communityCards,
                displayMode = stackDisplayMode,
                isClassicCardsEnabled =  isClassicCardsEnabled,
                isFourColorMode = isFourColorMode,
                modifier = singleBoardModifier
            )
        }
    } ?: WaitingPlayersLayout(modifier = waitingModifier, specsCount = specsCount)
}

// todo использовать в главном экране
//val configuration = LocalConfiguration.current
//when (configuration.orientation) {
//    Configuration.ORIENTATION_LANDSCAPE -> { /* Логика для ландшафта */ }
//    else -> { /* Логика для портрета */ }
//}