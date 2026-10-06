package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.JapaSessionEntity
import com.example.data.repository.JapaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

enum class HistoryFilter {
    TODAY, YESTERDAY, THIS_WEEK, THIS_MONTH, ALL
}

class HistoryViewModel(private val repository: JapaRepository) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(HistoryFilter.ALL)
    val selectedFilter: StateFlow<HistoryFilter> = _selectedFilter

    val allSessions: StateFlow<List<JapaSessionEntity>> = repository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredSessions: StateFlow<List<JapaSessionEntity>> = combine(allSessions, _selectedFilter) { sessions, filter ->
        val now = LocalDate.now()
        val zone = ZoneId.systemDefault()

        when (filter) {
            HistoryFilter.ALL -> sessions
            HistoryFilter.TODAY -> {
                val startOfDay = now.atStartOfDay(zone).toInstant().toEpochMilli()
                sessions.filter { it.completedAt >= startOfDay }
            }
            HistoryFilter.YESTERDAY -> {
                val yesterday = now.minusDays(1)
                val startOfYesterday = yesterday.atStartOfDay(zone).toInstant().toEpochMilli()
                val endOfYesterday = now.atStartOfDay(zone).toInstant().toEpochMilli()
                sessions.filter { it.completedAt in startOfYesterday until endOfYesterday }
            }
            HistoryFilter.THIS_WEEK -> {
                val startOfWeek = now.minusDays(7).atStartOfDay(zone).toInstant().toEpochMilli()
                sessions.filter { it.completedAt >= startOfWeek }
            }
            HistoryFilter.THIS_MONTH -> {
                val startOfMonth = now.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
                sessions.filter { it.completedAt >= startOfMonth }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setFilter(filter: HistoryFilter) {
        _selectedFilter.value = filter
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch {
            repository.deleteSession(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }

    companion object {
        fun provideFactory(repository: JapaRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HistoryViewModel(repository) as T
                }
            }
    }
}
