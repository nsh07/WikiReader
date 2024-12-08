package org.nsh07.wikireader.ui.settingsScreen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSearchBar(
    searchStr: String,
    setSearchStr: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    DockedSearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = searchStr,
                onQueryChange = setSearchStr,
                onSearch = {},
                expanded = false,
                onExpandedChange = {},
                placeholder = { Text("Search languages...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search") },
            )
        },
        expanded = false,
        onExpandedChange = {},
        modifier = modifier
    ) {}
}