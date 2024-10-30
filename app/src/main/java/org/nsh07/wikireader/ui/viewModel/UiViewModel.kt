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
import org.nsh07.wikireader.WikiReaderApplication
import org.nsh07.wikireader.data.WikipediaRepository
import org.nsh07.wikireader.data.parseText

class UiViewModel(
    private val wikipediaRepository: WikipediaRepository
) : ViewModel() {
    private val _searchBarState = MutableStateFlow(SearchBarState())
    val searchBarState: StateFlow<SearchBarState> = _searchBarState.asStateFlow()

    private val _homeScreenState = MutableStateFlow(HomeScreenState())
    val homeScreenState: StateFlow<HomeScreenState> = _homeScreenState.asStateFlow()

    private val _listState = MutableStateFlow(LazyListState(0, 0))
    val listState: StateFlow<LazyListState> = _listState.asStateFlow()

    /**
     * Updates history and performs search
     *
     * The search query string is trimmed before being added to the history and performing the search
     *
     * @param query Search query string
     */
    fun performSearch(query: String) {
        val q = query.trim()
        val history = searchBarState.value.history.toMutableList()

        if (q != "") {
            history.remove(q)
            history.add(0, q)

            viewModelScope.launch {
                _homeScreenState.update { currentState ->
                    currentState.copy(isLoading = true)
                }

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
                } catch (e: Exception) {
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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as WikiReaderApplication)
                val wikipediaRepository = application.container.wikipediaRepository
                UiViewModel(wikipediaRepository = wikipediaRepository)
            }
        }
    }
}