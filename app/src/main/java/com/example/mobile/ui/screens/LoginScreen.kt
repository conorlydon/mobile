package com.example.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.mobile.MetricsService
import com.example.mobile.SupabaseClient
import com.example.mobile.ui.theme.MobileThemeExtras
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        MetricsService.track("login_viewed")
    }

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
                    text = "Team Login",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MobileThemeExtras.colors.heading
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = MobileThemeExtras.formFieldColors()
                )

                Spacer(modifier = Modifier.height(16.dp))

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

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            errorMessage = ""

                            try {
                                SupabaseClient.signInWithEmail(
                                    email = email.trim(),
                                    password = password
                                )
                                onLoginSuccess()
                            } catch (e: Exception) {
                                errorMessage = when {
                                    email.isBlank() -> "Please enter your email address"
                                    password.isBlank() -> "Please enter your password"
                                    !email.contains("@") -> "Please enter a valid email address"
                                    password.length < 6 -> "Password must be at least 6 characters long"
                                    e.message?.contains("Invalid login", ignoreCase = true) == true -> "Incorrect email or password. Please try again."
                                    e.message?.contains("User not found", ignoreCase = true) == true -> "No account found with this email. Please register first."
                                    else -> "Login failed. Please check your credentials and try again."
                                }
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(MobileThemeExtras.shapes.actionButton),
                    enabled = !isLoading,
                    colors = MobileThemeExtras.primaryButtonColors()
                ) {
                    Text(if (isLoading) "Logging in..." else "Login")
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onNavigateToRegister,
                    modifier = Modifier.align(Alignment.End),
                    colors = MobileThemeExtras.textButtonColors()
                ) {
                    Text("Don't have an account? Register")
                }
            }
        }
    }
}
