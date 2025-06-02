package com.devpush.twinmind.presentation.memories

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpush.twinmind.data.local.RecordingDao
import com.devpush.twinmind.data.local.RecordingItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MemoriesViewModel(
    private val application: Application,
    private val recordingDao: RecordingDao,
    private val audioRecorderHelper: AudioRecorderHelper,
    private val audioPlayerHelper: AudioPlayerHelper
) : ViewModel() {

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentAudioTotalDuration = MutableStateFlow(0L)
    val currentAudioTotalDuration: StateFlow<Long> = _currentAudioTotalDuration.asStateFlow()


    private val _recordingsList = MutableStateFlow<List<RecordingItem>>(emptyList())
    val recordingsList: StateFlow<List<RecordingItem>> = _recordingsList.asStateFlow()

    private val _requestAudioPermission = MutableStateFlow(false)
    val requestAudioPermission: StateFlow<Boolean> = _requestAudioPermission.asStateFlow()

    private var currentRecordingFile: File? = null
    private var currentRecordingStartTime: Long = 0
    private var recordingChunkJob: Job? = null

    init {
        recordingDao.getAllRecordings()
            .onEach { _recordingsList.value = it }
            .launchIn(viewModelScope)
    }

    fun onRecordButtonPressed() {
        if (_isRecording.value) {
            stopActualRecording()
        } else {
            if (hasAudioPermission()) {
                startActualRecording()
            } else {
                _requestAudioPermission.value = true
            }
        }
    }

    private fun startActualRecording() {
        currentRecordingFile = createNewAudioFile()
        currentRecordingStartTime = System.currentTimeMillis()
        currentRecordingFile?.let {
            audioRecorderHelper.startRecording(it)
            _isRecording.value = true
            // Start 30-second chunking
            recordingChunkJob?.cancel() // Cancel previous if any
            recordingChunkJob = viewModelScope.launch {
                while (_isRecording.value) {
                    delay(30_000)
                    if (_isRecording.value) { // Re-check after delay
                        // This part is tricky: MediaRecorder typically finalizes on stop.
                        // To save a "chunk" and continue, we need to stop, save, and restart.
                        val chunkFile = currentRecordingFile
                        val chunkStartTime = currentRecordingStartTime
                        val chunkDuration = System.currentTimeMillis() - chunkStartTime

                        // Stop current recording (helper should not release if we plan to restart immediately)
                        // For simplicity, we'll stop and then start a new one.
                        audioRecorderHelper.stopRecording() // Finalizes the current file

                        if (chunkFile != null && chunkFile.exists() && chunkFile.length() > 0) {
                            val recordingItem = RecordingItem(
                                filePath = chunkFile.absolutePath,
                                timestamp = chunkStartTime,
                                durationMillis = chunkDuration
                            )
                            viewModelScope.launch { recordingDao.insert(recordingItem) }
                        }

                        // Start new recording for the next chunk
                        if (_isRecording.value) { // Ensure we still want to record
                            currentRecordingFile = createNewAudioFile()
                            currentRecordingStartTime = System.currentTimeMillis()
                            currentRecordingFile?.let { newFile ->
                                audioRecorderHelper.startRecording(newFile)
                            } ?: run {
                                // Failed to create new file, stop recording process
                                _isRecording.value = false
                            }
                        }
                    }
                }
            }
        }
    }

    private fun stopActualRecording() {
        recordingChunkJob?.cancel()
        audioRecorderHelper.stopRecording()
        _isRecording.value = false

        val fileToSave = currentRecordingFile
        val startTimeToSave = currentRecordingStartTime
        if (fileToSave != null && fileToSave.exists() && fileToSave.length() > 0) {
            val duration = System.currentTimeMillis() - startTimeToSave
            val recordingItem = RecordingItem(
                filePath = fileToSave.absolutePath,
                timestamp = startTimeToSave,
                durationMillis = duration
            )
            viewModelScope.launch { recordingDao.insert(recordingItem) }
        }
        currentRecordingFile = null
        currentRecordingStartTime = 0
    }

    fun onAudioPermissionGranted() {
        _requestAudioPermission.value = false // Reset trigger
        if (!_isRecording.value) { // Only start if not already recording
            startActualRecording()
        }
    }

    fun onAudioPermissionDenied() {
        _requestAudioPermission.value = false // Reset trigger
        // Optionally, set a message to show to the user
    }

    fun playRecording(recordingItem: RecordingItem) {
        audioPlayerHelper.play(recordingItem.filePath)
        _currentAudioTotalDuration.value = recordingItem.durationMillis/1000
        _isPlaying.value = true
    }
    fun stopPlaying() {
        audioPlayerHelper.stop()
        _isPlaying.value = false
        _currentAudioTotalDuration.value = 0L
    }

    private fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createNewAudioFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault()).format(Date())
        val storageDir = application.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File.createTempFile("REC_${timeStamp}_", ".m4a", storageDir).also {
            currentRecordingFile = it
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioRecorderHelper.stopRecording()
        audioPlayerHelper.release()
        recordingChunkJob?.cancel()
    }
}
