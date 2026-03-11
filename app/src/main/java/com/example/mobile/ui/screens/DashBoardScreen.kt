package com.example.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.mobile.domain.challenges.display
import com.example.mobile.MetricsService
import com.example.mobile.presentation.challenges.DashboardUiState
import com.example.mobile.ui.theme.MobileThemeExtras
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onNavigateToCreate: () -> Unit,
    onViewDetails: (String) -> Unit,
    onLogout: () -> Unit,
    onRetry: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // Runs once when the screen enters composition
    LaunchedEffect(Unit) {
        MetricsService.track("dashboard_viewed")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Open Challenges") },
                colors = MobileThemeExtras.topAppBarColors(),
                actions = {
                    TextButton(
                        onClick = onLogout,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) {
                        Text("Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scope.launch { MetricsService.track("create_challenge_tapped") }
                    onNavigateToCreate()
                },
                containerColor = MobileThemeExtras.colors.brandPrimary,
                contentColor = MobileThemeExtras.colors.brandOnPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MobileThemeExtras.screenBackgroundBrush()),
            contentAlignment = Alignment.Center
        ) {
            // Each branch renders a different UI for the corresponding state
            when (uiState) {
                is DashboardUiState.Loading -> {
                    CircularProgressIndicator()
                }

                is DashboardUiState.Empty -> {
                    Text(
                        text = "No challenges available yet.\nTap + to create one!",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                is DashboardUiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(
                            onClick = onRetry,
                            colors = MobileThemeExtras.primaryButtonColors(),
                            modifier = Modifier.clip(MobileThemeExtras.shapes.actionButton)
                        ) {
                            Text("Retry")
                        }
                    }
                }

                is DashboardUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        items(uiState.challenges) { challenge ->
                            ChallengeCard(challenge, onViewDetails)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChallengeCard(challenge: com.example.mobile.domain.challenges.Challenge, onViewDetails: (String) -> Unit) {
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = MobileThemeExtras.surfaceCardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                challenge.teamName,
                style = MaterialTheme.typography.titleMedium,
                color = MobileThemeExtras.colors.heading
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Level: ${challenge.skillLevel}")
            Text("Location: ${challenge.location}")
            Text("Date: ${challenge.date.display()}")
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    scope.launch { MetricsService.track("view_challenge_tapped") }
                    onViewDetails(challenge.id)
                },
                colors = MobileThemeExtras.primaryButtonColors(),
                modifier = Modifier.clip(MobileThemeExtras.shapes.actionButton)
            ) {
                Text("View Details")
            }
        }
    }
}
