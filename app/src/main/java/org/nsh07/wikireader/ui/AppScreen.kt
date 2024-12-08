package org.nsh07.wikireader.ui

import android.os.Build.VERSION.SDK_INT
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import coil3.ImageLoader
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.svg.SvgDecoder
import kotlinx.coroutines.launch
import org.nsh07.wikireader.ui.aboutScreen.AboutScreen
import org.nsh07.wikireader.ui.homeScreen.AppFab
import org.nsh07.wikireader.ui.homeScreen.AppHomeScreen
import org.nsh07.wikireader.ui.homeScreen.AppSearchBar
import org.nsh07.wikireader.ui.image.FullScreenImage
import org.nsh07.wikireader.ui.settingsScreen.SettingsScreen
import org.nsh07.wikireader.ui.viewModel.PreferencesState
import org.nsh07.wikireader.ui.viewModel.UiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    viewModel: UiViewModel,
    preferencesState: PreferencesState,
    modifier: Modifier = Modifier
) {
    val searchBarState by viewModel.searchBarState.collectAsState()
    val homeScreenState by viewModel.homeScreenState.collectAsState()
    val listState by viewModel.listState.collectAsState()
    val languageSearchStr = viewModel.languageSearchStr.collectAsState()
    val languageSearchQuery = viewModel.languageSearchQuery.collectAsState("")
    var showArticleLanguageSheet by remember { mutableStateOf(false) }

    val imageLoader = ImageLoader.Builder(LocalContext.current)
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

    val index by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val (showDeleteDialog, setShowDeleteDialog) = remember { mutableStateOf(false) }
    var (historyItem, setHistoryItem) = remember { mutableStateOf("") }

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
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
        composable(
            "home?query={query}&lang={lang}",
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://{lang}.wikipedia.org/wiki/{query}" },
                navDeepLink { uriPattern = "https://{lang}.m.wikipedia.org/wiki/{query}" },
                navDeepLink { uriPattern = "http://{lang}.wikipedia.org/wiki/{query}" },
                navDeepLink { uriPattern = "http://{lang}.m.wikipedia.org/wiki/{query}" }
            )
        ) { backStackEntry ->
            LaunchedEffect(null) {
                val uriQuery = backStackEntry.arguments?.getString("query") ?: ""
                if (uriQuery != "") {
                    viewModel.performSearch(
                        uriQuery,
                        fromLink = true,
                        lang = backStackEntry.arguments?.getString("lang")
                    )
                }
            }

            BackHandler(!homeScreenState.isBackStackEmpty) {
                val curr = viewModel.popBackStack()
                viewModel.performSearch(query = curr?.first, lang = curr?.second, fromBackStack = true)
            }

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
                        searchBarState = searchBarState,
                        searchBarEnabled = !showArticleLanguageSheet,
                        index = index,
                        performSearch = { viewModel.performSearch(it) },
                        setExpanded = { viewModel.setExpanded(it) },
                        setQuery = { viewModel.setQuery(it) },
                        clearHistory = {
                            setHistoryItem("")
                            setShowDeleteDialog(true)
                        },
                        removeHistoryItem = {
                            setHistoryItem(it)
                            setShowDeleteDialog(true)
                        },
                        onSettingsClick = {
                            navController.navigate("Settings")
                            it(false)
                        },
                        onAboutClick = {
                            navController.navigate("About")
                            it(false)
                        },
                        modifier = Modifier.padding(
                            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                        )
                    )
                },
                floatingActionButton = {
                    AppFab(
                        focusSearch = { viewModel.focusSearchBar() },
                        scrollToTop = { coroutineScope.launch { listState.animateScrollToItem(0) } },
                        performRandomPageSearch = {
                            viewModel.performSearch(
                                query = null,
                                random = true
                            )
                        },
                        index = index
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) { insets ->
                AppHomeScreen(
                    homeScreenState = homeScreenState,
                    listState = listState,
                    preferencesState = preferencesState,
                    imageLoader = imageLoader,
                    languageSearchStr = languageSearchStr.value,
                    languageSearchQuery = languageSearchQuery.value,
                    showLanguageSheet = showArticleLanguageSheet,
                    onImageClick = {
                        if (homeScreenState.photo != null)
                            navController.navigate("FullScreenImage")
                    },
                    insets = insets,
                    onLinkClick = { viewModel.performSearch(it, fromLink = true) },
                    setLang = { viewModel.saveLang(it) },
                    setSearchStr = { viewModel.updateLanguageSearchStr(it) },
                    setShowArticleLanguageSheet = { showArticleLanguageSheet = it },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = insets.calculateTopPadding())
                )
            }
        }

        composable("FullScreenImage") {
            if (homeScreenState.photo == null) navController.navigateUp()
            FullScreenImage(
                photo = homeScreenState.photo,
                photoDesc = homeScreenState.photoDesc,
                imageLoader = imageLoader,
                onBack = { navController.navigateUp() }
            )
        }

        composable("Settings") {
            SettingsScreen(
                preferencesState = preferencesState,
                onBack = { navController.navigateUp() },
                viewModel = viewModel
            )
        }

        composable("About") {
            AboutScreen(
                onBack = { navController.navigateUp() }
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
