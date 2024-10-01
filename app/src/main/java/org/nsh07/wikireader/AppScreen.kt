package org.nsh07.wikireader

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import org.nsh07.wikireader.ui.AppHomeScreen
import org.nsh07.wikireader.ui.AppSearchBar
import org.nsh07.wikireader.ui.UiViewModel

@Composable
fun AppScreen(
    modifier: Modifier = Modifier,
    viewModel: UiViewModel = viewModel()
) {
    val searchBarState by viewModel.searchBarState.collectAsState()
    val homeScreenState by viewModel.homeScreenState.collectAsState()
    val listState by viewModel.listState.collectAsState()

    viewModel.setDefaultContent(LocalContext.current)

    Scaffold(modifier = modifier) { _ ->
        Column(modifier = modifier) {
            AppSearchBar(
                searchBarState = searchBarState,
                performSearch = { viewModel.performSearch(it) },
                setExpanded = { viewModel.setExpanded(it) },
                setQuery = { viewModel.setQuery(it) }
            )
            AppHomeScreen(
                homeScreenState = homeScreenState,
                listState = listState,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}
