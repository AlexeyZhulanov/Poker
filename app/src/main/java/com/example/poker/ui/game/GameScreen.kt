package com.example.poker.ui.game

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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.poker.domain.model.Chip
import com.example.poker.domain.model.Suit
import com.example.poker.domain.model.standardChipSet
import com.example.poker.ui.theme.MerriWeatherFontFamily
import com.example.poker.util.toBB
import com.example.poker.util.toBBFloat
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.random.Random

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val roomInfo by viewModel.roomInfo.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val myUserId by viewModel.myUserId.collectAsState()
    val playersOnTable by viewModel.playersOnTable.collectAsState()
    val runItState by viewModel.runItUiState.collectAsState()
    val isActionPanelLocked by viewModel.isActionPanelLocked.collectAsState()
    val allInEquity by viewModel.allInEquity.collectAsState()
    val staticCards by viewModel.staticCommunityCards.collectAsState()
    val boardRunouts by viewModel.boardRunouts.collectAsState()
    val runsCount by viewModel.runsCount.collectAsState()
    val visibleActionIds by viewModel.visibleActionIds.collectAsState()
    val stackDisplayMode by viewModel.stackDisplayMode.collectAsState()
    val boardResult by viewModel.boardResult.collectAsState()
    var showSettingsMenu by remember { mutableStateOf(false) }
    val winnerIds = remember(boardResult) {
        boardResult?.map { it.first }?.toSet() ?: emptySet()
    }

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

    val myPlayerState = gameState?.playerStates?.find { it.player.userId == myUserId }
    val activePlayerId = gameState?.playerStates?.getOrNull(gameState!!.activePlayerPosition)?.player?.userId

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF003D33))) {
        Box(modifier = Modifier
            .background(Color.Black)
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(60.dp))
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            val specsCount = roomInfo?.players?.filter { it.status == PlayerStatus.SPECTATING }?.size ?: 0
            val animatedCount by animateIntAsState(
                targetValue = specsCount,
                animationSpec = tween(durationMillis = 300)
            )
            val modeText = if(roomInfo?.gameMode == GameMode.TOURNAMENT) "Tournament" else "Cash"
            val (sb, bb) = roomInfo?.blindStructure?.let { it.first().smallBlind to it.first().bigBlind } ?: (10L to 20L)
            val topBarText = "$modeText $sb / $bb"
            Box(Modifier
                .align(Alignment.TopCenter)
                .height(30.dp)
                .fillMaxWidth()) {
                Text(topBarText, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center), color = Color.White)
                if (animatedCount != 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(3.dp, 0.dp)
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

            // todo нормально паддинги вписать как константы
            Box(Modifier
                .padding(0.dp, 30.dp, 0.dp, 63.dp)
                .background(Color(0xFF004D40))
                .fillMaxSize()) {

                // Кнопка настроек
                Icon(painter = painterResource(R.drawable.ic_settings), contentDescription = "Settings", tint = Color.Black,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(50.dp)
                        .clip(CircleShape)
                        .padding(0.dp, 0.dp, 5.dp, 5.dp)
                        .clickable(onClick = { showSettingsMenu = !showSettingsMenu })
                )

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
                    val isActivePlayer = playerState.player.userId == activePlayerId
                    val verticalOffset = if(isMyPlayer) (-25).dp else 15.dp
                    val bias = alignments[index].horizontalBias
                    val biasOffset = when {
                        bias > 0f -> (-10).dp
                        bias < 0f -> 10.dp
                        else -> 0.dp
                    }
                    val totalOffset = offset + biasOffset
                    val totalOffset2 = offset + biasOffset * -1.6f

                    allInEquity?.let { (equities, outs, _) ->
                        val equity = equities[playerState.player.userId]
                        val out = outs[playerState.player.userId]
                        if(out == null && equity != null) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier
                                .align(alignments[index])
                                .offset(
                                    totalOffset * scaleMultiplier * 0.8f,
                                    verticalOffset * scaleMultiplier
                                )) {
                                EquityBubble(equity, tailDirection, scaleMultiplier)
                            }
                        } else if(out != null) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier
                                .align(alignments[index])
                                .offset(totalOffset2 * scaleMultiplier, 0.dp)) {
                                OutsBubble(equity, out, scaleMultiplier)
                            }
                        }
                    }

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
                            modifier = Modifier.align(animatedAlignment).alpha(animatedAlpha.value)) {
                            PerspectiveChipStack(
                                chips = calculateChipStack(ceil(bet).toLong()),
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
                                modifier = Modifier.align(animatedAlignment).alpha(animatedAlpha.value)) {
                                PerspectiveChipStack(
                                    chips = calculateChipStack(ceil(bet).toLong()),
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

                    PlayerDisplay(
                        modifier = Modifier
                            .align(alignments[index])
                            .padding(3.dp),
                        playerState = playerState,
                        isMyPlayer = isMyPlayer,
                        isActivePlayer = isActivePlayer,
                        turnExpiresAt = gameState?.turnExpiresAt,
                        isGameStarted = gameState != null,
                        visibleActionIds = visibleActionIds,
                        scaleMultiplier = scaleMultiplier,
                        displayMode = stackDisplayMode,
                        isWinner = playerState.player.userId in winnerIds,
                        bigBlind = gameState?.bigBlindAmount ?: 0L
                    )
                }
                gameState?.let {
                    if (boardRunouts.isNotEmpty()) {
                        MultiBoardLayout(staticCards = staticCards, runouts = boardRunouts, runs = runsCount, pot = it.pot,
                            displayMode = stackDisplayMode, bigBlind = it.bigBlindAmount,
                            modifier = Modifier.align(Alignment.CenterStart))
                    } else SingleBoardLayout(it, stackDisplayMode, Modifier.align(Alignment.Center))
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
                // Выдвижное меню настроек
                AnimatedVisibility(
                    visible = showSettingsMenu,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 60.dp, end = 16.dp),
                    enter = slideInHorizontally { it },
                    exit = slideOutHorizontally { it }
                ) {
                    Card(elevation = CardDefaults.cardElevation(8.dp)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) { // todo scaleMultiplier управление сюда добавить
                            // todo запоминать в preferences и scale и режим, режим можно для турнира и cash свой
                            Text("Show stack in BB")
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = stackDisplayMode == StackDisplayMode.BIG_BLINDS,
                                onCheckedChange = { viewModel.toggleStackDisplayMode() }
                            )
                        }
                    }
                }
            }

            val myPlayer = roomInfo?.players?.find { it.userId == myUserId }
            val isMyTurn = gameState?.let { activePlayerId == myUserId && !isActionPanelLocked && allInEquity == null && it.stage != GameStage.SHOWDOWN } ?: false

            when (val state = runItState) {
                is RunItUiState.Hidden -> {
                    ActionPanel(
                        viewModel = viewModel,
                        isMyTurn = isMyTurn,
                        myPlayer = myPlayer,
                        playerState = myPlayerState,
                        gameState = gameState,
                        displayMode = stackDisplayMode,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
                is RunItUiState.AwaitingUnderdogChoice -> {
                    UnderdogChoiceUi(
                        viewModel = viewModel,
                        modifier = Modifier.align(Alignment.BottomCenter),
                        expiresAt = state.expiresAt,
                        onChoice = { times -> viewModel.onRunItChoice(times) }
                    )
                }
                is RunItUiState.AwaitingFavoriteConfirmation -> {
                    val underdogName = playersOnTable.find { it.player.userId == state.underdogId }?.player?.username
                    FavoriteConfirmationUi(
                        viewModel = viewModel,
                        underdogName = underdogName ?: state.underdogId,
                        times = state.times,
                        expiresAt = state.expiresAt,
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onConfirm = { accepted -> viewModel.onRunItConfirmation(accepted) }
                    )
                }
            }
        }
    }
}

