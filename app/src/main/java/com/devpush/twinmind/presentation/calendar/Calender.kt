package com.devpush.twinmind.presentation.calendar

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devpush.twinmind.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    calendarViewModel: CalendarViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiStateValue by calendarViewModel.uiState.collectAsState()
    var showCreateEventDialog by rememberSaveable { mutableStateOf(false) }

    val calendarInteractionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Timber.d("User consent/recovery action successful. Reloading calendar events.")
            GoogleSignIn.getLastSignedInAccount(context)?.let { account ->
                calendarViewModel.loadUpcomingCalendarEvents(account)
            } ?: Timber.w("Could not reload events: GoogleSignInAccount is null after interaction.")
        } else {
            Timber.w("User consent/recovery action was cancelled or failed. Result code: ${result.resultCode}")
            Toast.makeText(context, "Calendar access not granted or action cancelled.", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateEventDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Create new event")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(horizontal = 16.dp), // Additional horizontal padding for content
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text("Calendar Events", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                val mainActivity = context as? MainActivity
                if (mainActivity == null) {
                    Timber.e("MainActivity context is null, cannot request permissions.")
                    Toast.makeText(context, "Error: Could not get Activity context.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                mainActivity.checkAndRequestCalendarPermissions()
                GoogleSignIn.getLastSignedInAccount(context)?.let { account ->
                    calendarViewModel.loadUpcomingCalendarEvents(account)
                } ?: Toast.makeText(context, "Please sign in with Google first.", Toast.LENGTH_LONG).show()
            }) {
                Text("Load Upcoming Calendar Events")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Handle UI state for event list display and creation feedback
            when (val state = uiStateValue) {
                is CalendarUiState.Idle -> Text("Click 'Load' or '+' to manage events.")
                is CalendarUiState.Loading -> CircularProgressIndicator()
                is CalendarUiState.EventCreating -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text("Creating event...")
                }
                is CalendarUiState.InteractionRequired -> {
                    LaunchedEffect(state.intent) {
                        Timber.d("CalendarScreen: Launching intent for user recoverable auth action.")
                        calendarInteractionLauncher.launch(state.intent)
                    }
                    Text("Please complete the required action to access calendar data.")
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator()
                }
                is CalendarUiState.Success -> {
                    if (state.events.isEmpty()) {
                        Text("No upcoming events found.")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.events) { event -> EventItem(event = event) }
                        }
                    }
                }
                is CalendarUiState.Error -> Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                is CalendarUiState.EventCreationSuccess -> {
                    // This state is transient, typically UI shows a Toast/Snackbar
                    // and then ViewModel moves to Loading/Success for event list.
                    // For now, a simple text, but usually handled by a side-effect (LaunchedEffect)
                    // to show a Toast and then trigger list refresh if not already done by VM.
                    LaunchedEffect(Unit) {
                        Toast.makeText(context, "Event created successfully!", Toast.LENGTH_SHORT).show()
                        // ViewModel should already be refreshing the list.
                    }
                    Text("Event created successfully! Refreshing list...")
                    CircularProgressIndicator() // Show loading as list refreshes
                }
                is CalendarUiState.EventCreationError -> {
                    Text("Error creating event: ${state.message}", color = MaterialTheme.colorScheme.error)
                    // A button to retry or dismiss could be added here.
                }
            }
        } // End Column

        if (showCreateEventDialog) {
            CreateEventDialog(
                onDismissRequest = { showCreateEventDialog = false },
                onSubmit = { summary, description, startTimeMillis, endTimeMillis ->
                    showCreateEventDialog = false
                    GoogleSignIn.getLastSignedInAccount(context)?.let { account ->
                        calendarViewModel.createCalendarEvent(summary, description, startTimeMillis, endTimeMillis, TimeZone.getDefault().id, account)
                    } ?: Toast.makeText(context, "Not signed in. Cannot create event.", Toast.LENGTH_SHORT).show()
                }
            )
        }
    } // End Scaffold
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventDialog(
    onDismissRequest: () -> Unit,
    onSubmit: (summary: String, description: String?, startTimeMillis: Long, endTimeMillis: Long) -> Unit
) {
    var summary by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    // Date & Time Picker States
    val currentCalendar = remember { Calendar.getInstance() }
    val startDatePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val startTimePickerState = rememberTimePickerState(initialHour = currentCalendar.get(Calendar.HOUR_OF_DAY), initialMinute = currentCalendar.get(Calendar.MINUTE), is24Hour = true)
    val endDatePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis() + 3600000) // Default 1 hour later
    val endTimePickerState = rememberTimePickerState(initialHour = (currentCalendar.get(Calendar.HOUR_OF_DAY) + 1) % 24, initialMinute = currentCalendar.get(Calendar.MINUTE), is24Hour = true)

    var showStartDateDialog by remember { mutableStateOf(false) }
    var showStartTimeDialog by remember { mutableStateOf(false) }
    var showEndDateDialog by remember { mutableStateOf(false) }
    var showEndTimeDialog by remember { mutableStateOf(false) }

    val sdfDate = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val sdfTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val selectedStartDateText = startDatePickerState.selectedDateMillis?.let { sdfDate.format(it) } ?: "Select Start Date"
    val selectedStartTimeText = "${String.format("%02d", startTimePickerState.hour)}:${String.format("%02d", startTimePickerState.minute)}"
    val selectedEndDateText = endDatePickerState.selectedDateMillis?.let { sdfDate.format(it) } ?: "Select End Date"
    val selectedEndTimeText = "${String.format("%02d", endTimePickerState.hour)}:${String.format("%02d", endTimePickerState.minute)}"


    Dialog(onDismissRequest = onDismissRequest) {
        Card(modifier = Modifier.wrapContentSize().padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Create New Event", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = summary, onValueChange = { summary = it }, label = { Text("Event Summary") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description (Optional)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                // Start Date/Time
                Text("Start Time", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(selectedStartDateText, modifier = Modifier.weight(1f).clickable { showStartDateDialog = true })
                    Button(onClick = { showStartDateDialog = true }) { Text("Date") }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(selectedStartTimeText, modifier = Modifier.weight(1f).clickable { showStartTimeDialog = true })
                    Button(onClick = { showStartTimeDialog = true }) { Text("Time") }
                }

                // End Date/Time
                Spacer(modifier = Modifier.height(8.dp))
                Text("End Time", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(selectedEndDateText, modifier = Modifier.weight(1f).clickable { showEndDateDialog = true })
                    Button(onClick = { showEndDateDialog = true }) { Text("Date") }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(selectedEndTimeText, modifier = Modifier.weight(1f).clickable { showEndTimeDialog = true })
                    Button(onClick = { showEndTimeDialog = true }) { Text("Time") }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    TextButton(onClick = onDismissRequest) { Text("Cancel") }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = {
                        val startCal = Calendar.getInstance()
                        startDatePickerState.selectedDateMillis?.let { startCal.timeInMillis = it }
                        startCal.set(Calendar.HOUR_OF_DAY, startTimePickerState.hour)
                        startCal.set(Calendar.MINUTE, startTimePickerState.minute)
                        val finalStartTimeMillis = startCal.timeInMillis

                        val endCal = Calendar.getInstance()
                        endDatePickerState.selectedDateMillis?.let { endCal.timeInMillis = it }
                        endCal.set(Calendar.HOUR_OF_DAY, endTimePickerState.hour)
                        endCal.set(Calendar.MINUTE, endTimePickerState.minute)
                        val finalEndTimeMillis = endCal.timeInMillis

                        if (summary.isBlank()) {
                            // TODO: Show error for blank summary
                            return@Button
                        }
                        if (finalEndTimeMillis <= finalStartTimeMillis) {
                            // TODO: Show error for end time before start time
                            return@Button
                        }
                        onSubmit(summary, description.ifBlank { null }, finalStartTimeMillis, finalEndTimeMillis)
                    }) { Text("Save Event") }
                }

                // Date Picker Dialogs
                if (showStartDateDialog) {
                    DatePickerDialog(
                        onDismissRequest = { showStartDateDialog = false },
                        confirmButton = { TextButton(onClick = { showStartDateDialog = false }) { Text("OK") } },
                        dismissButton = { TextButton(onClick = { showStartDateDialog = false }) { Text("Cancel") } }
                    ) { DatePicker(state = startDatePickerState) }
                }
                if (showEndDateDialog) {
                    DatePickerDialog(
                        onDismissRequest = { showEndDateDialog = false },
                        confirmButton = { TextButton(onClick = { showEndDateDialog = false }) { Text("OK") } },
                        dismissButton = { TextButton(onClick = { showEndDateDialog = false }) { Text("Cancel") } }
                    ) { DatePicker(state = endDatePickerState) }
                }

                // Time Picker Dialogs (using custom Dialog for TimePicker)
                if (showStartTimeDialog) {
                    TimePickerDialogWrapper( // Using a wrapper for cleaner state management
                        showDialog = showStartTimeDialog,
                        onDismissRequest = { showStartTimeDialog = false },
                        timePickerState = startTimePickerState,
                        title = "Select Start Time"
                    )
                }
                if (showEndTimeDialog) {
                    TimePickerDialogWrapper(
                        showDialog = showEndTimeDialog,
                        onDismissRequest = { showEndTimeDialog = false },
                        timePickerState = endTimePickerState,
                        title = "Select End Time"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialogWrapper(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    timePickerState: TimePickerState,
    title: String = "Select Time"
) {
    if (showDialog) {
        AlertDialog( // Or use Dialog for more custom layout
            onDismissRequest = onDismissRequest,
            title = { Text(title) },
            text = {
                TimePicker(state = timePickerState, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                TextButton(onClick = onDismissRequest) { // TimePickerState updates automatically
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text("Cancel")
                }
            }
        )
    }
}


// EventItem Composable (already defined in the prompt, assuming it's here or accessible)
@Composable
fun EventItem(event: com.google.api.services.calendar.model.Event) {
    val sdfDateTime = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val sdfDate = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = event.summary ?: "No Title",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))

            val startDateTime = event.start?.dateTime
            val startDateOnly = event.start?.date
            val endDateTime = event.end?.dateTime
            val endDateOnly = event.end?.date

            if (startDateTime != null) {
                Text(
                    text = "Start: ${sdfDateTime.format(startDateTime.value)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else if (startDateOnly != null) {
                Text(
                    text = "Start: ${sdfDate.format(startDateOnly.value)} (All day)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (endDateTime != null) {
                Text(
                    text = "End: ${sdfDateTime.format(endDateTime.value)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else if (endDateOnly != null) {
                Text(
                    text = "End: ${sdfDate.format(endDateOnly.value)} (All day, exclusive date)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (!event.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Description: ${event.description}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
