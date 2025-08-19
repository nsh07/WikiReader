package org.nsh07.wikireader.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalWideNavigationRail
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailItemDefaults
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
import androidx.compose.ui.util.fastForEach
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.window.core.layout.WindowSizeClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.nsh07.wikireader.R.drawable
import org.nsh07.wikireader.R.string
import org.nsh07.wikireader.ui.homeScreen.viewModel.FeedSection
import org.nsh07.wikireader.ui.homeScreen.viewModel.HomeSubscreen
import kotlin.reflect.KClass


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppNavigationDrawer(
    state: WideNavigationRailState,
    homeBackStackEntry: HomeSubscreen,
    windowSizeClass: WindowSizeClass,
    backStackEntry: NavBackStackEntry?,
    historyEnabled: Boolean,
    isImageView: Boolean,
    onAboutClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onHomeClick: () -> Unit,
    onSavedArticlesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val motionScheme = motionScheme
    val items = listOf(
        Item(
            string.home,
            painterResource(drawable.outline_home),
            painterResource(drawable.filled_home),
            HomeScreen::class,
            onHomeClick
        ),
        Item(
            string.saved,
            painterResource(drawable.download_done),
            painterResource(drawable.filled_download_done),
            SavedArticlesScreen::class,
            onSavedArticlesClick
        ),
        Item(
            string.history,
            painterResource(drawable.history),
            painterResource(drawable.history_selected),
            HistoryScreen::class,
            onHistoryClick,
            historyEnabled
        ),
        Item(
            string.settings,
            painterResource(drawable.outline_settings),
            painterResource(drawable.filled_settings),
            SettingsScreen::class,
            onSettingsClick
        ),
        Item(
            string.about,
            painterResource(drawable.outline_info),
            painterResource(drawable.filled_info),
            AboutScreen::class,
            onAboutClick
        )
    )

    val compactScreen =
        remember { !windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) }
    val expandedScreen =
        remember { windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) }

    val boxWidth = remember { 360.dp }

    LaunchedEffect(expandedScreen) {
        scope.launch {
            if (expandedScreen) state.expand()
            else state.collapse()
        }
    }

    Row(modifier = modifier) {
        AnimatedVisibility(
            visible = !isImageView,
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
                        homeBackStackEntry = homeBackStackEntry,
                        items = items,
                        state = state,
                        scope = scope,
                        expandedScreen = expandedScreen,
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
                        homeBackStackEntry = homeBackStackEntry,
                        items = items,
                        state = state,
                        scope = scope,
                        expandedScreen = expandedScreen,
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
    scope: CoroutineScope,
    homeBackStackEntry: HomeSubscreen,
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

    val navigationItemSelectedTextColor by animateColorAsState(
        if (!expanded) colorScheme.secondary else colorScheme.onSecondaryContainer,
        animationSpec = motionScheme.defaultSpatialSpec(),
    )
    val navigationColors = WideNavigationRailItemDefaults.colors(
        unselectedIconColor = colorScheme.onSurfaceVariant,
        unselectedTextColor = colorScheme.onSurfaceVariant,
        selectedIconColor = colorScheme.onSecondaryContainer,
        selectedTextColor = navigationItemSelectedTextColor,
        selectedIndicatorColor = colorScheme.secondaryContainer,
    )

    val sectionItemSelectedTextColor by animateColorAsState(
        if (!expanded) colorScheme.onSurface else colorScheme.onSurface,
        animationSpec = motionScheme.defaultSpatialSpec(),
    )
    val sectionColors = WideNavigationRailItemDefaults.colors(
        unselectedIconColor = colorScheme.onSurfaceVariant,
        unselectedTextColor = colorScheme.onSurfaceVariant,
        selectedIconColor = colorScheme.onSurface,
        selectedTextColor = sectionItemSelectedTextColor,
        selectedIndicatorColor = colorScheme.surfaceContainerHighest,
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(itemSpacing),
        modifier = Modifier.verticalScroll(
            rememberScrollState()
        )
    ) {
        items.fastForEach { item ->
            val selected = backStackEntry?.destination?.hasRoute(item.route) == true
            WideNavigationRailItem(
                selected = selected,
                enabled = item.enabled,
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
                railExpanded = expanded,
                colors = navigationColors
            )
        }
        if (expanded && backStackEntry?.destination?.hasRoute(HomeScreen::class) == true) {
            Text(
                stringResource(string.sections),
                style = typography.titleSmall,
                modifier = Modifier
                    .padding(start = 36.dp, top = 12.dp, bottom = 8.dp)
            )
            when (homeBackStackEntry) {
                is HomeSubscreen.Feed -> {
                    homeBackStackEntry.sections.fastForEach { section ->
                        WideNavigationRailItem(
                            railExpanded = true,
                            label = {
                                Text(
                                    feedSectionName(section.second),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = boxWidth - 96.dp)
                                )
                            },
                            selected = homeBackStackEntry.listState.firstVisibleItemIndex == section.first,
                            onClick = {
                                scope.launch {
                                    if (!expandedScreen) state.collapse()
                                    homeBackStackEntry.listState.scrollToItem(section.first)
                                }
                            },
                            icon = {
                                Icon(
                                    painterResource(drawable.keyboard_arrow_right),
                                    contentDescription = null
                                )
                            },
                            colors = sectionColors
                        )
                    }
                }

                is HomeSubscreen.Article -> {
                    homeBackStackEntry.sections.fastForEach { section ->
                        WideNavigationRailItem(
                            railExpanded = true,
                            label = {
                                Text(
                                    section.second,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = boxWidth - 96.dp)
                                )
                            },
                            selected = homeBackStackEntry.listState.firstVisibleItemIndex == section.first ||
                                    homeBackStackEntry.listState.firstVisibleItemIndex == section.first + 1,
                            onClick = {
                                scope.launch {
                                    if (!expandedScreen) state.collapse()
                                    homeBackStackEntry.listState.scrollToItem(section.first)
                                }
                            },
                            icon = {
                                Icon(
                                    painterResource(drawable.keyboard_arrow_right),
                                    contentDescription = null
                                )
                            },
                            colors = sectionColors
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
    val onClick: () -> Unit,
    val enabled: Boolean = true
)