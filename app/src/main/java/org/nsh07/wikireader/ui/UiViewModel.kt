package org.nsh07.wikireader.ui

import android.content.Context
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nsh07.wikireader.R
import org.nsh07.wikireader.network.WikipediaApi

class UiViewModel : ViewModel() {
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
                    val apiResponse = WikipediaApi.retrofitService
                        .searchWikipedia(q)
                        .query
                        ?.pages?.get(0)

                    _homeScreenState.update { currentState ->
                        currentState.copy(
                            title = apiResponse?.title ?: "Error",
                            extract = apiResponse?.extract
                                ?: "No search results found for search term \"$q\"",
                            photo = apiResponse?.photo,
                            photoDesc = apiResponse?.photoDesc,
                            isLoading = false
                        )
                    }
                } catch (e: Exception) {
                    _homeScreenState.update { currentState ->
                        currentState.copy(
                            title = "Error",
                            extract = "No internet connection",
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

    fun setDefaultContent(context: Context) {
        _homeScreenState.update { currentState ->
            currentState.copy(
                title = context.resources.getString(R.string.default_title),
                extract = context.resources.getString(R.string.default_extract)
            )
        }
    }

    fun focusSearchBar() {
        searchBarState.value.focusRequester.requestFocus()
    }
}