package org.nsh07.wikireader.ui.homeScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
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
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarScrollBehavior
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopSearchBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
    onMenuIconClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = appSearchBarState.focusRequester
    val haptic = LocalHapticFeedback.current
    val colorScheme = colorScheme
    val history = appSearchBarState.history.toList()
    val size = history.size
    LaunchedEffect(textFieldState.text) {
        loadSearchDebounced(textFieldState.text.toString())
    }
    val inputField =
        @Composable {
            SearchBarDefaults.InputField(
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                onSearch = loadSearch,
                placeholder = { Text(stringResource(R.string.searchWikipedia)) },
                leadingIcon = {
                    AnimatedContent(
                        searchBarState.targetValue == SearchBarValue.Collapsed &&
                                windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
                    ) { currentValue ->
                        when (currentValue) {
                            true ->
                                IconButton(onClick = onMenuIconClicked) {
                                    Icon(
                                        Icons.Outlined.Menu,
                                        contentDescription = stringResource(R.string.moreOptions)
                                    )
                                }

                            else ->
                                IconButton(onClick = { loadSearch(textFieldState.text.toString()) }) {
                                    Icon(
                                        Icons.Outlined.Search,
                                        contentDescription = null
                                    )
                                }
                        }
                    }
                },
                trailingIcon = {
                    if (textFieldState.text != "") {
                        IconButton(
                            onClick = {
                                setQuery("")
                                focusRequester.requestFocus()
                            }
                        ) {
                            Icon(
                                Icons.Outlined.Clear,
                                contentDescription = stringResource(R.string.clearSearchField)
                            )
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
            .drawBehind {
                drawRect(color = colorScheme.surface)
            }
            .padding(horizontal = 8.dp)
    )

    ExpandedFullScreenSearchBar(
        state = searchBarState,
        inputField = inputField
    ) {
        AnimatedContent(textFieldState.text.trim().isEmpty()) {
            when (it) {
                true ->
                    if (preferencesState.searchHistory) {
                        LazyColumn(Modifier.weight(4f)) {
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        stringResource(R.string.history),
                                        style = typography.labelLarge,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                    Spacer(Modifier.weight(1f))
                                    TextButton(
                                        onClick = clearHistory,
                                        enabled = size > 0,
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        Text(stringResource(R.string.clear))
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
                                                haptic.performHapticFeedback(
                                                    HapticFeedbackType.LongPress
                                                )
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
                                        if (it.thumbnail != null && !preferencesState.dataSaver)
                                            FeedImage(
                                                source = it.thumbnail.source,
                                                imageLoader = imageLoader,
                                                contentScale = ContentScale.Crop,
                                                loadingIndicator = true,
                                                background = preferencesState.imageBackground,
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
                                                stringResource(
                                                    R.string.redirectedFrom,
                                                    it.redirectTitle
                                                ),
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
                                        if (it.thumbnail != null && !preferencesState.dataSaver)
                                            FeedImage(
                                                source = it.thumbnail.source,
                                                imageLoader = imageLoader,
                                                contentScale = ContentScale.Crop,
                                                loadingIndicator = true,
                                                background = preferencesState.imageBackground,
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
                                    text = stringResource(R.string.titleMatches),
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
                                        if (it.thumbnail != null && !preferencesState.dataSaver)
                                            FeedImage(
                                                source = it.thumbnail.source,
                                                imageLoader = imageLoader,
                                                contentScale = ContentScale.Crop,
                                                loadingIndicator = true,
                                                background = preferencesState.imageBackground,
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
                                    text = stringResource(R.string.inArticleMatches),
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
                                                stringResource(
                                                    R.string.redirectedFrom,
                                                    it.redirectTitle
                                                ),
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
                                        if (it.thumbnail != null && !preferencesState.dataSaver)
                                            FeedImage(
                                                source = it.thumbnail.source,
                                                imageLoader = imageLoader,
                                                contentScale = ContentScale.Crop,
                                                loadingIndicator = true,
                                                background = preferencesState.imageBackground,
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
