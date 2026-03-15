package com.example.poker.ui.game

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.poker.R
import com.example.poker.shared.dto.GameMode
import com.example.poker.shared.dto.GameStage
import com.example.poker.shared.dto.OutsInfo
import com.example.poker.shared.dto.Player
import com.example.poker.shared.dto.PlayerAction
import com.example.poker.shared.dto.PlayerStatus
import com.example.poker.shared.model.Card
import com.example.poker.ui.theme.MerriWeatherFontFamily
import com.example.poker.util.CardListSaver
import com.example.poker.util.NormalizedPosition
import com.example.poker.util.OutDisplayItem
import com.example.poker.util.calculateChipStack
import com.example.poker.util.calculatePlayerPosition
import com.example.poker.util.calculatePlayerPositionLandscape
import com.example.poker.util.centerAt
import com.example.poker.util.formatBet
import com.example.poker.util.getStickerResource
import com.example.poker.util.getThrowItemResource
import com.example.poker.util.parseBet
import com.example.poker.util.prepareOutDisplayItems
import com.example.poker.util.toBB
import com.example.poker.util.toBBFloat
import com.example.poker.util.toMinutesSeconds
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.random.Random

sealed interface GameScreenLayoutParams {
    val topBarModifier: Modifier
    val topBarAlignment: Alignment
    val boxModifier3: Modifier
    val settingsModifier: Modifier
    val settingsAlignment: Alignment
    val bottomModifier: Modifier
    val bottomAlignment: Alignment
    val bottomDp: Dp
    val boxModifier2: Modifier
    val boxModifier0: Modifier
    val boardModifier: Modifier
    val boardModifier2: Modifier

    object Portrait : GameScreenLayoutParams {
        override val topBarModifier: Modifier = Modifier.height(30.dp).fillMaxWidth()
        override val topBarAlignment: Alignment = Alignment.TopCenter
        override val boxModifier3: Modifier = Modifier.padding(top = 30.dp, bottom = 63.dp).background(Color(0xFF004D40)).fillMaxSize()
        override val settingsModifier: Modifier = Modifier.size(50.dp).clip(CircleShape).padding(end = 5.dp, bottom = 5.dp)
        override val settingsAlignment: Alignment = Alignment.BottomEnd
        override val bottomModifier: Modifier = Modifier
        override val bottomAlignment: Alignment = Alignment.BottomCenter
        override val bottomDp: Dp = 63.dp
        override val boxModifier2: Modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
        override val boxModifier0: Modifier = Modifier.fillMaxSize().background(Color(0xFF003D33))
        override val boardModifier: Modifier = Modifier
        override val boardModifier2: Modifier = Modifier
    }
    object Landscape : GameScreenLayoutParams {
        override val topBarModifier: Modifier = Modifier.height(50.dp).background(Color(0xFF003D33), RoundedCornerShape(4.dp))
        override val topBarAlignment: Alignment = Alignment.BottomStart
        override val boxModifier3: Modifier = Modifier.padding(bottom = 50.dp).background(Color(0xFF004D40)).fillMaxSize()
        override val settingsModifier: Modifier = Modifier.size(50.dp).clip(CircleShape)
        override val settingsAlignment: Alignment = Alignment.BottomStart
        override val bottomModifier: Modifier = Modifier.fillMaxWidth(fraction = 0.4f).background(Color(0xFF003D33), RoundedCornerShape(4.dp))
        override val bottomAlignment: Alignment = Alignment.BottomEnd
        override val bottomDp: Dp = 60.dp
        override val boxModifier2: Modifier = Modifier.fillMaxSize()
        override val boxModifier0: Modifier = Modifier.fillMaxSize().background(Color(0xFF004D40))
        override val boardModifier: Modifier = Modifier.fillMaxWidth(0.4f)
        override val boardModifier2: Modifier = Modifier.padding(top = 50.dp)
    }
}