private fun calculatePlayerPosition(playersCount: Int): Pair<List<BiasAlignment>, List<Boolean>> {
    // 0.68 - центр
    val list = mutableListOf(BiasAlignment(0f, 1f)) // first
    val equityList = mutableListOf(true) // right = false, left = true
    when(playersCount) {
        1 -> return list to equityList
        2 -> { list.add(BiasAlignment(0f, -1f)); equityList.add(false) }
        3 -> {
            list.add(BiasAlignment(-1f, -0.9f))
            equityList.add(false)
            list.add(BiasAlignment(1f, -0.9f))
            equityList.add(true)
        }
        4 -> {
            list.add(BiasAlignment(-1f, -0.5f))
            equityList.add(false)
            list.add(BiasAlignment(0f, -1f))
            equityList.add(false)
            list.add(BiasAlignment(1f, -0.5f))
            equityList.add(true)
        }
        5 -> {
            list.add(BiasAlignment(-1f, 0.5f))
            equityList.add(false)
            list.add(BiasAlignment(-1f, -0.9f))
            equityList.add(false)
            list.add(BiasAlignment(1f, -0.9f))
            equityList.add(true)
            list.add(BiasAlignment(1f, 0.5f))
            equityList.add(true)
        }
        6 -> {
            list.add(BiasAlignment(-1f, 0.5f))
            equityList.add(false)
            list.add(BiasAlignment(-1f, -0.5f))
            equityList.add(false)
            list.add(BiasAlignment(0f, -1f))
            equityList.add(false)
            list.add(BiasAlignment(1f, -0.5f))
            equityList.add(true)
            list.add(BiasAlignment(1f, 0.5f))
            equityList.add(true)
        }
        7 -> {
            list.add(BiasAlignment(-1f, 0.5f))
            equityList.add(false)
            list.add(BiasAlignment(-1f, -0.5f))
            equityList.add(false)
            list.add(BiasAlignment(-0.5f, -1f))
            equityList.add(false)
            list.add(BiasAlignment(0.5f, -1f))
            equityList.add(true)
            list.add(BiasAlignment(1f, -0.5f))
            equityList.add(true)
            list.add(BiasAlignment(1f, 0.5f))
            equityList.add(true)
        }
        8 -> {
            list.add(BiasAlignment(-1f, 0.5f))
            equityList.add(false)
            list.add(BiasAlignment(-1f, -0.4f))
            equityList.add(false)
            list.add(BiasAlignment(-1f, -0.85f))
            equityList.add(false)
            list.add(BiasAlignment(0f, -1f))
            equityList.add(false)
            list.add(BiasAlignment(1f, -0.85f))
            equityList.add(true)
            list.add(BiasAlignment(1f, -0.4f))
            equityList.add(true)
            list.add(BiasAlignment(1f, 0.5f))
            equityList.add(true)
        }
        9 -> {
            list.add(BiasAlignment(-1f, 0.85f))
            equityList.add(false)
            list.add(BiasAlignment(-1f, 0.4f))
            equityList.add(false)
            list.add(BiasAlignment(-1f, -0.5f))
            equityList.add(false)
            list.add(BiasAlignment(-0.5f, -1f))
            equityList.add(false)
            list.add(BiasAlignment(0.5f, -1f))
            equityList.add(true)
            list.add(BiasAlignment(1f, -0.5f))
            equityList.add(true)
            list.add(BiasAlignment(1f, 0.4f))
            equityList.add(true)
            list.add(BiasAlignment(1f, 0.85f))
            equityList.add(true)
        }
        else -> return Pair(listOf(), listOf())
    }
    return list to equityList
}

