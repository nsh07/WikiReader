package org.nsh07.wikireader.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.ModalWideNavigationRail
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailState
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.nsh07.wikireader.R.drawable
import org.nsh07.wikireader.R.string
import org.nsh07.wikireader.ui.viewModel.FeedSection
import org.nsh07.wikireader.ui.viewModel.FeedState
import org.nsh07.wikireader.ui.viewModel.HomeScreenState
import kotlin.reflect.KClass


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppNavigationDrawer(
    state: WideNavigationRailState,
    feedState: FeedState,
    homeScreenState: HomeScreenState,
    listState: LazyListState,
    feedListState: LazyListState,
    windowSizeClass: WindowSizeClass,
    backStackEntry: NavBackStackEntry?,
    modifier: Modifier = Modifier,
    onAboutClick: () -> Unit,
    onHomeClick: () -> Unit,
    onSavedArticlesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val motionScheme = motionScheme
    val items = listOf(
        Item(
            string.home,
            painterResource(drawable.outline_home),
            painterResource(drawable.filled_home),
            Home::class,
            onHomeClick
        ),
        Item(
            string.saved,
            painterResource(drawable.download_done),
            painterResource(drawable.filled_download_done),
            SavedArticles::class,
            onSavedArticlesClick
        ),
        Item(
            string.settings,
            painterResource(drawable.outline_settings),
            painterResource(drawable.filled_settings),
            Settings::class,
            onSettingsClick
        ),
        Item(
            string.about,
            painterResource(drawable.outline_info),
            painterResource(drawable.filled_info),
            About::class,
            onAboutClick
        )
    )

    val compactScreen =
        remember { windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT }
    val mediumScreen =
        remember { windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM }
    val expandedScreen =
        remember { windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED }

    LaunchedEffect(expandedScreen) {
        scope.launch {
            if (expandedScreen) state.expand()
            else state.collapse()
        }
    }

    Row {
        if (compactScreen || mediumScreen) {
            ModalWideNavigationRail(
                state = state,
                hideOnCollapse = compactScreen,
                header = if (mediumScreen) {
                    {
                        val expanded = state.targetValue == WideNavigationRailValue.Expanded
                        val rotation by animateFloatAsState(
                            if (expanded) 0f else -180f
                        )
                        IconButton(
                            modifier = Modifier.padding(start = 24.dp),
                            onClick = {
                                scope.launch {
                                    if (expanded)
                                        state.collapse()
                                    else state.expand()
                                }
                            }
                        ) {
                            Crossfade(
                                expanded,
                                modifier = Modifier.graphicsLayer {
                                    rotationZ = rotation
                                }
                            ) {
                                when (it) {
                                    true -> Icon(
                                        painterResource(drawable.menu_open),
                                        "Collapse rail"
                                    )

                                    else -> {
                                        Icon(painterResource(drawable.menu), "Expand rail")
                                    }
                                }
                            }
                        }
                    }
                } else null
            ) {
                AppNavigationRailContent(
                    backStackEntry = backStackEntry,
                    items = items,
                    state = state,
                    scope = scope,
                    expandedScreen = expandedScreen
                )
            }
        } else {
            WideNavigationRail(
                state = state,
                header = {
                    val expanded = state.targetValue == WideNavigationRailValue.Expanded
                    val rotation by animateFloatAsState(
                        if (expanded) 0f else -180f
                    )
                    IconButton(
                        modifier = Modifier.padding(start = 24.dp),
                        onClick = {
                            scope.launch {
                                if (expanded)
                                    state.collapse()
                                else state.expand()
                            }
                        }
                    ) {
                        Crossfade(
                            expanded,
                            modifier = Modifier.graphicsLayer {
                                rotationZ = rotation
                            }
                        ) {
                            when (it) {
                                true -> Icon(
                                    painterResource(drawable.menu_open),
                                    "Collapse rail",
                                )

                                else -> Icon(
                                    painterResource(drawable.menu),
                                    "Expand rail"
                                )
                            }
                        }
                    }
                }
            ) {
                AppNavigationRailContent(
                    backStackEntry = backStackEntry,
                    items = items,
                    state = state,
                    scope = scope,
                    expandedScreen = expandedScreen
                )
            }
        }
        content()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AppNavigationRailContent(
    state: WideNavigationRailState,
    scope: CoroutineScope,
    backStackEntry: NavBackStackEntry?,
    expandedScreen: Boolean,
    items: List<Item>
) {
    items.forEach { item ->
        val selected = backStackEntry?.destination?.hasRoute(item.route) == true
        WideNavigationRailItem(
            selected = selected,
            onClick = {
                scope.launch {
                    if (!expandedScreen) state.collapse()
                    item.onClick()
                }
            },
            icon = {
                Crossfade(selected) {
                    when (it) {
                        true -> Icon(item.filledIcon, null)
                        else -> Icon(item.outlinedIcon, null)
                    }
                }
            },
            label = { Text(stringResource(item.labelId)) },
            railExpanded = state.targetValue == WideNavigationRailValue.Expanded,
//            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun feedSectionName(section: FeedSection): String {
    return when (section) {
        FeedSection.TFA -> stringResource(string.featuredArticle)
        FeedSection.MOST_READ -> stringResource(string.trendingArticles)
        FeedSection.IMAGE -> stringResource(string.picOfTheDay)
        FeedSection.NEWS -> stringResource(string.inTheNews)
        FeedSection.ON_THIS_DAY -> stringResource(string.onThisDay)
    }
}

private data class Item(
    val labelId: Int,
    val outlinedIcon: Painter,
    val filledIcon: Painter,
    val route: KClass<out Any>,
    val onClick: () -> Unit
)