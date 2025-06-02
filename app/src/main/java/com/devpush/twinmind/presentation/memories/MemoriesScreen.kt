package com.devpush.twinmind.presentation.memories

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.devpush.twinmind.data.local.RecordingItem
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoriesScreen(
    viewModel: MemoriesViewModel = koinViewModel()
) {
    val isRecording by viewModel.isRecording.collectAsState()
    val recordings by viewModel.recordingsList.collectAsState()
    val requestAudioPermission by viewModel.requestAudioPermission.collectAsState() // Collect permission request state

    // Permission Launcher
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

    // Effect to launch permission request when needed
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
            if (recordings.isEmpty()) {
                Text("No recordings yet. Tap the mic to start.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(recordings) { recordingItem ->
                        RecordingListItem(
                            recordingItem = recordingItem,
                            onPlayClick = { viewModel.playRecording(recordingItem) }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun RecordingListItem(recordingItem: RecordingItem, onPlayClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Rec: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(recordingItem.timestamp))}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Duration: ${formatDuration(recordingItem.durationMillis)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
        IconButton(onClick = onPlayClick) {
            Icon(Icons.Filled.PlayArrow, contentDescription = "Play")
        }
    }
}

fun formatDuration(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%02d:%02d", minutes, seconds)
}
