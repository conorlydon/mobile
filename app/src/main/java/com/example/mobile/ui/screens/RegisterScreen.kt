package com.example.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.mobile.MetricsService
import com.example.mobile.SupabaseClient
import com.example.mobile.ui.theme.MobileThemeExtras

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var skillLevel by remember { mutableStateOf("") }
    var eircode by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

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
                    value = skillLevel,
                    onValueChange = { skillLevel = it },
                    label = { Text("Skill Level") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = MobileThemeExtras.formFieldColors()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = eircode,
                    onValueChange = { eircode = it },
                    label = { Text("Eircode") },
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
                        coroutineScope.launch {
                            isLoading = true
                            errorMessage = ""

                            try {
                                SupabaseClient.registerTeam(
                                    email = email.trim(),
                                    password = password,
                                    skillLevel = skillLevel.trim(),
                                    eircode = eircode.trim()
                                )
                                MetricsService.track("club_joined")
                                onRegisterSuccess()

                            } catch (e: Exception) {
                                errorMessage = when {
                                    skillLevel.isBlank() -> "Please enter your team's skill level"
                                    eircode.isBlank() -> "Please enter your eircode"
                                    email.isBlank() -> "Please enter your email address"
                                    password.isBlank() -> "Please enter a password"
                                    !email.contains("@") -> "Please enter a valid email address"
                                    password.length < 6 -> "Password must be at least 6 characters long"
                                    eircode.length < 3 -> "Please enter a valid eircode"
                                    e.message?.contains("already registered", ignoreCase = true) == true -> "An account with this email already exists. Please login instead."
                                    e.message?.contains("weak password", ignoreCase = true) == true -> "Password is too weak. Please choose a stronger password."
                                    else -> "Registration failed. Please check your information and try again."
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
