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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.nsh07.wikireader.data.AppStatus
import org.nsh07.wikireader.data.isRtl
import org.nsh07.wikireader.data.toColor
import org.nsh07.wikireader.ui.AppScreen
import org.nsh07.wikireader.ui.homeScreen.viewModel.HomeScreenViewModel
import org.nsh07.wikireader.ui.settingsScreen.viewModel.SettingsViewModel
import org.nsh07.wikireader.ui.theme.WikiReaderTheme

class MainActivity : ComponentActivity() {

    val viewModel: HomeScreenViewModel by viewModels<HomeScreenViewModel>(factoryProducer = { HomeScreenViewModel.Factory })
    val settingsViewModel: SettingsViewModel by viewModels<SettingsViewModel>(factoryProducer = { SettingsViewModel.Factory })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition { viewModel.appStatus.value != AppStatus.INITIALIZED }

        viewModel.setFilesDir(filesDir.path)

        enableEdgeToEdge()

        setContent {
            val preferencesState by settingsViewModel.preferencesState.collectAsStateWithLifecycle()

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
                CompositionLocalProvider(
                    LocalLayoutDirection provides
                            if (isRtl(preferencesState.lang)) LayoutDirection.Rtl
                            else LayoutDirection.Ltr
                ) {
                    AppScreen(
                        viewModel = viewModel,
                        settingsViewModel = settingsViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        reportFullyDrawn()
    }
}
