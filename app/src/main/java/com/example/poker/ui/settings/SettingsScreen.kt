package com.example.poker.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onLogout: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // TODO: Поля для изменения никнейма и т.д.

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