package com.example.photocatalog

import android.app.Application
import com.example.photocatalog.data.source.AuthRepository
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PhotoCatalogApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AuthRepository.init(this)
    }
}
