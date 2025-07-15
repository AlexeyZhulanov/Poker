package com.example.poker.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import com.example.poker.navigation.Screen
import com.example.poker.ui.AuthViewModel

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val username by viewModel.username.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Слушаем события навигации
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { route ->
            if (route == Screen.Lobby.route) {
                onRegisterSuccess()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Register", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(16.dp))

            // Показываем ошибку, если она есть
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(value = username, onValueChange = viewModel::onUsernameChange, label = { Text("Username") })
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = email, onValueChange = viewModel::onEmailChange, label = { Text("Email") })
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = password, onValueChange = viewModel::onPasswordChange, label = { Text("Password") })
            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = { viewModel.onRegisterClick() }, enabled = !isLoading) {
                Text("Sign Up")
            }
            TextButton(onClick = onNavigateToLogin) {
                Text("Already have an account? Sign In")
            }
        }

        // Показываем индикатор загрузки
        if (isLoading) {
            CircularProgressIndicator()
        }
    }
}