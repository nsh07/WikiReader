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
import org.nsh07.wikireader.data.SavedStatus
import org.nsh07.wikireader.data.WRStatus
import org.nsh07.wikireader.data.WikiLang
import org.nsh07.wikireader.data.WikiPhoto
import org.nsh07.wikireader.data.WikiPhotoDesc
import org.nsh07.wikireader.data.WikiPrefixSearchResult
import org.nsh07.wikireader.data.WikiSearchResult
import org.nsh07.wikireader.ui.savedArticlesScreen.LanguageFilterOption

@Immutable
data class AppSearchBarState(
    val prefixSearchResults: List<WikiPrefixSearchResult>? = emptyList(),
    val searchResults: List<WikiSearchResult>? = emptyList(),
    val history: Set<String> = setOf(),
    val focusRequester: FocusRequester = FocusRequester()
)

@Immutable
data class HomeScreenState(
    val title: String = "",
    val ref: String = "",
    val extract: List<List<AnnotatedString>> = emptyList(),
    val sections: List<Pair<Int, String>> = emptyList(),
    val photo: WikiPhoto? = null,
    val photoDesc: WikiPhotoDesc? = null,
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
data class PreferencesState(
    val theme: String = "auto",
    val lang: String = "en",
    val fontStyle: String = "sans",
    val colorScheme: String = Color.White.toString(),
    val fontSize: Int = 16,
    val blackTheme: Boolean = false,
    val dataSaver: Boolean = false,
    val feedEnabled: Boolean = true,
    val expandedSections: Boolean = false,
    val imageBackground: Boolean = false,
    val immersiveMode: Boolean = true,
    val renderMath: Boolean = true,
    val searchHistory: Boolean = true
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

@Immutable
data class SavedArticlesState(
    val isLoading: Boolean = false,
    val savedArticles: List<String> = emptyList(),
    val languageFilters: List<LanguageFilterOption> = emptyList(),
    val articlesSize: Long = 0L
)

enum class FeedSection {
    TFA, MOST_READ, IMAGE, NEWS, ON_THIS_DAY
}