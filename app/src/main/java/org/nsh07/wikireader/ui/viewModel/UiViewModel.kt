package org.nsh07.wikireader.ui.viewModel

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.nsh07.wikireader.WikiReaderApplication
import org.nsh07.wikireader.data.AppPreferencesRepository
import org.nsh07.wikireader.data.WRStatus
import org.nsh07.wikireader.data.WikiApiResponse
import org.nsh07.wikireader.data.WikipediaRepository
import org.nsh07.wikireader.data.parseText
import org.nsh07.wikireader.network.HostSelectionInterceptor
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

    var isReady = false
    var isAnimDurationComplete = false

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

    private fun updateBackstack(q: String, setLang: String, fromBackStack: Boolean) {
        if (lastQuery != null) {
            if (!fromBackStack && (Pair(q, setLang) != lastQuery)) {
                backStack.add(lastQuery!!)
                Log.d(
                    "BackStack",
                    "Add ${lastQuery?.first ?: "null"} : ${lastQuery?.second ?: "null"}"
                )
            }
            lastQuery = Pair(q, setLang)
        } else lastQuery = Pair(q, setLang)
    }

    /**
     * Updates history and performs search
     *
     * The search query string is trimmed before being added to the history and performing the search
     *
     * @param query Search query string
     */
    fun performSearch(
        query: String?,
        lang: String? = null,
        random: Boolean = false,
        fromLink: Boolean = false,
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
                if (!random && !fromLink && !fromBackStack) {
                    history.remove(q)
                    history.add(q)
                    if (history.size > 50) history.remove(history.first())
                }

                if (!random) updateBackstack(q, setLang, fromBackStack)

                _homeScreenState.update { currentState ->
                    currentState.copy(isLoading = true)
                }

                if (!random && !fromLink && !fromBackStack)
                    appPreferencesRepository.saveHistory(history)

                try {
                    val apiResponse = when (random) {
                        false -> wikipediaRepository
                            .getSearchResult(q)
                            .query
                            ?.pages?.get(0)

                        else -> wikipediaRepository
                            .getRandomResult()
                            .query
                            ?.pages?.get(0)
                    }

                    val extractText = apiResponse
                        ?.extract ?: ""

                    val extract: List<String>
                    val status: WRStatus
                    var saved = false
                    if (extractText != "") {
                        extract = parseText(extractText)
                        status = WRStatus.SUCCESS

                        try {
                            val articlesDir = File(filesDir, "savedArticles")

                            val file = File(
                                articlesDir,
                                "${apiResponse!!.title}.${apiResponse.pageId}.${lastQuery!!.second}"
                            )
                            if (file.exists()) saved = true
                        } catch (_: Exception) {
                        }
                    } else {
                        extract = listOf("No search results found for \"$q\"")
                        status = WRStatus.NO_SEARCH_RESULT
                    }

                    if (random && apiResponse != null)
                        updateBackstack(
                            apiResponse.title,
                            setLang,
                            fromBackStack
                        )

                    _homeScreenState.update { currentState ->
                        currentState.copy(
                            title = apiResponse?.title ?: "Error",
                            extract = extract,
                            photo = apiResponse?.photo,
                            photoDesc = apiResponse?.photoDesc,
                            langs = apiResponse?.langs,
                            currentLang = setLang,
                            status = status,
                            pageId = apiResponse?.pageId,
                            isLoading = false,
                            isBackStackEmpty = backStack.isEmpty(),
                            isSaved = saved
                        )
                    }
                } catch (e: Exception) {
                    Log.e("ViewModel", "Error in fetching results: ${e.message}")
                    _homeScreenState.update { currentState ->
                        currentState.copy(
                            title = "Error",
                            extract = listOf("An error occurred :(\nPlease check your internet connection"),
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
                }

                if (lang != null)
                    _preferencesState.update { currentState ->
                        currentState.copy(lang = lang)
                    }

                listState.value.scrollToItem(0)
            }
        }

        _searchBarState.update { currentState ->
            if (!random && !fromLink && !fromBackStack)
                currentState.copy(
                    query = q,
                    isSearchBarExpanded = false,
                    history = history
                )
            else
                currentState.copy(isSearchBarExpanded = false)
        }
    }

    fun refreshSearch(
        random: Boolean = false,
        fromLink: Boolean = false,
        fromBackStack: Boolean = false
    ) {
        performSearch(
            lastQuery?.first,
            random = random,
            fromLink = fromLink,
            fromBackStack = fromBackStack
        )
    }

    /**
     * Saves the current article to the given directory
     *
     * @return A [WRStatus] enum value indicating the status of the save operation
     */
    suspend fun saveArticle(): WRStatus {
        if (homeScreenState.value.status == WRStatus.UNINITIALIZED) {
            Log.e("SaveArticle", "Cannot save article, HomeScreenState not initialized")
            return WRStatus.OTHER
        }

        val currentLang = preferencesState.value.lang
        interceptor.setHost("${homeScreenState.value.currentLang}.wikipedia.org")

        try {
            val apiResponse = wikipediaRepository
                .getSearchResult(homeScreenState.value.title)

            val apiResponseQuery = apiResponse
                .query
                ?.pages?.get(0)

            if (apiResponseQuery == null) {
                Log.e("SaveArticle", "Cannot save article, apiResponse is null")
                interceptor.setHost("$currentLang.wikipedia.org")
                return WRStatus.NO_SEARCH_RESULT
            }

            try {
                val fileName =
                    "${apiResponseQuery.title}.${apiResponseQuery.pageId}.${homeScreenState.value.currentLang}"
                val articlesDir = File(filesDir, "savedArticles")
                articlesDir.mkdirs()

                val file = File(
                    articlesDir,
                    fileName
                )

                FileOutputStream(file).use {
                    it.write(Json.encodeToString(apiResponse).toByteArray())
                }

                _homeScreenState.update { currentState ->
                    currentState.copy(isSaved = true)
                }
                Log.d("HomeScreenState", "Updated saved state to ${homeScreenState.value.isSaved}")
                return WRStatus.SUCCESS
            } catch (e: Exception) {
                Log.e(
                    "SaveArticle",
                    "Cannot save article, file IO error"
                )
                e.printStackTrace()
                interceptor.setHost("$currentLang.wikipedia.org")
                return WRStatus.IO_ERROR
            }
        } catch (_: Exception) {
            Log.e("SaveArticle", "Cannot save article, network error")
            interceptor.setHost("$currentLang.wikipedia.org")
            return WRStatus.NETWORK_ERROR
        }

        return WRStatus.OTHER
    }

    /**
     * Deletes the current article
     *
     * @return A [WRStatus] enum value indicating the status of the delete operation
     */
    fun deleteArticle(fileName: String? = null): WRStatus {
        if (homeScreenState.value.status == WRStatus.UNINITIALIZED && fileName == null) {
            Log.e("DeleteArticle", "Cannot delete article, HomeScreenState is uninitialized")
            return WRStatus.OTHER
        }

        try {
            val articlesDir = File(filesDir, "savedArticles")
            val file = if (fileName == null)
                File(
                    articlesDir,
                    "${homeScreenState.value.title}.${homeScreenState.value.pageId}.${homeScreenState.value.currentLang}"
                )
            else
                File(articlesDir, fileName)

            val deleted = file.delete()
            if (deleted) {
                _homeScreenState.update { currentState ->
                    currentState.copy(isSaved = false)
                }
                return WRStatus.SUCCESS
            } else return WRStatus.IO_ERROR
        } catch (e: Exception) {
            Log.e("DeleteArticle", "Cannot delete article")
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
            Log.e("DeleteArticle", "Cannot delete all articles")
            e.printStackTrace()
            return WRStatus.IO_ERROR
        }
    }

    fun listArticles(): List<String> {
        try {
            val articlesDir = File(filesDir, "savedArticles")
            val directoryEntries = articlesDir.toPath().listDirectoryEntries()
            val out = mutableListOf<String>()
            directoryEntries.forEach {
                out.add(it.fileName.toString())
            }
            out.sort()
            Log.d("ListArticles", "${out.size} articles loaded")
            return out.toList()
        } catch (e: Exception) {
            Log.e("ListArticles", "Cannot load list of downloaded articles, IO error")
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
            Log.d("ArticlesSize", "Articles size loaded: $size")
            return size
        } catch (e: Exception) {
            Log.e("ArticlesSize", "Cannot load total article size, IO error")
            e.printStackTrace()
            return 0
        }
    }

    fun loadSavedArticle(fileName: String): WRStatus {
        try {
            val articlesDir = File(filesDir, "savedArticles")
            val file = File(articlesDir, fileName)
            val apiResponse =
                Json.decodeFromString<WikiApiResponse>(file.readText()).query?.pages?.get(0)

            val extract: List<String> = if (apiResponse?.extract != null)
                parseText(apiResponse.extract)
            else
                listOf("Unknown error")

            _preferencesState.update { currentState ->
                currentState.copy(
                    lang = fileName.substringAfterLast('.')
                )
            }
            _homeScreenState.update { currentState ->
                currentState.copy(
                    title = apiResponse?.title ?: "Error",
                    extract = extract,
                    photo = apiResponse?.photo,
                    photoDesc = apiResponse?.photoDesc,
                    langs = apiResponse?.langs,
                    currentLang = preferencesState.value.lang,
                    status = WRStatus.SUCCESS,
                    pageId = apiResponse?.pageId,
                    isLoading = false,
                    isBackStackEmpty = backStack.isEmpty(),
                    isSaved = true
                )
            }

            return WRStatus.SUCCESS
        } catch (e: Exception) {
            Log.e("LoadSavedArticle", "Cannot load saved article, IO error")
            e.printStackTrace()
            return WRStatus.IO_ERROR
        }
    }

    fun popBackStack(): Pair<String, String>? {
        val res = backStack.removeLastOrNull()
        Log.d("BackStack", "Pop ${res?.first ?: "null"} : ${res?.second ?: "null"}")
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