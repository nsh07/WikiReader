package org.nsh07.wikireader

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.nsh07.wikireader.ui.AppHomeScreen
import org.nsh07.wikireader.ui.FullScreenImage
import org.nsh07.wikireader.ui.SettingsScreen
import org.nsh07.wikireader.ui.scaffoldComponents.AppFab
import org.nsh07.wikireader.ui.scaffoldComponents.AppSearchBar
import org.nsh07.wikireader.ui.viewModel.PreferencesState
import org.nsh07.wikireader.ui.viewModel.UiViewModel

@Composable
fun AppScreen(
    viewModel: UiViewModel,
    preferencesState: PreferencesState,
    modifier: Modifier = Modifier
) {
    val searchBarState by viewModel.searchBarState.collectAsState()
    val homeScreenState by viewModel.homeScreenState.collectAsState()
    val listState by viewModel.listState.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val index by remember { derivedStateOf { listState.firstVisibleItemIndex } }

    val fabEnter = scaleIn(transformOrigin = TransformOrigin(1f, 1f)) + fadeIn()
    val fabExit = scaleOut(transformOrigin = TransformOrigin(1f, 1f)) + fadeOut()

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "HomeScreen",
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
        modifier = Modifier.background(androidx.compose.ui.graphics.Color.Black)
    ) {
        composable("HomeScreen") {
            Scaffold(
                floatingActionButton = {
                    AppFab(
                        focusSearch = { viewModel.focusSearchBar() },
                        scrollToTop = { coroutineScope.launch { listState.animateScrollToItem(0) } },
                        index = index,
                        fabEnter = fabEnter,
                        fabExit = fabExit
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) { insets ->
                Column(modifier = modifier.padding(top = insets.calculateTopPadding())) {
                    AppSearchBar(
                        searchBarState = searchBarState,
                        performSearch = { viewModel.performSearch(it) },
                        setExpanded = { viewModel.setExpanded(it) },
                        setQuery = { viewModel.setQuery(it) },
                        onSettingsClick = {
                            navController.navigate("Settings")
                            it(false)
                        }
                    )
                    AppHomeScreen(
                        homeScreenState = homeScreenState,
                        listState = listState,
                        onImageClick = {
                            if (homeScreenState.photo != null)
                                navController.navigate("FullScreenImage")
                        },
                        insets = insets,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            }
        }

        composable("FullScreenImage") {
            if (homeScreenState.photo == null) navController.navigateUp()
            FullScreenImage(
                photo = homeScreenState.photo,
                photoDesc = homeScreenState.photoDesc,
                onBack = { navController.navigateUp() }
            )
        }

        composable("Settings") {
            SettingsScreen(
                preferencesState = preferencesState,
                onBack = { navController.navigateUp() },
                onThemeChanged = { viewModel.setTheme(it) }
            )
        }
    }
}
