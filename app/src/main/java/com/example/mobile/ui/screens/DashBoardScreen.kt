package com.example.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Challenge(
    val id: String,
    val teamName: String,
    val skillLevel: String,
    val location: String,
    val date: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit
) {

    val dummyChallenges = listOf(
        Challenge("1", "Ballina GAA", "Intermediate", "Ballina Pitch", "14 Feb 2026"),
        Challenge("2", "Castlebar FC", "Junior", "Castlebar Astro", "20 Feb 2026"),
        Challenge("3", "Westport United", "Senior", "Westport Grounds", "25 Feb 2026")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Open Challenges") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { }) {
                Icon(Icons.Default.Add, contentDescription = "Create")
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            items(dummyChallenges) { challenge ->
                ChallengeCard(challenge)
            }
        }
    }
}

@Composable
fun ChallengeCard(challenge: Challenge) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(challenge.teamName, style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(4.dp))
            Text("Level: ${challenge.skillLevel}")
            Text("Location: ${challenge.location}")
            Text("Date: ${challenge.date}")

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { }) {
                Text("View Details")
            }
        }
    }
}
