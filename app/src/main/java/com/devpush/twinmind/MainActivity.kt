package com.devpush.twinmind

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.core.content.ContextCompat
import com.devpush.twinmind.data.UserPreferencesRepository
import com.devpush.twinmind.presentation.navigation.AppNavGraph
import com.devpush.twinmind.ui.theme.TwinMindTheme
import timber.log.Timber
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {

    private val requestCalendarPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val readGranted = permissions[Manifest.permission.READ_CALENDAR] ?: false
            val writeGranted = permissions[Manifest.permission.WRITE_CALENDAR] ?: false
            if (readGranted && writeGranted) {
                Timber.d("Calendar permissions granted via launcher.")
            } else {
                Timber.w("Calendar permissions denied via launcher.")
                Toast.makeText(this, "Calendar access not granted.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val userPreferencesRepository = UserPreferencesRepository(applicationContext)
        setContent {
            TwinMindTheme {
                val isLoggedIn by userPreferencesRepository.isLoggedIn.collectAsState(initial = false)
                AppNavGraph(
                    isUserLoggedIn = isLoggedIn
                )
            }
        }
    }

    fun checkAndRequestCalendarPermissions() {
        val readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
        val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)

        if (readPermission == PackageManager.PERMISSION_GRANTED &&
            writePermission == PackageManager.PERMISSION_GRANTED) {
            Timber.d("Calendar permissions already granted.")
        } else {
            Timber.i("Requesting calendar permissions.")
            requestCalendarPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR
                )
            )
        }
    }
}
