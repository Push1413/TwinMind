package com.devpush.twinmind.presentation.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

/**
 * A Composable function that displays a CircularWavyProgressIndicator with an optional icon centered inside.
 *
 * @param progress A lambda that returns the current progress (0.0 to 1.0).
 * @param modifier The modifier to be applied to the outer Box containing the indicator and icon.
 * @param indicatorSize The size of the CircularWavyProgressIndicator.
 * @param icon Optional: The ImageVector for the icon to be displayed inside.
 * @param iconSize The size of the icon.
 * @param iconTint The tint color for the icon. Defaults to LocalContentColor.
 * @param indicatorColor The color of the progress wave. Defaults to MaterialTheme.colorScheme.primary.
 * @param trackColor The color of the track behind the progress wave. Defaults to MaterialTheme.colorScheme.surfaceVariant.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CircularWavyProgressIndicatorWithIcon(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    indicatorSize: Dp = 82.dp,
    icon: ImageVector,
    iconSize: Dp = 40.dp,
    iconTint: Color = LocalContentColor.current,
    onIconClick: (() -> Unit),
    iconContentDescription: String? = null,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Box(
        modifier = modifier, // Apply the passed modifier to the outer Box
        contentAlignment = Alignment.Center
    ) {
        CircularWavyProgressIndicator(
            progress = progress,
            modifier = Modifier.size(indicatorSize),
            color = indicatorColor,
            trackColor = trackColor
        )

        val iconModifier = Modifier
            .size(iconSize)
            .clip(MaterialTheme.shapes.small)
            .clickable(
                onClick = onIconClick,
                role = Role.Button,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true)
            )

        Icon(
            imageVector = icon,
            contentDescription = iconContentDescription,
            modifier = iconModifier,
            tint = iconTint
        )

    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CircularWavyProgressIndicatorWithTimer(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    indicatorSize: Dp = 82.dp,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    timerText: String, // Timer text is now a required parameter
    timerTextStyle: TextStyle = MaterialTheme.typography.labelMedium,
    timerTextColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularWavyProgressIndicator(
            progress = progress,
            modifier = Modifier.size(indicatorSize),
            color = indicatorColor,
            trackColor = trackColor
        )
        Text(
            text = timerText,
            style = timerTextStyle,
            color = timerTextColor,
            modifier = Modifier
                .align(Alignment.Center)
        )
    }
}

@Composable
fun CountdownProgressIndicator(
    modifier: Modifier = Modifier,
    totalDurationInSeconds: Long,
    isPlaying: Boolean,
    onTimerFinish: () -> Unit,
    indicatorSize: Dp = 82.dp,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    timerTextStyle: TextStyle = MaterialTheme.typography.labelMedium,
    timerTextColor: Color = MaterialTheme.colorScheme.onSurface
) {

    var remainingTimeInSeconds by remember { mutableStateOf(totalDurationInSeconds) }
    var currentProgress by remember { mutableStateOf(1f) }

    // Reset remaining time if totalDuration changes or if not playing and it should reset
    LaunchedEffect(totalDurationInSeconds, isPlaying) {
        if (!isPlaying) {
            remainingTimeInSeconds = totalDurationInSeconds
            currentProgress = 1f
        } else {
            // If isPlaying becomes true and remainingTime was 0 (or less), reset to full duration
            if (remainingTimeInSeconds <= 0 && totalDurationInSeconds > 0) {
                remainingTimeInSeconds = totalDurationInSeconds
            }
        }
    }

    LaunchedEffect(key1 = remainingTimeInSeconds, key2 = isPlaying) {
        if (isPlaying && remainingTimeInSeconds > 0) {
            delay(1000L) // Wait for 1 second
            remainingTimeInSeconds--
        } else if (isPlaying && remainingTimeInSeconds <= 0) {
            onTimerFinish()
        }
    }

    // Calculate progress based on remaining time
    // Ensure totalDurationInSeconds is not zero to avoid division by zero
    currentProgress = if (totalDurationInSeconds > 0) {
        (remainingTimeInSeconds.toFloat() / totalDurationInSeconds.toFloat()).coerceIn(0f, 1f)
    } else {
        1f // Default to full progress if duration is 0
    }

    val formattedTime = remember(remainingTimeInSeconds) {
        val minutes = TimeUnit.SECONDS.toMinutes(remainingTimeInSeconds)
        val seconds = remainingTimeInSeconds - TimeUnit.MINUTES.toSeconds(minutes)
        String.format("%02d:%02d", minutes, seconds)
    }

    CircularWavyProgressIndicatorWithTimer(
        progress = { currentProgress },
        timerText = formattedTime,
        modifier = modifier,
        indicatorSize = indicatorSize,
        indicatorColor = indicatorColor,
        trackColor = trackColor,
        timerTextStyle = timerTextStyle,
        timerTextColor = timerTextColor
    )

}
