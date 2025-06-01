package org.nsh07.wikireader.ui.homeScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.window.core.layout.WindowSizeClass
import coil3.ImageLoader
import org.nsh07.wikireader.R
import org.nsh07.wikireader.data.WikiPrefixSearchResult
import org.nsh07.wikireader.data.WikiSearchResult
import org.nsh07.wikireader.data.langCodeToName
import org.nsh07.wikireader.ui.image.FeedImage
import org.nsh07.wikireader.ui.settingsScreen.LanguageBottomSheet
import org.nsh07.wikireader.ui.theme.ExpressiveListItemShapes.bottomListItemShape
import org.nsh07.wikireader.ui.theme.ExpressiveListItemShapes.middleListItemShape
import org.nsh07.wikireader.ui.theme.ExpressiveListItemShapes.topListItemShape
import org.nsh07.wikireader.ui.viewModel.AppSearchBarState
import org.nsh07.wikireader.ui.viewModel.PreferencesState

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
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
    scrollBehavior: TopAppBarScrollBehavior?,
    languageSearchStr: String,
    languageSearchQuery: String,
    saveLang: (String) -> Unit,
    updateLanguageSearchStr: (String) -> Unit,
    loadSearch: (String) -> Unit,
    loadSearchDebounced: (String) -> Unit,
    loadPage: (String) -> Unit,
    loadRandom: () -> Unit,
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
    val compactWindow =
        !windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    val (showLanguageSheet, setShowLanguageSheet) = remember { mutableStateOf(false) }

    LaunchedEffect(textFieldState.text) {
        loadSearchDebounced(textFieldState.text.toString())
    }

    val inputField =
        @Composable {
            SearchBarDefaults.InputField(
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                onSearch = loadSearch,
                placeholder = {
                    val weight by animateFloatAsState(
                        if (searchBarState.targetValue == SearchBarValue.Expanded) 0f else 1f,
                        animationSpec = motionScheme.defaultSpatialSpec()
                    )
                    Row {
                        if (weight > 0f) Spacer(Modifier.weight(weight))
                        Text(stringResource(R.string.searchWikipedia), style = typography.bodyLarge)
                        Spacer(Modifier.weight(1f))
                    }
                },
                trailingIcon =
                    if (textFieldState.text != "") {
                        {
                            IconButton(
                                shapes = IconButtonDefaults.shapes(),
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
                    } else null,
                enabled = searchBarEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
        }

    val textStyle = LocalTextStyle.current
    TopAppBar(
        title = {
            CompositionLocalProvider(
                LocalTextStyle provides textStyle
            ) {
                BoxWithConstraints {
                    SearchBar(
                        state = searchBarState,
                        inputField = inputField,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .padding(
                                top = TopAppBarDefaults.windowInsets.asPaddingValues()
                                    .calculateTopPadding()
                            )
                            .width(max(320.dp, this@BoxWithConstraints.maxWidth / 2))
                    )
                }
            }
        },
        subtitle = {},
        navigationIcon = {
            AnimatedContent(
                searchBarState.targetValue == SearchBarValue.Collapsed &&
                        compactWindow,
                modifier = Modifier.padding(
                    top = TopAppBarDefaults.windowInsets.asPaddingValues()
                        .calculateTopPadding(),
                    start = 4.dp,
                    end = 4.dp
                )
            ) { currentValue ->
                when (currentValue) {
                    true ->
                        IconButton(
                            shapes = IconButtonDefaults.shapes(),
                            onClick = onMenuIconClicked
                        ) {
                            Icon(
                                painterResource(R.drawable.menu),
                                contentDescription = stringResource(R.string.moreOptions)
                            )
                        }

                    else ->
                        IconButton(
                            shapes = IconButtonDefaults.shapes(),
                            onClick = {
                                if (searchBarState.currentValue == SearchBarValue.Expanded)
                                    loadSearch(textFieldState.text.toString())
                                else
                                    focusRequester.requestFocus()
                            }
                        ) {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = null
                            )
                        }
                }
            }
        },
        actions = {
            FilledTonalIconButton(
                shapes = IconButtonDefaults.shapes(),
                onClick = loadRandom,
                modifier = Modifier.padding(
                    top = TopAppBarDefaults.windowInsets.asPaddingValues()
                        .calculateTopPadding(),
                    start = 4.dp,
                    end = 4.dp
                )
            ) {
                Icon(
                    painterResource(R.drawable.shuffle),
                    contentDescription = null
                )
            }
        },
        scrollBehavior = scrollBehavior,
        windowInsets =
            if (!compactWindow)
                TopAppBarDefaults.windowInsets
                    .only(WindowInsetsSides.Bottom + WindowInsetsSides.End)
            else
                TopAppBarDefaults.windowInsets
                    .only(WindowInsetsSides.Bottom + WindowInsetsSides.End + WindowInsetsSides.Start),
        titleHorizontalAlignment = Alignment.CenterHorizontally,
        colors = TopAppBarDefaults.topAppBarColors(scrolledContainerColor = colorScheme.surface),
        modifier = modifier
    )

    ExpandedFullScreenSearchBar(
        state = searchBarState,
        inputField = inputField
    ) {
        if (showLanguageSheet)
            LanguageBottomSheet(
                lang = preferencesState.lang,
                searchStr = languageSearchStr,
                searchQuery = languageSearchQuery,
                setShowSheet = setShowLanguageSheet,
                setLang = {
                    saveLang(it)
                    loadSearchDebounced(textFieldState.text.toString())
                },
                setSearchStr = updateLanguageSearchStr
            )
        AnimatedContent(textFieldState.text.trim().isEmpty()) {
            when (it) {
                true ->
                    if (preferencesState.searchHistory) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 24.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.history),
                                        style = typography.labelLarge
                                    )
                                    Spacer(Modifier.weight(1f))
                                    TextButton(
                                        shapes = ButtonDefaults.shapes(),
                                        onClick = clearHistory,
                                        enabled = size > 0
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
                                            shapes = IconButtonDefaults.shapes(),
                                            onClick = { setQuery(currentText) },
                                            modifier = Modifier.wrapContentSize()
                                        ) {
                                            Icon(
                                                painterResource(R.drawable.north_west),
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .clip(
                                            if (size == 1) shapes.large
                                            else if (it == 0) topListItemShape
                                            else if (it == size - 1) bottomListItemShape
                                            else middleListItemShape
                                        )
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
                    if (!compactWindow) {
                        LazyVerticalGrid(columns = GridCells.Fixed(2)) {
                            item(span = { GridItemSpan(2) }) {
                                Box(Modifier.padding(bottom = 16.dp)) {
                                    FilledTonalButton(
                                        shapes = ButtonDefaults.shapes(),
                                        onClick = { setShowLanguageSheet(true) },
                                        modifier = Modifier.padding(
                                            start = 16.dp,
                                            end = 16.dp,
                                            top = 16.dp
                                        )
                                    ) {
                                        Icon(painterResource(R.drawable.translate), null)
                                        Spacer(Modifier.width(8.dp))
                                        Text(langCodeToName(preferencesState.lang))
                                    }
                                }
                            }
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
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            state = searchListState
                        ) {
                            item {
                                FilledTonalButton(
                                    shapes = ButtonDefaults.shapes(),
                                    onClick = { setShowLanguageSheet(true) },
                                    modifier = Modifier.padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        top = 16.dp
                                    )
                                ) {
                                    Icon(painterResource(R.drawable.translate), null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(langCodeToName(preferencesState.lang))
                                }
                            }
                            item {
                                Text(
                                    text = stringResource(R.string.titleMatches),
                                    modifier = Modifier.padding(
                                        horizontal = 24.dp,
                                        vertical = 16.dp
                                    ),
                                    style = typography.labelLarge
                                )
                            }
                            itemsIndexed(
                                appSearchBarState.prefixSearchResults ?: emptyList(),
                                key = { index: Int, it: WikiPrefixSearchResult -> "${it.title}-prefix" }
                            ) { index: Int, it: WikiPrefixSearchResult ->
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
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .clip(
                                            if (appSearchBarState.prefixSearchResults?.size == 1) shapes.large
                                            else if (index == 0) topListItemShape
                                            else if (index == appSearchBarState.prefixSearchResults?.lastIndex) bottomListItemShape
                                            else middleListItemShape
                                        )
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
                                    modifier = Modifier.padding(
                                        horizontal = 24.dp,
                                        vertical = 16.dp
                                    ),
                                    style = typography.labelLarge
                                )
                            }
                            itemsIndexed(
                                appSearchBarState.searchResults ?: emptyList(),
                                key = { index: Int, it: WikiSearchResult -> "${it.title}-search" }
                            ) { index: Int, it: WikiSearchResult ->
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
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .clip(
                                            if (appSearchBarState.searchResults?.size == 1) shapes.large
                                            else if (index == 0) topListItemShape
                                            else if (index == appSearchBarState.searchResults?.lastIndex) bottomListItemShape
                                            else middleListItemShape
                                        )
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
