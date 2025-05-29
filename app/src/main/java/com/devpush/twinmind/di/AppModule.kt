package com.devpush.twinmind.di

import org.koin.dsl.module
import com.devpush.twinmind.di.modules.networkModule

val appModule = module {
    // Add application-wide dependencies here
    includes(networkModule)
}