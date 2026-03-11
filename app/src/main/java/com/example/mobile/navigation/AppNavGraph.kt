package com.example.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.mobile.presentation.challenges.CreateUiState
import com.example.mobile.presentation.challenges.DetailUiState
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
            val dashboardUiState by challengesViewModel.dashboardUiState.collectAsStateWithLifecycle()
            DashboardScreen(
                uiState = dashboardUiState,
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
                },
                onRetry = { challengesViewModel.refreshChallenges() }
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

            val detailUiState by challengesViewModel.observeChallenge(challengeId)
                .collectAsStateWithLifecycle(initialValue = DetailUiState.Loading)

            ChallengeDetailScreen(
                uiState = detailUiState,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CREATE_CHALLENGE) {
            val createUiState by challengesViewModel.createUiState.collectAsStateWithLifecycle()

            LaunchedEffect(createUiState) {
                if (createUiState is CreateUiState.Success) {
                    challengesViewModel.resetCreateState()
                    navController.popBackStack()
                }
            }

            CreateChallengeScreen(
                uiState = createUiState,
                onCreate = { teamName, skillLevel, location, date ->
                    challengesViewModel.createChallenge(teamName, skillLevel, location, date)
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
