package com.example.poker.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.poker.R
import com.example.poker.shared.dto.UserResponse

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onLogout: () -> Unit
) {
    val isPerformanceMode by viewModel.isPerformanceMode.collectAsState()
    val user by viewModel.userResponse.collectAsState()
    var showEditUsernameDialog by remember { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is SettingsUiEvent.ShowSnackBar -> {
                    snackBarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.Center)) {
                user?.let {
                    UserProfileSection(
                        user = it,
                        onEditUsernameClick = { showEditUsernameDialog = true }
                    )
                }
                SettingsSwitchItem(
                    title = "Режим производительности",
                    description = "Упрощает графику для повышения FPS на слабых устройствах",
                    icon = R.drawable.ic_speed,
                    checked = isPerformanceMode,
                    onCheckedChange = { viewModel.setPerformanceMode(it) }
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        viewModel.onLogoutClick()
                        onLogout() // Вызываем навигацию
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout")
                }
            }
        }
        if (showEditUsernameDialog) {
            EditUsernameDialog(
                currentUsername = user?.username ?: "",
                onDismiss = { showEditUsernameDialog = false },
                onConfirm = { newUsername ->
                    viewModel.changeUsername(newUsername)
                    showEditUsernameDialog = false
                }
            )
        }
    }
}

@Composable
fun UserProfileSection(user: UserResponse, onEditUsernameClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Avatar",
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = user.username, style = MaterialTheme.typography.titleLarge)
            Text(text = user.email, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
        IconButton(onClick = onEditUsernameClick) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Username")
        }
    }
}

@Composable
fun EditUsernameDialog(
    currentUsername: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newUsername by remember { mutableStateOf(currentUsername) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Сменить имя пользователя") },
        text = {
            OutlinedTextField(
                value = newUsername,
                onValueChange = { newUsername = it },
                label = { Text("Новое имя") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(newUsername) }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

//@Composable
//fun SettingsIconToggleItem(
//    title: String,
//    isPerformanceMode: Boolean,
//    onModeChange: (Boolean) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    ListItem(
//        headlineContent = { Text(title) },
//        trailingContent = {
//            IconToggleButton(
//                checked = isPerformanceMode,
//                onCheckedChange = onModeChange
//            ) {
//                if (isPerformanceMode) {
//                    Icon(painter = painterResource(R.drawable.ic_speed), contentDescription = "Performance Mode")
//                } else {
//                    Icon(painter = painterResource(R.drawable.ic_high_quality), contentDescription = "Quality Mode")
//                }
//            }
//        }
//    )
//}