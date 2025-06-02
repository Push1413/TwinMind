package com.devpush.twinmind.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.devpush.twinmind.data.local.RecordingDao
import com.devpush.twinmind.data.local.RecordingItem

@Database(entities = [RecordingItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordingDao(): RecordingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "twinmind_database"
                )
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    // TODO: Add migration strategy for production apps.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
