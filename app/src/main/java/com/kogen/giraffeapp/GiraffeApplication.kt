package com.kogen.giraffeapp

import android.app.Application
import com.kogen.giraffeapp.di.setApplicationContext

class GiraffeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setApplicationContext(this)
    }
}
