package org.nsh07.wikireader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.nsh07.wikireader.data.toColor
import org.nsh07.wikireader.ui.AppScreen
import org.nsh07.wikireader.ui.theme.WikiReaderTheme
import org.nsh07.wikireader.ui.viewModel.UiViewModel

class MainActivity : ComponentActivity() {

    val viewModel: UiViewModel by viewModels<UiViewModel>(factoryProducer = { UiViewModel.Factory })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.startAnimDuration()
        installSplashScreen().setKeepOnScreenCondition {
            !viewModel.isReady || !viewModel.isAnimDurationComplete
        }
        viewModel.setFilesDir(filesDir.path)
        viewModel.migrateArticles()
        enableEdgeToEdge()

        setContent {
            val preferencesState by viewModel.preferencesState.collectAsState()

            val darkTheme = when (preferencesState.theme) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            val seed = preferencesState.colorScheme.toColor()

            WikiReaderTheme(
                darkTheme = darkTheme,
                seedColor = seed,
                blackTheme = preferencesState.blackTheme
            ) {
                viewModel.setCompositionLocals(
                    cs = colorScheme,
                    tg = typography
                )
                AppScreen(
                    viewModel = viewModel,
                    preferencesState = preferencesState,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
