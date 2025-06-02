package com.devpush.twinmind.di

import com.devpush.twinmind.di.modules.DataBaseKoinModule
import org.koin.dsl.module
import com.devpush.twinmind.di.modules.networkModule
import com.devpush.twinmind.di.modules.repositoryModule

val appModule = module {
    // Add application-wide dependencies here
    includes(networkModule)
    includes(DataBaseKoinModule)
    includes(repositoryModule)
}