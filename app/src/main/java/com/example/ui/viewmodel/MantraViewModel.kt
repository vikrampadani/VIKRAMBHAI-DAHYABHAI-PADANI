package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.MantraEntity
import com.example.data.repository.MantraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MantraViewModel(private val repository: MantraRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.ensureDefaultsLoaded()
        }
    }

    private val _selectedFilter = MutableStateFlow("ALL") // "ALL", "FAVORITE", or deity name
    val selectedFilter: StateFlow<String> = _selectedFilter

    val allMantras: StateFlow<List<MantraEntity>> = repository.allMantras
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteMantras: StateFlow<List<MantraEntity>> = repository.favoriteMantras
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredMantras: StateFlow<List<MantraEntity>> = combine(allMantras, _selectedFilter) { list, filter ->
        when (filter) {
            "FAVORITE" -> list.filter { it.isFavorite }
            "ALL" -> list
            else -> list.filter { it.deity.contains(filter, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun toggleFavorite(mantra: MantraEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(mantra.id, mantra.isFavorite)
        }
    }

    fun addCustomMantra(name: String, transliteration: String, meaning: String, deity: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertMantra(name, transliteration, meaning, deity)
        }
    }

    fun updateMantra(mantra: MantraEntity) {
        viewModelScope.launch {
            repository.updateMantra(mantra)
        }
    }

    fun deleteCustomMantra(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomMantra(id)
        }
    }

    fun resetDefaults() {
        viewModelScope.launch {
            repository.resetDefaults()
        }
    }

    companion object {
        fun provideFactory(repository: MantraRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MantraViewModel(repository) as T
                }
            }
    }
}
