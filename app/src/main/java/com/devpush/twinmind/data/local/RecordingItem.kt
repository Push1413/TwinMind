package com.devpush.twinmind.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordings")
data class RecordingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String,
    val timestamp: Long,
    val durationMillis: Long,
    val isSynced: Boolean = false,
    val transcription: String? = null,
)
