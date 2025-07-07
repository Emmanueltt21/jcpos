package com.example.jetpackpos

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PosApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialization code can go here if needed
    }
}
