package com.example.poker.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.poker.R

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onLogout: () -> Unit
) {
    val isPerformanceMode by viewModel.isPerformanceMode.collectAsState()

    Scaffold { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.Center)) {
                SettingsSwitchItem(
                    title = "Режим производительности",
                    description = "Упрощает графику для повышения FPS на слабых устройствах",
                    icon = R.drawable.ic_speed,
                    checked = isPerformanceMode,
                    onCheckedChange = { viewModel.setPerformanceMode(it) }
                )

//                SettingsIconToggleItem(
//                    title = "Режим производительности",
//                    isPerformanceMode = isPerformanceMode,
//                    onModeChange = { viewModel.setPerformanceMode(it) }
//                )
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
    }
}