package org.nsh07.wikireader.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import kotlinx.coroutines.launch
import org.nsh07.wikireader.R.drawable
import org.nsh07.wikireader.R.string
import org.nsh07.wikireader.data.WRStatus
import org.nsh07.wikireader.ui.viewModel.FeedSection
import org.nsh07.wikireader.ui.viewModel.FeedState
import org.nsh07.wikireader.ui.viewModel.HomeScreenState
import kotlin.reflect.KClass

@Composable
fun AppNavigationDrawer(
    drawerState: DrawerState,
    feedState: FeedState,
    homeScreenState: HomeScreenState,
    listState: LazyListState,
    feedListState: LazyListState,
    windowSizeClass: WindowSizeClass,
    onAboutClick: () -> Unit,
    onHomeClick: () -> Unit,
    onSavedArticlesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    hasRoute: (KClass<out Any>) -> Boolean,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val items = listOf(
        Item(
            string.home,
            painterResource(drawable.outline_home),
            painterResource(drawable.filled_home),
            Home::class,
            onHomeClick
        ),
        Item(
            string.savedArticles,
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
    if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT) {
        ModalNavigationDrawer(
            drawerContent = {
                AppNavigationDrawerSheet(
                    drawerState = drawerState,
                    feedState = feedState,
                    homeScreenState = homeScreenState,
                    listState = listState,
                    feedListState = feedListState,
                    windowSizeClass = windowSizeClass,
                    items = items,
                    hasRoute = hasRoute
                )
            },
            drawerState = drawerState,
            content = content
        )
    } else if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM) {
        Row {
            NavigationRail(
                header = {
                    AnimatedContent(drawerState.targetValue) { targetValue ->
                        if (targetValue == DrawerValue.Closed)
                            IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                Icon(Icons.Outlined.Menu, null)
                            }
                        else
                            Box(Modifier.width(316.dp)) {
                                IconButton(onClick = { coroutineScope.launch { drawerState.close() } }) {
                                    Icon(Icons.Outlined.Menu, null)
                                }
                            }
                    }
                },
                content = {
                    AppNavigationRailContent(
                        drawerState = drawerState,
                        feedState = feedState,
                        homeScreenState = homeScreenState,
                        listState = listState,
                        feedListState = feedListState,
                        items = items,
                        hasRoute = hasRoute
                    )
                }
            )
            content()
        }
    } else if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED) {
        PermanentNavigationDrawer(
            drawerContent = {
                AppNavigationDrawerSheet(
                    drawerState = drawerState,
                    feedState = feedState,
                    homeScreenState = homeScreenState,
                    listState = listState,
                    feedListState = feedListState,
                    windowSizeClass = windowSizeClass,
                    items = items,
                    hasRoute = hasRoute
                )
            },
            content = content
        )
    }
}

@Composable
private fun AppNavigationDrawerSheet(
    drawerState: DrawerState,
    feedState: FeedState,
    homeScreenState: HomeScreenState,
    listState: LazyListState,
    feedListState: LazyListState,
    windowSizeClass: WindowSizeClass,
    items: List<Item>,
    hasRoute: (KClass<out Any>) -> Boolean,
    modifier: Modifier = Modifier
) {
    if (windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.EXPANDED) {
        ModalDrawerSheet(
            modifier = modifier,
            drawerState = drawerState,
            windowInsets = WindowInsets(0.dp),
            content = {
                AppNavigationDrawerSheetContent(
                    drawerState = drawerState,
                    feedState = feedState,
                    homeScreenState = homeScreenState,
                    listState = listState,
                    feedListState = feedListState,
                    items = items,
                    hasRoute = hasRoute
                )
            }
        )
    } else {
        PermanentDrawerSheet(
            modifier = modifier,
            windowInsets = WindowInsets(0.dp),
            content = {
                AppNavigationDrawerSheetContent(
                    drawerState = drawerState,
                    feedState = feedState,
                    homeScreenState = homeScreenState,
                    listState = listState,
                    feedListState = feedListState,
                    items = items,
                    hasRoute = hasRoute

                )
            }
        )
    }
}