@Composable
fun GameScreen(viewModel: GameViewModel, onNavigateToLobby: () -> Unit) {
    val winnerId by viewModel.tournamentWinner.collectAsStateWithLifecycle()
    val scaleMultiplier by viewModel.scaleMultiplier.collectAsStateWithLifecycle()
    val stackDisplayMode by viewModel.stackDisplayMode.collectAsStateWithLifecycle()
    val gameMode by viewModel.gameMode.collectAsStateWithLifecycle()
    val specsCount by viewModel.specsCount.collectAsStateWithLifecycle()
    val tournamentInfo by viewModel.tournamentInfo.collectAsStateWithLifecycle()
    val isReconnecting by viewModel.isReconnecting.collectAsStateWithLifecycle()
    val isPerformanceMode by viewModel.isPerformanceMode.collectAsStateWithLifecycle()
    val isClassicCardsEnabled by viewModel.isClassicCardsEnabled.collectAsStateWithLifecycle()
    val isFourColorMode by viewModel.isFourColorMode.collectAsStateWithLifecycle()
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

    // Получаем текущую ориентацию экрана (книжная или альбомная)
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val layoutConfig = remember(isLandscape) {
        if(isLandscape) GameScreenLayoutParams.Landscape else GameScreenLayoutParams.Portrait
    }

    HideSystemBarsEffect(hidden = isLandscape)

    Box(modifier = layoutConfig.boxModifier0) {
        if(!isLandscape) {
            Box(modifier = Modifier.background(Color.Black).fillMaxWidth().height(50.dp).align(Alignment.BottomCenter))
        }

        Box(
            modifier = layoutConfig.boxModifier2,
            contentAlignment = Alignment.Center
        ) {
            TopBar(gameMode, tournamentInfo, specsCount, isReconnecting, layoutConfig.topBarModifier.align(layoutConfig.topBarAlignment), isLandscape)

            Box(layoutConfig.boxModifier3) {
                if(isLandscape) {
                    PokerTableBackground(Modifier.padding(top = 50.dp))
                }
                // Кнопка настроек
                Icon(painter = painterResource(R.drawable.ic_settings), contentDescription = "Settings",
                    tint = Color.Black, modifier = layoutConfig.settingsModifier.align(layoutConfig.settingsAlignment).clickable(onClick = { showSettingsMenu = !showSettingsMenu }))

                val waitingModifier = remember {
                    Modifier
                        .align(Alignment.Center)
                        .background(Color(0xFF00695C), shape = RoundedCornerShape(percent = 50))
                        .border(4.dp, Color(0xFF004D40), shape = RoundedCornerShape(percent = 50))
                        .padding(32.dp, 16.dp)
                }
                Box(modifier = layoutConfig.boardModifier.align(Alignment.Center)) {
                    BoardLayout(
                        viewModel = viewModel,
                        stackDisplayMode = stackDisplayMode,
                        specsCount = specsCount,
                        isClassicCardsEnabled = isClassicCardsEnabled,
                        isFourColorMode = isFourColorMode,
                        multiboardModifier = layoutConfig.boardModifier2.align(Alignment.CenterStart),
                        singleBoardModifier = layoutConfig.boardModifier2.align(Alignment.Center),
                        waitingModifier = waitingModifier,
                        isLandscape = isLandscape
                    )
                }
                PlayersLayout(
                    viewModel = viewModel,
                    scaleMultiplier = scaleMultiplier,
                    stackDisplayMode = stackDisplayMode,
                    isPerformanceMode = isPerformanceMode,
                    isLandscape = isLandscape,
                    isClassicCardsEnabled = isClassicCardsEnabled,
                    isFourColorMode = isFourColorMode,
                    onLastBoardResultChange = { amount -> lastBoardResult = amount }
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
                modifier = layoutConfig.bottomModifier.align(layoutConfig.bottomAlignment),
                stackDisplayMode = stackDisplayMode,
                gameMode = gameMode,
                bottomDp = layoutConfig.bottomDp,
                isLandscape = isLandscape
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

        // Запоминаем какие карты пришли в последний раз
        var previousCards by rememberSaveable(stateSaver = CardListSaver) {
            mutableStateOf(persistentListOf())
        }

        // 2. LaunchedEffect - "мозг" анимации. Запускается, когда меняется список карт
        LaunchedEffect(cards) {
            val prevCards = previousCards
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
            if (cards == prevCards) {
                // Сценарий 1: Полная перерисовка без анимаций (после поворота или входа в игру)
                coroutineScope {
                    isReadyForAnimation = true
                    cards.indices.forEach { i ->
                        val targetX = when (i) {
                            0 -> targets.flopTarget1X
                            1 -> targets.flopTarget2X
                            2 -> targets.flopTarget3X
                            3 -> targets.turnTargetX
                            else -> targets.riverTargetX
                        }
                        launch { cardOffsetsX[i].snapTo(targetX) }
                        launch { cardOffsetsY[i].snapTo(0f) }
                        launch { cardAlphas[i].snapTo(1f) }
                        launch { cardRotations[i].snapTo(if (i > 2) 360f else 0f) }
                    }
                }
            } else {
                // Сценарий 2: Пошаговая анимация (обычный ход игры)
                when(cards.size) {
                    3 -> { // Флоп
                        (0..2).forEach { i ->
                            cardOffsetsX[i].snapTo(targets.startX)
                            cardOffsetsY[i].snapTo(targets.startY)
                            cardAlphas[i].snapTo(0f)
                        }
                        // Теперь, когда все карты на стартовых позициях, разрешаем их показать
                        isReadyForAnimation = true

                        (0..2).forEach { i -> launch { cardAlphas[i].animateTo(1f, tween(100)) } }
                        (0..2).forEach { i -> launch { cardOffsetsX[i].animateTo(targets.flopTarget1X, tween((i + 1) * 300)) } }
                        (0..2).forEach { i -> launch { cardOffsetsY[i].animateTo(0f, tween((i + 1) * 300)) } }
                        delay(950)
                        launch { cardOffsetsX[1].animateTo(targets.flopTarget2X, spring(stiffness = Spring.StiffnessLow)) }
                        launch { cardOffsetsX[2].animateTo(targets.flopTarget3X, spring(stiffness = Spring.StiffnessLow)) }
                    }
                    4 -> { // Терн
                        cardOffsetsX[3].snapTo(targets.turnTargetX)
                        cardOffsetsY[3].snapTo(targets.startY)
                        launch { cardAlphas[3].animateTo(1f, tween(200)) }
                        launch { cardRotations[3].animateTo(360f, tween(600)) }
                        cardOffsetsY[3].animateTo(0f, tween(500))
                    }
                    5 -> { // Ривер
                        cardOffsetsX[4].snapTo(targets.riverTargetX)
                        cardOffsetsY[4].snapTo(targets.startY)
                        launch { cardAlphas[4].animateTo(1f, tween(200)) }
                        launch { cardRotations[4].animateTo(360f, tween(600)) }
                        cardOffsetsY[4].animateTo(0f, tween(500))
                    }
                }
            }
            previousCards = cards
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
    isLandscape: Boolean,
    modifier: Modifier) {
    val (heightPot, fontSize) = remember(isLandscape) { if(isLandscape) 2.dp to 13.sp else 8.dp to TextUnit.Unspecified }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        val text = if(displayMode == StackDisplayMode.CHIPS) pot.toString() else pot.toBB(bigBlindAmount) + " BB"
        Text("Pot: $text", color = Color.White, fontSize = fontSize)
        Spacer(modifier = Modifier.height(heightPot))
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
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val layoutData = remember(maxWidth, runs, isLandscape) {
            val cardWidth = maxWidth / 5
            val drawCardWidth = cardWidth - 5.dp
            val cardHeight = drawCardWidth * 1.5f

            // Вычисляем смещение для текста с банком
            val potTextOffset = if(isLandscape) -(cardHeight / 2) -(cardHeight / 3.5f)
            else if(runs == 2) -(cardHeight / 2) -(cardHeight / 2.5f)
            else -cardHeight -(cardHeight / 5)
            // Заранее вычисляем все целевые точки для анимации
            val runTargets = List(runs) { index ->
                if(isLandscape) {
                    when(index) {
                        0 -> -(cardHeight / 6)
                        1 -> if(runs == 2) cardHeight / 6 else 0.dp
                        else -> cardHeight / 6
                    }
                } else when (runs) {
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
                val potFontSize = if(isLandscape) 13.sp else TextUnit.Unspecified
            }
        }
        val potText = remember(pot, displayMode, bigBlind) {
            if (displayMode == StackDisplayMode.CHIPS) pot.toString() else pot.toBB(bigBlind) + " BB"
        }
        Text("Pot: $potText", color = Color.White, fontSize = layoutData.potFontSize, modifier = Modifier
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
    bottomDp: Dp,
    isLandscape: Boolean,
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
            .height(bottomDp)
            .fillMaxWidth()
    }
    val weightMiddle = remember(isLandscape) {
        if(isLandscape) 1.1f else 1f
    }
    // Используем Box для наложения ползунка поверх панели
    Box(contentAlignment = Alignment.TopCenter, modifier = boxModifier) {
        when {
            myPlayer?.status == PlayerStatus.SPECTATING && (!isTournament || gameState == null)  -> {
                BottomButton(onClick = { onSitAtTableClick() }, text = "Sit at Table", modifier = Modifier
                    .fillMaxWidth()
                    .height(bottomDp - 3.dp))
            }
            gameState == null -> {
                if (myPlayer != null) {
                    val text = if (myPlayer.isReady) "Cancel Ready" else "I'm Ready"
                    BottomButton(onClick = { onReadyClick(!myPlayer.isReady) }, text = text, modifier = Modifier
                        .fillMaxWidth()
                        .height(bottomDp - 3.dp))
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
                    BottomButton(onClick = { onFold() }, enabled = isMyTurn, text = "Fold", modifier = Modifier
                        .weight(1f)
                        .height(bottomDp - 3.dp))

                    // 2. Динамическая кнопка CHECK / CALL
                    val amountToCall = gameState.amountToCall
                    val myCurrentBet = playerState?.currentBet ?: 0L

                    if (amountToCall == 0L || amountToCall == myCurrentBet) {
                        // Если ставить не нужно, показываем CHECK
                        BottomButton(onClick = { onCheck() }, enabled = isMyTurn, text = "Check", modifier = Modifier
                            .weight(weightMiddle)
                            .height(bottomDp - 3.dp))
                    } else {
                        // Если нужно коллировать, показываем CALL с суммой
                        val callValue = minOf(playerState?.player?.stack ?: 0L, amountToCall - myCurrentBet)
                        val callText = remember(displayMode, callValue, gameState.bigBlindAmount) {
                            if (displayMode == StackDisplayMode.BIG_BLINDS) {
                                "Call ${callValue.toBB(gameState.bigBlindAmount)} BB"
                            } else "Call $callValue"
                        }
                        BottomButton(onClick = { onCall() }, enabled = isMyTurn, text = callText, modifier = Modifier
                            .weight(weightMiddle)
                            .height(bottomDp - 3.dp))
                    }

                    // 3. Кнопка BET / RAISE
                    val canRaise = (playerState?.player?.stack ?: 0L) > amountToCall
                    BottomButton(onClick = { showBetSlider = true }, enabled = isMyTurn && canRaise, text = "Bet", modifier = Modifier
                        .weight(1f)
                        .height(bottomDp - 3.dp))
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

@OptIn(FlowPreview::class)
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
    var sliderPosition by remember { mutableFloatStateOf(minBet.toFloat()) }
    var textFieldValue by remember(displayMode, bigBlind) {
        mutableStateOf(formatBet(minBet, displayMode, bigBlind))
    }

    var betAmountInChips by remember(minBet) { mutableLongStateOf(minBet) }
    val isBetValid = betAmountInChips in minBet..maxBet

    val (presetX2, presetX3, presetX4) = remember(amountToCall, minBet) {
        if(amountToCall > 0) Triple(amountToCall * 2, amountToCall * 3, amountToCall * 4)
        else Triple(minBet * 2, minBet * 3, minBet * 4)
    }
    LaunchedEffect(Unit) {
        snapshotFlow { sliderPosition }
            .sample(80)
            .collect { sampledValue ->
                val newChips = sampledValue.toLong()
                // Защита от зацикливания: обновляем только если значение реально изменилось
                if (betAmountInChips != newChips) {
                    betAmountInChips = newChips
                    textFieldValue = formatBet(newChips, displayMode, bigBlind)
                }
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
        CloseButton(iconModifier)
        Column(
            modifier = Modifier.padding(16.dp, 3.dp, 16.dp, 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BetSlider(
                sliderPosition = sliderPosition,
                onSliderChange = { sliderPosition = it },
                minBet = minBet,
                maxBet = maxBet
            )
            Spacer(modifier = Modifier.height(5.dp))

            PresetButtons(
                modifier = Modifier.align(Alignment.Start),
                minBet = minBet, maxBet = maxBet,
                presetX2 = presetX2, presetX3 = presetX3, presetX4 = presetX4,
                onPreset = { amount ->
                    betAmountInChips = amount
                    sliderPosition = amount.toFloat()
                    textFieldValue = formatBet(amount, displayMode, bigBlind)
                }
            )
            Spacer(modifier = Modifier.height(3.dp))

            BetInputRow(
                textFieldValue = textFieldValue,
                displayMode = displayMode,
                isBetValid = isBetValid,
                onTextChange = { newText ->
                    textFieldValue = newText
                    val parsedChips = parseBet(newText, displayMode, bigBlind)
                    if (parsedChips != null && parsedChips in minBet..maxBet) {
                        betAmountInChips = parsedChips
                        sliderPosition = parsedChips.toFloat()
                    }
                },
                onConfirm = { onBetConfirmed(betAmountInChips) }
            )
        }
    }
}

@Composable
private fun CloseButton(modifier: Modifier) {
    Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "Close bet controls",
        tint = Color.White,
        modifier = modifier
    )
}

@Composable
private fun BetSlider(sliderPosition: Float, onSliderChange: (Float) -> Unit, minBet: Long, maxBet: Long) {
    Slider(
        value = sliderPosition,
        onValueChange = onSliderChange,
        valueRange = minBet.toFloat()..maxBet.toFloat()
    )
}

@Composable
private fun PresetButtons(modifier: Modifier, minBet: Long, maxBet: Long, presetX2: Long,
                          presetX3: Long, presetX4: Long, onPreset: (Long) -> Unit) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
    ) {
        PresetButton(amount = minBet, label = "Min", onPreset = { onPreset(it) })
        PresetButton(amount = presetX2, label = "x2", isEnabled = presetX2 in minBet..maxBet, onPreset = { onPreset(it) })
        PresetButton(amount = presetX3, label = "x3", isEnabled = presetX3 in minBet..maxBet, onPreset = { onPreset(it) })
        PresetButton(amount = presetX4, label = "x4", isEnabled = presetX4 in minBet..maxBet, onPreset = { onPreset(it) })
        PresetButton(amount = maxBet, label = "Max", onPreset = { onPreset(it) })
    }
}

@Composable
fun PresetButton(amount: Long, label: String, isEnabled: Boolean = true, onPreset: (Long) -> Unit) {
    Button(
        modifier = Modifier.size(30.dp),
        contentPadding = PaddingValues(0.dp),
        shape = RectangleShape,
        enabled = isEnabled,
        onClick = { onPreset(amount) }
    ) { Text(label) }
}

@Composable
private fun BetInputRow(
    textFieldValue: String,
    displayMode: StackDisplayMode,
    isBetValid: Boolean,
    onTextChange: (String) -> Unit,
    onConfirm: () -> Unit
) {
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
                value = textFieldValue,
                onValueChange = { newText -> onTextChange(newText) },
                isError = !isBetValid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = { onConfirm() }, enabled = isBetValid) {
            Text("Confirm")
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
        modifier = modifier.padding(horizontal = 1.dp),
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = buttonColors,
        border = border
    ) {
        Text(text, fontSize = 18.sp, textAlign = TextAlign.Center)
    }
}

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
    scaleMultiplier: Float,
    isClassicCardsEnabled: Boolean = false,
    isFourColorMode: Boolean = false,
    isMyBottomPlayer: Boolean = false,
    onMyPlayerClicked: () -> Unit,
    onOtherPlayerClicked: () -> Unit
) {
    val boxModifier = remember(scaleMultiplier, isMyBottomPlayer, modifier) {
        val m = modifier
            .width(70.dp * scaleMultiplier)
            .height(80.dp * scaleMultiplier)
            .padding(horizontal = 5.dp * scaleMultiplier)
        if(isMyBottomPlayer) m.clickable(onClick = { onMyPlayerClicked() }) else m.clickable(onClick = { onOtherPlayerClicked() })
    }
    Box(modifier = boxModifier) {
        // Слой 1: Аватарка
        PlayerAvatarBox(
            isGameStarted = isGameStarted,
            hasFolded = playerState.hasFolded,
            isReady = playerState.player.isReady,
            scaleMultiplier = scaleMultiplier,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Слой 2: Карты и Таймер
        val arrangement = if(isPerformanceMode) Arrangement.Center else Arrangement.spacedBy((-15).dp * scaleMultiplier)
        Column(modifier = Modifier.align(Alignment.BottomCenter), verticalArrangement = arrangement) {
            PlayerCardsView(
                isGameStarted = isGameStarted,
                hasFolded = playerState.hasFolded,
                userId = playerState.player.userId,
                myUserId = myUserId,
                cards = playerState.cards,
                isPerformanceMode = isPerformanceMode,
                isClassicCardsEnabled = isClassicCardsEnabled,
                isFourColorMode = isFourColorMode,
                scaleMultiplier = scaleMultiplier
            )
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

        // Слой 3: Плашка действия
        PlayerActionDisplay(
            action = playerState.lastAction,
            scaleMultiplier = scaleMultiplier,
            modifier = Modifier.align(Alignment.Center)
        )

        // Слой 4: Оверлеи
        PlayerStatusOverlays(
            isConnected = playerState.player.isConnected,
            isWinner = isWinner,
            scaleMultiplier = scaleMultiplier,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun PlayerAvatarBox(
    isGameStarted: Boolean,
    hasFolded: Boolean,
    isReady: Boolean,
    scaleMultiplier: Float,
    modifier: Modifier = Modifier
) {
    val iconModifier = remember(scaleMultiplier) {
        modifier
            .padding(top = 5.dp * scaleMultiplier)
            .size(55.dp * scaleMultiplier)
            .clip(CircleShape)
            .background(Color.DarkGray)
            .border(1.dp, Color.White, shape = CircleShape)
    }

    if (isGameStarted) {
        if (hasFolded) {
            Icon(imageVector = Icons.Default.Person, contentDescription = "Avatar", tint = Color.White, modifier = iconModifier)
        }
    } else {
        Icon(imageVector = Icons.Default.Person, contentDescription = "Avatar", tint = Color.White, modifier = iconModifier)
        if (isReady) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Is ready",
                tint = Color.Green,
                modifier = Modifier.size(60.dp * scaleMultiplier) // Поверх иконки
            )
        }
    }
}

@Composable
fun PlayerCardsView(
    isGameStarted: Boolean,
    hasFolded: Boolean,
    userId: String,
    myUserId: String?,
    cards: List<Card>,
    isPerformanceMode: Boolean,
    isClassicCardsEnabled: Boolean,
    isFourColorMode: Boolean,
    scaleMultiplier: Float
) {
    if (!isGameStarted) return

    val (card1, card2) = remember(userId, myUserId, cards) {
        if (userId == myUserId || cards.isNotEmpty()) {
            cards.getOrNull(0) to cards.getOrNull(1)
        } else null to null
    }

    AnimatedVisibility(
        visible = !hasFolded,
        enter = fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = slideOutVertically(targetOffsetY = { it / 6 }) + fadeOut(animationSpec = tween(durationMillis = 500))
    ) {
        val arrangement2 = if(isPerformanceMode) Arrangement.Center else Arrangement.spacedBy((-20).dp * scaleMultiplier)
        Row(horizontalArrangement = arrangement2) {
            if (isPerformanceMode) {
                if (isClassicCardsEnabled) {
                    ClassicPlayerPokerCard(card1, isFourColorMode, scaleMultiplier)
                    ClassicPlayerPokerCard(card2, isFourColorMode, scaleMultiplier)
                } else {
                    SimplePokerCard(card1, scaleMultiplier)
                    SimplePokerCard(card2, scaleMultiplier)
                }
            } else {
                FlippingPokerCard(
                    card = card1,
                    flipDirection = FlipDirection.COUNTER_CLOCKWISE,
                    scaleMultiplier = scaleMultiplier,
                    rotation = -10f,
                    isClassicFace = isClassicCardsEnabled,
                    isFourColorMode = isFourColorMode
                )
                FlippingPokerCard(
                    card = card2,
                    flipDirection = FlipDirection.CLOCKWISE,
                    scaleMultiplier = scaleMultiplier,
                    rotation = 10f,
                    isClassicFace = isClassicCardsEnabled,
                    isFourColorMode = isFourColorMode
                )
            }
        }
    }
}

@Composable
fun PlayerStatusOverlays(
    isConnected: Boolean,
    isWinner: Boolean,
    scaleMultiplier: Float,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Оверлей потери соединения
        if (!isConnected) {
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

        // Анимация победителя
        AnimatedVisibility(
            visible = isWinner,
            enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(animationSpec = tween(1000), initialOffsetY = { -it }),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box {
                RadiantGlowEffectEnhanced(
                    modifier = Modifier
                        .size(40.dp * scaleMultiplier)
                        .align(Alignment.Center),
                    color = Color(0xFFFDFFD8),
                    rayCount = 32,
                    innerRadiusRatio = 0.2f
                )
                Image(
                    painter = painterResource(R.drawable.winner_cup),
                    contentDescription = "Winner",
                    modifier = Modifier
                        .width(20.dp * scaleMultiplier)
                        .height(30.dp * scaleMultiplier)
                        .align(Alignment.Center)
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
    val remainingTimeState = remember { mutableLongStateOf(totalTime) }

    LaunchedEffect(isActivePlayer, turnExpiresAt, isPerformanceMode) {
        val updateDelay = if (isPerformanceMode) 1000L else 50L
        if (isActivePlayer && turnExpiresAt != null) {
            while (isActive) {
                val newRemaining = (turnExpiresAt - System.currentTimeMillis()).coerceAtLeast(0L)
                remainingTimeState.longValue = newRemaining
                if (newRemaining == 0L) break
                delay(updateDelay)
            }
        } else {
            remainingTimeState.longValue = 0L
        }
    }

    val scaleData = remember(scaleMultiplier) {
        object {
            val shape = RoundedTrapezoidShape(cornerRadius = 4.dp * scaleMultiplier)
            val drawWidth = 3.dp * scaleMultiplier
            val verticalPadding = 1.dp * scaleMultiplier
        }
    }

    val greenColor = Color(0xFF00C853)
    val redColor = Color.Red

    Box(
        modifier = modifier
            .drawWithContent {
                // Сначала рисуем сам Box с текстами
                drawContent()

                // Читаем стейт только внутри фазы отрисовки
                val currentRemaining = remainingTimeState.longValue

                if (isActivePlayer && currentRemaining > 0L) {
                    val progress = (currentRemaining.toFloat() / totalTime).coerceIn(0f, 1f)

                    // Вычисляем цвет прямо здесь, перед мазком кисти
                    val progressColor = lerp(redColor, greenColor, progress)

                    val outline = scaleData.shape.createOutline(size, layoutDirection, this)
                    if (outline is Outline.Generic) {
                        val path = outline.path
                        val pathMeasure = PathMeasure()
                        val segmentPath = Path()

                        pathMeasure.setPath(path, false)
                        pathMeasure.getSegment(
                            startDistance = 0f,
                            stopDistance = pathMeasure.length * progress,
                            destination = segmentPath,
                            startWithMoveTo = true
                        )

                        // Отрисовываем таймер
                        drawPath(
                            path = segmentPath,
                            color = progressColor,
                            style = Stroke(width = scaleData.drawWidth.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }
    ) {
        // Этот Column перерисуется (CPU) только если игрок поменяет ник или изменится стек.
        Column(modifier = Modifier
            .clip(scaleData.shape)
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
                    .padding(scaleData.drawWidth, scaleData.verticalPadding + pd),
                style = TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.Both
                    )
                ),
                maxLines = 1
            )
            HorizontalDivider()

            val (stackText, textColor) = if(playerState.player.stack != 0L) {
                if(displayMode == StackDisplayMode.BIG_BLINDS)
                    playerState.player.stack.toBB(bigBlind) + " BB" to Color.White
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
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
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
    bottomDp: Dp,
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
        onHideRunItState()
    }
    val totalDurationMillis = 15000f
    val progress = (remainingTime / totalDurationMillis).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .height(bottomDp + 12.dp)
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
                    BottomButton(onClick = { onChoice(times) }, text = text, modifier = Modifier
                        .weight(1f)
                        .height(bottomDp - 3.dp))
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
    bottomDp: Dp,
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
        onHideRunItState()
    }
    val totalDurationMillis = 15000f
    val progress = (remainingTime / totalDurationMillis).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .height(bottomDp + 32.dp)
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
                BottomButton(onClick = { onConfirm(true) }, text = "Accept", modifier = Modifier
                    .weight(1f)
                    .height(bottomDp - 3.dp))
                BottomButton(onClick = { onConfirm(false) }, text = "Decline", modifier = Modifier
                    .weight(1f)
                    .height(bottomDp - 3.dp))
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
                            scaleMultiplier = 1.2f,
                            onMyPlayerClicked = {},
                            onOtherPlayerClicked = {}
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
    modifier: Modifier,
    isLandScape: Boolean
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
    if(isLandScape) {
        Column(modifier.padding(start = 10.dp, end = 5.dp)) {
            if(gameMode == GameMode.CASH) {
                if(isReconnecting) {
                    ReconnectingText(Modifier.padding(horizontal = 10.dp), 18.sp)
                } else Text(topBarText, textAlign = TextAlign.Center, color = Color.White)
            } else {
                if(isReconnecting) {
                    ReconnectingText(Modifier.padding(horizontal = 10.dp), 18.sp)
                } else {
                    val textSize = when(topBarText.length) {
                        in 0..20 -> 14.sp
                        in 21..24 -> 12.sp
                        else -> 11.sp
                    }
                    leftText?.let { Text(it, textAlign = TextAlign.Start, modifier = Modifier.padding(horizontal = 3.dp),
                        color = Color.White, fontSize = textSize) }
                    Text(topBarText, textAlign = TextAlign.Center, color = Color.White, fontSize = textSize, modifier = Modifier.padding(start = 10.dp, end = 3.dp))
                }
            }
            if (animatedCount != 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .align(Alignment.End)
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
    } else {
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

private enum class AnimationStage { FLYING, SPLAT }

@Composable
fun PlayersLayout(
    viewModel: GameViewModel,
    scaleMultiplier: Float,
    stackDisplayMode: StackDisplayMode,
    isPerformanceMode: Boolean,
    isLandscape: Boolean,
    isClassicCardsEnabled: Boolean,
    isFourColorMode: Boolean,
    onLastBoardResultChange: (Long) -> Unit
) {
    // Выжимки из ViewModel
    val myUserId by viewModel.myUserId.collectAsStateWithLifecycle()
    val reorderedPlayers by viewModel.reorderedPlayers.collectAsStateWithLifecycle()
    val activePlayerId by viewModel.activePlayerId.collectAsStateWithLifecycle()
    val winnerIds by viewModel.winnerIds.collectAsStateWithLifecycle()
    val bigBlindAmount by viewModel.bigBlindAmount.collectAsStateWithLifecycle()
    val turnExpiresAt by viewModel.turnExpiresAt.collectAsStateWithLifecycle()
    val isGameStarted by viewModel.isGameStarted.collectAsStateWithLifecycle()

    val boardResult by viewModel.boardResult.collectAsStateWithLifecycle()
    val allInEquity by viewModel.allInEquity.collectAsStateWithLifecycle()
    val showStickerActions by viewModel.showStickerActions.collectAsStateWithLifecycle()
    val throwItemActions by viewModel.throwItemActions.collectAsStateWithLifecycle()

    var isShowStickersMenu by remember { mutableStateOf(false) }
    var showThrowStickersMenuIndex by remember { mutableIntStateOf(-1) }

    val positions = remember(reorderedPlayers.size, isLandscape) {
        if(isLandscape) calculatePlayerPositionLandscape(reorderedPlayers.size)
            else calculatePlayerPosition(reorderedPlayers.size)
    }
    BoxWithConstraints(Modifier.fillMaxSize().padding(3.dp)) {
        val parentWidthPx = constraints.maxWidth.toFloat()
        val parentHeightPx = constraints.maxHeight.toFloat()

        reorderedPlayers.forEachIndexed { index, playerState ->
            val userId = playerState.player.userId
            key(userId) {
                val normalizedPos = positions.getOrNull(index) ?: NormalizedPosition(0.5f, 0.5f, true)
                val winAmount = remember(boardResult, userId) {
                    boardResult?.find { it.first == userId }?.second ?: 0L
                }
                val isWinner = userId in winnerIds
                val isActive = userId == activePlayerId
                val isMyBottomPlayer = index == 0
                val stickerAction = showStickerActions[userId]

                PlayerNode(
                    playerState = playerState,
                    normalizedX = normalizedPos.x,
                    normalizedY = normalizedPos.y,
                    parentHeightPx = parentHeightPx,
                    parentWidthPx = parentWidthPx,
                    isLeftEquity = normalizedPos.isLeftEquity,
                    isActivePlayer = isActive,
                    isWinner = isWinner,
                    winAmount = winAmount,
                    stickerAction = stickerAction,
                    myUserId = myUserId,
                    bigBlindAmount = bigBlindAmount,
                    turnExpiresAt = turnExpiresAt,
                    allInEquity = allInEquity,
                    scaleMultiplier = scaleMultiplier,
                    stackDisplayMode = stackDisplayMode,
                    isPerformanceMode = isPerformanceMode,
                    isClassicCardsEnabled = isClassicCardsEnabled,
                    isFourColorMode = isFourColorMode,
                    isMyBottomPlayer = isMyBottomPlayer,
                    isLandscape = isLandscape,
                    isGameStarted = isGameStarted,
                    isShowThrowStickerMenu = showThrowStickersMenuIndex == index,
                    onMyPlayerClicked = { isShowStickersMenu = true },
                    onOtherPlayerClicked = { showThrowStickersMenuIndex = index },
                    onLastBoardResultChange = onLastBoardResultChange,
                    onHideThrowStickerMenu = { showThrowStickersMenuIndex = -1 },
                    onStickerThrowSelected = { stickerId, userId -> viewModel.onStickerThrowSelected(stickerId, userId) }
                )
            }
        }
        StickerSelectionMenu(
            onStickerSelected = { stickerId ->
                isShowStickersMenu = false
                viewModel.onStickerSelected(stickerId)
            },
            onDismiss = { isShowStickersMenu = false },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .graphicsLayer {
                    translationY = if (isShowStickersMenu) 0f else parentHeightPx // убираем меню под экран
                    alpha = if (isShowStickersMenu) 1f else 0f
                }
        )
        val density = LocalDensity.current
        val throwItemSizePx = remember(scaleMultiplier) {
            with(density) { (50.dp * scaleMultiplier).toPx() }
        }

        // Собираем точные пиксельные координаты центров для всех видимых игроков
        val playerCenters = remember(positions, parentWidthPx, parentHeightPx, scaleMultiplier, isLandscape) {
            val playerWidthPx = with(density) { (70.dp * scaleMultiplier).toPx() }
            val playerHeightPx = with(density) { (80.dp * scaleMultiplier).toPx() }
            val yOffset = if(isLandscape) with(density) { 50.dp.toPx() } else 0f

            reorderedPlayers.mapIndexed { index, playerState ->
                val pos = positions.getOrNull(index) ?: NormalizedPosition(0.5f, 0.5f, true)

                // Та самая "Умная формула"
                val topLeftX = pos.x * (parentWidthPx - playerWidthPx)
                val topLeftY = pos.y * (parentHeightPx - playerHeightPx)

                val centerX = topLeftX + (playerWidthPx / 2)
                val centerY = topLeftY + (playerHeightPx / 2)
                val finalCenterY = if (index == 0) centerY + yOffset else centerY

                // Сохраняем userId как ключ, а Pair(X, Y) как значение
                playerState.player.userId to Pair(centerX, finalCenterY)
            }.toMap()
        }

        throwItemActions.forEach { (throwerId, actionData) ->
            val (item, targetId) = actionData
            val startCenter = playerCenters[throwerId]
            val endCenter = playerCenters[targetId]

            if (startCenter != null && endCenter != null) {
                key(item.instanceId) {
                    ThrownItemAnimation(
                        itemStickerId = item.stickerId,
                        startX = startCenter.first,
                        startY = startCenter.second,
                        endX = endCenter.first,
                        endY = endCenter.second,
                        throwItemSizePx = throwItemSizePx,
                        scaleMultiplier = scaleMultiplier
                    )
                }
            }
        }
    }
}

@Composable
fun ThrownItemAnimation(
    itemStickerId: String,
    startX: Float,
    startY: Float,
    endX: Float,
    endY: Float,
    throwItemSizePx: Float,
    scaleMultiplier: Float
) {
    val animatedX = remember { Animatable(startX) }
    val animatedY = remember { Animatable(startY) }
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(1f) }
    var stage by remember { mutableStateOf(AnimationStage.FLYING) }

    LaunchedEffect(Unit) {
        // Этап 1: Полет
        launch { animatedX.animateTo(endX, tween(durationMillis = 1200)) }
        launch {
            animatedY.animateTo(endY, tween(durationMillis = 1200))
            // Этап 2: "Взрыв"
            stage = AnimationStage.SPLAT // Меняем картинку
            scale.animateTo(targetValue = 1.2f, animationSpec = tween(durationMillis = 600))
            alpha.animateTo(targetValue = 0f, animationSpec = tween(durationMillis = 400, delayMillis = 800))
        }
    }

    Box(
        modifier = Modifier
            .graphicsLayer {
                translationX = animatedX.value - (throwItemSizePx / 2)
                translationY = animatedY.value - (throwItemSizePx / 2)
                if (stage == AnimationStage.SPLAT) {
                    scaleX = scale.value
                    scaleY = scale.value
                    this.alpha = alpha.value
                }
            }
    ) {
        val painter = when (stage) {
            AnimationStage.FLYING -> painterResource(id = getThrowItemResource(itemStickerId, false))
            AnimationStage.SPLAT -> painterResource(id = getThrowItemResource(itemStickerId, true))
        }
        Image(
            painter = painter,
            contentDescription = "Thrown item",
            modifier = Modifier.size(50.dp * scaleMultiplier)
        )
    }
}

@Composable
fun PlayerNode(
    playerState: PlayerState,
    normalizedX: Float,
    normalizedY: Float,
    parentWidthPx: Float,
    parentHeightPx: Float,
    isLeftEquity: Boolean,
    isActivePlayer: Boolean,
    isWinner: Boolean,
    winAmount: Long,
    stickerAction: StickerDisplay?,
    isShowThrowStickerMenu: Boolean,
    myUserId: String?,
    bigBlindAmount: Long,
    turnExpiresAt: Long?,
    allInEquity: AllInEquity?,
    scaleMultiplier: Float,
    stackDisplayMode: StackDisplayMode,
    isPerformanceMode: Boolean,
    isClassicCardsEnabled: Boolean,
    isFourColorMode: Boolean,
    isMyBottomPlayer: Boolean,
    isLandscape: Boolean,
    isGameStarted: Boolean,
    onStickerThrowSelected: (String, String) -> Unit,
    onHideThrowStickerMenu: () -> Unit,
    onMyPlayerClicked: () -> Unit,
    onOtherPlayerClicked: () -> Unit,
    onLastBoardResultChange: (Long) -> Unit
) {
    val density = LocalDensity.current

    // 1. Узнаем точный физический размер игрока в пикселях
    val playerWidthPx = with(density) { (70.dp * scaleMultiplier).toPx() }
    val playerHeightPx = with(density) { (80.dp * scaleMultiplier).toPx() }

    // 2. Вычисляем координаты верхнего левого угла
    val playerTopLeftX = normalizedX * (parentWidthPx - playerWidthPx)
    val playerTopLeftY = normalizedY * (parentHeightPx - playerHeightPx)

    // 3. Вычисляем центр для нашего модификатора centerAt
    val playerCenterX = playerTopLeftX + (playerWidthPx / 2)

    val (playerCenterY, yCorrection) = remember(isMyBottomPlayer, playerTopLeftY, playerHeightPx, isLandscape) {
        val baseY = playerTopLeftY + (playerHeightPx / 2)
        val yCorrect = if (isMyBottomPlayer && isLandscape) with(density) { 50.dp.toPx() } else 0f
        (baseY + yCorrect) to yCorrect
    }

    // Центр стола в пикселях
    val tableCenterX = parentWidthPx / 2f
    val tableCenterY = parentHeightPx / 2f

    // Вектор от центра стола до центра игрока (в пикселях)
    val vectorXPx = playerCenterX - tableCenterX
    val vectorYPx = playerCenterY - tableCenterY - yCorrection

    if (playerState.currentBet > 0) {
        val (bet, textBet) = remember(playerState.currentBet, stackDisplayMode, bigBlindAmount) {
            if (stackDisplayMode == StackDisplayMode.CHIPS) {
                playerState.currentBet.toFloat() to playerState.currentBet.toString()
            } else {
                playerState.currentBet.toBBFloat(bigBlindAmount) to playerState.currentBet.toBB(bigBlindAmount) + " BB"
            }
        }
        val (startX, startY, endX, endY) = remember(vectorXPx, vectorYPx, parentWidthPx, parentHeightPx, scaleMultiplier) {
            val baseDist1 = if (parentWidthPx > parentHeightPx) 0.45f + (1 - scaleMultiplier) / 2 else 0.72f
            val maxDist1 = if (parentWidthPx > parentHeightPx) 0.55f + (1 - scaleMultiplier) / 2 else 0.9f
            val minDist1 = if (parentWidthPx > parentHeightPx) 0.4f + (1 - scaleMultiplier) / 2 else 0.55f

            val endCorrection = if (scaleMultiplier <= 1) {
                maxDist1 + (baseDist1 - maxDist1) * ((scaleMultiplier - 0.5f) / 0.5f)
            } else {
                baseDist1 + (minDist1 - baseDist1) * ((scaleMultiplier - 1.0f) / 0.5f)
            }

            // Старт - чуть ближе к центру от самого игрока (0.85 от вектора)
            val sX = tableCenterX + vectorXPx * 0.85f
            val sY = tableCenterY + vectorYPx * 0.85f
            // Конец - с учетом коррекции
            val eX = tableCenterX + vectorXPx * endCorrection
            val eY = tableCenterY + vectorYPx * endCorrection

            // Возвращаем абсолютные координаты в пикселях
            listOf(sX, sY + yCorrection, eX, eY + yCorrection)
        }
        val animatedAlpha = remember { Animatable(0f) }
        val animatedX = remember { Animatable(startX) }
        val animatedY = remember { Animatable(startY) }

        var previousBet by remember { mutableLongStateOf(0L) }

        LaunchedEffect(playerState.currentBet, endX, endY) {
            val isNewBet = playerState.currentBet != previousBet
            previousBet = playerState.currentBet // сразу обновляем для будущих проверок
            if (isNewBet) {
                // Сценарий 1: Игрок изменил ставку (сделал бет/рейз)
                if (playerState.currentBet > 0) {
                    animatedX.snapTo(startX)
                    animatedY.snapTo(startY)
                    launch { animatedAlpha.animateTo(1f, tween(200)) }
                    launch { animatedX.animateTo(endX, tween(400)) }
                    launch { animatedY.animateTo(endY, tween(400)) }
                } else {
                    animatedAlpha.snapTo(0f) // Если ставка стала 0 (конец раздачи)
                }
            } else {
                // Сценарий 2: Ставка осталась прежней, но изменились endX/endY
                if (playerState.currentBet > 0) {
                    animatedX.snapTo(endX)
                    animatedY.snapTo(endY)
                    animatedAlpha.snapTo(1f)
                }
            }
        }
        ChipStackAndText(
            bet = bet,
            textBet = textBet,
            scaleMultiplier = scaleMultiplier,
            isPerformanceMode = isPerformanceMode,
            modifier = Modifier
                .centerAt(animatedX.value, animatedY.value)
                .graphicsLayer { alpha = animatedAlpha.value }
        )
    }
    if(isWinner) {
        if(winAmount > 0) {
            onLastBoardResultChange(winAmount)
            val (bet, textBet) = remember(winAmount, stackDisplayMode, bigBlindAmount) {
                if(stackDisplayMode == StackDisplayMode.CHIPS) {
                    winAmount.toFloat() to winAmount.toString()
                }
                else {
                    winAmount.toBBFloat(bigBlindAmount) to winAmount.toBB(bigBlindAmount) + " BB"
                }
            }
            val (startX, startY, endX, endY) = remember(vectorXPx, vectorYPx, parentWidthPx, parentHeightPx, scaleMultiplier) {
                val baseDist2 = if (parentWidthPx > parentHeightPx) 0.5f + (1 - scaleMultiplier) / 2 else 0.75f
                val maxDist2 = if (parentWidthPx > parentHeightPx) 0.6f + (1 - scaleMultiplier) / 2 else 0.9f
                val minDist2 = if (parentWidthPx > parentHeightPx) 0.45f + (1 - scaleMultiplier) / 2 else 0.6f

                val endCorrection = if (scaleMultiplier <= 1) {
                    maxDist2 + (baseDist2 - maxDist2) * ((scaleMultiplier - 0.5f) / 0.5f)
                } else {
                    baseDist2 + (minDist2 - baseDist2) * ((scaleMultiplier - 1.0f) / 0.5f)
                }

                // Конец - с учетом коррекции
                val eX = tableCenterX + vectorXPx * endCorrection
                val eY = tableCenterY + vectorYPx * endCorrection

                // Возвращаем абсолютные координаты в пикселях
                listOf(tableCenterX, tableCenterY + yCorrection, eX, eY + yCorrection)
            }

            val animatedAlpha = remember { Animatable(0f) }
            val animatedX = remember { Animatable(startX) }
            val animatedY = remember { Animatable(startY) }

            LaunchedEffect(key1 = winAmount) {
                animatedX.snapTo(startX)
                animatedY.snapTo(startY)
                launch {
                    animatedAlpha.animateTo(1f, animationSpec = tween(200))
                    animatedAlpha.animateTo(0f, animationSpec = tween(500, delayMillis = 1500))
                }
                launch { animatedX.animateTo(endX, animationSpec = tween(2000)) }
                launch { animatedY.animateTo(endY, animationSpec = tween(2000)) }
            }
            ChipStackAndText(
                bet = bet,
                textBet = textBet,
                scaleMultiplier = scaleMultiplier,
                isPerformanceMode = isPerformanceMode,
                modifier = Modifier
                    .centerAt(animatedX.value, animatedY.value)
                    .graphicsLayer { alpha = animatedAlpha.value }
            )
        }
    }
    val userId = playerState.player.userId
    val equity = allInEquity?.equities?.get(userId)
    val out = allInEquity?.outs?.get(userId)

    if (equity != null || out != null) {
        val tailDirection = if (isLeftEquity) TailDirection.RIGHT else TailDirection.LEFT

        // Вычисляем расстояние от центра игрока до центра пузыря
        // Разные пузыри имеют разную ширину, поэтому сдвиг разный
        val distanceDp = if (out != null) 80.dp else 65.dp
        val distancePx = with(density) { (distanceDp * scaleMultiplier).toPx() }

        // Если isLeftEquity == true, пузырь слева (вычитаем X). Иначе справа (прибавляем X)
        val bubbleCenterX = if (isLeftEquity) playerCenterX - distancePx else playerCenterX + distancePx

        if(out == null && equity != null) {
            EquityBubble(
                equity = equity,
                tailDirection = tailDirection,
                scaleMultiplier = scaleMultiplier,
                modifier = Modifier.centerAt(bubbleCenterX, playerCenterY)
            )
        } else if(out != null) {
            OutsBubble(
                equity = equity,
                outsInfo = out,
                scaleMultiplier = scaleMultiplier,
                modifier = Modifier.centerAt(bubbleCenterX, playerCenterY)
            )
        }
    }
    PlayerDisplay(
        playerState = playerState,
        isActivePlayer = isActivePlayer,
        isPerformanceMode = isPerformanceMode,
        turnExpiresAt = turnExpiresAt,
        myUserId = myUserId,
        modifier = Modifier.centerAt(playerCenterX, playerCenterY),
        isGameStarted = isGameStarted,
        isWinner = isWinner,
        displayMode = stackDisplayMode,
        bigBlind = bigBlindAmount,
        scaleMultiplier = scaleMultiplier,
        isClassicCardsEnabled = isClassicCardsEnabled,
        isFourColorMode = isFourColorMode,
        isMyBottomPlayer = isMyBottomPlayer,
        onMyPlayerClicked = onMyPlayerClicked,
        onOtherPlayerClicked = onOtherPlayerClicked
    )

    AnimatedVisibility(
        visible = stickerAction != null,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(200)),
        modifier = Modifier.centerAt(playerCenterX, playerCenterY)
    ) {
        // Compose уничтожает старый блок и создает новый, запуская анимацию заново.
        key(stickerAction?.instanceId) {
            stickerAction?.let { display ->
                // Начальное значение теперь всегда 0.5f для нового стикера
                val scale = remember { Animatable(0.5f) }

                // Запускается один раз при появлении этого блока с ключом
                LaunchedEffect(Unit) {
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
                    )
                }
                Image(
                    painter = painterResource(id = getStickerResource(display.stickerId)),
                    contentDescription = "Стикер",
                    modifier = Modifier
                        .size(85.dp * scaleMultiplier)
                        .scale(scale.value)
                )
            }
        }
    }
    if(isShowThrowStickerMenu) {
        ThrowItemSelectionMenu(
            onStickerSelected = { stickerId ->
                onStickerThrowSelected(stickerId, playerState.player.userId)
                onHideThrowStickerMenu()
            },
            onDismiss = { onHideThrowStickerMenu() },
            modifier = Modifier.centerAt(playerCenterX, playerCenterY)
        )
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
    gameMode: GameMode?,
    bottomDp: Dp,
    isLandscape: Boolean
) {
    val roomInfo by viewModel.roomInfo.collectAsStateWithLifecycle()
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val runItState by viewModel.runItUiState.collectAsStateWithLifecycle()
    val myUserId by viewModel.myUserId.collectAsStateWithLifecycle()
    val isActionPanelLocked by viewModel.isActionPanelLocked.collectAsStateWithLifecycle()
    val allInEquity by viewModel.allInEquity.collectAsStateWithLifecycle()

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
                bottomDp = bottomDp,
                isLandscape = isLandscape,
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
                bottomDp = bottomDp,
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
                bottomDp = bottomDp,
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
    isLandscape: Boolean,
    @SuppressLint("ModifierParameter") multiboardModifier: Modifier,
    singleBoardModifier: Modifier,
    waitingModifier: Modifier
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val boardRunouts by viewModel.boardRunouts.collectAsStateWithLifecycle()
    val staticCards by viewModel.staticCommunityCards.collectAsStateWithLifecycle()
    val runsCount by viewModel.runsCount.collectAsStateWithLifecycle()
    gameState?.let {
        if(boardRunouts.isNotEmpty()) {
            MultiBoardLayout(staticCards = staticCards, runouts = boardRunouts, runs = runsCount, pot = it.pot,
                displayMode = stackDisplayMode, isClassicCardsEnabled = isClassicCardsEnabled, bigBlind = it.bigBlindAmount,
                modifier = multiboardModifier, isFourColorMode = isFourColorMode, isLandscape = isLandscape)
        } else {
            SingleBoardLayout(
                pot = it.pot,
                bigBlindAmount = it.bigBlindAmount,
                communityCards = it.communityCards,
                displayMode = stackDisplayMode,
                isClassicCardsEnabled = isClassicCardsEnabled,
                isFourColorMode = isFourColorMode,
                isLandscape = isLandscape,
                modifier = singleBoardModifier
            )
        }
    } ?: WaitingPlayersLayout(modifier = waitingModifier, specsCount = specsCount)
}

@Composable
fun HideSystemBarsEffect(hidden: Boolean) {
    val view = LocalView.current

    DisposableEffect(hidden) {
        val window = (view.context as Activity).window

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowCompat.getInsetsController(window, view)

        if (hidden) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }

        onDispose { }
    }
}