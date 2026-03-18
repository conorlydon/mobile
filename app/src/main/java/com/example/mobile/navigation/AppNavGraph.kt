package com.example.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
//getValue allows use of by with delegated properties
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
//collectAsStateWithLifecycle() collects a Flow/StateFlow into Compose state safely
import androidx.lifecycle.compose.collectAsStateWithLifecycle
//viewModel() gets or creates a ViewModel inside Compose
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.mobile.MetricsService
//AuthUiState represents auth state like Idle/Loading/Success/Error
import com.example.mobile.presentation.auth.AuthUiState
import com.example.mobile.presentation.auth.AuthViewModel
import com.example.mobile.presentation.challenges.ChallengesViewModel
import com.example.mobile.presentation.challenges.CreateUiState
import com.example.mobile.presentation.challenges.DetailUiState
import com.example.mobile.ui.screens.ChallengeDetailScreen
import com.example.mobile.ui.screens.CreateChallengeScreen
import com.example.mobile.ui.screens.DashboardScreen
import com.example.mobile.ui.screens.LoginScreen
import com.example.mobile.ui.screens.RegisterScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text

//takes navController, which is what moves between screens.
@Composable
fun AppNavGraph(navController: NavHostController) {
    //gets view models used by app
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
    val challengesViewModel: ChallengesViewModel = viewModel(factory = ChallengesViewModel.Factory)
    //state value the UI watches
    val logoutUiState by authViewModel.logoutUiState.collectAsStateWithLifecycle()

    val startDestination = if (authViewModel.hasActiveSession()) {
        Routes.DASHBOARD
    } else {
        Routes.LOGIN
    }
//is used to run side effects like navigation when state changes
    LaunchedEffect(logoutUiState) {
        if (logoutUiState is AuthUiState.Success) {
            authViewModel.resetLogoutState()
            navController.navigate(Routes.LOGIN) {
                popUpTo(0)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(Routes.LOGIN) {
            val loginUiState by authViewModel.loginUiState.collectAsStateWithLifecycle()

            LaunchedEffect(loginUiState) {
                if (loginUiState is AuthUiState.Success) {
                    authViewModel.resetLoginState()
                    challengesViewModel.refreshChallenges()
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            }

            LoginScreen(
                uiState = loginUiState,
                onLogin = authViewModel::login,
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER) {
            val registerUiState by authViewModel.registerUiState.collectAsStateWithLifecycle()

            LaunchedEffect(registerUiState) {
                if (registerUiState is AuthUiState.Success) {
                    authViewModel.resetRegisterState()
                    MetricsService.track("club_joined")
                    challengesViewModel.refreshChallenges()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            }

            RegisterScreen(
                uiState = registerUiState,
                onRegister = authViewModel::register,
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
                onLogout = authViewModel::logout,
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
            val joinUiState by challengesViewModel.joinChallengeUiState.collectAsStateWithLifecycle()

            ChallengeDetailScreen(
                uiState = detailUiState,
                joinUiState = joinUiState,
                onJoinChallenge = challengesViewModel::requestJoinChallenge,
                onJoinResultShown = challengesViewModel::resetJoinChallengeState,
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
                onCreate = { skillLevel, date ->
                    challengesViewModel.createChallenge(skillLevel, date)
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
