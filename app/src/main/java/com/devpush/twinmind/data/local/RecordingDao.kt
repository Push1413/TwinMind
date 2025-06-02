package com.devpush.twinmind.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recording: RecordingItem)

    @Query("SELECT * FROM recordings ORDER BY timestamp DESC")
    fun getAllRecordings(): Flow<List<RecordingItem>>

    @Query("SELECT * FROM recordings WHERE id = :id")
    suspend fun getRecordingById(id: Long): RecordingItem?

    @Update
    suspend fun update(recording: RecordingItem)

    @Delete
    suspend fun delete(recording: RecordingItem)

    @Query("SELECT * FROM recordings WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedRecordings(): List<RecordingItem>
}
