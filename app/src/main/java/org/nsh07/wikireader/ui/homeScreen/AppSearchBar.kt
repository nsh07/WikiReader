package org.nsh07.wikireader.ui.homeScreen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import org.nsh07.wikireader.R
import org.nsh07.wikireader.ui.image.FeedImage
import org.nsh07.wikireader.ui.theme.WikiReaderTheme
import org.nsh07.wikireader.ui.viewModel.SearchBarState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppSearchBar(
    searchBarState: SearchBarState,
    searchBarEnabled: Boolean,
    index: Int,
    imageLoader: ImageLoader,
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
    val focusRequester = searchBarState.focusRequester
    val haptic = LocalHapticFeedback.current
    val (dropdownExpanded, setDropdownExpanded) = remember { mutableStateOf(false) }
    val searchBarPadding by animateDpAsState(
        targetValue = if (searchBarState.isSearchBarExpanded) 0.dp else 16.dp,
        label = "Search bar padding"
    )
    val history = searchBarState.history.toList()
    val size = history.size

    Column {
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = searchBarState.query,
                    onQueryChange = {
                        setQuery(it)
                        loadSearchDebounced(it)
                    },
                    onSearch = loadSearch,
                    expanded = searchBarState.isSearchBarExpanded,
                    onExpandedChange = onExpandedChange,
                    placeholder = { Text("Search Wikipedia...") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search") },
                    trailingIcon = {
                        Row {
                            if (searchBarState.query != "") {
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
            },
            expanded = searchBarState.isSearchBarExpanded,
            onExpandedChange = onExpandedChange,
            modifier = modifier
                .fillMaxWidth()
                .padding(searchBarPadding)
        ) {
            Crossfade(searchBarState.query.trim().isEmpty()) {
                when (it) {
                    true ->
                        LazyColumn {
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "History",
                                        style = MaterialTheme.typography.labelLarge,
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
                                            onClick = { loadSearch(currentText) },
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                removeHistoryItem(currentText)
                                            }
                                        )
                                        .animateItem()
                                )
                            }
                        }

                    else -> LazyColumn(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(searchBarState.searchResults, key = { it.title }) {
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
                                leadingContent = {
                                    if (it.thumbnail != null)
                                        FeedImage(
                                            source = it.thumbnail.source,
                                            imageLoader = imageLoader,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .clip(shapes.large)
                                                .size(64.dp)
                                        )
                                    else
                                        Box(Modifier.size(64.dp)) {
                                            Icon(
                                                painterResource(R.drawable.image),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .align(Alignment.Center)
                                            )
                                        }
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
        if (index > 0) HorizontalDivider()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    widthDp = 400,
    heightDp = 750,
    showBackground = true
)
@Composable
fun AppSearchBarPreview() {
    WikiReaderTheme {
        AppSearchBar(
            searchBarState = SearchBarState(), true, 0, ImageLoader(LocalContext.current),
            {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
        )
    }
}
