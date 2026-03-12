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
    onRegister: (email: String, password: String, skillLevel: String, eircode: String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var skillLevel by remember { mutableStateOf("") }
    var eircode by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSkillLevelDropdownExpanded by remember { mutableStateOf(false) }
    val isLoading = uiState is AuthUiState.Loading
    val errorMessage = (uiState as? AuthUiState.Error)?.message.orEmpty()

    val skillLevels = listOf(
        "Senior", "Intermediate", "Minor",
        "U17", "U16", "U15", "U14", "U13", "U12", "U11", "U10", "U9",
        "Casual"
    )

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


                ExposedDropdownMenuBox(
                    expanded = isSkillLevelDropdownExpanded,
                    onExpandedChange = { isSkillLevelDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = skillLevel,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Skill Level") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSkillLevelDropdownExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = MobileThemeExtras.formFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = isSkillLevelDropdownExpanded,
                        onDismissRequest = { isSkillLevelDropdownExpanded = false }
                    ) {
                        skillLevels.forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level) },
                                onClick = {
                                    skillLevel = level
                                    isSkillLevelDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
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
                        onRegister(email.trim(), password, skillLevel.trim(), eircode.trim())
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
