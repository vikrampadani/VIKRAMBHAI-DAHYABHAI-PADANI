package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class SoundHapticHelper(private val context: Context) {

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
        } catch (_: Exception) {
            toneGenerator = null
        }
    }

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun playTapFeedback(hapticEnabled: Boolean, soundEnabled: Boolean) {
        if (hapticEnabled) {
            triggerTapVibration()
        }
        if (soundEnabled) {
            playTapSound()
        }
    }

    fun playCompletionFeedback(hapticEnabled: Boolean, soundEnabled: Boolean) {
        if (hapticEnabled) {
            triggerCompletionVibration()
        }
        if (soundEnabled) {
            playCompletionSound()
        }
    }

    private fun triggerTapVibration() {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    vibrator?.vibrate(effect)
                } else {
                    val effect = VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE)
                    vibrator?.vibrate(effect)
                }
            }
        } catch (_: Exception) {}
    }

    private fun triggerCompletionVibration() {
        try {
            if (vibrator?.hasVibrator() == true) {
                // Two peaceful pulses for target reached
                val timings = longArrayOf(0, 100, 80, 150)
                val amplitudes = intArrayOf(0, 200, 0, 255)
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                vibrator?.vibrate(effect)
            }
        } catch (_: Exception) {}
    }

    private fun playTapSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
        } catch (_: Exception) {}
    }

    private fun playCompletionSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 250)
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
