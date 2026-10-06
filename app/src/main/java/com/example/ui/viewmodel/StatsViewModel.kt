package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.DailyStatsEntity
import com.example.data.repository.JapaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class DailyBarData(
    val dayLabel: String,
    val count: Int,
    val isToday: Boolean
)

class StatsViewModel(private val repository: JapaRepository) : ViewModel() {

    val lifetimeTotal: StateFlow<Int> = repository.lifetimeTotalCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val lifetimeSessions: StateFlow<Int> = repository.lifetimeSessionCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val completed108Rounds: StateFlow<Int> = repository.completed108RoundsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val mostUsedMantra: StateFlow<String?> = repository.mostUsedMantra
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayStats: StateFlow<DailyStatsEntity?> = repository.getTodayStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private val _longestStreak = MutableStateFlow(0)
    val longestStreak: StateFlow<Int> = _longestStreak.asStateFlow()

    private val _sevenDaysTotal = MutableStateFlow(0)
    val sevenDaysTotal: StateFlow<Int> = _sevenDaysTotal.asStateFlow()

    private val _thirtyDaysTotal = MutableStateFlow(0)
    val thirtyDaysTotal: StateFlow<Int> = _thirtyDaysTotal.asStateFlow()

    private val _last7DaysBars = MutableStateFlow<List<DailyBarData>>(emptyList())
    val last7DaysBars: StateFlow<List<DailyBarData>> = _last7DaysBars.asStateFlow()

    init {
        refreshStats()
    }

    fun refreshStats() {
        viewModelScope.launch {
            _currentStreak.value = repository.calculateCurrentStreak()
            _longestStreak.value = repository.calculateLongestStreak()

            val now = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            // Calculate last 7 days bars
            val daysList = mutableListOf<DailyBarData>()
            var sum7Days = 0
            for (i in 6 downTo 0) {
                val date = now.minusDays(i.toLong())
                val dateStr = date.format(formatter)
                val dayName = when (date.dayOfWeek.value) {
                    1 -> "સોમ"
                    2 -> "મંગળ"
                    3 -> "બુધ"
                    4 -> "ગુરુ"
                    5 -> "શુક્ર"
                    6 -> "શનિ"
                    7 -> "રવિ"
                    else -> ""
                }
                // Fetch stats from dailyStatsDao via repository
                val count = repository.getTodayDateString().let {
                    // For the loop
                    0
                }
                daysList.add(DailyBarData(dayName, 0, i == 0))
            }

            // Observe recent 7 days stats
            repository.getRecent7DaysStats().collect { statsList ->
                val statsMap = statsList.associateBy { it.date }
                var sum7 = 0
                val bars = mutableListOf<DailyBarData>()
                for (i in 6 downTo 0) {
                    val date = now.minusDays(i.toLong())
                    val dateStr = date.format(formatter)
                    val stat = statsMap[dateStr]
                    val count = stat?.totalCount ?: 0
                    sum7 += count

                    val dayName = when (date.dayOfWeek.value) {
                        1 -> "સોમ"
                        2 -> "મંગળ"
                        3 -> "બુધ"
                        4 -> "ગુરુ"
                        5 -> "શુક્ર"
                        6 -> "શનિ"
                        7 -> "રવિ"
                        else -> ""
                    }
                    bars.add(DailyBarData(dayName, count, i == 0))
                }
                _sevenDaysTotal.value = sum7
                _last7DaysBars.value = bars
            }
        }

        viewModelScope.launch {
            repository.getRecent30DaysStats().collect { list ->
                _thirtyDaysTotal.value = list.sumOf { it.totalCount }
            }
        }
    }

    companion object {
        fun provideFactory(repository: JapaRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StatsViewModel(repository) as T
                }
            }
    }
}
