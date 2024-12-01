package org.nsh07.wikireader.ui.viewModel

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nsh07.wikireader.WikiReaderApplication
import org.nsh07.wikireader.data.AppPreferencesRepository
import org.nsh07.wikireader.data.WikipediaRepository
import org.nsh07.wikireader.data.parseText
import org.nsh07.wikireader.network.HostSelectionInterceptor

class UiViewModel(
    private val interceptor: HostSelectionInterceptor,
    private val wikipediaRepository: WikipediaRepository,
    private val appPreferencesRepository: AppPreferencesRepository
) : ViewModel() {
    private val _searchBarState = MutableStateFlow(SearchBarState())
    val searchBarState: StateFlow<SearchBarState> = _searchBarState.asStateFlow()

    private val _homeScreenState = MutableStateFlow(HomeScreenState())
    val homeScreenState: StateFlow<HomeScreenState> = _homeScreenState.asStateFlow()

    private val _listState = MutableStateFlow(LazyListState(0, 0))
    val listState: StateFlow<LazyListState> = _listState.asStateFlow()

    private val _preferencesState = MutableStateFlow(PreferencesState())
    val preferencesState: StateFlow<PreferencesState> = _preferencesState.asStateFlow()

    private val backStack = mutableListOf<String>()
    private var lastQuery: String? = null

    var isReady = false
    var isAnimDurationComplete = false

    init {
        viewModelScope.launch { // Run blocking to delay app startup until theme is determined
            val theme = appPreferencesRepository.readStringPreference("theme")
                ?: appPreferencesRepository.saveStringPreference("theme", "auto")

            val lang = appPreferencesRepository.readStringPreference("lang")
                ?: appPreferencesRepository.saveStringPreference("lang", "en")

            val colorScheme = appPreferencesRepository.readStringPreference("color-scheme")
                ?: appPreferencesRepository.saveStringPreference(
                    "color-scheme",
                    Color.White.toString()
                )

            val fontSize = appPreferencesRepository.readIntPreference("font-size")
                ?: appPreferencesRepository.saveIntPreference("font-size", 16)

            val blackTheme = appPreferencesRepository.readBooleanPreference("black-theme")
                ?: appPreferencesRepository.saveBooleanPreference("black-theme", false)

            val expandedSections =
                appPreferencesRepository.readBooleanPreference("expanded-sections")
                    ?: appPreferencesRepository.saveBooleanPreference("expanded-sections", false)

            val dataSaver = appPreferencesRepository.readBooleanPreference("data-saver")
                ?: appPreferencesRepository.saveBooleanPreference("data-saver", false)

            _preferencesState.update { currentState ->
                currentState.copy(
                    theme = theme,
                    lang = lang,
                    colorScheme = colorScheme,
                    fontSize = fontSize,
                    blackTheme = blackTheme,
                    expandedSections = expandedSections,
                    dataSaver = dataSaver
                )
            }

            _searchBarState.update { currentState ->
                currentState.copy(history = appPreferencesRepository.readHistory() ?: emptySet())
            }

            interceptor.setHost("$lang.wikipedia.org")
            isReady = true
        }
    }

    fun startAnimDuration() {
        viewModelScope.launch {
            delay(600)
            isAnimDurationComplete = true
        }
    }

    /**
     * Updates history and performs search
     *
     * The search query string is trimmed before being added to the history and performing the search
     *
     * @param query Search query string
     */
    fun performSearch(
        query: String?,
        random: Boolean = false,
        fromLink: Boolean = false,
        fromBackStack: Boolean = false
    ) {
        val q = query?.trim() ?: " "
        val history = searchBarState.value.history.toMutableSet()

        if (q != "") {
            if (!random && !fromLink && !fromBackStack) {
                history.remove(q)
                history.add(q)
                if (history.size > 50) history.remove(history.first())
            }

            viewModelScope.launch {
                if (lastQuery != null) {
                    if (!fromBackStack) backStack.add(lastQuery!!)
                    lastQuery = q
                } else lastQuery = q

                _homeScreenState.update { currentState ->
                    currentState.copy(isLoading = true)
                }

                if (!random && !fromLink && !fromBackStack)
                    appPreferencesRepository.saveHistory(history)

                try {
                    val apiResponse = when (random) {
                        false -> wikipediaRepository
                            .getSearchResult(q)
                            .query
                            ?.pages?.get(0)

                        else -> wikipediaRepository
                            .getRandomResult()
                            .query
                            ?.pages?.get(0)
                    }

                    val extractText = apiResponse
                        ?.extract ?: ""

                    val extract = if (extractText != "")
                        parseText(extractText)
                    else
                        listOf("No search results found for \"$q\"")

                    _homeScreenState.update { currentState ->
                        currentState.copy(
                            title = apiResponse?.title ?: "Error",
                            extract = extract,
                            photo = apiResponse?.photo,
                            photoDesc = apiResponse?.photoDesc,
                            isLoading = false,
                            isBackStackEmpty = backStack.isEmpty()
                        )
                    }
                } catch (e: Exception) {
                    Log.e("ViewModel", "Error in fetching results", e)
                    _homeScreenState.update { currentState ->
                        currentState.copy(
                            title = "Error",
                            extract = listOf("No internet connection"),
                            photo = null,
                            photoDesc = null,
                            isLoading = false
                        )
                    }
                }

                listState.value.scrollToItem(0)
                if (fromLink) resetLang()
            }
        }

        _searchBarState.update { currentState ->
            if (!random && !fromLink && !fromBackStack)
                currentState.copy(
                    query = q,
                    isSearchBarExpanded = false,
                    history = history
                )
            else
                currentState.copy(isSearchBarExpanded = false)
        }
    }

    fun popBackStack(): String? {
        return backStack.removeLastOrNull()
    }

    fun setExpanded(expanded: Boolean) {
        _searchBarState.update { currentState ->
            currentState.copy(isSearchBarExpanded = expanded)
        }
    }

    fun setQuery(query: String) {
        _searchBarState.update { currentState ->
            currentState.copy(query = query)
        }
    }

    fun focusSearchBar() {
        searchBarState.value.focusRequester.requestFocus()
    }

    fun removeHistoryItem(item: String) {
        viewModelScope.launch {
            val history = searchBarState.value.history.toMutableSet()
            history.remove(item)
            _searchBarState.update { currentState ->
                currentState.copy(history = history)
            }
            appPreferencesRepository.saveHistory(history)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            _searchBarState.update { currentState ->
                currentState.copy(history = emptySet())
            }
            appPreferencesRepository.saveHistory(emptySet())
        }
    }

    fun saveTheme(theme: String) {
        viewModelScope.launch {
            _preferencesState.update { currentState ->
                currentState.copy(
                    theme = appPreferencesRepository
                        .saveStringPreference("theme", theme)
                )
            }
        }
    }

    fun saveLang(lang: String) {
        viewModelScope.launch {
            _preferencesState.update { currentState ->
                currentState.copy(
                    lang = appPreferencesRepository
                        .saveStringPreference("lang", lang)
                )
            }
            interceptor.setHost("$lang.wikipedia.org")
        }
    }

    fun setLang(lang: String) {
        interceptor.setHost("$lang.wikipedia.org")
    }

    fun resetLang() {
        interceptor.setHost("${preferencesState.value.lang}.wikipedia.org")
    }

    fun saveColorScheme(colorScheme: String) {
        viewModelScope.launch {
            _preferencesState.update { currentState ->
                currentState.copy(
                    colorScheme = appPreferencesRepository
                        .saveStringPreference("color-scheme", colorScheme)
                )
            }
        }
    }

    fun saveBlackTheme(blackTheme: Boolean) {
        viewModelScope.launch {
            _preferencesState.update { currentState ->
                currentState.copy(
                    blackTheme = appPreferencesRepository
                        .saveBooleanPreference("black-theme", blackTheme)
                )
            }
        }
    }

    fun saveFontSize(fontSize: Int) {
        viewModelScope.launch {
            appPreferencesRepository.saveIntPreference("font-size", fontSize)
            _preferencesState.update { currentState ->
                currentState.copy(fontSize = fontSize)
            }
        }
    }

    fun saveExpandedSections(expandedSections: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.saveBooleanPreference("expanded-sections", expandedSections)
            _preferencesState.update { currentState ->
                currentState.copy(expandedSections = expandedSections)
            }
        }
    }

    fun saveDataSaver(dataSaver: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.saveBooleanPreference("data-saver", dataSaver)
            _preferencesState.update { currentState ->
                currentState.copy(dataSaver = dataSaver)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as WikiReaderApplication)
                val interceptor = application.container.interceptor
                val wikipediaRepository = application.container.wikipediaRepository
                val appPreferencesRepository = application.container.appPreferencesRepository
                UiViewModel(
                    interceptor = interceptor,
                    wikipediaRepository = wikipediaRepository,
                    appPreferencesRepository = appPreferencesRepository
                )
            }
        }
    }
}