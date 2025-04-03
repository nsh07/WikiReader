package org.nsh07.wikireader.ui.viewModel

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.nsh07.wikireader.WikiReaderApplication
import org.nsh07.wikireader.data.AppPreferencesRepository
import org.nsh07.wikireader.data.WRStatus
import org.nsh07.wikireader.data.WikiApiPageData
import org.nsh07.wikireader.data.WikipediaRepository
import org.nsh07.wikireader.data.parseSections
import org.nsh07.wikireader.network.HostSelectionInterceptor
import org.nsh07.wikireader.parser.cleanUpWikitext
import org.nsh07.wikireader.parser.toWikitextAnnotatedString
import java.io.File
import java.io.FileOutputStream
import kotlin.io.path.listDirectoryEntries

class UiViewModel(
    private val interceptor: HostSelectionInterceptor,
    private val wikipediaRepository: WikipediaRepository,
    private val appPreferencesRepository: AppPreferencesRepository
) : ViewModel() {
    private val _searchBarState = MutableStateFlow(SearchBarState())
    val searchBarState: StateFlow<SearchBarState> = _searchBarState.asStateFlow()

    private val _homeScreenState = MutableStateFlow(HomeScreenState())
    val homeScreenState: StateFlow<HomeScreenState> = _homeScreenState.asStateFlow()

    private val _feedState = MutableStateFlow(FeedState())
    val feedState: StateFlow<FeedState> = _feedState.asStateFlow()

    private val _listState = MutableStateFlow(LazyListState(0, 0))
    val listState: StateFlow<LazyListState> = _listState.asStateFlow()

    private val _preferencesState = MutableStateFlow(PreferencesState())
    val preferencesState: StateFlow<PreferencesState> = _preferencesState.asStateFlow()

    private val _languageSearchStr = MutableStateFlow("")
    val languageSearchStr: StateFlow<String> = _languageSearchStr.asStateFlow()

    @OptIn(FlowPreview::class)
    val languageSearchQuery = languageSearchStr.debounce(500L)

    private val backStack = mutableListOf<Pair<String, String>>()
    private var lastQuery: Pair<String, String>? = null
    private var filesDir: String = ""
    private var loaderJob = Job()
        get() {
            if (field.isCancelled) field = Job()
            return field
        }
    private var searchDebounceJob: Job? = null
    private var colorScheme: ColorScheme = lightColorScheme()
    private var typography: Typography = Typography()

    var isReady = false
    var isAnimDurationComplete = false

    private var sections = 0
    private var currentSection = 0

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

            val renderMath = appPreferencesRepository.readBooleanPreference("render-math")
                ?: appPreferencesRepository.saveBooleanPreference("render-math", true)

            _preferencesState.update { currentState ->
                currentState.copy(
                    theme = theme,
                    lang = lang,
                    colorScheme = colorScheme,
                    fontSize = fontSize,
                    blackTheme = blackTheme,
                    expandedSections = expandedSections,
                    dataSaver = dataSaver,
                    renderMath = renderMath
                )
            }

            _searchBarState.update { currentState ->
                currentState.copy(history = appPreferencesRepository.readHistory() ?: emptySet())
            }

            interceptor.setHost("$lang.wikipedia.org")
            isReady = true
            loadFeed()
        }
    }

    fun startAnimDuration() {
        viewModelScope.launch {
            delay(600)
            isAnimDurationComplete = true
        }
    }

    fun setFilesDir(path: String) {
        filesDir = path
    }

    fun setCompositionLocals(cs: ColorScheme, tg: Typography) {
        colorScheme = cs
        typography = tg
    }

    private fun updateBackstack(q: String, setLang: String, fromBackStack: Boolean) {
        if (lastQuery != null) {
            if (!fromBackStack && (Pair(q, setLang) != lastQuery)) {
                backStack.add(lastQuery!!)
                Log.d(
                    "ViewModel",
                    "Add ${lastQuery?.first ?: "null"} : ${lastQuery?.second ?: "null"}"
                )
            }
            lastQuery = Pair(q, setLang)
        } else lastQuery = Pair(q, setLang)
    }

    private suspend fun loadSearchResults(query: String) {
        val q = query.trim()
        if (q.isNotEmpty()) {
            val searchResults = wikipediaRepository.getSearchResults(query)
            val results = searchResults.query.pages.sortedBy { it.index }
            val resultsParsed = results.map {
                it.copy(
                    snippet = it.snippet.replace("<span.+?(?<!/)>".toRegex(), "<b>")
                        .replace("</span>", "</b>"),
                    titleSnippet = it.titleSnippet.replace("<span.+?(?<!/)>".toRegex(), "<b>")
                        .replace("</span>", "</b>")
                )
            }
            _searchBarState.update { currentState ->
                currentState.copy(
                    searchResults = resultsParsed,
                    totalHits = searchResults.query.searchInfo.totalHits
                )
            }
        } else clearSearchResults()
    }

    private fun clearSearchResults() {
        _searchBarState.update { currentState ->
            currentState.copy(totalHits = 0, searchResults = emptyList())
        }
    }

    fun loadSearchResultsDebounced(query: String) {
        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            delay(500)
            loadSearchResults(query)
        }
    }

    fun loadSearch(
        query: String?,
        lang: String? = null,
        random: Boolean = false,
        fromBackStack: Boolean = false
    ) {
        val q = query?.trim() ?: " "
        val history = searchBarState.value.history.toMutableSet()
        if (q != "") {
            viewModelScope.launch {
                var setLang = preferencesState.value.lang
                if (lang != null) {
                    interceptor.setHost("$lang.wikipedia.org")
                    setLang = lang
                }
                _homeScreenState.update { currentState->
                    currentState.copy(isLoading = true, loadingProgress = null)
                }
                if (!random && !fromBackStack) {
                    history.remove(q)
                    history.add(q)
                    if (history.size > 50) history.remove(history.first())
                    appPreferencesRepository.saveHistory(history)
                }
                if (!random) loadSearchResults(q)
                try {
                    if (!random)
                        loadPage(
                            title = searchBarState.value.searchResults[0].title,
                            lang = setLang,
                            fromBackStack = fromBackStack
                        )
                    else
                        loadPage(title = null, random = true)
                } catch (_: Exception) {
                    _homeScreenState.update { currentState ->
                        currentState.copy(
                            title = "Error",
                            extract = listOf("No search results found for $q").map {
                                parseWikitext(
                                    it,
                                    0
                                )
                            },
                            photo = null,
                            photoDesc = null,
                            langs = null,
                            currentLang = setLang,
                            status = WRStatus.NO_SEARCH_RESULT,
                            pageId = null,
                            isLoading = false,
                            isBackStackEmpty = backStack.isEmpty(),
                            isSaved = false
                        )
                    }
                }
            }

            _searchBarState.update { currentState ->
                if (!random && !fromBackStack)
                    currentState.copy(
                        query = q,
                        isSearchBarExpanded = false,
                        history = history
                    )
                else
                    currentState.copy(isSearchBarExpanded = false)
            }
        }
    }

    /**
     * Updates history and loads page
     *
     * @param title Page title
     */
    fun loadPage(
        title: String?,
        lang: String? = null,
        random: Boolean = false,
        fromBackStack: Boolean = false
    ) {
        loaderJob.cancel()
        viewModelScope.launch(loaderJob) {
            var setLang = preferencesState.value.lang
            if (title != null) {
                Log.d("ViewModel", "Cancelled all jobs")

                if (lang != null) {
                    interceptor.setHost("$lang.wikipedia.org")
                    setLang = lang
                }
                if (!random) updateBackstack(title, setLang, fromBackStack)

                _homeScreenState.update { currentState ->
                    currentState.copy(isLoading = true, loadingProgress = null)
                }

                try {
                    val apiResponse = when (random) {
                        false -> wikipediaRepository
                            .getPageData(title)
                            .query
                            ?.pages?.get(0)

                        else -> wikipediaRepository
                            .getRandomResult()
                            .query
                            ?.pages?.get(0)
                    }

                    val extractText = if (apiResponse != null)
                        wikipediaRepository.getPageContent(apiResponse.title)
                    else ""
                    var saved = false
                    val extract: List<String> = parseSections(extractText)
                    val status: WRStatus = WRStatus.SUCCESS

                    try {
                        val articlesDir = File(filesDir, "savedArticles")

                        val apiFile = File(
                            articlesDir,
                            "${apiResponse!!.title}.${apiResponse.pageId}-api.${lastQuery!!.second}"
                        )
                        if (apiFile.exists()) saved = true
                    } catch (_: Exception) {
                    }

                    if (random && apiResponse != null)
                        updateBackstack(
                            apiResponse.title,
                            setLang,
                            fromBackStack
                        )

                    sections = extract.size
                    val parsedExtract = mutableListOf<List<AnnotatedString>>()

                    _homeScreenState.update{ currentState->
                        currentState.copy(
                            title = apiResponse?.title ?: "Error",
                            photo = apiResponse?.photo,
                            photoDesc = apiResponse?.photoDesc,
                            langs = apiResponse?.langs,
                            currentLang = setLang,
                            status = status,
                            pageId = apiResponse?.pageId,
                            isBackStackEmpty = backStack.isEmpty(),
                            isSaved = saved
                        )
                    }

                    listState.value.scrollToItem(0)

                    extract.forEachIndexed { index, it ->
                        currentSection = index + 1
                        parsedExtract.add(parseWikitext(it, index))
                        _homeScreenState.update { currentState ->
                            currentState.copy(
                                loadingProgress = currentSection.toFloat() / sections,
                                extract = parsedExtract,
                            )
                        }
                    }

                    _homeScreenState.update { currentState ->
                        currentState.copy(isLoading = false)
                    }
                    Log.d("ViewModel", "Search: HomeScreenState updated")
                } catch (e: Exception) {
                    Log.e("ViewModel", "Error in fetching results: ${e.message}")
                    e.printStackTrace()
                    _homeScreenState.update { currentState ->
                        currentState.copy(
                            title = "Error",
                            extract = listOf("An error occurred :(\nPlease check your internet connection").map {
                                parseWikitext(
                                    it, 0
                                )
                            },
                            langs = null,
                            currentLang = null,
                            photo = null,
                            photoDesc = null,
                            status = WRStatus.NETWORK_ERROR,
                            pageId = null,
                            isLoading = false,
                            isSaved = false
                        )
                    }
                    Log.d("ViewModel", "Search: HomeScreenState updated")
                }

                if (lang != null)
                    _preferencesState.update { currentState ->
                        currentState.copy(lang = lang)
                    }
            } else {
                _homeScreenState.update { currentState ->
                    currentState.copy(
                        title = "Error",
                        extract = listOf("Null search query").map {
                            parseWikitext(
                                it,
                                0
                            )
                        },
                        photo = null,
                        photoDesc = null,
                        langs = null,
                        currentLang = setLang,
                        status = WRStatus.OTHER,
                        pageId = null,
                        isLoading = false,
                        isBackStackEmpty = backStack.isEmpty(),
                        isSaved = false
                    )
                }
            }
        }
    }

    fun reloadPage(
        persistLang: Boolean = false
    ) {
        if (persistLang)
            loadPage(
                lastQuery?.first,
                lang = lastQuery?.second,
                random = false,
                fromBackStack = false
            )
        else
            loadPage(
                lastQuery?.first,
                random = false,
                fromBackStack = false
            )
    }

    /**
     * Loads feed, updates the [FeedState] and sets the app status to [WRStatus.FEED_LOADED]
     *
     * If an error is encountered, app status is set to [WRStatus.FEED_NETWORK_ERROR] and home screen
     * text is updated to the error
     */
    fun loadFeed(fromBack: Boolean = false) {
        viewModelScope.launch(loaderJob) {
            if (!preferencesState.value.dataSaver) {
                _homeScreenState.update { currentState ->
                    currentState.copy(isLoading = true, loadingProgress = null)
                }

                try {
                    val feed = wikipediaRepository.getFeed()

                    _feedState.update { currentState ->
                        currentState.copy(
                            tfa = feed.tfa,
                            mostReadArticles = feed.mostRead?.articles?.sortedBy { it.rank },
                            image = feed.image,
                            news = feed.news,
                            onThisDay = feed.onThisDay
                        )
                    }

                    if (!listOf(
                            WRStatus.SUCCESS,
                            WRStatus.NO_SEARCH_RESULT,
                            WRStatus.NETWORK_ERROR
                        ).contains(homeScreenState.value.status) || fromBack
                    ) _homeScreenState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            status = WRStatus.FEED_LOADED
                        )
                    }
                    else _homeScreenState.update { currentState ->
                        currentState.copy(isLoading = false)
                    }

                    Log.d("ViewModel", "Feed: HomeScreenState updated")
                } catch (e: Exception) {
                    Log.e("ViewModel", "Error in loading feed: ${e.message}")
                    _homeScreenState.update { currentState ->
                        currentState.copy(
                            title = "Error",
                            extract = listOf(
                                listOf(
                                    buildAnnotatedString {
                                        append(
                                            "An error occurred, feed could not be loaded :(\nPlease " +
                                                    "check your internet connection"
                                        )
                                    }
                                )
                            ),
                            langs = null,
                            currentLang = null,
                            photo = null,
                            photoDesc = null,
                            status = WRStatus.FEED_NETWORK_ERROR,
                            pageId = null,
                            isLoading = false,
                            isSaved = false
                        )
                    }
                    Log.d("ViewModel", "Feed: HomeScreenState updated")
                }
            } else {
                _homeScreenState.update { currentState ->
                    currentState.copy(status = WRStatus.UNINITIALIZED)
                }
            }
        }
    }

    /**
     * Saves the current article to the given directory
     *
     * @return A [WRStatus] enum value indicating the status of the save operation
     */
    suspend fun saveArticle(
        title: String? = null,
        lang: String? = null
    ): WRStatus {
        if (homeScreenState.value.status == WRStatus.UNINITIALIZED && title == null) {
            Log.e("ViewModel", "Cannot save article, HomeScreenState not initialized")
            return WRStatus.OTHER
        }

        val currentLang = lang ?: preferencesState.value.lang
        interceptor.setHost("${currentLang}.wikipedia.org")

        try {
            val pageTitle = title ?: homeScreenState.value.title
            val apiResponse = wikipediaRepository
                .getPageData(pageTitle)

            val apiResponseQuery = apiResponse
                .query
                ?.pages?.get(0)

            if (apiResponseQuery == null) {
                Log.e("ViewModel", "Cannot save article, apiResponse is null")
                interceptor.setHost("${preferencesState.value.lang}.wikipedia.org")
                return WRStatus.NO_SEARCH_RESULT
            }

            val pageContent = wikipediaRepository.getPageContent(apiResponseQuery.title)

            try {
                val apiFileName =
                    "${apiResponseQuery.title}.${apiResponseQuery.pageId}-api.${currentLang}"
                val contentFileName =
                    "${apiResponseQuery.title}.${apiResponseQuery.pageId}-content.${currentLang}"

                val articlesDir = File(filesDir, "savedArticles")
                articlesDir.mkdirs()

                val apiFile = File(
                    articlesDir,
                    apiFileName
                )
                val contentFile = File(
                    articlesDir,
                    contentFileName
                )

                FileOutputStream(apiFile).use {
                    it.write(Json.encodeToString(apiResponse).toByteArray())
                }
                FileOutputStream(contentFile).use {
                    it.write(pageContent.toByteArray())
                }

                if (title == null)
                    _homeScreenState.update { currentState ->
                        currentState.copy(isSaved = true)
                    }
                Log.d("ViewModel", "Updated saved state to ${homeScreenState.value.isSaved}")
                return WRStatus.SUCCESS
            } catch (e: Exception) {
                Log.e(
                    "ViewModel",
                    "Cannot save article, file IO error"
                )
                e.printStackTrace()
                interceptor.setHost("${preferencesState.value.lang}.wikipedia.org")
                return WRStatus.IO_ERROR
            }
        } catch (e: Exception) {
            Log.e("ViewModel", "Cannot save article, network error")
            e.printStackTrace()
            interceptor.setHost("${preferencesState.value.lang}.wikipedia.org")
            return WRStatus.NETWORK_ERROR
        }

        return WRStatus.OTHER
    }

    /**
     * Migrates articles downloaded in the pre-2.0 format (single file) to the new format (split api
     * and content files). The old file corresponding to an article is only deleted when the new files
     * have been successfully written, so accidental data loss is not a concern.
     *
     * It is recommended to run this function on every startup to ensure any pending migrations are
     * completed
     */
    fun migrateArticles() {
        Log.d("ViewModel", "Migrating pre-2.0 articles")
        val articleList =
            listArticles(filter = false).filterNot { it.contains("-api.") || it.contains("-content.") }
        val articlesDir = File(filesDir, "savedArticles")
        viewModelScope.launch {
            articleList.forEach { // Sequentially reload and save articles, then delete old files
                val oldFile = File(articlesDir, it)
                Log.d(
                    "ViewModel",
                    "Migrating ${it.substringBefore('.')} : ${it.substringAfterLast('.')}"
                )
                val saved =
                    saveArticle(title = it.substringBefore('.'), lang = it.substringAfterLast('.'))
                if (saved == WRStatus.SUCCESS) {
                    oldFile.delete()
                    Log.d("ViewModel", "Migrated $it")
                }
            }
            Log.d("ViewModel", "${articleList.size} articles migrated")
            interceptor.setHost("${preferencesState.value.lang}.wikipedia.org")
        }
    }

    /**
     * Deletes the current article
     *
     * @return A [WRStatus] enum value indicating the status of the delete operation
     */
    fun deleteArticle(apiFileName: String? = null): WRStatus {
        if (homeScreenState.value.status == WRStatus.UNINITIALIZED && apiFileName == null) {
            Log.e("ViewModel", "Cannot delete article, HomeScreenState is uninitialized")
            return WRStatus.OTHER
        }

        try {
            val articlesDir = File(filesDir, "savedArticles")

            val apiFile = if (apiFileName == null)
                File(
                    articlesDir,
                    "${homeScreenState.value.title}.${homeScreenState.value.pageId}-api.${homeScreenState.value.currentLang}"
                )
            else
                File(articlesDir, apiFileName)
            val contentFile = if (apiFileName == null)
                File(
                    articlesDir,
                    "${homeScreenState.value.title}.${homeScreenState.value.pageId}-content.${homeScreenState.value.currentLang}"
                )
            else
                File(articlesDir, apiFileName.replace("-api.", "-content."))

            val deleted = apiFile.delete() && contentFile.delete()
            if (deleted) {
                _homeScreenState.update { currentState ->
                    currentState.copy(isSaved = false)
                }
                return WRStatus.SUCCESS
            } else return WRStatus.IO_ERROR
        } catch (e: Exception) {
            Log.e("ViewModel", "Cannot delete article")
            e.printStackTrace()
            return WRStatus.IO_ERROR
        }
    }

    fun deleteAllArticles(): WRStatus {
        try {
            val articlesDir = File(filesDir, "savedArticles")
            val deleted = articlesDir.deleteRecursively()
            return if (deleted) WRStatus.SUCCESS
            else WRStatus.IO_ERROR
        } catch (e: Exception) {
            Log.e("ViewModel", "Cannot delete all articles")
            e.printStackTrace()
            return WRStatus.IO_ERROR
        }
    }

    fun listArticles(filter: Boolean = true): List<String> {
        try {
            val articlesDir = File(filesDir, "savedArticles")
            val directoryEntries = articlesDir.toPath().listDirectoryEntries()
            val out = mutableListOf<String>()
            directoryEntries.forEach {
                out.add(it.fileName.toString())
            }
            out.sort()
            Log.d("ViewModel", "${out.size} articles loaded")
            return if (filter) out.filter { it.contains("-api.") } else out
        } catch (e: Exception) {
            Log.e("ViewModel", "Cannot load list of downloaded articles, IO error")
            e.printStackTrace()
            return listOf<String>()
        }
    }

    fun totalArticlesSize(): Long {
        try {
            val size = File(filesDir, "savedArticles")
                .walkTopDown()
                .map { it.length() }
                .sum()
            Log.d("ViewModel", "Articles size loaded: $size")
            return size
        } catch (e: Exception) {
            Log.e("ViewModel", "Cannot load total article size, IO error")
            e.printStackTrace()
            return 0
        }
    }

    suspend fun loadSavedArticle(apiFileName: String): WRStatus =
        withContext(Dispatchers.IO) {
            try {
                _homeScreenState.update { currentState ->
                    currentState.copy(isLoading = true, loadingProgress = null)
                }
                val articlesDir = File(filesDir, "savedArticles")
                val apiFile = File(articlesDir, apiFileName)
                val contentFile = File(articlesDir, apiFileName.replace("-api.", "-content."))
                val apiResponse =
                    Json.decodeFromString<WikiApiPageData>(apiFile.readText()).query?.pages?.get(0)

                val extract: List<String> = parseSections(contentFile.readText())

                sections = extract.size
                val parsedExtract = mutableListOf<List<AnnotatedString>>()

                _preferencesState.update { currentState ->
                    currentState.copy(
                        lang = apiFileName.substringAfterLast('.')
                    )
                }

                _homeScreenState.update{ currentState->
                    currentState.copy(
                        title = apiResponse?.title ?: "Error",
                        photo = apiResponse?.photo,
                        photoDesc = apiResponse?.photoDesc,
                        langs = apiResponse?.langs,
                        currentLang = preferencesState.value.lang,
                        pageId = apiResponse?.pageId,
                        isBackStackEmpty = backStack.isEmpty(),
                        status = WRStatus.SUCCESS,
                        isSaved = true
                    )
                }

                extract.forEachIndexed { index, it ->
                    currentSection = index + 1
                    parsedExtract.add(parseWikitext(it, index))
                    _homeScreenState.update { currentState ->
                        currentState.copy(
                            loadingProgress = currentSection.toFloat() / sections,
                            extract = parsedExtract,
                        )
                    }
                }

                _homeScreenState.update { currentState ->
                    currentState.copy(
                        isLoading = false
                    )
                }

                if (apiResponse != null) updateBackstack(
                    apiResponse.title,
                    apiFileName.substringAfterLast('.'),
                    false
                )

                return@withContext WRStatus.SUCCESS
            } catch (e: Exception) {
                Log.e("ViewModel", "Cannot load saved article, IO error")
                e.printStackTrace()
                return@withContext WRStatus.IO_ERROR
            }
        }

    suspend fun parseWikitext(wikitext: String, index: Int): List<AnnotatedString> =
        withContext(Dispatchers.IO) {
            val parsed = cleanUpWikitext(wikitext, index)
            var curr = ""
            var i = 0
            val out = mutableListOf<AnnotatedString>()

            while (i < parsed.length) {
                if (parsed[i] == '<') {
                    val currSubstring = parsed.substring(i)
                    if (currSubstring.startsWith("<math display")) {
                        out.add(
                            curr.toWikitextAnnotatedString(
                                colorScheme = colorScheme,
                                typography = typography,
                                performSearch = ::loadPage,
                                fontSize = preferencesState.value.fontSize
                            )
                        )
                        curr = currSubstring.substringAfter('>').substringBefore("</math>")
                        out.add(buildAnnotatedString { append(curr) })
                        i += currSubstring.substringBefore('>').length + curr.length + "</math>".length
                        curr = ""
                    } else curr += parsed[i]
                } else curr += parsed[i]
                i++
            }
            out.add(
                curr.toWikitextAnnotatedString(
                    colorScheme = colorScheme,
                    typography = typography,
                    performSearch = { loadPage(it) },
                    fontSize = preferencesState.value.fontSize
                )
            )
            return@withContext out.toList()
        }

    fun popBackStack(): Pair<String, String>? {
        val res = backStack.removeLastOrNull()
        if (backStack.isEmpty()) lastQuery = null
        Log.d("ViewModel", "Pop ${res?.first ?: "null"} : ${res?.second ?: "null"}")
        return res
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
        interceptor.setHost("$lang.wikipedia.org")
        _preferencesState.update { currentState ->
            currentState.copy(
                lang = lang
            )
        }
        viewModelScope.launch {
            appPreferencesRepository.saveStringPreference("lang", lang)
        }
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

    fun saveRenderMath(renderMath: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.saveBooleanPreference("render-math", renderMath)
            _preferencesState.update { currentState ->
                currentState.copy(renderMath = renderMath)
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

    fun updateLanguageSearchStr(str: String) {
        _languageSearchStr.update {
            str
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