package com.devpush.twinmind.presentation.calendar

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.Events // For the list response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.util.Collections // For Collections.singleton

class CalendarServiceHelper(context: Context, account: GoogleSignInAccount) {

    private val service: Calendar

    init {
        // Ensure account.account is not null. If it can be, handle appropriately.
        // For this example, assuming it's valid from a successful sign-in.
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            Collections.singleton(CalendarScopes.CALENDAR)
        ).setSelectedAccount(account.account) // account.account is of type android.accounts.Account

        service = Calendar.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("TwinMind") // Set your application name
            .build()
    }

    suspend fun getUpcomingEvents(maxResults: Int = 10): List<Event>? = withContext(Dispatchers.IO) {
        try {
            val now = DateTime(System.currentTimeMillis())
            // .list("primary") indicates the primary calendar of the authenticated user
            val eventList: Events = service.events().list("primary")
                .setMaxResults(maxResults)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true) // Expand recurring events into single instances
                .execute()
            eventList.items // This can be null if no events are found or items list is empty
        } catch (e: IOException) {
            Timber.e(e, "Error fetching calendar events (IOException)")
            null // Return null or emptyList() based on how you want to signal this error
        } catch (e: Exception) { // Catch other potential Google API or general exceptions
            Timber.e(e, "Error fetching calendar events (Generic Exception)")
            null
        }
    }

    suspend fun createEvent(
        summary: String,
        description: String?,
        startTimeMillis: Long,
        endTimeMillis: Long,
        timeZone: String? = null // Optional: Specify timezone e.g., "America/Los_Angeles"
    ): Event? = withContext(Dispatchers.IO) {
        try {
            val event = Event().apply {
                this.summary = summary
                this.description = description // Can be null

                val startDateTime = DateTime(startTimeMillis)
                val endDateTime = DateTime(endTimeMillis)

                this.start = EventDateTime().setDateTime(startDateTime)
                if (timeZone != null) {
                    this.start.timeZone = timeZone
                }

                this.end = EventDateTime().setDateTime(endDateTime)
                if (timeZone != null) {
                    this.end.timeZone = timeZone
                }
            }

            service.events().insert("primary", event).execute()
        } catch (e: IOException) {
            Timber.e(e, "Error creating calendar event (IOException)")
            null
        } catch (e: Exception) {
            Timber.e(e, "Error creating calendar event (Generic Exception)")
            null
        }
    }
}
