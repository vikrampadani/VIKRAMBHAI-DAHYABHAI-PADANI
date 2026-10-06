package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.HaldiYellow
import com.example.ui.theme.RudrakshaBrown
import com.example.ui.theme.SacredAmber
import com.example.ui.theme.SacredGold
import com.example.ui.theme.SaffronPrimary
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun MalaProgressIndicator(
    progress: Float, // 0.0f to 1.0f
    target: Int,
    currentCount: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 200),
        label = "mala_progress"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val diameter = min(size.width, size.height) - strokeWidth
            val radius = diameter / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Background Track Circle
            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Progress Arc
            val sweepAngle = animatedProgress * 360f
            if (sweepAngle > 0f) {
                drawArc(
                    color = primaryColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Draw Mala Bead markers
            // If target is 108 or 54 or 27, draw 27 or 54 subtle bead marks
            val beadCount = when {
                target <= 27 -> target
                target == 54 -> 27
                target == 108 -> 36 // 36 visual markers for 108 (every 3 chants)
                else -> 27
            }

            val beadRadius = 3.dp.toPx()
            for (i in 0 until beadCount) {
                val angleDeg = -90f + (i * 360f / beadCount)
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val bx = center.x + (radius * cos(angleRad)).toFloat()
                val by = center.y + (radius * sin(angleRad)).toFloat()

                val beadFraction = i.toFloat() / beadCount
                val isChanted = beadFraction <= animatedProgress

                drawCircle(
                    color = if (isChanted) HaldiYellow else trackColor.copy(alpha = 0.8f),
                    radius = beadRadius,
                    center = Offset(bx, by)
                )
            }

            // Sumeru bead marker at the very top (angle -90)
            val sumeruX = center.x
            val sumeruY = center.y - radius
            drawCircle(
                color = if (animatedProgress >= 1f) SacredGold else primaryColor,
                radius = 7.dp.toPx(),
                center = Offset(sumeruX, sumeruY)
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = Offset(sumeruX, sumeruY)
            )
        }

        // Center content (Large count display)
        content()
    }
}
