package com.devpush.twinmind.presentation.memories

import android.Manifest
import android.R.attr.strokeWidth
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.devpush.twinmind.R
import com.devpush.twinmind.data.local.RecordingItem
import com.devpush.twinmind.presentation.utils.CircularWavyProgressIndicatorWithIcon
import com.devpush.twinmind.presentation.utils.CountdownProgressIndicator
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MemoriesScreen(
    viewModel: MemoriesViewModel = koinViewModel()
) {
    val isRecording by viewModel.isRecording.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val audioDurationSec by viewModel.currentAudioTotalDuration.collectAsState()
    val recordings by viewModel.recordingsList.collectAsState()
    val requestAudioPermission by viewModel.requestAudioPermission.collectAsState()

    var progress by remember { mutableStateOf(0f) }
    var timerSeconds by remember { mutableStateOf(0) }

    // Animate progress (used for smoother visual)
    val animatedProgress by animateFloatAsState(targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "Progress")

    // Effect to increment progress only when recording
    LaunchedEffect(isRecording) {
        if (isRecording) {
            progress = 0f
            timerSeconds = 0
            while (timerSeconds < 30 && isRecording) {
                delay(1000)
                timerSeconds++
                progress = timerSeconds / 30f
            }
            viewModel.onRecordButtonPressed() // Auto stop after 30s
        } else {
            progress = 0f
            timerSeconds = 0
        }
    }


    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.onAudioPermissionGranted()
            } else {
                viewModel.onAudioPermissionDenied()
            }
        }
    )


    LaunchedEffect(requestAudioPermission) {
        if (requestAudioPermission) {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.onRecordButtonPressed() }) {
                Icon(
                    imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                    contentDescription = if (isRecording) "Stop Recording" else "Start Recording"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isRecording){
                CircularWavyProgressIndicatorWithIcon(
                    progress = { animatedProgress },
                    modifier = Modifier.size(100.dp),
                    indicatorSize = 82.dp,
                    icon = Icons.Filled.Pause,
                    iconSize = 40.dp,
                    onIconClick = {
                        viewModel.onRecordButtonPressed()
                    },
                    iconContentDescription = "Stop Action",
                    iconTint = MaterialTheme.colorScheme.primary
                )
            }
            if (isPlaying &&  audioDurationSec > 0L){
                CountdownProgressIndicator(
                    totalDurationInSeconds = audioDurationSec,
                    isPlaying = isPlaying,
                    onTimerFinish = {
                        viewModel.stopPlaying()
                    },
                    modifier = Modifier.size(120.dp),
                    indicatorSize = 100.dp,
                    indicatorColor = MaterialTheme.colorScheme.secondary,
                )
            }
            if (recordings.isEmpty()) {
                Text(stringResource(R.string.no_recordings_yet_tap_the_mic_to_start))
            } else if (!isRecording && !isPlaying) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(recordings) { recordingItem ->
                        RecordingListItem(
                            isRecordingOn = isRecording,
                            recordingItem = recordingItem,
                            onPlayClick = { viewModel.playRecording(recordingItem) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun RecordingListItem(isRecordingOn: Boolean, recordingItem: RecordingItem, onPlayClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(
                    R.string.rec, SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(Date(recordingItem.timestamp))
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(
                    R.string.duration,
                    formatDuration(recordingItem.durationMillis)
                ),
                style = MaterialTheme.typography.bodySmall
            )
        }
        IconButton(onClick = onPlayClick) {
            Icon(imageVector = if (isRecordingOn) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                 contentDescription = if (isRecordingOn) "Stop Recording" else "Start Recording")
        }
    }
}

fun formatDuration(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%02d:%02d", minutes, seconds)
}
