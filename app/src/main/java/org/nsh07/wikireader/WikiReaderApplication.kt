package org.nsh07.wikireader

import android.app.Application
import org.nsh07.wikireader.data.AppContainer
import org.nsh07.wikireader.data.DefaultAppContainer

class WikiReaderApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}