@Composable
private fun AppNavigationDrawerSheetContent(
    drawerState: DrawerState,
    feedState: FeedState,
    homeScreenState: HomeScreenState,
    listState: LazyListState,
    feedListState: LazyListState,
    items: List<Item>,
    hasRoute: (KClass<out Any>) -> Boolean,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val statusList = remember { listOf(WRStatus.FEED_LOADED, WRStatus.SUCCESS) }
    val windowInsets = WindowInsets.systemBars.asPaddingValues()
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(windowInsets.calculateTopPadding()))
        Text(
            stringResource(string.app),
            style = typography.titleSmall,
            modifier = Modifier
                .padding(NavigationDrawerItemDefaults.ItemPadding)
                .padding(16.dp)
        )
        items.forEach {
            NavigationDrawerItem(
                label = { Text(stringResource(it.labelId), style = typography.labelLarge) },
                icon = {
                    Crossfade(hasRoute(it.route)) { selected ->
                        when (selected) {
                            true -> Icon(
                                it.filledIcon,
                                contentDescription = null
                            )

                            else ->
                                Icon(
                                    it.outlinedIcon,
                                    contentDescription = null
                                )
                        }
                    }
                },
                selected = hasRoute(it.route),
                onClick = {
                    coroutineScope.launch {
                        it.onClick()
                        drawerState.close()
                    }
                },
                modifier = Modifier
                    .padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
        AnimatedVisibility(homeScreenState.status in statusList && hasRoute(Home::class)) {
            Column {
                HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                Text(
                    stringResource(string.sections),
                    style = typography.titleSmall,
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .padding(16.dp)
                )
                when (homeScreenState.status) {
                    WRStatus.FEED_LOADED -> {
                        feedState.sections.forEach { section ->
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        feedSectionName(section.second),
                                        style = typography.labelLarge
                                    )
                                },
                                selected = feedListState.firstVisibleItemIndex == section.first,
                                onClick = {
                                    coroutineScope.launch {
                                        drawerState.close()
                                        feedListState.animateScrollToItem(section.first)
                                    }
                                },
                                icon = {
                                    Icon(
                                        Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                        contentDescription = null
                                    )
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = colorScheme.tertiaryContainer,
                                    selectedTextColor = colorScheme.onTertiaryContainer,
                                    selectedIconColor = colorScheme.onTertiaryContainer
                                ),
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                    }

                    WRStatus.SUCCESS -> {
                        homeScreenState.sections.forEach { section ->
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        section.second,
                                        style = typography.labelLarge
                                    )
                                },
                                selected = listState.firstVisibleItemIndex == section.first || listState.firstVisibleItemIndex == section.first + 1,
                                onClick = {
                                    coroutineScope.launch {
                                        drawerState.close()
                                        listState.animateScrollToItem(section.first)
                                    }
                                },
                                icon = {
                                    Icon(
                                        Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                        contentDescription = null
                                    )
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = colorScheme.tertiaryContainer,
                                    selectedTextColor = colorScheme.onTertiaryContainer,
                                    selectedIconColor = colorScheme.onTertiaryContainer
                                ),
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                    }

                    else -> {}
                }
            }
        }
        Spacer(Modifier.height(windowInsets.calculateBottomPadding()))
    }
}

@Composable
private fun AppNavigationRailContent(
    drawerState: DrawerState,
    feedState: FeedState,
    homeScreenState: HomeScreenState,
    listState: LazyListState,
    feedListState: LazyListState,
    items: List<Item>,
    hasRoute: (KClass<out Any>) -> Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedContent(drawerState.targetValue == DrawerValue.Closed) {
        if (it) {
            Column(modifier = modifier) {
                items.forEach {
                    NavigationRailItem(
                        icon = {
                            Crossfade(hasRoute(it.route)) { selected ->
                                when (selected) {
                                    true -> Icon(
                                        it.filledIcon,
                                        contentDescription = null
                                    )

                                    else ->
                                        Icon(
                                            it.outlinedIcon,
                                            contentDescription = null
                                        )
                                }
                            }
                        },
                        selected = hasRoute(it.route),
                        onClick = it.onClick
                    )
                }
            }
        } else {
            AppNavigationDrawerSheetContent(
                drawerState = drawerState,
                feedState = feedState,
                homeScreenState = homeScreenState,
                listState = listState,
                feedListState = feedListState,
                items = items,
                hasRoute = hasRoute,
                modifier = Modifier.width(360.dp)
            )
        }
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