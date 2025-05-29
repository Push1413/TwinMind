package com.devpush.twinmind

import android.app.Application
import com.devpush.twinmind.di.appModule
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MyApp:Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        startKoin {
            androidContext(this@MyApp)
            modules(appModule)
        }
    }
}