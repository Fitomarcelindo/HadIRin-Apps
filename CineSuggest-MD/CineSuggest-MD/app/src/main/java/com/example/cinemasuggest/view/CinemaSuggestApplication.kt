package com.example.cinemasuggest.view

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class CinemaSuggestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}
