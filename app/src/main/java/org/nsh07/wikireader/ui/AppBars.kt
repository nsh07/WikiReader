package org.nsh07.wikireader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.nsh07.wikireader.R
import org.nsh07.wikireader.ui.theme.WikiReaderTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSearchBar(
    searchBarState: SearchBarState,
    performSearch: (String) -> Unit,
    setExpanded: (Boolean) -> Unit,
    setQuery: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = searchBarState.focusRequester
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
                },
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
            items(searchBarState.history.size) {
                ListItem(
                    leadingContent = {
                        Icon(
                        Icons.Outlined.Search,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                            .padding(4.dp)
                        )
                    },
                    headlineContent = {
                        Text(
                            searchBarState.history[it],
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )
                    },
                    trailingContent = {
                        IconButton(
                        onClick = { setQuery(searchBarState.history[it]) },
                        modifier = Modifier.wrapContentSize()
                        ) {
                            Icon(painterResource(R.drawable.north_west), contentDescription = null)
                        }
                    },
                    colors = ListItemDefaults
                        .colors(containerColor = SearchBarDefaults.colors().containerColor),
                    modifier = Modifier
                        .clickable(onClick = { performSearch(searchBarState.history[it]) })
                )
            }
        }
    }
}

@Preview(
    widthDp = 400,
    showBackground = true
)
@Composable
fun AppSearchBarPreview() {
    WikiReaderTheme {
        AppSearchBar(
            searchBarState = SearchBarState(),
            {}, {}, {}
        )
    }
}
