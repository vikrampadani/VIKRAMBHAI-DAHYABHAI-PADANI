package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.MantraEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.JapaRepository
import com.example.data.repository.MantraRepository
import com.example.util.SoundHapticHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JapaCounterViewModel(
    private val mantraRepository: MantraRepository,
    private val japaRepository: JapaRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val soundHapticHelper: SoundHapticHelper
) : ViewModel() {

    private val _currentMantra = MutableStateFlow<MantraEntity?>(null)
    val currentMantra: StateFlow<MantraEntity?> = _currentMantra.asStateFlow()

    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()

    private val _target = MutableStateFlow(108)
    val target: StateFlow<Int> = _target.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private val _sessionCompleted = MutableSharedFlow<Int>()
    val sessionCompleted: SharedFlow<Int> = _sessionCompleted.asSharedFlow()

    val preferences = userPreferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.example.data.preferences.UserPreferences()
        )

    private var timerJob: Job? = null
    private var lastTapTime = 0L
    private var startedAtTimestamp = System.currentTimeMillis()

    fun loadMantra(mantraId: Long) {
        viewModelScope.launch {
            val mantra = mantraRepository.getMantraByIdDirect(mantraId)
                ?: mantraRepository.getMantraByIdDirect(1)
            _currentMantra.value = mantra
            _count.value = 0
            _elapsedSeconds.value = 0L
            _isPaused.value = false
            startedAtTimestamp = System.currentTimeMillis()
            _target.value = preferences.value.defaultTarget
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (!_isPaused.value) {
                    _elapsedSeconds.value += 1
                }
            }
        }
    }

    fun onTap() {
        val now = System.currentTimeMillis()
        // Debounce to prevent accidental double taps (120ms)
        if (now - lastTapTime < 120) return
        lastTapTime = now

        if (_isPaused.value) {
            _isPaused.value = false
        }

        val newCount = _count.value + 1
        _count.value = newCount

        val currentTarget = _target.value
        val haptic = preferences.value.hapticEnabled
        val sound = preferences.value.soundEnabled

        if (newCount == currentTarget) {
            soundHapticHelper.playCompletionFeedback(haptic, sound)
            viewModelScope.launch {
                _sessionCompleted.emit(newCount)
            }
        } else {
            soundHapticHelper.playTapFeedback(haptic, sound)
        }
    }

    fun onUndo() {
        if (_count.value > 0) {
            _count.value -= 1
        }
    }

    fun onReset() {
        _count.value = 0
        _elapsedSeconds.value = 0L
        startedAtTimestamp = System.currentTimeMillis()
    }

    fun togglePause() {
        _isPaused.value = !_isPaused.value
    }

    fun setTarget(newTarget: Int) {
        _target.value = newTarget
    }

    fun saveSession(onSaved: (() -> Unit)? = null) {
        val current = _currentMantra.value ?: return
        val currentCount = _count.value
        val currentTarget = _target.value
        val durationMs = _elapsedSeconds.value * 1000

        if (currentCount <= 0) {
            onSaved?.invoke()
            return
        }

        viewModelScope.launch {
            japaRepository.recordSession(
                mantraId = current.id,
                mantraName = current.name,
                count = currentCount,
                target = currentTarget,
                durationMs = durationMs,
                startedAt = startedAtTimestamp,
                completedAt = System.currentTimeMillis(),
                isCompleted = currentCount >= currentTarget
            )
            onSaved?.invoke()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        soundHapticHelper.release()
    }

    companion object {
        fun provideFactory(
            mantraRepository: MantraRepository,
            japaRepository: JapaRepository,
            userPreferencesRepository: UserPreferencesRepository,
            soundHapticHelper: SoundHapticHelper
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return JapaCounterViewModel(
                    mantraRepository,
                    japaRepository,
                    userPreferencesRepository,
                    soundHapticHelper
                ) as T
            }
        }
    }
}
