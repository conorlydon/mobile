package com.example.mobile.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mobile.SupabaseClient
import com.example.mobile.ui.screens.Challenge
import com.example.mobile.ui.screens.CreateChallengeScreen
import com.example.mobile.ui.screens.DashboardScreen
import com.example.mobile.ui.screens.LoginScreen
import com.example.mobile.ui.screens.RegisterScreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val CREATE_CHALLENGE = "create_challenge"
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    val scope = rememberCoroutineScope()
    val startDestination = if (SupabaseClient.hasActiveSession()) {
        Routes.DASHBOARD
    } else {
        Routes.LOGIN
    }

    val challenges = remember { mutableStateListOf<Challenge>() }

    val dateFormatter = remember { SimpleDateFormat("d MMM yyyy", Locale.ENGLISH) }
    val activeChallenges = remember(challenges.toList()) {
        val today = dateFormatter.parse(SimpleDateFormat("d MMM yyyy", Locale.ENGLISH).format(Date()))
        challenges.filter { challenge ->
            try {
                val challengeDate = dateFormatter.parse(challenge.date)
                challengeDate != null && !challengeDate.before(today)
            } catch (e: Exception) {
                true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.DASHBOARD) {
            LaunchedEffect(Unit) {
                val fetched = SupabaseClient.fetchChallenges()
                challenges.clear()
                challenges.addAll(fetched)
            }
            DashboardScreen(
                challenges = activeChallenges,
                onNavigateToCreate = {
                    navController.navigate(Routes.CREATE_CHALLENGE)
                },
                onLogout = {
                    scope.launch {
                        SupabaseClient.signOut()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0)
                        }
                    }
                }
            )
        }

        composable(Routes.CREATE_CHALLENGE) {
            CreateChallengeScreen(
                onCreate = { newChallenge ->
                    scope.launch {
                        SupabaseClient.insertChallenge(newChallenge)
                    }
                    challenges.add(newChallenge)
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
