package com.devpush.twinmind.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    suspend fun saveLoginStatus(isLoggedIn: Boolean)
    val isLoggedIn: Flow<Boolean>
}