package com.devpush.twinmind.di.modules

import com.devpush.twinmind.data.local.AppDatabase
import com.devpush.twinmind.presentation.memories.AudioPlayerHelper
import com.devpush.twinmind.presentation.memories.AudioRecorderHelper
import com.devpush.twinmind.presentation.memories.MemoriesViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val DataBaseKoinModule = module {
    // Database providers
    single { AppDatabase.getDatabase(androidContext()) }
    single { get<AppDatabase>().recordingDao() }

    // Audio Helpers
    single { AudioRecorderHelper(androidApplication()) } // Using androidApplication for Application context
    single { AudioPlayerHelper(androidApplication()) }   // Using androidApplication for Application context

    // ViewModels
    viewModel { MemoriesViewModel(androidApplication(), get(), get(), get()) }
}