package org.nsh07.wikireader.ui.homeScreen.viewModel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.AnnotatedString
import org.nsh07.wikireader.data.FeedApiImage
import org.nsh07.wikireader.data.FeedApiNews
import org.nsh07.wikireader.data.FeedApiOTD
import org.nsh07.wikireader.data.FeedApiTFA
import org.nsh07.wikireader.data.MostReadArticle
import org.nsh07.wikireader.data.SavedStatus
import org.nsh07.wikireader.data.WRStatus
import org.nsh07.wikireader.data.WikiLang
import org.nsh07.wikireader.data.WikiPhoto
import org.nsh07.wikireader.data.WikiPrefixSearchResult
import org.nsh07.wikireader.data.WikiSearchResult

@Immutable
data class AppSearchBarState(
    val prefixSearchResults: List<WikiPrefixSearchResult>? = emptyList(),
    val searchResults: List<WikiSearchResult>? = emptyList(),
    val focusRequester: FocusRequester = FocusRequester()
)

@Immutable
data class HomeScreenState(
    val title: String = "",
    val ref: AnnotatedString = AnnotatedString(""),
    val extract: List<List<AnnotatedString>> = emptyList(),
    val sections: List<Pair<Int, String>> = emptyList(),
    val photo: WikiPhoto? = null,
    val photoDesc: String? = null,
    val langs: List<WikiLang>? = null,
    val currentLang: String? = null,
    val pageId: Int? = null,
    val status: WRStatus = WRStatus.UNINITIALIZED,
    val savedStatus: SavedStatus = SavedStatus.NOT_SAVED,
    val isLoading: Boolean = false,
    val showRef: Boolean = false,
    val loadingProgress: Float? = null,
    val backStackSize: Int = 0
)

@Immutable
data class FeedState(
    val tfa: FeedApiTFA? = null,
    val mostReadArticles: List<MostReadArticle>? = null,
    val image: FeedApiImage? = null,
    val news: List<FeedApiNews>? = null,
    val onThisDay: List<FeedApiOTD>? = null,
    val sections: List<Pair<Int, FeedSection>> = emptyList()
)

enum class FeedSection {
    TFA, MOST_READ, IMAGE, NEWS, ON_THIS_DAY
}