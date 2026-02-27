package com.example.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.mobile.navigation.AppNavGraph
import com.example.mobile.navigation.Routes
import com.example.mobile.ui.theme.MobileTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MobileTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }
    }
}

@Composable
fun App() {
    val navController = rememberNavController()
    val context = LocalContext.current
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val sessionManager = SessionManager(context)
        val stored = sessionManager.readSession()
        if (stored == null) {
            startDestination = Routes.LOGIN
        } else {
            try {
                SupabaseClient.auth.refreshSession(stored.refreshToken)
                startDestination = Routes.DASHBOARD
            } catch (e: Exception) {
                sessionManager.clearSession()
                startDestination = Routes.LOGIN
            }
        }
    }

    if (startDestination != null) {
        AppNavGraph(
            navController = navController,
            startDestination = startDestination!!
        )
    }
}
