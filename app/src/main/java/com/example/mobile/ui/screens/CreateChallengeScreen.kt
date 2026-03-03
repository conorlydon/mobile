package com.example.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChallengeScreen(
    onCreate: (teamName: String, skillLevel: String, location: String, date: LocalDate) -> Unit,
    onBack: () -> Unit
) {
    var teamName by remember { mutableStateOf("") }
    var skillLevel by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }

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
            parsedDate == null -> "Use format: 14 Feb 2026"
            parsedDate < today -> "Date must be today or in the future"
            else -> null
        }
    }

    val isValid = teamName.isNotBlank() && skillLevel.isNotBlank() && location.isNotBlank() && parsedDate != null && dateError == null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Challenge") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = teamName,
                onValueChange = { teamName = it },
                label = { Text("Team Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = skillLevel,
                onValueChange = { skillLevel = it },
                label = { Text("Skill Level") },
                placeholder = { Text("e.g. Junior, Intermediate, Senior") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date") },
                placeholder = { Text("e.g. 14 Feb 2026") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = dateError != null,
                supportingText = dateError?.let { error -> { Text(error) } }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    onCreate(
                        teamName.trim(),
                        skillLevel.trim(),
                        location.trim(),
                        parsedDate!!
                    )
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Challenge")
            }
        }
    }
}
