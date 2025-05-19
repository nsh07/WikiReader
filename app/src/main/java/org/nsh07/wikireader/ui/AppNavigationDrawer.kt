package org.nsh07.wikireader.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalWideNavigationRail
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.nsh07.wikireader.R.drawable
import org.nsh07.wikireader.R.string
import org.nsh07.wikireader.data.WRStatus
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
    val expandedScreen =
        remember { windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED }


    val boxWidth = remember { 360.dp }

    LaunchedEffect(expandedScreen) {
        scope.launch {
            if (expandedScreen) state.expand()
            else state.collapse()
        }
    }

    Row(modifier = modifier) {
        AnimatedVisibility(
            backStackEntry?.destination?.hasRoute(FullScreenImage::class) != true,
            enter = expandHorizontally(animationSpec = motionScheme.defaultSpatialSpec()),
            exit = shrinkHorizontally(animationSpec = motionScheme.defaultSpatialSpec())
        ) {
            if (compactScreen) {
                ModalWideNavigationRail(
                    state = state,
                    header = { Box(Modifier.fillMaxWidth()) {} }, // Disgusting hack to make the rail occupy the full width of the screen
                    hideOnCollapse = true
                ) {
                    AppNavigationRailContent(
                        backStackEntry = backStackEntry,
                        items = items,
                        state = state,
                        scope = scope,
                        expandedScreen = expandedScreen,
                        feedState = feedState,
                        feedListState = feedListState,
                        homeScreenState = homeScreenState,
                        listState = listState,
                        boxWidth = boxWidth
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
                        Box(Modifier.fillMaxWidth()) {
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
                    }
                ) {
                    AppNavigationRailContent(
                        backStackEntry = backStackEntry,
                        items = items,
                        state = state,
                        scope = scope,
                        expandedScreen = expandedScreen,
                        feedState = feedState,
                        feedListState = feedListState,
                        homeScreenState = homeScreenState,
                        listState = listState,
                        boxWidth = boxWidth
                    )
                }
            }
        }
        content()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AppNavigationRailContent(
    state: WideNavigationRailState,
    feedState: FeedState,
    feedListState: LazyListState,
    homeScreenState: HomeScreenState,
    listState: LazyListState,
    scope: CoroutineScope,
    backStackEntry: NavBackStackEntry?,
    expandedScreen: Boolean,
    boxWidth: Dp,
    items: List<Item>
) {
    val expanded = state.targetValue == WideNavigationRailValue.Expanded
    val itemSpacing by animateDpAsState(
        if (!expanded) 16.dp else 0.dp,
        animationSpec = motionScheme.defaultSpatialSpec()
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(itemSpacing),
        modifier = Modifier.verticalScroll(
            rememberScrollState()
        )
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
                railExpanded = expanded
            )
        }
        if (expanded && backStackEntry?.destination?.hasRoute(Home::class) == true) {
            Text(
                stringResource(string.sections),
                style = typography.titleSmall,
                modifier = Modifier
                    .padding(start = 36.dp, top = 12.dp, bottom = 8.dp)
            )
            when (homeScreenState.status) {
                WRStatus.FEED_LOADED -> {
                    feedState.sections.forEach { section ->
                        WideNavigationRailItem(
                            railExpanded = true,
                            label = {
                                Text(
                                    feedSectionName(section.second),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = typography.labelLarge,
                                    modifier = Modifier.widthIn(max = boxWidth - 96.dp)
                                )
                            },
                            selected = feedListState.firstVisibleItemIndex == section.first,
                            onClick = {
                                scope.launch {
                                    if (!expandedScreen) state.collapse()
                                    feedListState.scrollToItem(section.first)
                                }
                            },
                            icon = {
                                Icon(
                                    Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }

                WRStatus.SUCCESS -> {
                    homeScreenState.sections.forEach { section ->
                        WideNavigationRailItem(
                            railExpanded = true,
                            label = {
                                Text(
                                    section.second,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = typography.labelLarge,
                                    modifier = Modifier.widthIn(max = boxWidth - 96.dp)
                                )
                            },
                            selected = listState.firstVisibleItemIndex == section.first || listState.firstVisibleItemIndex == section.first + 1,
                            onClick = {
                                scope.launch {
                                    if (!expandedScreen) state.collapse()
                                    listState.scrollToItem(section.first)
                                }
                            },
                            icon = {
                                Icon(
                                    Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }

                else -> null
            }
        }
        Spacer(
            Modifier.height(
                WideNavigationRailDefaults.windowInsets.asPaddingValues().calculateBottomPadding()
            )
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