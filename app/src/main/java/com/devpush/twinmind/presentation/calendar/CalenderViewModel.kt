package com.devpush.twinmind.presentation.calendar // Changed package

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.calendar.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

sealed interface CalendarUiState {
    object Idle : CalendarUiState
    object Loading : CalendarUiState
    data class Success(val events: List<Event>) : CalendarUiState
    data class Error(val message: String) : CalendarUiState
    data class InteractionRequired(val intent: Intent) : CalendarUiState
    object EventCreating : CalendarUiState
    object EventCreationSuccess : CalendarUiState
    data class EventCreationError(val message: String) : CalendarUiState
}

class CalendarViewModel(application: Application) : AndroidViewModel(application) { // Renamed class

    private val _uiState = MutableStateFlow<CalendarUiState>(CalendarUiState.Idle)
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private var calendarServiceHelper: CalendarServiceHelper? = null

    private fun initializeCalendarService(account: GoogleSignInAccount) {
        calendarServiceHelper = CalendarServiceHelper(getApplication<Application>().applicationContext, account)
        Timber.d("CalendarServiceHelper initialized for CalendarViewModel.")
    }

    fun loadUpcomingCalendarEvents(account: GoogleSignInAccount) {
        if (calendarServiceHelper == null) {
            initializeCalendarService(account)
        }
        // Consider if re-initialization is needed if 'account' changes from what helper was init with.
        // For now, simple init if null.

        _uiState.value = CalendarUiState.Loading
        Timber.d("Loading calendar events in CalendarViewModel...")
        viewModelScope.launch {
            try {
                val eventsList = calendarServiceHelper?.getUpcomingEvents()
                if (eventsList != null) {
                    _uiState.value = CalendarUiState.Success(eventsList)
                    Timber.i("Successfully fetched ${eventsList.size} events in CalendarViewModel.")
                } else {
                    _uiState.value = CalendarUiState.Error("Failed to fetch events: Service returned no data.")
                    Timber.w("Failed to fetch events in CalendarViewModel: CalendarServiceHelper returned null event list.")
                }
            } catch (e: UserRecoverableAuthIOException) {
                Timber.w(e, "User recoverable auth action required in CalendarViewModel for fetching events.")
                _uiState.value = CalendarUiState.InteractionRequired(e.intent)
            } catch (e: Exception) {
                Timber.e(e, "Error fetching calendar events in CalendarViewModel")
                _uiState.value = CalendarUiState.Error("Error fetching events: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    fun createCalendarEvent(
        summary: String,
        description: String?,
        startTimeMillis: Long,
        endTimeMillis: Long,
        timeZone: String? = null,
        currentAccount: GoogleSignInAccount
    ) {
        // Ensure calendarServiceHelper is initialized with the current account.
        // If the account might change, or if helper could be null for other reasons,
        // this initialization step is crucial.
        initializeCalendarService(currentAccount)

        // Check if helper is initialized, though initializeCalendarService should handle it.
        // This is more a safeguard or for contexts where initializeCalendarService might not be called immediately before.
        if (calendarServiceHelper == null) {
            _uiState.value = CalendarUiState.EventCreationError("Calendar service not properly initialized. Please ensure you are signed in.")
            Timber.e("createCalendarEvent called but calendarServiceHelper is null.")
            return
        }

        _uiState.value = CalendarUiState.EventCreating
        Timber.d("Creating calendar event: $summary")
        viewModelScope.launch {
            try {
                val createdEvent = calendarServiceHelper!!.createEvent(summary, description, startTimeMillis, endTimeMillis, timeZone)
                if (createdEvent != null) {
                    _uiState.value = CalendarUiState.EventCreationSuccess
                    Timber.i("Successfully created event: ${createdEvent.id}")
                    // Refresh the event list to show the new event
                    loadUpcomingCalendarEvents(currentAccount)
                } else {
                    _uiState.value = CalendarUiState.EventCreationError("Failed to create event: Service returned null.")
                    Timber.w("Event creation failed: service returned null for event '$summary'")
                }
            } catch (e: UserRecoverableAuthIOException) {
                Timber.w(e, "User recoverable auth action required during event creation for '$summary'.")
                _uiState.value = CalendarUiState.InteractionRequired(e.intent)
            } catch (e: Exception) {
                Timber.e(e, "Error creating calendar event '$summary' in ViewModel")
                _uiState.value = CalendarUiState.EventCreationError("Error creating event: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }
}
