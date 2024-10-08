package org.nsh07.wikireader.ui

import androidx.compose.ui.focus.FocusRequester
import org.nsh07.wikireader.data.WikiPhoto
import org.nsh07.wikireader.data.WikiPhotoDesc

data class SearchBarState(
    val query: String = "",
    val isSearchBarExpanded: Boolean = false,
    val history: List<String> = listOf(),
    val focusRequester: FocusRequester = FocusRequester()
)

data class HomeScreenState(
    val title: String = "",
    val extract: String = "",
    val photo: WikiPhoto? = null,
    val photoDesc: WikiPhotoDesc? = null,
    val isLoading: Boolean = false
)
