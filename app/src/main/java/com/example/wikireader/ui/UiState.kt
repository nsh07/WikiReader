package com.example.wikireader.ui

import com.example.wikireader.data.WikiPhoto
import com.example.wikireader.data.WikiPhotoDesc

data class SearchBarState(
    val query: String = "",
    val isSearchBarExpanded: Boolean = false,
    val history: List<String> = listOf()
)

data class HomeScreenState(
    val title: String = "",
    val extract: String = "",
    val photo: WikiPhoto? = null,
    val photoDesc: WikiPhotoDesc? = null,
    val isLoading: Boolean = false
)
