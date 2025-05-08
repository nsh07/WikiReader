package org.nsh07.wikireader.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
    status: WRStatus,
    onAboutClick: () -> Unit,
    onHomeClick: () -> Unit,
    onSavedArticlesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    hasRoute: (KClass<out Any>) -> Boolean,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val statusList = remember { listOf(WRStatus.FEED_LOADED, WRStatus.SUCCESS) }
    val windowInsets = WindowInsets.systemBars.asPaddingValues()
    ModalDrawerSheet(
        modifier = modifier,
        drawerState = drawerState,
        windowInsets = WindowInsets(0.dp)
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(windowInsets.calculateTopPadding()))
            Text(
                stringResource(string.app),
                style = typography.titleSmall,
                modifier = Modifier
                    .padding(NavigationDrawerItemDefaults.ItemPadding)
                    .padding(16.dp)
            )
            NavigationDrawerItem(
                label = { Text(stringResource(string.home), style = typography.labelLarge) },
                icon = {
                    Icon(
                        Icons.Outlined.Home,
                        contentDescription = null
                    )
                },
                selected = hasRoute(Home::class),
                onClick = {
                    coroutineScope.launch {
                        if (!hasRoute(Home::class)) onHomeClick()
                        drawerState.close()
                    }
                },
                modifier = Modifier
                    .padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            NavigationDrawerItem(
                label = {
                    Text(
                        stringResource(string.savedArticles),
                        style = typography.labelLarge
                    )
                },
                icon = {
                    Icon(
                        painterResource(drawable.download_done),
                        contentDescription = null
                    )
                },
                selected = hasRoute(SavedArticles::class),
                onClick = {
                    coroutineScope.launch {
                        if (!hasRoute(SavedArticles::class)) onSavedArticlesClick()
                        drawerState.close()
                    }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            NavigationDrawerItem(
                label = {
                    Text(
                        stringResource(string.settings),
                        style = typography.labelLarge
                    )
                },
                icon = {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = null
                    )
                },
                selected = hasRoute(Settings::class),
                onClick = {
                    coroutineScope.launch {
                        if (!hasRoute(Settings::class)) onSettingsClick()
                        drawerState.close()
                    }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            NavigationDrawerItem(
                label = {
                    Text(
                        stringResource(string.about),
                        style = typography.labelLarge
                    )
                },
                icon = {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null
                    )
                },
                selected = hasRoute(About::class),
                onClick = {
                    coroutineScope.launch {
                        if (!hasRoute(About::class)) onAboutClick()
                        drawerState.close()
                    }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            AnimatedVisibility(status in statusList && hasRoute(Home::class)) {
                Column {
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    Text(
                        stringResource(string.sections),
                        style = typography.titleSmall,
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .padding(16.dp)
                    )
                    if (status == WRStatus.FEED_LOADED) {
                        feedState.sections.forEach { section ->
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        feedSectionName(section.second),
                                        style = typography.labelLarge
                                    )
                                },
                                badge = {
                                    Text(
                                        (section.first + 1).toString(),
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
                                    selectedIconColor = colorScheme.onTertiaryContainer,
                                    selectedBadgeColor = colorScheme.onTertiaryContainer
                                ),
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                    } else if (status == WRStatus.SUCCESS) {
                        homeScreenState.sections.forEach { section ->
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        section.second,
                                        style = typography.labelLarge
                                    )
                                },
                                badge = {
                                    Text(
                                        (section.first / 2).toString(),
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
                                    selectedIconColor = colorScheme.onTertiaryContainer,
                                    selectedBadgeColor = colorScheme.onTertiaryContainer
                                ),
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(windowInsets.calculateBottomPadding()))
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