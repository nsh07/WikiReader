package org.nsh07.wikireader.ui.homeScreen

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.LoadingIndicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import coil3.ImageLoader
import kotlinx.coroutines.delay
import org.nsh07.wikireader.R
import org.nsh07.wikireader.data.SavedStatus
import org.nsh07.wikireader.data.WRStatus
import org.nsh07.wikireader.data.langCodeToName
import org.nsh07.wikireader.ui.image.ImageCard
import org.nsh07.wikireader.ui.shimmer.AnimatedShimmer
import org.nsh07.wikireader.ui.shimmer.FeedLoader
import org.nsh07.wikireader.ui.theme.isDark
import org.nsh07.wikireader.ui.viewModel.FeedState
import org.nsh07.wikireader.ui.viewModel.HomeScreenState
import org.nsh07.wikireader.ui.viewModel.PreferencesState

/**
 * The app home screen composable.
 *
 * @param homeScreenState A [HomeScreenState] object provided by the app's ViewModel
 * @param listState A [LazyListState] object provided by the app's ViewModel
 * @param preferencesState A [PreferencesState] object provided by the app's ViewModel
 * @param imageLoader A [ImageLoader] object, used to load the page image
 * @param languageSearchStr A [String] used for the search string of the language search bar
 * @param languageSearchQuery A [String] used for the actual language search. This is generally a
 * debounced state flow.
 * @param onImageClick A lambda that is called when the image in the home screen is clicked
 * @param onLinkClick A lambda that is called when a page link is clicked
 * @param setLang A lambda that is called when the user picks a language from the language list
 * @param setSearchStr A lambda that is called when the user types in the language search bar
 * @param insets A [PaddingValues] object provided by the parent [androidx.compose.material3.Scaffold]
 * @param modifier Self explanatory
 */

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppHomeScreen(
    homeScreenState: HomeScreenState,
    listState: LazyListState,
    preferencesState: PreferencesState,
    feedState: FeedState,
    feedListState: LazyListState,
    imageLoader: ImageLoader,
    languageSearchStr: String,
    languageSearchQuery: String,
    showLanguageSheet: Boolean,
    searchBarOffset: Float,
    searchBarOffsetLimit: Float,
    onImageClick: () -> Unit,
    onGalleryImageClick: (String, String) -> Unit,
    onLinkClick: (String) -> Unit,
    refreshSearch: () -> Unit,
    refreshFeed: () -> Unit,
    setLang: (String) -> Unit,
    setSearchStr: (String) -> Unit,
    setShowArticleLanguageSheet: (Boolean) -> Unit,
    saveArticle: () -> Unit,
    showFeedErrorSnackBar: () -> Unit,
    insets: PaddingValues,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    val photo = homeScreenState.photo
    val photoDesc = homeScreenState.photoDesc
    val fontSize = preferencesState.fontSize
    val weight = remember {
        if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM ||
            windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
        )
            1f
        else 0f
    }
    val fontFamily = remember(preferencesState.fontStyle) {
        if (preferencesState.fontStyle == "sans") FontFamily.SansSerif
        else FontFamily.Serif
    }

    val pullToRefreshState = rememberPullToRefreshState()

    var isRefreshing by remember { mutableStateOf(false) }

    var s = homeScreenState.extract.size
    if (s > 1) s -= 2
    else s = 0

    val sendIntent: Intent = Intent()
        .apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "https://${preferencesState.lang}.wikipedia.org/wiki/${
                    homeScreenState.title.replace(' ', '_')
                }"
            )
            type = "text/plain"
        }
    val shareIntent = Intent.createChooser(sendIntent, null)

    val context = LocalContext.current
    val systemBars = WindowInsets.systemBars

    val lang = preferencesState.lang
    val pageId = homeScreenState.pageId

    if (showLanguageSheet)
        ArticleLanguageBottomSheet(
            langs = homeScreenState.langs ?: emptyList(),
            searchStr = languageSearchStr,
            searchQuery = languageSearchQuery,
            setShowSheet = setShowArticleLanguageSheet,
            setLang = setLang,
            loadPage = onLinkClick,
            setSearchStr = setSearchStr
        )

    Box(modifier = modifier) { // The container for all the composables in the home screen
        if (homeScreenState.status != WRStatus.UNINITIALIZED &&
            homeScreenState.status != WRStatus.FEED_LOADED &&
            homeScreenState.status != WRStatus.FEED_NETWORK_ERROR
        ) {
            LaunchedEffect(isRefreshing) {
                delay(3000)
                isRefreshing = false
            } // hide refresh indicator after a delay
            Row {
                if (weight != 0f) Spacer(modifier = Modifier.weight(weight))
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    state = pullToRefreshState,
                    onRefresh = {
                        if (homeScreenState.status == WRStatus.FEED_NETWORK_ERROR)
                            refreshFeed()
                        else
                            refreshSearch()
                        isRefreshing = true
                    },
                    indicator = {
                        LoadingIndicator(
                            modifier = Modifier.align(Alignment.TopCenter),
                            isRefreshing = isRefreshing,
                            state = pullToRefreshState
                        )
                    },
                    modifier = Modifier.weight(4f)
                ) {
                    LazyColumn( // The article
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item { // Title
                            Row(modifier = Modifier.padding(16.dp)) {
                                FilledTonalButton(
                                    onClick = { setShowArticleLanguageSheet(true) },
                                    enabled = homeScreenState.langs?.isEmpty() == false
                                ) {
                                    Icon(painterResource(R.drawable.translate), null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(langCodeToName(preferencesState.lang))
                                }
                                Spacer(Modifier.weight(1f))
                                FilledTonalIconButton(
                                    onClick = {
                                        context.startActivity(shareIntent)
                                    },
                                    enabled = homeScreenState.status == WRStatus.SUCCESS,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Icon(
                                        painterResource(R.drawable.share),
                                        contentDescription = stringResource(R.string.sharePage)
                                    )
                                }
                                FilledTonalIconButton(
                                    onClick = saveArticle,
                                    enabled = homeScreenState.status == WRStatus.SUCCESS
                                ) {
                                    AnimatedContent(
                                        homeScreenState.savedStatus,
                                        label = "saveAnimation"
                                    ) { saved ->
                                        when (saved) {
                                            SavedStatus.SAVED ->
                                                Icon(
                                                    painterResource(R.drawable.download_done),
                                                    contentDescription = stringResource(R.string.deleteArticle)
                                                )

                                            SavedStatus.SAVING ->
                                                LoadingIndicator()

                                            else ->
                                                Icon(
                                                    painterResource(R.drawable.download),
                                                    contentDescription = stringResource(R.string.downloadArticle)
                                                )
                                        }
                                    }
                                }
                            }
                            HorizontalDivider()
                        }
                        item { // Title + Image/description
                            Text(
                                text = homeScreenState.title,
                                style = MaterialTheme.typography.displayMedium,
                                fontFamily = FontFamily.Serif,
                                modifier = Modifier.padding(16.dp)
                            )
                            if (photoDesc != null) {
                                ImageCard(
                                    photo = photo,
                                    photoDesc = photoDesc,
                                    title = homeScreenState.title,
                                    imageLoader = imageLoader,
                                    showPhoto = !preferencesState.dataSaver,
                                    onClick = onImageClick,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                        item { // Main description
                            if (homeScreenState.extract.isNotEmpty())
                                SelectionContainer {
                                    ParsedBodyText(
                                        body = homeScreenState.extract[0],
                                        fontSize = fontSize,
                                        fontFamily = fontFamily,
                                        renderMath = preferencesState.renderMath,
                                        imageLoader = imageLoader,
                                        darkTheme = colorScheme.isDark(),
                                        onLinkClick = onLinkClick,
                                        onGalleryImageClick = onGalleryImageClick
                                    )
                                }
                        }
                        itemsIndexed(
                            homeScreenState.extract,
                            key = { i, it -> "$pageId.$lang#$i" }
                        ) { i: Int, it: List<AnnotatedString> ->// Expandable sections logic
                            if (i % 2 == 1)
                                SelectionContainer {
                                    ExpandableSection(
                                        title = homeScreenState.extract[i],
                                        body = homeScreenState.extract.getOrElse(i + 1) { emptyList() },
                                        fontSize = fontSize,
                                        fontFamily = fontFamily,
                                        imageLoader = imageLoader,
                                        expanded = preferencesState.expandedSections,
                                        darkTheme = colorScheme.isDark(),
                                        renderMath = preferencesState.renderMath,
                                        onLinkClick = onLinkClick,
                                        onGalleryImageClick = onGalleryImageClick
                                    )
                                }
                        }

                        item {
                            Spacer(Modifier.height(insets.calculateBottomPadding() + 152.dp))
                        }
                    }
                }
                if (weight != 0f) Spacer(modifier = Modifier.weight(weight))
            }
        } else if ((homeScreenState.status == WRStatus.UNINITIALIZED) && !preferencesState.dataSaver) {
            Row {
                if (weight != 0f) Spacer(modifier = Modifier.weight(weight))
                AnimatedShimmer {
                    FeedLoader(brush = it, insets = insets, modifier = Modifier.weight(4f))
                }
                if (weight != 0f) Spacer(modifier = Modifier.weight(weight))
            }
        } else if (homeScreenState.status == WRStatus.FEED_NETWORK_ERROR || homeScreenState.status == WRStatus.UNINITIALIZED) {
            Icon(
                painterResource(R.drawable.ic_launcher_monochrome),
                contentDescription = null,
                modifier = Modifier
                    .size(400.dp)
                    .align(Alignment.Center)
            )
        } else {
            Row {
                if (weight != 0f) Spacer(modifier = Modifier.weight(weight))
                ArticleFeed(
                    feedState = feedState,
                    imageLoader = imageLoader,
                    insets = insets,
                    loadPage = onLinkClick,
                    refreshFeed = refreshFeed,
                    onImageClick = onImageClick,
                    listState = feedListState,
                    windowSizeClass = windowSizeClass,
                    modifier = Modifier.weight(4f)
                )
                if (weight != 0f) Spacer(modifier = Modifier.weight(weight))
            }
        }

        AnimatedVisibility( // The linear progress bar that shows up when the article is loading
            visible = homeScreenState.isLoading,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top),
            modifier = Modifier
                .padding(
                    top = (systemBars.asPaddingValues()
                        .calculateTopPadding() + (searchBarOffsetLimit.dp - searchBarOffset.dp)).coerceAtLeast(
                        0.dp
                    )
                )
        ) {
            if (homeScreenState.loadingProgress == null)
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                )
            else {
                val animatedProgress by animateFloatAsState(
                    targetValue = homeScreenState.loadingProgress,
                    animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
                )
                LinearProgressIndicator(
                    { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                )
            }
        }
    }

    LaunchedEffect(homeScreenState.status) {
        if (homeScreenState.status == WRStatus.FEED_NETWORK_ERROR)
            showFeedErrorSnackBar()
    }
}

