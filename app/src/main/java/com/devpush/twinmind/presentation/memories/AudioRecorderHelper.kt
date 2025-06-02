package com.devpush.twinmind.presentation.memories

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

class AudioRecorderHelper(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startRecording(file: File) {
        if (mediaRecorder != null) {
            // Already recording or not properly stopped
            stopRecording() // Try to stop just in case
        }

        outputFile = file
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        mediaRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4) // M4A format
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)

            try {
                prepare()
                start()
            } catch (e: IOException) {
                // Handle prepare/start error (e.g., log, notify UI)
                mediaRecorder?.release() // Clean up
                mediaRecorder = null
                outputFile = null
                // Optionally re-throw or handle as a specific state
            } catch (e: IllegalStateException) {
                // Handle case where start() is called in an invalid state
                mediaRecorder?.release()
                mediaRecorder = null
                outputFile = null
            }
        }
    }

    fun stopRecording() {
        mediaRecorder?.apply {
            try {
                stop()
            } catch (e: IllegalStateException) {
                // Handle case where stop() is called in an invalid state (e.g., not recording)
                // May not be critical, but good to be aware of
            }
            release()
        }
        mediaRecorder = null
        // outputFile remains as is, as it points to the completed recording
    }

    // Optional: A method to get the current output file path
    fun getCurrentOutputFile(): File? = outputFile

    // Optional: A method to check if currently recording
    fun isRecording(): Boolean = mediaRecorder != null
}
