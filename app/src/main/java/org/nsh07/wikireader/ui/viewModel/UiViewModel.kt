package org.nsh07.wikireader.ui.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.nsh07.wikireader.WikiReaderApplication
import org.nsh07.wikireader.data.AppPreferencesRepository
import org.nsh07.wikireader.data.WikipediaRepository
import org.nsh07.wikireader.data.parseText

class UiViewModel(
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

    /**
     * Updates history and performs search
     *
     * The search query string is trimmed before being added to the history and performing the search
     *
     * @param query Search query string
     */
    fun performSearch(query: String) {
        val q = query.trim()
        val history = searchBarState.value.history.toMutableSet()

        if (q != "") {
            history.add(q)
            if (history.size > 50) history.remove(history.first())

            viewModelScope.launch {
                _homeScreenState.update { currentState ->
                    currentState.copy(isLoading = true)
                }

                appPreferencesRepository.saveHistory(history)

                try {
                    val apiResponse = wikipediaRepository
                        .searchWikipedia(q)
                        .query
                        ?.pages?.get(0)

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
                            isLoading = false
                        )
                    }
                } catch (_: Exception) {
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
            }
        }

        _searchBarState.update { currentState ->
            currentState.copy(
                query = q,
                isSearchBarExpanded = false,
                history = history
            )
        }
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

    fun loadHistory() {
        runBlocking {
            _searchBarState.update { currentState ->
                currentState.copy(history = appPreferencesRepository.readHistory() ?: emptySet())
            }
        }
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

    fun loadTheme() {
        runBlocking { // Run blocking to delay app startup until theme is determined
            val theme = appPreferencesRepository.readPreference("theme")
                ?: appPreferencesRepository.savePreference("theme", "auto")

            _preferencesState.update { currentState ->
                currentState.copy(theme = theme)
            }
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            _preferencesState.update { currentState ->
                currentState.copy(theme = appPreferencesRepository.savePreference("theme", theme))
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as WikiReaderApplication)
                val wikipediaRepository = application.container.wikipediaRepository
                val appPreferencesRepository = application.container.appPreferencesRepository
                UiViewModel(
                    wikipediaRepository = wikipediaRepository,
                    appPreferencesRepository = appPreferencesRepository
                )
            }
        }
    }
}