package com.example.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

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

    Scaffold { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Create Team Account",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(teamName, { teamName = it }, label = { Text("Team Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))


            OutlinedTextField(skillLevel, { skillLevel = it }, label = { Text("Skill Level") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(eircode, { eircode = it }, label = { Text("Eircode") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onRegisterSuccess,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text("Already have an account? Login")
            }
        }
    }
}
