package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.CompletionScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.JapaCounterScreen
import com.example.ui.screens.MantraListScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.viewmodel.HistoryViewModel
import com.example.ui.viewmodel.JapaCounterViewModel
import com.example.ui.viewmodel.MantraViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.StatsViewModel
import com.example.util.AppStrings

sealed class Screen(
    val route: String,
    val stringKey: AppStrings.Key?,
    val selectedIcon: ImageVector?,
    val unselectedIcon: ImageVector?
) {
    object Splash : Screen("splash", null, null, null)
    object Onboarding : Screen("onboarding", null, null, null)
    object Home : Screen("home", AppStrings.Key.HOME, Icons.Filled.Home, Icons.Outlined.Home)
    object Mantras : Screen("mantras", AppStrings.Key.MY_MANTRAS, Icons.Filled.SelfImprovement, Icons.Outlined.SelfImprovement)
    object Stats : Screen("stats", AppStrings.Key.STATISTICS, Icons.Filled.BarChart, Icons.Outlined.BarChart)
    object Settings : Screen("settings", AppStrings.Key.SETTINGS, Icons.Filled.Settings, Icons.Outlined.Settings)
    object History : Screen("history", AppStrings.Key.JAPA_HISTORY, null, null)
    object Counter : Screen("counter/{mantraId}", null, null, null) {
        fun createRoute(mantraId: Long) = "counter/$mantraId"
    }
    object Completion : Screen("completion/{mantraId}/{count}/{durationSeconds}", null, null, null) {
        fun createRoute(mantraId: Long, count: Int, durationSeconds: Long) =
            "completion/$mantraId/$count/$durationSeconds"
    }
}

