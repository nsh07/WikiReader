package org.nsh07.wikireader.ui.homeScreen.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Immutable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.AnnotatedString
import org.nsh07.wikireader.data.FeedApiImage
import org.nsh07.wikireader.data.FeedApiNews
import org.nsh07.wikireader.data.FeedApiOTD
import org.nsh07.wikireader.data.FeedApiTFA
import org.nsh07.wikireader.data.MostReadArticle
import org.nsh07.wikireader.data.SavedStatus
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
    val isLoading: Boolean = false,
    val loadingProgress: Float? = null,
    val showRef: Boolean = false
)

sealed class HomeSubscreen {
    data class Article(
        val title: String = "",
        val ref: AnnotatedString = AnnotatedString(""),
        val extract: List<List<AnnotatedString>> = emptyList(),
        val sections: List<Pair<Int, String>> = emptyList(),
        val listState: LazyListState = LazyListState(),
        val photo: WikiPhoto? = null,
        val photoDesc: String? = null,
        val langs: List<WikiLang>? = null,
        val currentLang: String? = null,
        val pageId: Int? = null,
        val savedStatus: SavedStatus = SavedStatus.NOT_SAVED
    ) : HomeSubscreen()

    data class Feed(
        val tfa: FeedApiTFA? = null,
        val mostReadArticles: List<MostReadArticle>? = null,
        val image: FeedApiImage? = null,
        val news: List<FeedApiNews>? = null,
        val onThisDay: List<FeedApiOTD>? = null,
        val sections: List<Pair<Int, FeedSection>> = emptyList(),
        val listState: LazyListState = LazyListState()
    ) : HomeSubscreen()

    object FeedLoader : HomeSubscreen()
    object Logo : HomeSubscreen()
}

enum class FeedSection {
    TFA, MOST_READ, IMAGE, NEWS, ON_THIS_DAY
}