package org.nsh07.wikireader.ui.settingsScreen.viewModel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nsh07.wikireader.WikiReaderApplication
import org.nsh07.wikireader.data.AppDatabaseRepository
import org.nsh07.wikireader.data.AppStatus
import org.nsh07.wikireader.data.PreferencesRepository
import org.nsh07.wikireader.network.HostSelectionInterceptor

class SettingsViewModel(
    private val appStatusFlow: MutableStateFlow<AppStatus>,
    private val preferencesStateMutableFlow: MutableStateFlow<PreferencesState>,
    private val interceptor: HostSelectionInterceptor,
    private val appDatabaseRepository: AppDatabaseRepository,
    private val appPreferencesRepository: PreferencesRepository
) : ViewModel() {

    val preferencesState: StateFlow<PreferencesState> = preferencesStateMutableFlow.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val colorScheme = appPreferencesRepository.readStringPreference("color-scheme")
                ?: appPreferencesRepository.saveStringPreference(
                    "color-scheme",
                    Color.White.toString()
                )
            val fontStyle = appPreferencesRepository.readStringPreference("font-style")
                ?: appPreferencesRepository.saveStringPreference("font-style", "sans")
            val lang = appPreferencesRepository.readStringPreference("lang")
                ?: appPreferencesRepository.saveStringPreference("lang", "en")
            val theme = appPreferencesRepository.readStringPreference("theme")
                ?: appPreferencesRepository.saveStringPreference("theme", "auto")
            val fontSize = appPreferencesRepository.readIntPreference("font-size")
                ?: appPreferencesRepository.saveIntPreference("font-size", 16)
            val blackTheme = appPreferencesRepository.readBooleanPreference("black-theme")
                ?: appPreferencesRepository.saveBooleanPreference("black-theme", false)
            val dataSaver = appPreferencesRepository.readBooleanPreference("data-saver")
                ?: appPreferencesRepository.saveBooleanPreference("data-saver", false)
            val feedEnabled = appPreferencesRepository.readBooleanPreference("feed-enabled")
                ?: appPreferencesRepository.saveBooleanPreference("feed-enabled", true)
            val expandedSections =
                appPreferencesRepository.readBooleanPreference("expanded-sections")
                    ?: appPreferencesRepository.saveBooleanPreference("expanded-sections", false)
            val imageBackground = appPreferencesRepository.readBooleanPreference("image-background")
                ?: appPreferencesRepository.saveBooleanPreference("image-background", false)
            val immersiveMode = appPreferencesRepository.readBooleanPreference("immersive-mode")
                ?: appPreferencesRepository.saveBooleanPreference("immersive-mode", true)
            val renderMath = appPreferencesRepository.readBooleanPreference("render-math")
                ?: appPreferencesRepository.saveBooleanPreference("render-math", true)
            val browsingHistory = appPreferencesRepository.readBooleanPreference("browsing-history")
                ?: appPreferencesRepository.saveBooleanPreference("browsing-history", true)
            val searchHistory = appPreferencesRepository.readBooleanPreference("search-history")
                ?: appPreferencesRepository.saveBooleanPreference("search-history", true)

            preferencesStateMutableFlow.update { currentState ->
                currentState.copy(
                    blackTheme = blackTheme,
                    colorScheme = colorScheme,
                    dataSaver = dataSaver,
                    feedEnabled = feedEnabled,
                    expandedSections = expandedSections,
                    fontSize = fontSize,
                    fontStyle = fontStyle,
                    imageBackground = imageBackground,
                    immersiveMode = immersiveMode,
                    lang = lang,
                    renderMath = renderMath,
                    searchHistory = searchHistory,
                    browsingHistory = browsingHistory,
                    theme = theme
                )
            }

            appDatabaseRepository.deleteOldSearchHistory()
            appDatabaseRepository.deleteOldViewHistory()

            interceptor.setHost("$lang.wikipedia.org")

            appStatusFlow.update { AppStatus.INITIALIZED }
        }
    }

    fun saveTheme(theme: String) {
        viewModelScope.launch {
            preferencesStateMutableFlow.update { currentState ->
                currentState.copy(theme = theme)
            }
            appPreferencesRepository.saveStringPreference("theme", theme)
        }
    }

    fun saveFontStyle(fontStyle: String) {
        viewModelScope.launch {
            preferencesStateMutableFlow.update { currentState ->
                currentState.copy(fontStyle = fontStyle)
            }
            appPreferencesRepository.saveStringPreference("font-style", fontStyle)
        }
    }

    fun saveLang(lang: String) {
        interceptor.setHost("$lang.wikipedia.org")
        preferencesStateMutableFlow.update { currentState ->
            currentState.copy(lang = lang)
        }
        viewModelScope.launch {
            appPreferencesRepository.saveStringPreference("lang", lang)
        }
    }

    fun saveColorScheme(colorScheme: String) {
        viewModelScope.launch {
            preferencesStateMutableFlow.update { currentState ->
                currentState.copy(colorScheme = colorScheme)
            }
            appPreferencesRepository.saveStringPreference("color-scheme", colorScheme)
        }
    }

    fun saveBlackTheme(blackTheme: Boolean) {
        viewModelScope.launch {
            preferencesStateMutableFlow.update { currentState ->
                currentState.copy(blackTheme = blackTheme)
            }
            appPreferencesRepository.saveBooleanPreference("black-theme", blackTheme)
        }
    }

    fun saveFontSize(fontSize: Int) {
        viewModelScope.launch {
            appPreferencesRepository.saveIntPreference("font-size", fontSize)
            preferencesStateMutableFlow.update { currentState ->
                currentState.copy(fontSize = fontSize)
            }
        }
    }

    fun saveExpandedSections(expandedSections: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.saveBooleanPreference("expanded-sections", expandedSections)
            preferencesStateMutableFlow.update { currentState ->
                currentState.copy(expandedSections = expandedSections)
            }
        }
    }

    fun saveRenderMath(renderMath: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.saveBooleanPreference("render-math", renderMath)
            preferencesStateMutableFlow.update { currentState ->
                currentState.copy(renderMath = renderMath)
            }
        }
    }

    fun saveHistory(history: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.saveBooleanPreference("browsing-history", history)
            preferencesStateMutableFlow.update { currentState ->
                currentState.copy(browsingHistory = history)
            }
        }
    }

    fun saveSearchHistory(searchHistory: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.saveBooleanPreference("search-history", searchHistory)
            preferencesStateMutableFlow.update { currentState ->
                currentState.copy(searchHistory = searchHistory)
            }
        }
    }

    fun saveDataSaver(dataSaver: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.saveBooleanPreference("data-saver", dataSaver)
            preferencesStateMutableFlow.update { currentState ->
                currentState.copy(dataSaver = dataSaver)
            }
        }
    }

    fun saveFeedEnabled(feedEnabled: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.saveBooleanPreference("feed-enabled", feedEnabled)
            preferencesStateMutableFlow.update { currentState ->
                currentState.copy(feedEnabled = feedEnabled)
            }
        }
    }

    fun saveImageBackground(imageBackground: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.saveBooleanPreference("image-background", imageBackground)
            preferencesStateMutableFlow.update { currentState ->
                currentState.copy(imageBackground = imageBackground)
            }
        }
    }

    fun saveImmersiveMode(immersiveMode: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.saveBooleanPreference("immersive-mode", immersiveMode)
            preferencesStateMutableFlow.update { currentState ->
                currentState.copy(immersiveMode = immersiveMode)
            }
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            try {
                appPreferencesRepository.resetSettings()
                preferencesStateMutableFlow.update {
                    PreferencesState()
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error in restoring settings: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as WikiReaderApplication)
                val appStatusFlow = application.container.appStatus
                val preferencesStateMutableFlow = application.container.preferencesStateMutableFlow
                val interceptor = application.container.interceptor
                val appPreferencesRepository = application.container.appPreferencesRepository
                val appHistoryRepository = application.container.appDatabaseRepository
                SettingsViewModel(
                    appStatusFlow = appStatusFlow,
                    preferencesStateMutableFlow = preferencesStateMutableFlow,
                    interceptor = interceptor,
                    appPreferencesRepository = appPreferencesRepository,
                    appDatabaseRepository = appHistoryRepository
                )
            }
        }
    }

}