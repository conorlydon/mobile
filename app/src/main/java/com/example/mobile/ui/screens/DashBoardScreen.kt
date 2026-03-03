package com.example.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mobile.MetricsService
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Challenge(
    val id: String,
    @SerialName("team_name") val teamName: String,
    @SerialName("skill_level") val skillLevel: String,
    val location: String,
    val date: LocalDate,
    @SerialName("created_by_email") val createdByEmail: String? = null
)

fun LocalDate.display(): String {
    val mon = month.name[0] + month.name.drop(1).take(2).lowercase()
    return "$dayOfMonth $mon $year"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    challenges: List<Challenge>,
    onNavigateToCreate: () -> Unit,
    onViewDetails: (Challenge) -> Unit,
    onLogout: () -> Unit
) {

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        MetricsService.track("dashboard_viewed")
    }

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
            FloatingActionButton(onClick = {
                scope.launch { MetricsService.track("create_challenge_tapped") }
                onNavigateToCreate()
            }) {
                Icon(Icons.Default.Add, contentDescription = "Create")
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            items(challenges) { challenge ->
                ChallengeCard(challenge, onViewDetails)
            }
        }
    }
}

@Composable
fun ChallengeCard(challenge: Challenge, onViewDetails: (Challenge) -> Unit) {

    val scope = rememberCoroutineScope()

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
            Text("Date: ${challenge.date.display()}")

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                scope.launch { MetricsService.track("view_challenge_tapped") }
                onViewDetails(challenge)
            }) {
                Text("View Details")
            }
        }
    }
}