@Composable
fun AppNavHost(
    mantraViewModel: MantraViewModel,
    counterViewModel: JapaCounterViewModel,
    historyViewModel: HistoryViewModel,
    statsViewModel: StatsViewModel,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val preferences by settingsViewModel.preferences.collectAsStateWithLifecycle()
    val isGujarati = preferences.language == "gu"

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Mantras,
        Screen.Stats,
        Screen.Settings
    )

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Mantras.route,
        Screen.Stats.route,
        Screen.Settings.route
    )

    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                val icon = if (selected) screen.selectedIcon else screen.unselectedIcon
                                if (icon != null) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = screen.stringKey?.let { AppStrings.get(it, isGujarati) }
                                    )
                                }
                            },
                            label = {
                                screen.stringKey?.let {
                                    Text(
                                        text = AppStrings.get(it, isGujarati),
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Splash Screen
            composable(Screen.Splash.route) {
                SplashScreen(
                    isGujarati = isGujarati,
                    onboardingCompleted = preferences.onboardingCompleted,
                    onNavigateNext = { completed ->
                        if (completed) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Onboarding.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    }
                )
            }

            // Onboarding Screen
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    isGujarati = isGujarati,
                    onStartClick = {
                        settingsViewModel.setOnboardingCompleted(true)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // Home Screen
            composable(Screen.Home.route) {
                val todayStats by statsViewModel.todayStats.collectAsStateWithLifecycle()
                val currentStreak by statsViewModel.currentStreak.collectAsStateWithLifecycle()

                HomeScreen(
                    isGujarati = isGujarati,
                    todayStats = todayStats,
                    streakDays = currentStreak,
                    target = preferences.defaultTarget,
                    onStartJapaClick = { mantraId ->
                        navController.navigate(Screen.Counter.createRoute(mantraId))
                    },
                    onNavigateToMantras = {
                        navController.navigate(Screen.Mantras.route)
                    },
                    onNavigateToHistory = {
                        navController.navigate(Screen.History.route)
                    },
                    onNavigateToStats = {
                        navController.navigate(Screen.Stats.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            // Mantras List Screen
            composable(Screen.Mantras.route) {
                val mantras by mantraViewModel.filteredMantras.collectAsStateWithLifecycle()
                val filter by mantraViewModel.selectedFilter.collectAsStateWithLifecycle()

                MantraListScreen(
                    isGujarati = isGujarati,
                    mantras = mantras,
                    selectedFilter = filter,
                    onFilterChange = { mantraViewModel.setFilter(it) },
                    onMantraSelect = { mantraId ->
                        navController.navigate(Screen.Counter.createRoute(mantraId))
                    },
                    onToggleFavorite = { mantraViewModel.toggleFavorite(it) },
                    onAddMantra = { name, transliteration, meaning, deity ->
                        mantraViewModel.addCustomMantra(name, transliteration, meaning, deity)
                    },
                    onUpdateMantra = { mantraViewModel.updateMantra(it) },
                    onDeleteMantra = { mantraViewModel.deleteCustomMantra(it) }
                )
            }

            // Japa Counter Screen (Full Screen)
            composable(
                route = Screen.Counter.route,
                arguments = listOf(navArgument("mantraId") { type = NavType.LongType })
            ) { backStackEntry ->
                val mantraId = backStackEntry.arguments?.getLong("mantraId") ?: 1L

                LaunchedEffect(mantraId) {
                    counterViewModel.loadMantra(mantraId)
                }

                val currentMantra by counterViewModel.currentMantra.collectAsStateWithLifecycle()
                val count by counterViewModel.count.collectAsStateWithLifecycle()
                val target by counterViewModel.target.collectAsStateWithLifecycle()
                val isPaused by counterViewModel.isPaused.collectAsStateWithLifecycle()
                val elapsedSeconds by counterViewModel.elapsedSeconds.collectAsStateWithLifecycle()

                // Listen for completion
                LaunchedEffect(Unit) {
                    counterViewModel.sessionCompleted.collect { completedCount ->
                        counterViewModel.saveSession {
                            navController.navigate(
                                Screen.Completion.createRoute(mantraId, completedCount, elapsedSeconds)
                            ) {
                                popUpTo(Screen.Counter.route) { inclusive = true }
                            }
                        }
                    }
                }

                JapaCounterScreen(
                    isGujarati = isGujarati,
                    mantra = currentMantra,
                    count = count,
                    target = target,
                    isPaused = isPaused,
                    elapsedSeconds = elapsedSeconds,
                    keepScreenAwake = preferences.keepScreenAwake,
                    hapticEnabled = preferences.hapticEnabled,
                    soundEnabled = preferences.soundEnabled,
                    onTap = { counterViewModel.onTap() },
                    onUndo = { counterViewModel.onUndo() },
                    onReset = { counterViewModel.onReset() },
                    onTogglePause = { counterViewModel.togglePause() },
                    onSetTarget = { counterViewModel.setTarget(it) },
                    onSaveAndExit = {
                        counterViewModel.saveSession {
                            statsViewModel.refreshStats()
                            navController.popBackStack()
                        }
                    },
                    onBackClick = {
                        counterViewModel.saveSession {
                            statsViewModel.refreshStats()
                            navController.popBackStack()
                        }
                    }
                )
            }

            // Completion Screen
            composable(
                route = Screen.Completion.route,
                arguments = listOf(
                    navArgument("mantraId") { type = NavType.LongType },
                    navArgument("count") { type = NavType.IntType },
                    navArgument("durationSeconds") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val mantraId = backStackEntry.arguments?.getLong("mantraId") ?: 1L
                val count = backStackEntry.arguments?.getInt("count") ?: 108
                val duration = backStackEntry.arguments?.getLong("durationSeconds") ?: 0L

                val currentMantra by counterViewModel.currentMantra.collectAsStateWithLifecycle()

                CompletionScreen(
                    isGujarati = isGujarati,
                    mantraName = currentMantra?.name ?: "ૐ નમઃ શિવાય",
                    completedCount = count,
                    durationSeconds = duration,
                    onChantAgain = {
                        navController.navigate(Screen.Counter.createRoute(mantraId)) {
                            popUpTo(Screen.Completion.route) { inclusive = true }
                        }
                    },
                    onSelectAnotherMantra = {
                        navController.navigate(Screen.Mantras.route) {
                            popUpTo(Screen.Completion.route) { inclusive = true }
                        }
                    },
                    onGoHome = {
                        statsViewModel.refreshStats()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            // History Screen
            composable(Screen.History.route) {
                val sessions by historyViewModel.filteredSessions.collectAsStateWithLifecycle()
                val filter by historyViewModel.selectedFilter.collectAsStateWithLifecycle()

                HistoryScreen(
                    isGujarati = isGujarati,
                    sessions = sessions,
                    selectedFilter = filter,
                    onFilterChange = { historyViewModel.setFilter(it) },
                    onDeleteSession = {
                        historyViewModel.deleteSession(it)
                        statsViewModel.refreshStats()
                    },
                    onClearAll = {
                        historyViewModel.clearAllHistory()
                        statsViewModel.refreshStats()
                    }
                )
            }

            // Stats Screen
            composable(Screen.Stats.route) {
                val todayStats by statsViewModel.todayStats.collectAsStateWithLifecycle()
                val sevenDaysTotal by statsViewModel.sevenDaysTotal.collectAsStateWithLifecycle()
                val thirtyDaysTotal by statsViewModel.thirtyDaysTotal.collectAsStateWithLifecycle()
                val lifetimeTotal by statsViewModel.lifetimeTotal.collectAsStateWithLifecycle()
                val completed108Rounds by statsViewModel.completed108Rounds.collectAsStateWithLifecycle()
                val mostUsedMantra by statsViewModel.mostUsedMantra.collectAsStateWithLifecycle()
                val currentStreak by statsViewModel.currentStreak.collectAsStateWithLifecycle()
                val longestStreak by statsViewModel.longestStreak.collectAsStateWithLifecycle()
                val last7DaysBars by statsViewModel.last7DaysBars.collectAsStateWithLifecycle()

                StatsScreen(
                    isGujarati = isGujarati,
                    todayStats = todayStats,
                    sevenDaysTotal = sevenDaysTotal,
                    thirtyDaysTotal = thirtyDaysTotal,
                    lifetimeTotal = lifetimeTotal,
                    completed108Rounds = completed108Rounds,
                    mostUsedMantra = mostUsedMantra,
                    currentStreak = currentStreak,
                    longestStreak = longestStreak,
                    last7DaysBars = last7DaysBars,
                    onRefresh = { statsViewModel.refreshStats() }
                )
            }

            // Settings Screen
            composable(Screen.Settings.route) {
                SettingsScreen(
                    isGujarati = isGujarati,
                    preferences = preferences,
                    onLanguageChange = { settingsViewModel.setLanguage(it) },
                    onThemeModeChange = { settingsViewModel.setThemeMode(it) },
                    onHapticChange = { settingsViewModel.setHapticEnabled(it) },
                    onSoundChange = { settingsViewModel.setSoundEnabled(it) },
                    onKeepAwakeChange = { settingsViewModel.setKeepScreenAwake(it) },
                    onDefaultTargetChange = { settingsViewModel.setDefaultTarget(it) },
                    onResetAllData = { onComplete ->
                        settingsViewModel.resetAllData {
                            statsViewModel.refreshStats()
                            onComplete()
                        }
                    }
                )
            }
        }
    }
}
