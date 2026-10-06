package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.JapaRepository
import com.example.data.repository.MantraRepository
import com.example.ui.navigation.AppNavHost
import com.example.ui.theme.MantraJapTheme
import com.example.ui.viewmodel.HistoryViewModel
import com.example.ui.viewmodel.JapaCounterViewModel
import com.example.ui.viewmodel.MantraViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.StatsViewModel
import com.example.util.SoundHapticHelper

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val mantraDao = database.mantraDao()
        val sessionDao = database.japaSessionDao()
        val dailyStatsDao = database.dailyStatsDao()

        val preferencesRepository = UserPreferencesRepository(applicationContext)
        val soundHapticHelper = SoundHapticHelper(applicationContext)

        val mantraRepository = MantraRepository(mantraDao)
        val japaRepository = JapaRepository(sessionDao, dailyStatsDao)

        val mantraViewModel by viewModels<MantraViewModel> {
            MantraViewModel.provideFactory(mantraRepository)
        }

        val counterViewModel by viewModels<JapaCounterViewModel> {
            JapaCounterViewModel.provideFactory(
                mantraRepository,
                japaRepository,
                preferencesRepository,
                soundHapticHelper
            )
        }

        val historyViewModel by viewModels<HistoryViewModel> {
            HistoryViewModel.provideFactory(japaRepository)
        }

        val statsViewModel by viewModels<StatsViewModel> {
            StatsViewModel.provideFactory(japaRepository)
        }

        val settingsViewModel by viewModels<SettingsViewModel> {
            SettingsViewModel.provideFactory(
                preferencesRepository,
                mantraRepository,
                japaRepository
            )
        }

        setContent {
            val preferences by settingsViewModel.preferences.collectAsStateWithLifecycle()

            MantraJapTheme(themeMode = preferences.themeMode) {
                AppNavHost(
                    mantraViewModel = mantraViewModel,
                    counterViewModel = counterViewModel,
                    historyViewModel = historyViewModel,
                    statsViewModel = statsViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}
