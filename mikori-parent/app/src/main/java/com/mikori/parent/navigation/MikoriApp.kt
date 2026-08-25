package com.mikori.parent.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mikori.parent.core.ui.components.LoadingState
import com.mikori.parent.feature.auth.LoginScreen
import com.mikori.parent.feature.auth.RegisterScreen
import com.mikori.parent.feature.child.AddChildScreen
import com.mikori.parent.feature.child.ChildDetailScreen
import com.mikori.parent.feature.control.AppRulesScreen
import com.mikori.parent.feature.control.ControlScreen
import com.mikori.parent.feature.control.SchedulesScreen
import com.mikori.parent.feature.dashboard.DashboardScreen
import com.mikori.parent.feature.limits.LimitsScreen
import com.mikori.parent.feature.linking.LinkingScreen
import com.mikori.parent.feature.settings.SettingsScreen
import com.mikori.parent.feature.stats.StatsScreen

@Composable
fun MikoriApp(rootViewModel: RootViewModel = hiltViewModel()) {
    val loggedIn by rootViewModel.isLoggedIn.collectAsStateWithLifecycle()

    Surface(color = MaterialTheme.colorScheme.background) {
        when (loggedIn) {
            null -> LoadingState(Modifier.fillMaxSize())
            false -> AuthNavHost()
            true -> MainScaffold()
        }
    }
}

@Composable
private fun AuthNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(onGoToRegister = { nav.navigate(Routes.REGISTER) })
        }
        composable(Routes.REGISTER) {
            RegisterScreen(onGoToLogin = { nav.popBackStack() })
        }
    }
}

private data class BottomDest(val route: String, val label: String, val icon: ImageVector)

private val bottomDestinations = listOf(
    BottomDest(Routes.DASHBOARD, "Inicio", Icons.Rounded.Home),
    BottomDest(Routes.SETTINGS, "Ajustes", Icons.Rounded.Settings),
)

@Composable
private fun MainScaffold() {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomDestinations.map { it.route }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AnimatedVisibility(visible = showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    bottomDestinations.forEach { dest ->
                        NavigationBarItem(
                            selected = currentRoute == dest.route,
                            onClick = {
                                nav.navigate(dest.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onChildClick = { id -> nav.navigate(Routes.childDetail(id)) },
                    onAddChild = { nav.navigate(Routes.ADD_CHILD) },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen()
            }
            composable(Routes.ADD_CHILD) {
                AddChildScreen(
                    onBack = { nav.popBackStack() },
                    onCreated = { nav.popBackStack() },
                )
            }
            composable(
                Routes.CHILD_DETAIL,
                arguments = listOf(navArgument("childId") { type = NavType.StringType }),
            ) { entry ->
                val id = currentChildId(entry)
                ChildDetailScreen(
                    onBack = { nav.popBackStack() },
                    onOpenStats = { nav.navigate(Routes.stats(id)) },
                    onOpenLimits = { nav.navigate(Routes.limits(id)) },
                    onOpenLinking = { nav.navigate(Routes.linking(id)) },
                    onOpenControl = { nav.navigate(Routes.control(id)) },
                    onDeleted = { nav.popBackStack(Routes.DASHBOARD, inclusive = false) },
                )
            }
            composable(
                Routes.CONTROL,
                arguments = listOf(navArgument("childId") { type = NavType.StringType }),
            ) { entry ->
                val id = currentChildId(entry)
                ControlScreen(
                    onBack = { nav.popBackStack() },
                    onOpenAppRules = { nav.navigate(Routes.appRules(id)) },
                    onOpenSchedules = { nav.navigate(Routes.schedules(id)) },
                )
            }
            composable(
                Routes.APP_RULES,
                arguments = listOf(navArgument("childId") { type = NavType.StringType }),
            ) {
                AppRulesScreen(onBack = { nav.popBackStack() })
            }
            composable(
                Routes.SCHEDULES,
                arguments = listOf(navArgument("childId") { type = NavType.StringType }),
            ) {
                SchedulesScreen(onBack = { nav.popBackStack() })
            }
            composable(
                Routes.STATS,
                arguments = listOf(navArgument("childId") { type = NavType.StringType }),
            ) {
                StatsScreen(onBack = { nav.popBackStack() })
            }
            composable(
                Routes.LIMITS,
                arguments = listOf(navArgument("childId") { type = NavType.StringType }),
            ) {
                LimitsScreen(onBack = { nav.popBackStack() })
            }
            composable(
                Routes.LINKING,
                arguments = listOf(navArgument("childId") { type = NavType.StringType }),
            ) {
                LinkingScreen(onBack = { nav.popBackStack() })
            }
        }
    }
}

private fun currentChildId(entry: androidx.navigation.NavBackStackEntry?): Long =
    entry?.arguments?.getString("childId")?.toLongOrNull() ?: 0L
