package com.devpush.twinmind.di.modules

import com.devpush.twinmind.data.repository.UserPreferencesRepositoryImpl
import com.devpush.twinmind.domain.repository.UserPreferencesRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<UserPreferencesRepository> {
        UserPreferencesRepositoryImpl(context = get())
    }
}