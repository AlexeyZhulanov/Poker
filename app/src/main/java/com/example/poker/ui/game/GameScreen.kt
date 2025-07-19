package com.example.poker.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poker.data.remote.dto.GameState
import com.example.poker.data.remote.dto.PlayerState

@Composable
fun GameScreen(viewModel: GameViewModel) {
    // todo сделать state условно playersList, в качестве initial вытягиваем его из GameRoom, а дальше уже из GameState
    val gameState by viewModel.gameState.collectAsState()
    val myUserId by viewModel.myUserId.collectAsState()
    val gameRoom by viewModel.roomInfo.collectAsState()
    // todo можно по ready запускать комнату и неважно кто владелец

    val myPlayerState = gameState?.playerStates?.find { it.player.userId == myUserId }
    val activePlayerId = gameState?.playerStates?.getOrNull(gameState!!.activePlayerPosition)?.player?.userId

    Scaffold(
        bottomBar = {
            ActionPanel(
                viewModel = viewModel,
                isMyTurn = gameState != null && activePlayerId == myUserId,
                playerState = myPlayerState,
                gameState = gameState
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF004D40)), // Цвет сукна
            contentAlignment = Alignment.Center
        ) {
            val boxWithConstraintsScope = this
            val width = boxWithConstraintsScope.maxWidth
            val height = boxWithConstraintsScope.maxHeight
            // Отображаем стол
            //PokerTable() // todo можно будет красиво сделать и вернуть

            // ЗДЕСЬ ИГРОКОВ РИСУЕМ и РЕЖИМ
            // todo как-то получать игроков до старта игры
//            val myPlayerIndex = state.playerStates.indexOfFirst { it.player.userId == myUserId }
//
//            state.playerStates.forEach { playerState ->
//                PlayerDisplay(
//                    playerState = playerState,
//                    isMyPlayer = playerState.player.userId == myUserId,
//                    isFourColorMode = true
//                )
//            }


            gameState?.let {
                CardsLayout(it, width)
            } ?: Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Waiting for game to start...", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                // TODO: Добавить проверку на владельца комнаты, как только будет готов GET /rooms/{roomId}
                // if (myUserId == roomInfo?.ownerId)
                Button(onClick = { viewModel.onStartGameClick() }) {
                    Text("Start Game")
                }
            }
        }
    }
}

@Composable
fun CardsLayout(state: GameState, w: Dp) {
    val cardWidth = w / 5
    val cardHeight = cardWidth * 1.5f
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
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
    gameState: GameState? // Общее состояние игры
) {
    // Состояние для отображения/скрытия ползунка
    var showBetSlider by remember { mutableStateOf(false) }

    // Используем Box для наложения ползунка поверх панели
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        // --- ОСНОВНАЯ ПАНЕЛЬ С ТРЕМЯ КНОПКАМИ ---
        BottomAppBar {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Кнопка FOLD
                BottomButton(onClick = { viewModel.onFold() }, enabled = isMyTurn, text = "Fold")

                // 2. Динамическая кнопка CHECK / CALL
                val amountToCall = gameState?.amountToCall ?: 0L
                val myCurrentBet = playerState?.currentBet ?: 0L

                if (amountToCall == 0L || amountToCall == myCurrentBet) {
                    // Если ставить не нужно, показываем CHECK
                    BottomButton(onClick = { viewModel.onCheck() }, enabled = isMyTurn, text = "Check")
                } else {
                    // Если нужно коллировать, показываем CALL с суммой
                    val callValue = minOf(playerState?.player?.stack ?: 0L, amountToCall - myCurrentBet)
                    BottomButton(onClick = { viewModel.onCall() }, enabled = isMyTurn, text = "Call $callValue")
                }

                // 3. Кнопка BET / RAISE
                BottomButton(onClick = { showBetSlider = true }, enabled = isMyTurn, text = "Bet / Raise")
            }
        }

        // --- ПОЛЗУНОК ДЛЯ СТАВКИ (появляется по условию) ---
        if (showBetSlider && playerState != null && gameState != null) {
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
            .padding(bottom = 80.dp, start = 16.dp, end = 16.dp), // Располагаем над основной панелью
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
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
fun BottomButton(onClick: () -> Unit, enabled: Boolean, text: String) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonColors(Color.Red.copy(alpha = 0.7f), Color.Black, Color.Gray, Color.Black),
        border = BorderStroke(5.dp, Brush.radialGradient(listOf(Color.Red, Color(0xFF640D14)), radius = 150f))
    ) {
        Text(text)
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
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier
        .width(IntrinsicSize.Max)
        .height(80.dp)
        .padding(5.dp, 0.dp)) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Player Avatar",
            tint = Color.White,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color.DarkGray)
                .border(1.dp, Color.White, shape = CircleShape)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy((-20).dp),
            modifier = Modifier.align(Alignment.Center)
        ) {
            val (card1, card2) = if (isMyPlayer || playerState.cards.isNotEmpty()) {
                playerState.cards.getOrNull(0) to playerState.cards.getOrNull(1)
            } else {
                null to null
            }
            PokerCard(
                card = card1,
                isFourColorMode = isFourColorMode,
                modifier = Modifier
                    .width(40.dp)
                    .height(60.dp)
                    .graphicsLayer { rotationZ = -10f }
            )
            PokerCard(
                card = card2,
                isFourColorMode = isFourColorMode,
                modifier = Modifier
                    .width(40.dp)
                    .height(60.dp)
                    .graphicsLayer { rotationZ = 10f }
            )
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
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(0.dp, 1.dp)
            )
            HorizontalDivider()
            Text(
                text = playerState.player.stack.toString(),
                color = Color.White,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(0.dp, 1.dp)
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