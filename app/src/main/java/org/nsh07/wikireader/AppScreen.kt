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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.nsh07.wikireader.ui.AppFab
import org.nsh07.wikireader.ui.AppHomeScreen
import org.nsh07.wikireader.ui.AppSearchBar
import org.nsh07.wikireader.ui.FullScreenImage
import org.nsh07.wikireader.ui.UiViewModel

@Serializable
object HomeScreen

@Serializable
object FSImage

@Composable
fun AppScreen(
    modifier: Modifier = Modifier,
    viewModel: UiViewModel = viewModel()
) {
    val searchBarState by viewModel.searchBarState.collectAsState()
    val homeScreenState by viewModel.homeScreenState.collectAsState()
    val listState by viewModel.listState.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val index by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val extendedFab by remember {
        derivedStateOf {
            listState.lastScrolledBackward || !listState.canScrollForward
        }
    }

    val fabEnter = scaleIn(transformOrigin = TransformOrigin(1f, 1f)) + fadeIn()
    val fabExit = scaleOut(transformOrigin = TransformOrigin(1f, 1f)) + fadeOut()

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HomeScreen,
        modifier = Modifier.background(androidx.compose.ui.graphics.Color.Black)
    ) {
        composable<HomeScreen>(
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it/8 },
                    animationSpec = tween(300)
                ) + fadeIn(tween(100))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it/8 },
                    animationSpec = tween(300)
                ) + fadeOut(tween(100))
            }
        ) {
            Scaffold(
                floatingActionButton = {
                    AppFab(
                        focusSearch = { viewModel.focusSearchBar() },
                        scrollToTop = { coroutineScope.launch { listState.animateScrollToItem(0) } },
                        index = index,
                        extendedFab = extendedFab,
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
                        setQuery = { viewModel.setQuery(it) }
                    )
                    AppHomeScreen(
                        homeScreenState = homeScreenState,
                        listState = listState,
                        onImageClick = { navController.navigate(FSImage) },
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            }
        }

        composable<FSImage>(
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it/8 },
                    animationSpec = tween(300)
                ) + fadeIn(tween(100))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it/8 },
                    animationSpec = tween(300)
                ) + fadeOut(tween(100))
            }
        ) {
            FullScreenImage(
                photo = homeScreenState.photo!!,
                photoDesc = homeScreenState.photoDesc!!,
                onBack = { navController.navigateUp() }
            )
        }
    }
}