@Composable
fun AnimatedCommunityCards(
    cards: List<Card>,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val boxWithConstraintsScope = this
        val cardWidth = boxWithConstraintsScope.maxWidth / 5

        // 1. Создаем анимируемые состояния для каждой из 5 карт
        val cardOffsetsX = remember { List(5) { Animatable(0f) } }
        val cardOffsetsY = remember { List(5) { Animatable(0f) } }
        val cardAlphas = remember { List(5) { Animatable(0f) } }
        val cardRotations = remember { List(5) { Animatable(0f) } }

        var isReadyForAnimation by remember { mutableStateOf(false) }

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
            Box(modifier = Modifier.fillMaxWidth().height((cardWidth - 1.dp) * 1.5f)) {
                (0..4).forEach { i ->
                    val card = cards.getOrNull(i)
                    card?.let {
                        PokerCard(
                            card = it,
                            isFourColorMode = true,
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
fun SingleBoardLayout(state: GameState, displayMode: StackDisplayMode, modifier: Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        val text = if(displayMode == StackDisplayMode.CHIPS) state.pot.toString() else state.pot.toBB(state.bigBlindAmount) + " BB"
        Text("Pot: $text", color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        AnimatedCommunityCards(cards = state.communityCards)
//        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(1.dp)) {
//            // Отображаем 5 общих карт, даже если их еще нет
//            (0 until 5).forEach { index ->
//                val card = state.communityCards.getOrNull(index)
//                val mod = if(card == null) Modifier
//                    .weight(1f)
//                    .aspectRatio(0.6667f)
//                    .alpha(0.2f)
//                else Modifier
//                    .weight(1f)
//                    .aspectRatio(0.6667f)
//                PokerCard(
//                    card = card,
//                    isFourColorMode = true,
//                    modifier = mod
//                )
//            }
//        }
    }
}

@Composable
fun MultiBoardLayout(
    staticCards: List<Card>,
    runouts: List<List<Card>>,
    runs: Int,
    pot: Long,
    displayMode: StackDisplayMode,
    bigBlind: Long,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val boxWithConstraintsScope = this
        val cardWidth = boxWithConstraintsScope.maxWidth / 5
        val cardHeight = cardWidth * 1.5f

        val offset = if(runs == 2) -(cardHeight / 2) -(cardHeight / 2.5f) else -cardHeight -(cardHeight / 5)
        val text = if(displayMode == StackDisplayMode.CHIPS) pot.toString() else pot.toBB(bigBlind) + " BB"
        Text("Pot: $text", color = Color.White, modifier = Modifier
            .align(Alignment.Center)
            .offset(0.dp, offset))

        // 1. Рисуем статичные карты
        Row(verticalAlignment = Alignment.CenterVertically) {
            staticCards.forEach { card ->
                PokerCard(
                    card = card,
                    isFourColorMode = true,
                    modifier = Modifier
                        .width(cardWidth)
                        .height(cardHeight)
                )
            }
        Column(
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
                        modifier = Modifier
                            .offset(y = yOffset)
                            .width(cardWidth * (5 - staticCards.size))
                    ) {
                        // 2. Рисуем карты этого прогона
                        runout.forEach { card ->
                            PokerCard(
                                card = card,
                                isFourColorMode = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.6667f)
                            )
                        }
                        // 3. Добавляем плейсхолдеры до 5 карт
                        repeat(5 - staticCards.size - runout.size) {
                            CardBack(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.6667f)
                                    .alpha(0.2f)
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
    displayMode: StackDisplayMode,
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
                        val callText = if (displayMode == StackDisplayMode.BIG_BLINDS) {
                            "Call ${callValue.toBB(gameState.bigBlindAmount)} BB"
                        } else "Call $callValue"
                        BottomButton(onClick = { viewModel.onCall() }, enabled = isMyTurn, text = callText, modifier = Modifier.weight(1f))
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
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            BetControls(
                minBet = minOf(gameState.amountToCall + gameState.lastRaiseAmount, playerState.player.stack),
                maxBet = playerState.player.stack + playerState.currentBet,
                amountToCall = gameState.amountToCall,
                displayMode = displayMode,
                bigBlind = gameState.bigBlindAmount,
                onBetConfirmed = { betAmount ->
                    viewModel.onBet(betAmount)
                    showBetSlider = false // Скрываем ползунок после подтверждения
                },
                onDismiss = { showBetSlider = false }
            )
        }
    }
}

//@Composable
//@Preview
//fun TestControls() {
//    BetControls(100, 1000, 100, { 1000 }) { }
//}

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
    FilledTonalButton(
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
//fun TestPlayerDisplay() {
//    PlayerDisplay(PlayerState(Player("", "test", 1000L), hasFolded = true, lastAction = PlayerAction.Call(1L, "123")), true, true, true, 15L, Modifier, true, setOf("123"), true,
//        StackDisplayMode.CHIPS, 20, 1.2f)
//}

@Composable
fun PlayerDisplay(
    playerState: PlayerState,
    isMyPlayer: Boolean,
    isActivePlayer: Boolean,
    turnExpiresAt: Long?,
    modifier: Modifier = Modifier,
    isGameStarted: Boolean,
    visibleActionIds: Set<String>,
    isWinner: Boolean = false,
    displayMode: StackDisplayMode,
    bigBlind: Long,
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

        if (isGameStarted) {
            val (card1, card2) = if (isMyPlayer || playerState.cards.isNotEmpty()) {
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
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(60.dp * scaleMultiplier)
                        .offset(0.dp, (-10).dp * scaleMultiplier)
                )
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
                )
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
    }
    AnimatedVisibility(
        modifier = modifier,
        visible = isWinner,
        enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(animationSpec = tween(1000), initialOffsetY = { -it }),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box {
            RadiantGlowEffectEnhanced(
                Modifier.size(52.dp * scaleMultiplier).align(Alignment.Center),
                color = Color(0xFFFDFFD8),
                rayCount = 32,
                innerRadiusRatio = 0.2f
            )
            Image(
                painter = painterResource(R.drawable.winner_cup),
                contentDescription = "Winner",
                modifier = Modifier.width(27.dp * scaleMultiplier).height(39.dp * scaleMultiplier).align(Alignment.Center)
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
    viewModel: GameViewModel,
    modifier: Modifier,
    expiresAt: Long,
    onChoice: (Int) -> Unit
) {
    var remainingTime by remember { mutableLongStateOf(expiresAt - System.currentTimeMillis()) }
    LaunchedEffect(expiresAt) {
        while (remainingTime > 0) {
            remainingTime = expiresAt - System.currentTimeMillis()
            delay(50L) // Обновляем часто для плавной анимации
        }
        viewModel.hideRunItState()
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
    viewModel: GameViewModel,
    underdogName: String,
    times: Int,
    expiresAt: Long,
    modifier: Modifier,
    onConfirm: (Boolean) -> Unit
) {
    var remainingTime by remember { mutableLongStateOf(expiresAt - System.currentTimeMillis()) }
    LaunchedEffect(expiresAt) {
        while (remainingTime > 0) {
            remainingTime = expiresAt - System.currentTimeMillis()
            delay(50L) // Обновляем часто для плавной анимации
        }
        viewModel.hideRunItState()
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

fun calculateChipStack(amount: Long): List<Chip> {
    if (amount <= 0) return emptyList()

    val result = mutableListOf<Chip>()
    var remainingAmount = amount

    // Проходим по нашему набору фишек от самой дорогой к самой дешевой
    for (chip in standardChipSet) {
        // Вычисляем, сколько фишек этого номинала "влезает" в остаток
        val count = remainingAmount / chip.value
        if (count > 0) {
            // Добавляем нужное количество фишек в результат
            repeat(count.toInt()) {
                result.add(chip)
            }
            // Уменьшаем остаток
            remainingAmount %= chip.value
        }
    }
    return result
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

// todo использовать в главном экране
//val configuration = LocalConfiguration.current
//when (configuration.orientation) {
//    Configuration.ORIENTATION_LANDSCAPE -> { /* Логика для ландшафта */ }
//    else -> { /* Логика для портрета */ }
//}