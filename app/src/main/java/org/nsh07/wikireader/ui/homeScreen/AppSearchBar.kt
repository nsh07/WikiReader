package org.nsh07.wikireader.ui.homeScreen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarScrollBehavior
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopSearchBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import coil3.ImageLoader
import org.nsh07.wikireader.R
import org.nsh07.wikireader.ui.image.FeedImage
import org.nsh07.wikireader.ui.viewModel.AppSearchBarState
import org.nsh07.wikireader.ui.viewModel.PreferencesState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppSearchBar(
    appSearchBarState: AppSearchBarState,
    searchBarState: SearchBarState,
    preferencesState: PreferencesState,
    textFieldState: TextFieldState,
    searchBarEnabled: Boolean,
    dataSaver: Boolean,
    imageLoader: ImageLoader,
    searchListState: LazyListState,
    windowSizeClass: WindowSizeClass,
    scrollBehavior: SearchBarScrollBehavior?,
    loadSearch: (String) -> Unit,
    loadSearchDebounced: (String) -> Unit,
    loadPage: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    setQuery: (String) -> Unit,
    removeHistoryItem: (String) -> Unit,
    clearHistory: () -> Unit,
    onSavedArticlesClick: ((Boolean) -> Unit) -> Unit,
    onSettingsClick: ((Boolean) -> Unit) -> Unit,
    onAboutClick: ((Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = appSearchBarState.focusRequester
    val haptic = LocalHapticFeedback.current
    val (dropdownExpanded, setDropdownExpanded) = remember { mutableStateOf(false) }
    val history = appSearchBarState.history.toList()
    val size = history.size
    val weight = remember {
        if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM ||
            windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
        )
            1f
        else 0f
    }
    LaunchedEffect(textFieldState.text) {
        loadSearchDebounced(textFieldState.text.toString())
    }
    val inputField =
        @Composable {
            SearchBarDefaults.InputField(
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                onSearch = loadSearch,
                placeholder = { Text("Search Wikipedia...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search") },
                trailingIcon = {
                    Row {
                        if (textFieldState.text != "") {
                            IconButton(
                                onClick = {
                                    setQuery("")
                                    focusRequester.requestFocus()
                                }
                            ) {
                                Icon(
                                    Icons.Outlined.Clear,
                                    contentDescription = "Clear search field"
                                )
                            }
                        }
                        Column {
                            IconButton(onClick = { setDropdownExpanded(!dropdownExpanded) }) {
                                Icon(
                                    Icons.Outlined.MoreVert,
                                    contentDescription = "More options"
                                )
                            }
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { setDropdownExpanded(false) }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Saved articles") },
                                    onClick = { onSavedArticlesClick(setDropdownExpanded) },
                                    leadingIcon = {
                                        Icon(
                                            painterResource(R.drawable.download_done),
                                            contentDescription = null
                                        )
                                    },
                                    modifier = Modifier.width(200.dp)
                                )
                                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                DropdownMenuItem(
                                    text = { Text("Settings") },
                                    onClick = { onSettingsClick(setDropdownExpanded) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.Settings,
                                            contentDescription = null
                                        )
                                    },
                                    modifier = Modifier.width(200.dp)
                                )
                                DropdownMenuItem(
                                    text = { Text("About") },
                                    onClick = { onAboutClick(setDropdownExpanded) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.Info,
                                            contentDescription = null
                                        )
                                    },
                                    modifier = Modifier.width(200.dp)
                                )
                            }
                        }
                    }
                },
                enabled = searchBarEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
        }

    TopSearchBar(
        state = searchBarState,
        scrollBehavior = scrollBehavior,
        inputField = inputField,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    )

    ExpandedFullScreenSearchBar(
        state = searchBarState,
        inputField = inputField
    ) {
        Crossfade(textFieldState.text.trim().isEmpty()) {
            when (it) {
                true ->
                    if (preferencesState.searchHistory) {
                        Row {
                            if (weight != 0f) Spacer(modifier = Modifier.weight(weight))
                            LazyColumn(Modifier.weight(4f)) {
                                item {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "History",
                                            style = typography.labelLarge,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                        Spacer(Modifier.weight(1f))
                                        TextButton(
                                            onClick = clearHistory,
                                            enabled = size > 0,
                                            modifier = Modifier.padding(4.dp)
                                        ) {
                                            Text("Clear")
                                        }
                                    }
                                }
                                items(size, key = { history[size - it - 1] }) {
                                    val currentText = history[size - it - 1]
                                    ListItem(
                                        leadingContent = {
                                            Icon(
                                                painterResource(R.drawable.history),
                                                contentDescription = null
                                            )
                                        },
                                        headlineContent = {
                                            Text(
                                                currentText,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        trailingContent = {
                                            IconButton(
                                                onClick = { setQuery(currentText) },
                                                modifier = Modifier.wrapContentSize()
                                            ) {
                                                Icon(
                                                    painterResource(R.drawable.north_west),
                                                    contentDescription = null
                                                )
                                            }
                                        },
                                        colors = ListItemDefaults
                                            .colors(containerColor = SearchBarDefaults.colors().containerColor),
                                        modifier = Modifier
                                            .combinedClickable(
                                                onClick = {
                                                    loadSearch(currentText)
                                                    textFieldState.setTextAndPlaceCursorAtEnd(
                                                        currentText
                                                    )
                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    removeHistoryItem(currentText)
                                                }
                                            )
                                            .animateItem()
                                    )
                                }
                                item {
                                    Spacer(
                                        Modifier.height(
                                            WindowInsets.systemBars.asPaddingValues()
                                                .calculateBottomPadding() + 152.dp
                                        )
                                    )
                                }
                            }
                            if (weight != 0f) Spacer(modifier = Modifier.weight(weight))
                        }
                    }

                else ->
                    if (windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT)
                        LazyVerticalGrid(columns = GridCells.Fixed(2)) {
                            items(
                                appSearchBarState.prefixSearchResults ?: emptyList(),
                                key = { "${it.title}-prefix" }
                            ) {
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            it.title,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    supportingContent = if (it.terms != null) {
                                        {
                                            Text(
                                                it.terms.description[0],
                                                softWrap = true,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    } else null,
                                    trailingContent = {
                                        if (it.thumbnail != null && !dataSaver)
                                            FeedImage(
                                                source = it.thumbnail.source,
                                                imageLoader = imageLoader,
                                                contentScale = ContentScale.Crop,
                                                loadingIndicator = true,
                                                modifier = Modifier
                                                    .padding(vertical = 4.dp)
                                                    .size(56.dp)
                                                    .clip(shapes.large)
                                            )
                                        else null
                                    },
                                    colors = ListItemDefaults
                                        .colors(containerColor = SearchBarDefaults.colors().containerColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(shapes.large)
                                        .clickable(
                                            onClick = {
                                                onExpandedChange(false)
                                                loadPage(it.title)
                                            }
                                        )
                                        .animateItem()
                                )
                            }
                            items(
                                appSearchBarState.searchResults ?: emptyList(),
                                key = { "${it.title}-search" }) {
                                ListItem(
                                    overlineContent = if (it.redirectTitle != null) {
                                        {
                                            Text(
                                                "Redirected from ${it.redirectTitle}",
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    } else null,
                                    headlineContent = {
                                        Text(
                                            AnnotatedString.fromHtml(
                                                if (it.titleSnippet.isNotEmpty())
                                                    it.titleSnippet
                                                else it.title
                                            ),
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    supportingContent = {
                                        Text(
                                            AnnotatedString.fromHtml(it.snippet),
                                            softWrap = true,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    trailingContent = {
                                        if (it.thumbnail != null && !dataSaver)
                                            FeedImage(
                                                source = it.thumbnail.source,
                                                imageLoader = imageLoader,
                                                contentScale = ContentScale.Crop,
                                                loadingIndicator = true,
                                                modifier = Modifier
                                                    .padding(vertical = 4.dp)
                                                    .size(56.dp)
                                                    .clip(shapes.large)
                                            )
                                        else null
                                    },
                                    colors = ListItemDefaults
                                        .colors(containerColor = SearchBarDefaults.colors().containerColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(shapes.large)
                                        .clickable(
                                            onClick = {
                                                onExpandedChange(false)
                                                loadPage(it.title)
                                            }
                                        )
                                        .animateItem()
                                )
                            }
                            item(span = { GridItemSpan(2) }) {
                                Spacer(
                                    Modifier.height(
                                        WindowInsets.systemBars.asPaddingValues()
                                            .calculateBottomPadding() + 152.dp
                                    )
                                )
                            }
                        }
                    else {
                        LazyColumn(state = searchListState) {
                            item {
                                Text(
                                    text = "Title matches",
                                    modifier = Modifier.padding(16.dp),
                                    style = typography.labelLarge
                                )
                            }
                            items(
                                appSearchBarState.prefixSearchResults ?: emptyList(),
                                key = { "${it.title}-prefix" }) {
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            it.title,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    supportingContent = if (it.terms != null) {
                                        {
                                            Text(
                                                it.terms.description[0],
                                                softWrap = true,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    } else null,
                                    trailingContent = {
                                        if (it.thumbnail != null && !dataSaver)
                                            FeedImage(
                                                source = it.thumbnail.source,
                                                imageLoader = imageLoader,
                                                contentScale = ContentScale.Crop,
                                                loadingIndicator = true,
                                                modifier = Modifier
                                                    .padding(vertical = 4.dp)
                                                    .size(56.dp)
                                                    .clip(shapes.large)
                                            )
                                        else null
                                    },
                                    colors = ListItemDefaults
                                        .colors(containerColor = SearchBarDefaults.colors().containerColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            onClick = {
                                                onExpandedChange(false)
                                                loadPage(it.title)
                                            }
                                        )
                                        .animateItem()
                                )
                            }
                            item {
                                Text(
                                    text = "In-article matches",
                                    modifier = Modifier.padding(16.dp),
                                    style = typography.labelLarge
                                )
                            }
                            items(
                                appSearchBarState.searchResults ?: emptyList(),
                                key = { "${it.title}-search" }) {
                                ListItem(
                                    overlineContent = if (it.redirectTitle != null) {
                                        {
                                            Text(
                                                "Redirected from ${it.redirectTitle}",
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    } else null,
                                    headlineContent = {
                                        Text(
                                            AnnotatedString.fromHtml(
                                                if (it.titleSnippet.isNotEmpty())
                                                    it.titleSnippet
                                                else it.title
                                            ),
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    supportingContent = {
                                        Text(
                                            AnnotatedString.fromHtml(it.snippet),
                                            softWrap = true,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    trailingContent = {
                                        if (it.thumbnail != null && !dataSaver)
                                            FeedImage(
                                                source = it.thumbnail.source,
                                                imageLoader = imageLoader,
                                                contentScale = ContentScale.Crop,
                                                loadingIndicator = true,
                                                modifier = Modifier
                                                    .padding(vertical = 4.dp)
                                                    .size(56.dp)
                                                    .clip(shapes.large)
                                            )
                                        else null
                                    },
                                    colors = ListItemDefaults
                                        .colors(containerColor = SearchBarDefaults.colors().containerColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            onClick = {
                                                onExpandedChange(false)
                                                loadPage(it.title)
                                            }
                                        )
                                        .animateItem()
                                )
                            }
                            item {
                                Spacer(
                                    Modifier.height(
                                        WindowInsets.systemBars.asPaddingValues()
                                            .calculateBottomPadding() + 152.dp
                                    )
                                )
                            }
                        }
                    }
            }
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Preview(
//    widthDp = 400,
//    heightDp = 750,
//    showBackground = true
//)
//@Composable
//fun AppSearchBarPreview() {
//    WikiReaderTheme {
//        AppSearchBar(
//            appSearchBarState = AppSearchBarState(), true, false, 0, ImageLoader(LocalContext.current),
//            rememberLazyListState(), windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
//            {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
//        )
//    }
//}
