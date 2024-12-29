package org.nsh07.wikireader.ui.homeScreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.nsh07.wikireader.R
import org.nsh07.wikireader.ui.theme.WikiReaderTheme
import org.nsh07.wikireader.ui.viewModel.SearchBarState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppSearchBar(
    searchBarState: SearchBarState,
    searchBarEnabled: Boolean,
    index: Int,
    performSearch: (String) -> Unit,
    setExpanded: (Boolean) -> Unit,
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
    Column {
        DockedSearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = searchBarState.query,
                    onQueryChange = setQuery,
                    onSearch = performSearch,
                    expanded = searchBarState.isSearchBarExpanded,
                    onExpandedChange = setExpanded,
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
            onExpandedChange = setExpanded,
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            ) {
                val history = searchBarState.history.toList()
                val size = history.size
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
                                Icon(painterResource(R.drawable.north_west), contentDescription = null)
                            }
                        },
                        colors = ListItemDefaults
                            .colors(containerColor = SearchBarDefaults.colors().containerColor),
                        modifier = Modifier
                            .combinedClickable(
                                onClick = { performSearch(currentText) },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    removeHistoryItem(currentText)
                                }
                            )
                            .animateItem()
                    )
                }
            }
        }
        if (index > 0) HorizontalDivider()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    widthDp = 400,
    showBackground = true
)
@Composable
fun AppSearchBarPreview() {
    WikiReaderTheme {
        AppSearchBar(
            searchBarState = SearchBarState(), true, 0,
            {}, {}, {}, {}, {}, {}, {}, {}
        )
    }
}
