package org.nsh07.wikireader.ui

import android.os.Build.VERSION.SDK_INT
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.material3.rememberWideNavigationRailState
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.text.parseAsHtml
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import androidx.window.core.layout.WindowSizeClass
import coil3.ImageLoader
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.svg.SvgDecoder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.nsh07.wikireader.R.string
import org.nsh07.wikireader.data.SavedStatus
import org.nsh07.wikireader.data.SearchHistoryItem
import org.nsh07.wikireader.data.UserLanguage
import org.nsh07.wikireader.data.WRStatus
import org.nsh07.wikireader.data.WikiPhoto
import org.nsh07.wikireader.ui.aboutScreen.AboutScreen
import org.nsh07.wikireader.ui.historyScreen.HistoryScreenRoot
import org.nsh07.wikireader.ui.homeScreen.AppHomeScreen
import org.nsh07.wikireader.ui.homeScreen.AppSearchBar
import org.nsh07.wikireader.ui.image.FullScreenImage
import org.nsh07.wikireader.ui.savedArticlesScreen.SavedArticlesScreenRoot
import org.nsh07.wikireader.ui.settingsScreen.SettingsScreenRoot
import org.nsh07.wikireader.ui.settingsScreen.viewModel.SettingsViewModel
import org.nsh07.wikireader.ui.viewModel.UiViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppScreen(
    viewModel: UiViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val preferencesState by settingsViewModel.preferencesState.collectAsStateWithLifecycle()

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val appSearchBarState by viewModel.appSearchBarState.collectAsStateWithLifecycle()
    val homeScreenState by viewModel.homeScreenState.collectAsStateWithLifecycle()
    val feedState by viewModel.feedState.collectAsStateWithLifecycle()
    val listState by viewModel.articleListState.collectAsStateWithLifecycle()
    val searchListState by viewModel.searchListState.collectAsStateWithLifecycle()

    val searchHistory by viewModel.searchHistoryFlow.collectAsState(emptyList())
    val recentLangs by viewModel.recentLangsFlow.collectAsStateWithLifecycle(emptyList())
    val userLangs by viewModel.userLangsFlow.collectAsState(
        listOf(
            UserLanguage(
                "en",
                "English",
                true
            )
        )
    )

    val searchBarState = rememberSearchBarState()
    val feedListState = rememberLazyListState()
    val railState = rememberWideNavigationRailState()
    val languageSearchStr by viewModel.languageSearchStr.collectAsStateWithLifecycle()
    val languageSearchQuery by viewModel.languageSearchQuery.collectAsState("")
    val motionScheme = motionScheme
    var showArticleLanguageSheet by rememberSaveable { mutableStateOf(false) }
    var deepLinkHandled by rememberSaveable { mutableStateOf(false) }

    val searchBarScrollBehavior =
        if (
            !windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND) ||
            preferencesState.immersiveMode
        ) TopAppBarDefaults.enterAlwaysScrollBehavior()
        else TopAppBarDefaults.pinnedScrollBehavior()
    val floatingToolbarScrollBehaviour =
        if (
            !windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND) ||
            preferencesState.immersiveMode
        ) FloatingToolbarDefaults.exitAlwaysScrollBehavior(
            exitDirection = FloatingToolbarExitDirection.Bottom,
            snapAnimationSpec = motionScheme.defaultSpatialSpec()
        )
        else null
    val textFieldState = viewModel.textFieldState

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory(scaleToDensity = true))
                if (SDK_INT >= 28) {
                    add(AnimatedImageDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    val snackBarHostState = remember { SnackbarHostState() }

    val index by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val feedIndex by remember { derivedStateOf { feedListState.firstVisibleItemIndex } }
    val (showDeleteDialog, setShowDeleteDialog) = remember { mutableStateOf(false) }
    var historyItem: SearchHistoryItem? by remember { mutableStateOf(null) }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    AppNavigationDrawer(
        state = railState,
        feedSections = feedState.sections,
        homeScreenSections = homeScreenState.sections,
        homeScreenStatus = homeScreenState.status,
        listState = listState,
        feedListState = feedListState,
        windowSizeClass = windowSizeClass,
        backStackEntry = navBackStackEntry,
        historyEnabled = preferencesState.browsingHistory,
        onAboutClick = {
            navController.navigate(AboutScreen) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        onHistoryClick = {
            navController.navigate(HistoryScreen) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        onHomeClick = {
            if (navBackStackEntry?.destination?.hasRoute(HomeScreen::class) == true) {
                viewModel.loadFeed(true)
            } else {
                navController.navigate(HomeScreen()) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        },
        onSavedArticlesClick = {
            navController.navigate(SavedArticlesScreen) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        onSettingsClick = {
            navController.navigate(SettingsScreen) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        modifier = modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        val compactWindow =
            !windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
        NavHost(
            navController = navController,
            startDestination = HomeScreen(),
            enterTransition = {
                if (compactWindow)
                    slideInHorizontally(
                        initialOffsetX = { it / 4 },
                        animationSpec = motionScheme.defaultSpatialSpec()
                    ) + fadeIn(motionScheme.defaultEffectsSpec())
                else
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                            scaleIn(
                                initialScale = 0.92f,
                                animationSpec = tween(220, delayMillis = 90)
                            )
            },
            exitTransition = {
                if (compactWindow)
                    slideOutHorizontally(
                        targetOffsetX = { -it / 4 },
                        animationSpec = motionScheme.fastSpatialSpec()
                    ) + fadeOut(motionScheme.fastEffectsSpec())
                else
                    fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                if (compactWindow)
                    slideInHorizontally(
                        initialOffsetX = { -it / 4 },
                        animationSpec = motionScheme.defaultSpatialSpec()
                    ) + fadeIn(motionScheme.defaultEffectsSpec())
                else
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                            scaleIn(
                                initialScale = 0.92f,
                                animationSpec = tween(220, delayMillis = 90)
                            )
            },
            popExitTransition = {
                if (compactWindow)
                    slideOutHorizontally(
                        targetOffsetX = { it / 4 },
                        animationSpec = motionScheme.fastSpatialSpec()
                    ) + fadeOut(motionScheme.fastEffectsSpec())
                else
                    fadeOut(animationSpec = tween(90))
            }
        ) {
            composable<HomeScreen>(
                deepLinks = listOf(
                    navDeepLink { uriPattern = "{lang}.m.wikipedia.org/wiki/{query}" },
                    navDeepLink { uriPattern = "{lang}.wikipedia.org/wiki/{query}" }
                )
            ) { backStackEntry ->
                val uriQuery = remember { backStackEntry.arguments?.getString("query") }
                LaunchedEffect(uriQuery) {
                    if (uriQuery != null && !deepLinkHandled) {
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
                        setShowDeleteDialog,
                        viewModel::removeSearchHistoryItem
                    )

                Scaffold(
                    topBar = {
                        AppSearchBar(
                            appSearchBarState = appSearchBarState,
                            searchBarState = searchBarState,
                            preferencesState = preferencesState,
                            textFieldState = textFieldState,
                            userLangs = userLangs,
                            recentLangs = recentLangs,
                            searchHistory = searchHistory,
                            scrollBehavior = searchBarScrollBehavior,
                            searchBarEnabled = !showArticleLanguageSheet,
                            imageLoader = imageLoader,
                            searchListState = searchListState,
                            windowSizeClass = windowSizeClass,
                            languageSearchStr = languageSearchStr,
                            languageSearchQuery = languageSearchQuery,
                            loadSearch = {
                                scope.launch {
                                    searchBarState.animateToCollapsed()
                                }
                                viewModel.loadSearch(it)
                            },
                            loadSearchDebounced = viewModel::loadSearchResultsDebounced,
                            loadPage = viewModel::loadPage,
                            loadRandom = {
                                viewModel.loadPage(
                                    title = null,
                                    random = true
                                )
                            },
                            saveLang = settingsViewModel::saveLang,
                            updateLanguageSearchStr = viewModel::updateLanguageSearchStr,
                            onExpandedChange = {
                                scope.launch {
                                    if (it) searchBarState.animateToExpanded()
                                    else searchBarState.animateToCollapsed()
                                }
                            },
                            setQuery = textFieldState::setTextAndPlaceCursorAtEnd,
                            clearHistory = {
                                historyItem = null
                                setShowDeleteDialog(true)
                            },
                            removeHistoryItem = {
                                historyItem = it
                                setShowDeleteDialog(true)
                            },
                            onMenuIconClicked = {
                                scope.launch {
                                    railState.expand()
                                }
                            },
                            markUserLanguageSelected = viewModel::markUserLanguageSelected,
                            insertUserLanguage = viewModel::insertUserLanguage,
                            deleteUserLanguage = viewModel::deleteUserLanguage
                        )
                    },
                    snackbarHost = { SnackbarHost(snackBarHostState) },
                    contentWindowInsets =
                        if (!compactWindow)
                            ScaffoldDefaults.contentWindowInsets
                                .only(WindowInsetsSides.Top + WindowInsetsSides.Bottom + WindowInsetsSides.End)
                        else ScaffoldDefaults.contentWindowInsets,
                    modifier = if (floatingToolbarScrollBehaviour != null)
                        Modifier
                            .fillMaxSize()
                            .nestedScroll(searchBarScrollBehavior.nestedScrollConnection)
                            .nestedScroll(floatingToolbarScrollBehaviour)
                    else
                        Modifier
                            .fillMaxSize()
                            .nestedScroll(searchBarScrollBehavior.nestedScrollConnection)
                ) { insets ->
                    AppHomeScreen(
                        homeScreenState = homeScreenState,
                        listState = listState,
                        preferencesState = preferencesState,
                        feedState = feedState,
                        recentLangs = recentLangs,
                        floatingToolbarScrollBehaviour = floatingToolbarScrollBehaviour,
                        feedListState = feedListState,
                        imageLoader = imageLoader,
                        languageSearchStr = languageSearchStr,
                        languageSearchQuery = languageSearchQuery,
                        showLanguageSheet = showArticleLanguageSheet,
                        onImageClick = {
                            if (homeScreenState.photo != null || homeScreenState.status == WRStatus.FEED_LOADED)
                                navController.navigate(FullScreenImage())
                        },
                        onGalleryImageClick = { uri, desc ->
                            navController.navigate(FullScreenImage(uri, desc))
                        },
                        onLinkClick = viewModel::loadPage,
                        refreshSearch = { viewModel.reloadPage(true) },
                        refreshFeed = viewModel::loadFeed,
                        setLang = settingsViewModel::saveLang,
                        setSearchStr = viewModel::updateLanguageSearchStr,
                        setShowArticleLanguageSheet = { showArticleLanguageSheet = it },
                        enableScrollButton = if (homeScreenState.status != WRStatus.FEED_LOADED) index >= 1 else feedIndex >= 1,
                        saveArticle = {
                            scope.launch {
                                if (homeScreenState.savedStatus == SavedStatus.NOT_SAVED) {
                                    val status = viewModel.saveArticle()
                                    if (status != WRStatus.SUCCESS)
                                        snackBarHostState.showSnackbar(
                                            context.getString(
                                                string.snackbarUnableToSave,
                                                status.name
                                            )
                                        )
                                    delay(150L)
                                } else if (homeScreenState.savedStatus == SavedStatus.SAVED) {
                                    val status = viewModel.deleteArticle(
                                        pageId = homeScreenState.pageId ?: 0,
                                        lang = preferencesState.lang
                                    )
                                    if (status != WRStatus.SUCCESS)
                                        snackBarHostState.showSnackbar(
                                            context.getString(
                                                string.snackbarUnableToDelete,
                                                status.name
                                            )
                                        )
                                }
                            }
                        },
                        showFeedErrorSnackBar = {
                            scope.launch {
                                if (!deepLinkHandled)
                                    snackBarHostState
                                        .showSnackbar(
                                            context.getString(
                                                string.snackbarUnableToLoadFeed,
                                                homeScreenState.status.name
                                            )
                                        )
                            }
                        },
                        onSearchButtonClick = {
                            viewModel.focusSearchBar()
                            textFieldState.setTextAndPlaceCursorAtEnd(textFieldState.text.toString())
                        },
                        loadRandom = {
                            viewModel.loadPage(
                                title = null,
                                random = true
                            )
                        },
                        scrollToTop = {
                            scope.launch {
                                if (homeScreenState.status != WRStatus.FEED_LOADED)
                                    listState.scrollToItem(0)
                                else
                                    feedListState.scrollToItem(0)
                            }
                        },
                        showRef = viewModel::updateRef,
                        hideRef = viewModel::hideRef,
                        insets = insets,
                        windowSizeClass = windowSizeClass,
                        modifier = Modifier.fillMaxSize()
                    )

                    StatusBarProtection()
                }
            }

            composable<FullScreenImage> {
                val uri = it.toRoute<FullScreenImage>().uri
                val description = it.toRoute<FullScreenImage>().description

                if (uri == null) {
                    if (homeScreenState.status != WRStatus.FEED_LOADED) {
                        if (homeScreenState.photo == null) navController.navigateUp()
                        FullScreenImage(
                            photo = homeScreenState.photo,
                            photoDesc = homeScreenState.photoDesc,
                            title = homeScreenState.title,
                            imageLoader = imageLoader,
                            background = preferencesState.imageBackground,
                            link = homeScreenState.photo?.source,
                            onBack = navController::navigateUp
                        )
                    } else {
                        FullScreenImage(
                            photo = WikiPhoto(
                                source = feedState.image?.thumbnail?.source ?: "",
                                width = feedState.image?.thumbnail?.width ?: 1,
                                height = feedState.image?.thumbnail?.height ?: 1
                            ),
                            photoDesc = feedState.image?.description?.text?.parseAsHtml()
                                .toString(),
                            title = feedState.image?.title ?: "",
                            imageLoader = imageLoader,
                            link = feedState.image?.filePage,
                            background = preferencesState.imageBackground,
                            onBack = navController::navigateUp
                        )
                    }
                } else {
                    FullScreenImage(
                        uri = uri,
                        description = description ?: "",
                        imageLoader = imageLoader,
                        link = uri,
                        background = preferencesState.imageBackground,
                        onBack = navController::navigateUp
                    )
                }
            }

            composable<SavedArticlesScreen> {
                SavedArticlesScreenRoot(
                    imageLoader = imageLoader,
                    imageBackground = preferencesState.imageBackground,
                    openSavedArticle = { pageId: Int, lang: String ->
                        scope.launch {
                            navController.navigateUp()
                            viewModel.loadSavedArticle(pageId, lang)
                        }
                    },
                    onBack = navController::navigateUp
                )
            }

            composable<HistoryScreen> {
                HistoryScreenRoot(
                    imageLoader = imageLoader,
                    imageBackground = preferencesState.imageBackground,
                    openArticle = { title, lang ->
                        viewModel.loadPage(title, lang)
                        navController.navigateUp()
                    },
                    onBack = navController::navigateUp
                )
            }

            composable<SettingsScreen> {
                SettingsScreenRoot(
                    preferencesState = preferencesState,
                    homeScreenState = homeScreenState,
                    recentLangs = recentLangs,
                    languageSearchStr = languageSearchStr,
                    languageSearchQuery = languageSearchQuery,
                    updateLanguageSearchStr = viewModel::updateLanguageSearchStr,
                    loadFeed = viewModel::loadFeed,
                    reloadPage = viewModel::reloadPage,
                    onBack = navController::navigateUp
                )
            }

            composable<AboutScreen> {
                AboutScreen(onBack = navController::navigateUp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DeleteHistoryItemDialog(
    item: SearchHistoryItem?,
    setShowDeleteDialog: (Boolean) -> Unit,
    removeHistoryItem: (SearchHistoryItem?) -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = { setShowDeleteDialog(false) }
    ) {
        val titleText =
            if (item != null) stringResource(string.dialogDeleteSearchHistory)
            else stringResource(string.dialogDeleteSearchHistoryDesc)
        val descText =
            if (item != null) stringResource(string.dialogDeleteHistoryItem, item.query)
            else stringResource(string.dialogDeleteHistoryItemDesc)

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
                    style = typography.headlineSmall
                )
                Spacer(modifier = Modifier.padding(16.dp))
                Text(
                    text = descText,
                    style = typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(
                        shapes = ButtonDefaults.shapes(),
                        onClick = { setShowDeleteDialog(false) }) {
                        Text(text = stringResource(string.cancel))
                    }
                    TextButton(shapes = ButtonDefaults.shapes(), onClick = {
                        setShowDeleteDialog(false)
                        removeHistoryItem(item)
                    }) {
                        Text(text = stringResource(string.delete))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBarProtection(
    color: Color = MaterialTheme.colorScheme.surface,
    heightProvider: () -> Float = calculateGradientHeight(),
) {
    Canvas(Modifier.fillMaxSize()) {
        val calculatedHeight = heightProvider()
        val gradient = Brush.verticalGradient(
            colors = listOf(
                color.copy(alpha = 1f),
                color.copy(alpha = 0.7f),
                Color.Transparent
            ),
            startY = 0f,
            endY = calculatedHeight
        )
        drawRect(
            brush = gradient,
            size = Size(size.width, calculatedHeight),
        )
    }
}

@Composable
fun calculateGradientHeight(): () -> Float {
    val statusBars = WindowInsets.statusBars
    val density = LocalDensity.current
    return { statusBars.getTop(density).times(1.2f) }
}

@Serializable
data class HomeScreen(
    val lang: String? = null,
    val query: String? = null
)

@Serializable
data class FullScreenImage(
    val uri: String? = null,
    val description: String? = null
)

@Serializable
object SavedArticlesScreen

@Serializable
object HistoryScreen

@Serializable
object SettingsScreen

@Serializable
object AboutScreen
