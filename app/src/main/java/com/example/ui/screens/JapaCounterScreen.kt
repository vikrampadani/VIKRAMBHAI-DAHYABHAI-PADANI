package com.example.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MantraEntity
import com.example.ui.components.MalaProgressIndicator
import com.example.util.AppStrings
import kotlinx.coroutines.launch

@Composable
fun JapaCounterScreen(
    isGujarati: Boolean,
    mantra: MantraEntity?,
    count: Int,
    target: Int,
    isPaused: Boolean,
    elapsedSeconds: Long,
    keepScreenAwake: Boolean,
    hapticEnabled: Boolean,
    soundEnabled: Boolean,
    onTap: () -> Unit,
    onUndo: () -> Unit,
    onReset: () -> Unit,
    onTogglePause: () -> Unit,
    onSetTarget: (Int) -> Unit,
    onSaveAndExit: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val buttonScale = remember { Animatable(1f) }

    // Intercept hardware back button to save session before exiting
    BackHandler {
        onSaveAndExit()
    }

    // Keep screen awake flag
    DisposableEffect(keepScreenAwake) {
        val window = (context as? Activity)?.window
        if (keepScreenAwake) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val targetOptions = listOf(11, 21, 27, 54, 108, 1008)
    val progress = if (target > 0) (count.toFloat() / target).coerceIn(0f, 1f) else 0f
    val malaRounds = if (target == 108) count / 108 else count / target
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Back & Status Indicators
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onSaveAndExit,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("counter_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Timer and Mala rounds chip
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⏱ $timeFormatted",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (malaRounds > 0) {
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "📿 $malaRounds ${AppStrings.get(AppStrings.Key.MALA_ROUNDS, isGujarati)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Save and Exit button
                    IconButton(
                        onClick = onSaveAndExit,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("counter_save_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Save and Exit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Mantra Title - Large, bold, spiritual
                Text(
                    text = mantra?.name ?: "ૐ નમઃ શિવાય",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = 34.sp
                )

                if (!mantra?.meaning.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = mantra?.meaning ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Target Selector Chips: 11, 21, 27, 54, 108, 1008
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    items(targetOptions) { opt ->
                        FilterChip(
                            selected = target == opt,
                            onClick = { onSetTarget(opt) },
                            label = {
                                Text(
                                    text = "$opt",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (target == opt) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .testTag("target_chip_$opt")
                        )
                    }
                }
            }

            // Center: Circular Mala Counter with Big Count Display
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        coroutineScope.launch {
                            buttonScale.animateTo(0.96f, tween(50))
                            buttonScale.animateTo(1f, tween(80))
                        }
                        onTap()
                    },
                contentAlignment = Alignment.Center
            ) {
                MalaProgressIndicator(
                    progress = progress,
                    target = target,
                    currentCount = count,
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(buttonScale.value)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$count",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 64.sp,
                            lineHeight = 64.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${AppStrings.get(AppStrings.Key.TARGET_LABEL, isGujarati)} $target",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Big Circular Tap Button: "જપ કરો" (TAP TO CHANT)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(buttonScale.value)
                        .clip(CircleShape)
                        .clickable {
                            coroutineScope.launch {
                                buttonScale.animateTo(0.92f, tween(50))
                                buttonScale.animateTo(1f, tween(80))
                            }
                            onTap()
                        }
                        .testTag("counter_tap_button"),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🕉️",
                                fontSize = 32.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = AppStrings.get(AppStrings.Key.TAP_TO_CHANT, isGujarati),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Bottom Action Buttons: Undo, Reset, Pause/Resume, Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Undo Button
                    IconButtonWithLabel(
                        icon = Icons.Default.Undo,
                        label = AppStrings.get(AppStrings.Key.UNDO, isGujarati),
                        onClick = onUndo,
                        enabled = count > 0,
                        tag = "btn_undo"
                    )

                    // Pause / Resume Button
                    IconButtonWithLabel(
                        icon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        label = if (isPaused) AppStrings.get(AppStrings.Key.RESUME, isGujarati) else AppStrings.get(AppStrings.Key.PAUSE, isGujarati),
                        onClick = onTogglePause,
                        tag = "btn_pause"
                    )

                    // Reset Button
                    IconButtonWithLabel(
                        icon = Icons.Default.Refresh,
                        label = AppStrings.get(AppStrings.Key.RESET, isGujarati),
                        onClick = onReset,
                        enabled = count > 0,
                        tag = "btn_reset"
                    )

                    // Finish / Save Button
                    IconButtonWithLabel(
                        icon = Icons.Default.Done,
                        label = AppStrings.get(AppStrings.Key.SAVE, isGujarati),
                        onClick = onSaveAndExit,
                        tag = "btn_finish"
                    )
                }
            }
        }
    }
}

@Composable
private fun IconButtonWithLabel(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    tag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(tag)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (enabled) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}
