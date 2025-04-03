package org.nsh07.wikireader.ui.viewModel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import org.nsh07.wikireader.data.FeedApiImage
import org.nsh07.wikireader.data.FeedApiNews
import org.nsh07.wikireader.data.FeedApiOTD
import org.nsh07.wikireader.data.FeedApiTFA
import org.nsh07.wikireader.data.MostReadArticle
import org.nsh07.wikireader.data.WRStatus
import org.nsh07.wikireader.data.WikiLang
import org.nsh07.wikireader.data.WikiPhoto
import org.nsh07.wikireader.data.WikiPhotoDesc
import org.nsh07.wikireader.data.WikiSearchResult

@Immutable
data class SearchBarState(
    val query: String = "",
    val isSearchBarExpanded: Boolean = false,
    val searchResults: List<WikiSearchResult> = emptyList(),
    val totalHits: Int = 0,
    val history: Set<String> = setOf(),
    val focusRequester: FocusRequester = FocusRequester()
)

@Immutable
data class HomeScreenState(
    val title: String = "",
    val extract: List<List<AnnotatedString>> = emptyList(),
    val photo: WikiPhoto? = null,
    val photoDesc: WikiPhotoDesc? = null,
    val langs: List<WikiLang>? = null,
    val currentLang: String? = null,
    val pageId: Int? = null,
    val status: WRStatus = WRStatus.UNINITIALIZED,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false,
    val loadingProgress: Float? = null,
    val isBackStackEmpty: Boolean = true
)

@Immutable
data class PreferencesState(
    val theme: String = "auto",
    val lang: String = "en",
    val colorScheme: String = Color.White.toString(),
    val fontSize: Int = 16,
    val blackTheme: Boolean = false,
    val expandedSections: Boolean = false,
    val dataSaver: Boolean = false,
    val renderMath: Boolean = true
)

@Immutable
data class FeedState(
    val tfa: FeedApiTFA? = null,
    val mostReadArticles: List<MostReadArticle>? = null,
    val image: FeedApiImage? = null,
    val news: List<FeedApiNews>? = null,
    val onThisDay: List<FeedApiOTD>? = null
)