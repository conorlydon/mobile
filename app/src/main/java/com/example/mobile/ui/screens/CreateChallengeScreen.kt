package com.example.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mobile.presentation.challenges.CreateUiState
import kotlinx.datetime.LocalDate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChallengeScreen(
    uiState: CreateUiState,
    onCreate: (teamName: String, skillLevel: String, location: String, date: LocalDate) -> Unit,
    onBack: () -> Unit
) {
    var teamName by remember { mutableStateOf("") }
    var skillLevel by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var isSkillLevelDropdownExpanded by remember { mutableStateOf(false) }
    
    val skillLevels = listOf(
        "Senior", "Intermediate", "Minor", 
        "U17", "U16", "U15", "U14", "U13", "U12", "U11", "U10", "U9",
        "Casual"
    )

    val parsedDate: LocalDate? = remember(date) {
        try {
            val sdf = SimpleDateFormat("d MMM yyyy", Locale.ENGLISH).apply { isLenient = false }
            val javaDate = sdf.parse(date.trim()) ?: return@remember null
            val cal = Calendar.getInstance().apply { time = javaDate }
            LocalDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        } catch (e: Exception) { null }
    }

    val dateError: String? = remember(date) {
        val cal = Calendar.getInstance()
        val today = LocalDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        when {
            date.isBlank() -> null
            parsedDate == null -> "Please enter a valid date (e.g., 14 Feb 2026)"
            parsedDate < today -> "Challenge date cannot be in the past. Please choose today or a future date."
            else -> null
        }
    }

    val isValid = teamName.isNotBlank() && skillLevel.isNotBlank() && location.isNotBlank() && parsedDate != null && dateError == null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Challenge") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2E7D32),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
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
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE8F5E8),
                            Color(0xFFF1F8E9),
                            Color.White
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            OutlinedTextField(
                value = teamName,
                onValueChange = { teamName = it },
                label = { Text("Team Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2E7D32),
                    focusedLabelColor = Color(0xFF2E7D32),
                    cursorColor = Color(0xFF2E7D32)
                )
            )
            
            // Skill Level Dropdown
            ExposedDropdownMenuBox(
                expanded = isSkillLevelDropdownExpanded,
                onExpandedChange = { isSkillLevelDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = skillLevel,
                    onValueChange = { skillLevel = it },
                    readOnly = true,
                    label = { Text("Skill Level") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSkillLevelDropdownExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2E7D32),
                        focusedLabelColor = Color(0xFF2E7D32),
                        cursorColor = Color(0xFF2E7D32)
                    )
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
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2E7D32),
                    focusedLabelColor = Color(0xFF2E7D32),
                    cursorColor = Color(0xFF2E7D32)
                )
            )
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date") },
                placeholder = { Text("e.g. 14 Feb 2026") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = dateError != null,
                supportingText = dateError?.let { error -> { Text(error) } },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2E7D32),
                    focusedLabelColor = Color(0xFF2E7D32),
                    cursorColor = Color(0xFF2E7D32),
                    errorBorderColor = Color(0xFFD32F2F),
                    errorLabelColor = Color(0xFFD32F2F)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState is CreateUiState.Error) {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            val isLoading = uiState is CreateUiState.Loading
            Button(
                onClick = {
                    onCreate(
                        teamName.trim(),
                        skillLevel.trim(),
                        location.trim(),
                        parsedDate!!
                    )
                },
                enabled = isValid && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32),
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Create Challenge",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            }
        }
    }
}
