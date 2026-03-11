package com.example.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mobile.domain.challenges.Challenge
import com.example.mobile.domain.challenges.display
import com.example.mobile.presentation.challenges.DetailUiState
import com.example.mobile.ui.theme.MobileThemeExtras

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeDetailScreen(
    uiState: DetailUiState,
    onBack: () -> Unit
) {
    when (uiState) {
        is DetailUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }
        is DetailUiState.Error -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Challenge") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(uiState.message)
                }
            }
            return
        }
        is DetailUiState.Success -> ChallengeDetailContent(uiState.challenge, onBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChallengeDetailContent(
    challenge: Challenge,
    onBack: () -> Unit
) {
    var showContactDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }

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
    
    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = { Text("Join Challenge") },
            text = { Text("Are you sure you want to join this challenge? An email will be sent to ${challenge.teamName}.") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showJoinDialog = false
                        // TODO: Implement email sending functionality
                    }
                ) {
                    Text("Join")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(challenge.teamName) },
                colors = MobileThemeExtras.topAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MobileThemeExtras.screenBackgroundBrush())
        ) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Team Name and Skill Level Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = MobileThemeExtras.surfaceCardColors(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = challenge.teamName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MobileThemeExtras.colors.heading
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Skill Level Badge
                        Box(
                            modifier = Modifier
                                .clip(MobileThemeExtras.shapes.badge)
                                .background(MobileThemeExtras.skillLevelColor(challenge.skillLevel))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = challenge.skillLevel,
                                color = MobileThemeExtras.colors.brandOnPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // Details Cards
                InfoCard(
                    icon = Icons.Default.Star,
                    title = "Skill Level",
                    content = challenge.skillLevel,
                    iconColor = MobileThemeExtras.skillLevelColor(challenge.skillLevel)
                )
                
                InfoCard(
                    icon = Icons.Default.LocationOn,
                    title = "Location",
                    content = challenge.location,
                    iconColor = MobileThemeExtras.colors.locationAccent
                )
                
                InfoCard(
                    icon = Icons.Default.Info,
                    title = "Date",
                    content = challenge.date.display(),
                    iconColor = MobileThemeExtras.colors.dateAccent
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showJoinDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(MobileThemeExtras.shapes.actionButton),
                        colors = MobileThemeExtras.primaryButtonColors()
                    ) {
                        Text("Join Challenge", fontWeight = FontWeight.Medium)
                    }
                    
                    OutlinedButton(
                        onClick = { showContactDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(MobileThemeExtras.shapes.actionButton),
                        border = BorderStroke(2.dp, MobileThemeExtras.colors.brandPrimary),
                        colors = MobileThemeExtras.outlinedButtonColors()
                    ) {
                        Text("Contact Team", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    icon: ImageVector,
    title: String,
    content: String,
    iconColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = MobileThemeExtras.surfaceCardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MobileThemeExtras.shapes.iconContainer)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MobileThemeExtras.colors.mutedText
                )
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
