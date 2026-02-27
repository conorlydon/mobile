package com.example.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.mobile.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import com.example.mobile.SupabaseClient
import com.example.mobile.StoredSession
import io.github.jan.supabase.auth.providers.builtin.Email



@Serializable
data class User(
    val id: String? = null,
    val email: String,
    val password: String,
    val skill_level: String,
    val eircode: String
)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var teamName by remember { mutableStateOf("") }
    var skillLevel by remember { mutableStateOf("") }
    var eircode by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {

            Text("Create Team Account", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = teamName,
                onValueChange = { teamName = it },
                label = { Text("Team Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = skillLevel,
                onValueChange = { skillLevel = it },
                label = { Text("Skill Level") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = eircode,
                onValueChange = { eircode = it },
                label = { Text("Eircode") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
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
                            val (session, inserted) = withContext(Dispatchers.IO) {
                                SupabaseClient.auth.signUpWith(Email) {
                                    this.email = email
                                    this.password = password
                                }

                                val session = SupabaseClient.auth.currentSessionOrNull()

                                // Attempt insertion and decode list of inserted rows
                                val inserted = SupabaseClient.postgrest["users"]
                                    .insert(
                                        mapOf(
                                            "email" to email,
                                            "password" to password,
                                            "skill_level" to skillLevel,
                                            "eircode" to eircode
                                        )
                                    ) {
                                        select()  // return the inserted row(s)
                                    }
                                    .decodeList<User>() // get inserted results

                                session to inserted
                            }

                            if (session != null) {
                                SessionManager(context).saveSession(
                                    StoredSession(
                                        accessToken = session.accessToken,
                                        refreshToken = session.refreshToken
                                    )
                                )
                            }

                            if (inserted.isEmpty()) {
                                errorMessage = "Failed to register (no response)"
                            } else {
                                onRegisterSuccess()
                            }

                        } catch (e: Exception) {
                            errorMessage = e.localizedMessage ?: "Unknown error"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Registering..." else "Register")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text("Already have an account? Login")
            }
        }
    }
}
