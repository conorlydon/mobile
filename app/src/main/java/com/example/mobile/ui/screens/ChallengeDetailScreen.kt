package com.example.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeDetailScreen(
    challenge: Challenge,
    onBack: () -> Unit
) {
    var showContactDialog by remember { mutableStateOf(false) }

    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = { Text("Contact ${challenge.teamName}") },
            text = { Text(challenge.createdByEmail ?: "No contact info available") },
            confirmButton = {
                TextButton(onClick = { showContactDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(challenge.teamName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Skill Level", style = MaterialTheme.typography.labelMedium)
            Text(challenge.skillLevel, style = MaterialTheme.typography.bodyLarge)

            HorizontalDivider()

            Text("Location", style = MaterialTheme.typography.labelMedium)
            Text(challenge.location, style = MaterialTheme.typography.bodyLarge)

            HorizontalDivider()

            Text("Date", style = MaterialTheme.typography.labelMedium)
            Text(challenge.date.display(), style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showContactDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Contact Team")
            }
        }
    }
}
