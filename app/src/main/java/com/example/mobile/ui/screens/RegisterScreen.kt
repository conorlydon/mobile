package com.example.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.mobile.MetricsService
import com.example.mobile.presentation.auth.AuthUiState
import com.example.mobile.ui.theme.MobileThemeExtras

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    uiState: AuthUiState,
    onRegister: (email: String, password: String, teamName: String, location: String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var teamName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isLoading = uiState is AuthUiState.Loading
    val errorMessage = (uiState as? AuthUiState.Error)?.message.orEmpty()

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MobileThemeExtras.screenBackgroundBrush())
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    "Create Team Account",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MobileThemeExtras.colors.heading
                )

                Spacer(modifier = Modifier.height(16.dp))


                OutlinedTextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    label = { Text("Team Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = MobileThemeExtras.formFieldColors()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = MobileThemeExtras.formFieldColors()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = MobileThemeExtras.formFieldColors()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = MobileThemeExtras.formFieldColors()
                )

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onRegister(email.trim(), password, teamName.trim(), location.trim())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(MobileThemeExtras.shapes.actionButton),
                    enabled = !isLoading,
                    colors = MobileThemeExtras.primaryButtonColors()
                ) {
                    Text(if (isLoading) "Registering..." else "Register")
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onNavigateToLogin,
                    colors = MobileThemeExtras.textButtonColors()
                ) {
                    Text("Already have an account? Login")
                }
            }
        }
    }
}
