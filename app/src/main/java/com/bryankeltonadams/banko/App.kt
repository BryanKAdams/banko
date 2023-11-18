package com.bryankeltonadams.banko

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

    }

}