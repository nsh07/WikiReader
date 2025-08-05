package org.nsh07.wikireader.ui.homeScreen

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.InputChip
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
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
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
import androidx.compose.ui.util.fastForEach
import androidx.window.core.layout.WindowSizeClass
import coil3.ImageLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.nsh07.wikireader.R
import org.nsh07.wikireader.data.SearchHistoryItem
import org.nsh07.wikireader.data.UserLanguage
import org.nsh07.wikireader.data.WikiPrefixSearchResult
import org.nsh07.wikireader.data.WikiSearchResult
import org.nsh07.wikireader.ui.homeScreen.viewModel.AppSearchBarState
import org.nsh07.wikireader.ui.homeScreen.viewModel.HomeAction
import org.nsh07.wikireader.ui.image.FeedImage
import org.nsh07.wikireader.ui.settingsScreen.LanguageBottomSheet
import org.nsh07.wikireader.ui.settingsScreen.viewModel.PreferencesState
import org.nsh07.wikireader.ui.settingsScreen.viewModel.SettingsAction
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.bottomListItemShape
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.middleListItemShape
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.topListItemShape

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
    userLangs: List<UserLanguage>,
    recentLangs: List<String>,
    searchHistory: List<SearchHistoryItem>,
    searchBarEnabled: Boolean,
    imageLoader: ImageLoader,
    searchListState: LazyListState,
    windowSizeClass: WindowSizeClass,
    scrollBehavior: TopAppBarScrollBehavior?,
    languageSearchStr: String,
    languageSearchQuery: String,
    onAction: (HomeAction) -> Unit,
    onSettingsAction: (SettingsAction) -> Unit,
    onSearchBarExpandedChange: (Boolean) -> Unit,
    removeHistoryItem: (SearchHistoryItem) -> Unit,
    clearHistory: () -> Unit,
    onMenuIconClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = appSearchBarState.focusRequester
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val colorScheme = colorScheme
    val size = searchHistory.size
    val compactWindow =
        !windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    val (showLanguageSheet, setShowLanguageSheet) = remember { mutableStateOf(false) }

    var enabled by remember { mutableStateOf(Build.VERSION.SDK_INT >= 29) }

    if (Build.VERSION.SDK_INT < 29) {
        LaunchedEffect(Unit) {
            scope.launch {
                delay(1000)
                enabled = true
            }
        }
    }

    LaunchedEffect(textFieldState.text) {
        onAction(HomeAction.LoadSearchResultsDebounced(textFieldState.text.toString()))
    }

    val inputField =
        @Composable {
            SearchBarDefaults.InputField(
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                onSearch = { onAction(HomeAction.LoadSearch(it)) },
                placeholder = {
                    val alignment by animateHorizontalAlignmentAsState(
                        if (searchBarState.targetValue == SearchBarValue.Expanded) -1f else 0f,
                        animationSpec = motionScheme.defaultSpatialSpec()
                    )
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
                        Text(stringResource(R.string.searchWikipedia), style = typography.bodyLarge)
                    }
                },
                trailingIcon =
                    if (textFieldState.text.isNotEmpty()) {
                        {
                            IconButton(
                                shapes = IconButtonDefaults.shapes(),
                                onClick = {
                                    onAction(HomeAction.SetQuery(""))
                                    focusRequester.requestFocus()
                                }
                            ) {
                                Icon(
                                    painterResource(R.drawable.clear),
                                    contentDescription = stringResource(R.string.clearSearchField)
                                )
                            }
                        }
                    } else null,
                enabled = searchBarEnabled && enabled,
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
                                    onAction(HomeAction.LoadSearch(textFieldState.text.toString()))
                                else
                                    focusRequester.requestFocus()
                            }
                        ) {
                            Icon(
                                painterResource(R.drawable.search),
                                contentDescription = null
                            )
                        }
                }
            }
        },
        actions = {
            FilledTonalIconButton(
                shapes = IconButtonDefaults.shapes(),
                onClick = { onAction(HomeAction.LoadRandom) },
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
                recentLangs = recentLangs,
                searchStr = languageSearchStr,
                searchQuery = languageSearchQuery,
                setShowSheet = setShowLanguageSheet,
                setLang = {
                    onSettingsAction(SettingsAction.SaveLang(it))
                    onAction(HomeAction.LoadSearchResultsDebounced(textFieldState.text.toString()))
                },
                setSearchStr = { onAction(HomeAction.UpdateLanguageSearchStr(it)) },
                userLanguageSelectionMode = true,
                insertUserLanguage = { onAction(HomeAction.InsertUserLanguage(it)) },
                deleteUserLanguage = { onAction(HomeAction.DeleteUserLanguage(it)) }
            )

        if (Build.VERSION.SDK_INT < 29) { // Workaround to fix the infinite search bar loop bug
            BackHandler(true) {
                scope.launch {
                    enabled = false
                    searchBarState.animateToCollapsed()
                    delay(1000)
                    enabled = true
                }
            }
        }

        AnimatedContent(textFieldState.text.trim().isEmpty()) { targetState ->
            when (targetState) {
                true ->
                    if (preferencesState.searchHistory) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 32.dp)
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
                            items(size, key = { searchHistory[it].time }) {
                                val currentText = searchHistory[it].query
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
                                            onClick = { onAction(HomeAction.SetQuery(currentText)) },
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
                                                onAction(HomeAction.LoadSearch(currentText))
                                                textFieldState.setTextAndPlaceCursorAtEnd(
                                                    currentText
                                                )
                                            },
                                            onLongClick = {
                                                haptic.performHapticFeedback(
                                                    HapticFeedbackType.LongPress
                                                )
                                                removeHistoryItem(searchHistory[it])
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
                                Box(Modifier.padding(vertical = 8.dp)) {
                                    FlowRow(
                                        Modifier.padding(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        userLangs.fastForEach { filterOption ->
                                            val filterChipInteractionSource =
                                                remember { MutableInteractionSource() }
                                            Box {
                                                FilterChip(
                                                    selected = filterOption.selected,
                                                    onClick = {},
                                                    label = { Text(filterOption.langName) },
                                                    leadingIcon =
                                                        if (filterOption.selected) {
                                                            {
                                                                Icon(
                                                                    painterResource(R.drawable.check),
                                                                    contentDescription = null
                                                                )
                                                            }
                                                        } else null,
                                                    interactionSource = filterChipInteractionSource
                                                )
                                                Box( // Workaround to enable
                                                    modifier = Modifier
                                                        .matchParentSize()
                                                        .combinedClickable(
                                                            onClick = {
                                                                scope.launch {
                                                                    onAction(
                                                                        HomeAction.MarkUserLanguageSelected(
                                                                            filterOption.lang
                                                                        )
                                                                    )
                                                                    haptic.performHapticFeedback(
                                                                        HapticFeedbackType.ToggleOn
                                                                    )
                                                                    onAction(
                                                                        HomeAction.LoadSearchResultsDebounced(
                                                                            textFieldState.text.toString()
                                                                        )
                                                                    )
                                                                }
                                                            },
                                                            onLongClick = {
                                                                haptic.performHapticFeedback(
                                                                    HapticFeedbackType.LongPress
                                                                )
                                                                if (userLangs.size > 1)
                                                                    onAction(
                                                                        HomeAction.DeleteUserLanguage(
                                                                            filterOption.lang
                                                                        )
                                                                    )
                                                            },
                                                            interactionSource = filterChipInteractionSource,
                                                            indication = null,
                                                        )
                                                )
                                            }
                                        }
                                        InputChip(
                                            onClick = { setShowLanguageSheet(true) },
                                            label = {},
                                            leadingIcon = {
                                                Icon(
                                                    painterResource(R.drawable.add),
                                                    "Add language"
                                                )
                                            },
                                            selected = false,
                                            modifier = Modifier.width(40.dp)
                                        )
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
                                                onSearchBarExpandedChange(false)
                                                onAction(HomeAction.LoadPage(it.title))
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
                                                it.titleSnippet.ifEmpty { it.title }
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
                                                onSearchBarExpandedChange(false)
                                                onAction(HomeAction.LoadPage(it.title))
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
                                FlowRow(
                                    Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    userLangs.fastForEach { filterOption ->
                                        val filterChipInteractionSource =
                                            remember { MutableInteractionSource() }
                                        Box {
                                            FilterChip(
                                                selected = filterOption.selected,
                                                onClick = {},
                                                label = { Text(filterOption.langName) },
                                                leadingIcon =
                                                    if (filterOption.selected) {
                                                        {
                                                            Icon(
                                                                painterResource(R.drawable.check),
                                                                contentDescription = null
                                                            )
                                                        }
                                                    } else null,
                                                interactionSource = filterChipInteractionSource
                                            )
                                            Box( // Workaround to enable
                                                modifier = Modifier
                                                    .matchParentSize()
                                                    .combinedClickable(
                                                        onClick = {
                                                            scope.launch {
                                                                onAction(
                                                                    HomeAction.MarkUserLanguageSelected(
                                                                        filterOption.lang
                                                                    )
                                                                )
                                                                haptic.performHapticFeedback(
                                                                    HapticFeedbackType.ToggleOn
                                                                )
                                                                onAction(
                                                                    HomeAction.LoadSearchResultsDebounced(
                                                                        textFieldState.text.toString()
                                                                    )
                                                                )
                                                            }
                                                        },
                                                        onLongClick = {
                                                            haptic.performHapticFeedback(
                                                                HapticFeedbackType.LongPress
                                                            )
                                                            if (userLangs.size > 1)
                                                                onAction(
                                                                    HomeAction.DeleteUserLanguage(
                                                                        filterOption.lang
                                                                    )
                                                                )
                                                        },
                                                        interactionSource = filterChipInteractionSource,
                                                        indication = null,
                                                    )
                                            )
                                        }
                                    }
                                    InputChip(
                                        onClick = { setShowLanguageSheet(true) },
                                        label = {},
                                        leadingIcon = {
                                            Icon(painterResource(R.drawable.add), "Add language")
                                        },
                                        selected = false,
                                        modifier = Modifier.width(40.dp)
                                    )
                                }
                            }
                            item {
                                Text(
                                    text = stringResource(R.string.titleMatches),
                                    modifier = Modifier.padding(
                                        horizontal = 32.dp,
                                        vertical = 14.dp
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
                                                onSearchBarExpandedChange(false)
                                                onAction(HomeAction.LoadPage(it.title))
                                            }
                                        )
                                        .animateItem()
                                )
                            }
                            item {
                                Text(
                                    text = stringResource(R.string.inArticleMatches),
                                    modifier = Modifier.padding(
                                        horizontal = 32.dp,
                                        vertical = 14.dp
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
                                                it.titleSnippet.ifEmpty { it.title }
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
                                                onSearchBarExpandedChange(false)
                                                onAction(HomeAction.LoadPage(it.title))
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

@Composable
private fun animateHorizontalAlignmentAsState(
    targetBiasValue: Float,
    animationSpec: AnimationSpec<Float>
): State<BiasAlignment.Horizontal> {
    val bias by animateFloatAsState(targetBiasValue, animationSpec)
    return remember { derivedStateOf { BiasAlignment.Horizontal(bias) } }
}
