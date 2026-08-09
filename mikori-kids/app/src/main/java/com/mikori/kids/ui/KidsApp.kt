package com.mikori.kids.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mikori.kids.ui.home.HomeScreen
import com.mikori.kids.ui.linking.LinkingScreen
import com.mikori.kids.ui.onboarding.OnboardingScreen

private object KidsRoutes {
    const val ONBOARDING = "onboarding"
    const val LINKING = "linking"
}

@Composable
fun KidsApp(rootViewModel: RootViewModel = hiltViewModel()) {
    val linked by rootViewModel.isLinked.collectAsStateWithLifecycle()

    Surface(color = MaterialTheme.colorScheme.background) {
        when (linked) {
            null -> Loading()
            false -> SetupNavHost()
            true -> HomeScreen()
        }
    }
}

@Composable
private fun SetupNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = KidsRoutes.ONBOARDING) {
        composable(KidsRoutes.ONBOARDING) {
            OnboardingScreen(onContinue = { nav.navigate(KidsRoutes.LINKING) })
        }
        composable(KidsRoutes.LINKING) {
            LinkingScreen(onBack = { nav.popBackStack() })
        }
    }
}

@Composable
private fun Loading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}
