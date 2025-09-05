package com.example.poker.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.poker.navigation.Screen

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Слушаем события навигации
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { route ->
            if (route == Screen.Lobby.route) {
                onLoginSuccess()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.statusBarsPadding().navigationBarsPadding().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Login", style = MaterialTheme.typography.headlineLarge, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))

            // Показываем ошибку, если она есть
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(value = email, onValueChange = viewModel::onEmailChange, label = { Text("Email") })
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = password, onValueChange = viewModel::onPasswordChange, label = { Text("Password") })
            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = { viewModel.onLoginClick() }, enabled = !isLoading) {
                Text("Sign In")
            }
            TextButton(onClick = onNavigateToRegister) {
                Text("Don't have an account? Sign Up")
            }
        }

        // Показываем индикатор загрузки
        if (isLoading) {
            CircularProgressIndicator()
        }
    }
}