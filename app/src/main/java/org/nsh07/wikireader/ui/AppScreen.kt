package org.nsh07.wikireader.ui

import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.text.parseAsHtml
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import coil3.ImageLoader
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.svg.SvgDecoder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.nsh07.wikireader.data.SavedStatus
import org.nsh07.wikireader.data.WRStatus
import org.nsh07.wikireader.data.WikiPhoto
import org.nsh07.wikireader.data.WikiPhotoDesc
import org.nsh07.wikireader.ui.aboutScreen.AboutScreen
import org.nsh07.wikireader.ui.homeScreen.AppFab
import org.nsh07.wikireader.ui.homeScreen.AppHomeScreen
import org.nsh07.wikireader.ui.homeScreen.AppSearchBar
import org.nsh07.wikireader.ui.image.FullScreenImage
import org.nsh07.wikireader.ui.savedArticlesScreen.SavedArticlesScreen
import org.nsh07.wikireader.ui.settingsScreen.SettingsScreen
import org.nsh07.wikireader.ui.viewModel.PreferencesState
import org.nsh07.wikireader.ui.viewModel.UiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    viewModel: UiViewModel,
    preferencesState: PreferencesState,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
) {
    val appSearchBarState by viewModel.appSearchBarState.collectAsState()
    val homeScreenState by viewModel.homeScreenState.collectAsState()
    val feedState by viewModel.feedState.collectAsState()
    val savedArticlesState by viewModel.savedArticlesState.collectAsState()
    val listState by viewModel.articleListState.collectAsState()
    val searchListState by viewModel.searchListState.collectAsState()
    val searchBarState = rememberSearchBarState()
    val feedListState = rememberLazyListState()
    val languageSearchStr = viewModel.languageSearchStr.collectAsState()
    val languageSearchQuery = viewModel.languageSearchQuery.collectAsState("")
    var showArticleLanguageSheet by remember { mutableStateOf(false) }
    var deepLinkHandled by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    val searchBarScrollBehavior =
        if (
            windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT ||
            preferencesState.immersiveMode
        ) SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()
        else null
    val textFieldState = viewModel.textFieldState

    val imageLoader = ImageLoader.Builder(context)
        .components {
            add(SvgDecoder.Factory())
            if (SDK_INT >= 28) {
                add(AnimatedImageDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    val index by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val feedIndex by remember { derivedStateOf { feedListState.firstVisibleItemIndex } }
    val (showDeleteDialog, setShowDeleteDialog) = remember { mutableStateOf(false) }
    var (historyItem, setHistoryItem) = remember { mutableStateOf("") }

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Home(),
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it / 8 },
                animationSpec = tween(200)
            ) + fadeIn(tween(100))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 8 },
                animationSpec = tween(200)
            ) + fadeOut(tween(100))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 8 },
                animationSpec = tween(200)
            ) + fadeIn(tween(200))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it / 8 },
                animationSpec = tween(200)
            ) + fadeOut(tween(100))
        },
        modifier = modifier.background(MaterialTheme.colorScheme.background)
    ) {
        composable<Home>(
            deepLinks = listOf(
                navDeepLink { uriPattern = "{lang}.m.wikipedia.org/wiki/{query}" },
                navDeepLink { uriPattern = "{lang}.wikipedia.org/wiki/{query}" }
            )
        ) { backStackEntry ->
            val uriQuery = remember { backStackEntry.arguments?.getString("query") }
            LaunchedEffect(uriQuery) {
                if (uriQuery != null && !deepLinkHandled) {
                    Log.d("AppScreen", "Deep link handled: uriQuery: $uriQuery")
                    deepLinkHandled = true
                    val lang = backStackEntry.arguments?.getString("lang")
                    viewModel.stopAll()
                    delay(500) // Avoids a race condition where the hostname might not get updated in time
                    viewModel.loadPage(
                        uriQuery,
                        lang = lang
                    )
                }
            }

            BackHandler(
                enabled = if (deepLinkHandled) {
                    homeScreenState.backStackSize >= 1
                } else {
                    homeScreenState.backStackSize != 0 ||
                            (homeScreenState.status != WRStatus.FEED_LOADED &&
                                    homeScreenState.status != WRStatus.FEED_NETWORK_ERROR &&
                                    homeScreenState.status != WRStatus.UNINITIALIZED)
                },
                onBack = viewModel::loadPreviousPage
            )

            if (showDeleteDialog)
                DeleteHistoryItemDialog(
                    historyItem,
                    setShowDeleteDialog
                ) {
                    if (historyItem != "") viewModel.removeHistoryItem(it)
                    else viewModel.clearHistory()
                }

            Scaffold(
                topBar = {
                    AppSearchBar(
                        appSearchBarState = appSearchBarState,
                        searchBarState = searchBarState,
                        preferencesState = preferencesState,
                        textFieldState = textFieldState,
                        scrollBehavior = searchBarScrollBehavior,
                        searchBarEnabled = !showArticleLanguageSheet,
                        dataSaver = preferencesState.dataSaver,
                        imageLoader = imageLoader,
                        searchListState = searchListState,
                        windowSizeClass = windowSizeClass,
                        loadSearch = {
                            coroutineScope.launch {
                                searchBarState.animateToCollapsed()
                            }
                            viewModel.loadSearch(it)
                        },
                        loadSearchDebounced = viewModel::loadSearchResultsDebounced,
                        loadPage = viewModel::loadPage,
                        onExpandedChange = {
                            coroutineScope.launch {
                                if (it) searchBarState.animateToExpanded()
                                else searchBarState.animateToCollapsed()
                            }
                        },
                        setQuery = textFieldState::setTextAndPlaceCursorAtEnd,
                        clearHistory = {
                            setHistoryItem("")
                            setShowDeleteDialog(true)
                        },
                        removeHistoryItem = {
                            setHistoryItem(it)
                            setShowDeleteDialog(true)
                        },
                        onSavedArticlesClick = {
                            navController.navigate(SavedArticles)
                            it(false)
                        },
                        onSettingsClick = {
                            navController.navigate(Settings)
                            it(false)
                        },
                        onAboutClick = {
                            navController.navigate(About)
                            it(false)
                        }
                    )
                },
                floatingActionButton = {
                    AppFab(
                        index = if (homeScreenState.status != WRStatus.FEED_LOADED) index else feedIndex,
                        visible = if (preferencesState.immersiveMode) {
                            if (homeScreenState.status != WRStatus.FEED_LOADED)
                                listState.lastScrolledBackward else feedListState.lastScrolledBackward
                        } else true,
                        focusSearch = {
                            viewModel.focusSearchBar()
                            textFieldState.setTextAndPlaceCursorAtEnd(textFieldState.text.toString())
                        },
                        scrollToTop = {
                            coroutineScope.launch {
                                if (homeScreenState.status != WRStatus.FEED_LOADED)
                                    listState.animateScrollToItem(0)
                                else
                                    feedListState.animateScrollToItem(0)
                            }
                        },
                        performRandomPageSearch = {
                            coroutineScope.launch {
                                searchBarState.animateToCollapsed()
                            }
                            viewModel.loadPage(
                                title = null,
                                random = true
                            )
                        }
                    )
                },
                snackbarHost = { SnackbarHost(snackBarHostState) },
                modifier =
                    if (searchBarScrollBehavior != null)
                        Modifier
                            .fillMaxSize()
                            .nestedScroll(searchBarScrollBehavior.nestedScrollConnection)
                    else Modifier.fillMaxSize()
            ) { insets ->
                AppHomeScreen(
                    homeScreenState = homeScreenState,
                    listState = listState,
                    preferencesState = preferencesState,
                    feedState = feedState,
                    feedListState = feedListState,
                    imageLoader = imageLoader,
                    languageSearchStr = languageSearchStr.value,
                    languageSearchQuery = languageSearchQuery.value,
                    showLanguageSheet = showArticleLanguageSheet,
                    onImageClick = {
                        if (homeScreenState.photo != null || homeScreenState.status == WRStatus.FEED_LOADED)
                            navController.navigate(FullScreenImage)
                    },
                    onLinkClick = viewModel::loadPage,
                    refreshSearch = { viewModel.reloadPage(true) },
                    refreshFeed = viewModel::loadFeed,
                    setLang = viewModel::saveLang,
                    setSearchStr = viewModel::updateLanguageSearchStr,
                    setShowArticleLanguageSheet = { showArticleLanguageSheet = it },
                    saveArticle = {
                        coroutineScope.launch {
                            if (homeScreenState.savedStatus == SavedStatus.NOT_SAVED) {
                                val status = viewModel.saveArticle()
                                if (status == WRStatus.SUCCESS)
                                    snackBarHostState.showSnackbar("Article saved for offline reading")
                                else
                                    snackBarHostState.showSnackbar("Unable to save article: ${status.name}")
                                delay(150L)
                            } else if (homeScreenState.savedStatus == SavedStatus.SAVED) {
                                val status = viewModel.deleteArticle()
                                if (status == WRStatus.SUCCESS)
                                    snackBarHostState.showSnackbar("Article deleted")
                                else
                                    snackBarHostState.showSnackbar("Unable to delete article: ${status.name}")
                            }
                        }
                    },
                    showFeedErrorSnackBar = {
                        coroutineScope.launch {
                            if (!deepLinkHandled)
                                snackBarHostState
                                    .showSnackbar("Unable to load feed: ${homeScreenState.status.name}")
                        }
                    },
                    insets = insets,
                    windowSizeClass = windowSizeClass,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = insets.calculateTopPadding()),
                    deepLinkHandled = deepLinkHandled
                )
            }
        }

        composable<FullScreenImage> {
            if (homeScreenState.status != WRStatus.FEED_LOADED) {
                if (homeScreenState.photo == null) navController.navigateUp()
                FullScreenImage(
                    photo = homeScreenState.photo,
                    photoDesc = homeScreenState.photoDesc,
                    title = homeScreenState.title,
                    imageLoader = imageLoader,
                    link = homeScreenState.photo?.source,
                    onBack = navController::navigateUp
                )
            } else {
                FullScreenImage(
                    photo = WikiPhoto(
                        source = feedState.image?.image?.source ?: "",
                        width = feedState.image?.image?.width ?: 1,
                        height = feedState.image?.image?.height ?: 1
                    ),
                    photoDesc = WikiPhotoDesc(
                        label = listOf(
                            feedState.image?.description?.text?.parseAsHtml().toString()
                        ),
                        description = null
                    ),
                    title = feedState.image?.title ?: "",
                    imageLoader = imageLoader,
                    link = feedState.image?.filePage,
                    onBack = navController::navigateUp
                )
            }
        }

        composable<SavedArticles> {
            SavedArticlesScreen(
                savedArticlesState = savedArticlesState,
                windowSizeClass = windowSizeClass,
                openSavedArticle = {
                    coroutineScope.launch {
                        navController.navigateUp()
                        viewModel.loadSavedArticle(it)
                    }
                },
                deleteArticle = viewModel::deleteArticle,
                deleteAll = viewModel::deleteAllArticles,
                onBack = {
                    navController.navigateUp()
                    viewModel.updateLanguageFilters()
                }
            )
        }

        composable<Settings> {
            SettingsScreen(
                preferencesState = preferencesState,
                homeScreenState = homeScreenState,
                onBack = navController::navigateUp,
                viewModel = viewModel,
                windowSizeClass = windowSizeClass
            )
        }

        composable<About> {
            AboutScreen(
                windowSizeClass = windowSizeClass,
                onBack = navController::navigateUp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteHistoryItemDialog(
    item: String,
    setShowDeleteDialog: (Boolean) -> Unit,
    removeHistoryItem: (String) -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = { setShowDeleteDialog(false) }
    ) {
        val titleText =
            if (item != "") "Delete this search from your history?"
            else "Delete your search history?"
        val descText =
            if (item != "") "\"$item\" will be permanently deleted from your search history."
            else "Your search history will be permanently deleted."

        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.padding(16.dp))
                Text(
                    text = descText,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = { setShowDeleteDialog(false) }) {
                        Text(text = "Cancel")
                    }
                    TextButton(onClick = {
                        setShowDeleteDialog(false)
                        removeHistoryItem(item)
                    }) {
                        Text(text = "Delete")
                    }
                }
            }
        }
    }
}

@Serializable
data class Home(
    val lang: String? = null,
    val query: String? = null
)

@Serializable
object FullScreenImage

@Serializable
object SavedArticles

@Serializable
object Settings

@Serializable
object About
