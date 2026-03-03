package com.example.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.mobile.SupabaseClient
import com.example.mobile.presentation.challenges.ChallengesViewModel
import com.example.mobile.ui.screens.ChallengeDetailScreen
import com.example.mobile.ui.screens.CreateChallengeScreen
import com.example.mobile.ui.screens.DashboardScreen
import com.example.mobile.ui.screens.LoginScreen
import com.example.mobile.ui.screens.RegisterScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text

@Composable
fun AppNavGraph(navController: NavHostController) {
    val scope: CoroutineScope = rememberCoroutineScope()
    val challengesViewModel: ChallengesViewModel = viewModel(factory = ChallengesViewModel.Factory)

    val startDestination = if (SupabaseClient.hasActiveSession()) {
        Routes.DASHBOARD
    } else {
        Routes.LOGIN
    }

    val activeChallenges by challengesViewModel.activeChallenges.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    challengesViewModel.refreshChallenges()
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
                    challengesViewModel.refreshChallenges()
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
            DashboardScreen(
                challenges = activeChallenges,
                onNavigateToCreate = {
                    navController.navigate(Routes.CREATE_CHALLENGE)
                },
                onViewDetails = { challengeId ->
                    navController.navigate("${Routes.CHALLENGE_DETAIL}/$challengeId")
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

        composable(
            route = "${Routes.CHALLENGE_DETAIL}/{challengeId}",
            arguments = listOf(navArgument("challengeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val challengeId = backStackEntry.arguments?.getString("challengeId")
            if (challengeId == null) {
                Text("Challenge not found", modifier = Modifier.fillMaxSize())
                return@composable
            }

            val challenge by challengesViewModel.observeChallenge(challengeId)
                .collectAsStateWithLifecycle(initialValue = null)

            challenge?.let {
                ChallengeDetailScreen(
                    challenge = it,
                    onBack = { navController.popBackStack() }
                )
            } ?: Text("Challenge not found", modifier = Modifier.fillMaxSize())
        }

        composable(Routes.CREATE_CHALLENGE) {
            CreateChallengeScreen(
                onCreate = { teamName, skillLevel, location, date ->
                    challengesViewModel.createChallenge(
                        teamName = teamName,
                        skillLevel = skillLevel,
                        location = location,
                        date = date,
                        onSuccess = { navController.popBackStack() }
                    )
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
