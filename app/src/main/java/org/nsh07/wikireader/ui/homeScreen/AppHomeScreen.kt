package org.nsh07.wikireader.ui.homeScreen

import android.content.ClipData
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.vibrantFloatingToolbarColors
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.LoadingIndicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.window.core.layout.WindowSizeClass
import coil3.ImageLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.nsh07.wikireader.R
import org.nsh07.wikireader.data.SavedStatus
import org.nsh07.wikireader.data.WRStatus
import org.nsh07.wikireader.data.WikiLang
import org.nsh07.wikireader.ui.homeScreen.viewModel.FeedState
import org.nsh07.wikireader.ui.homeScreen.viewModel.HomeAction
import org.nsh07.wikireader.ui.homeScreen.viewModel.HomeScreenState
import org.nsh07.wikireader.ui.image.ImageCard
import org.nsh07.wikireader.ui.settingsScreen.LanguageBottomSheet
import org.nsh07.wikireader.ui.settingsScreen.viewModel.PreferencesState
import org.nsh07.wikireader.ui.settingsScreen.viewModel.SettingsAction
import org.nsh07.wikireader.ui.shimmer.AnimatedShimmer
import org.nsh07.wikireader.ui.shimmer.FeedLoader
import org.nsh07.wikireader.ui.theme.isDark

/**
 * The main composable function for the app's home screen.
 *
 * @param homeScreenState The current state of the home screen, containing article data and UI status.
 * @param listState The [LazyListState] for the main article content.
 * @param preferencesState The current state of user preferences.
 * @param feedState The current state of the article feed.
 * @param recentLangs A list of recently used language codes.
 * @param floatingToolbarScrollBehaviour The [FloatingToolbarScrollBehavior] for the floating action toolbar.
 * @param feedListState The [LazyListState] for the article feed list.
 * @param imageLoader The app-wide [ImageLoader] used for loading images.
 * @param languageSearchStr The current search string for languages in the language bottom sheet.
 * @param languageSearchQuery The current search query for languages after debouncing.
 * @param showLanguageSheet A boolean indicating whether the language selection bottom sheet should be shown.
 * @param enableScrollButton A boolean indicating whether the scroll-to-top button should be enabled.
 * @param deepLinkHandled A boolean indicating if a deep link has been processed.
 * @param onImageClick A lambda function to be invoked when the main article image is clicked.
 * @param onGalleryImageClick A lambda function to be invoked when an image in the gallery is clicked.
 * It takes the image URL and description as parameters.
 * @param setShowArticleLanguageSheet A lambda function to control the visibility of the article language bottom sheet.
 * @param onAction A lambda function to dispatch [HomeAction] events to the ViewModel.
 * @param onSettingsAction A lambda function to dispatch [SettingsAction] events to the SettingsViewModel.
 * @param insets The [PaddingValues] for handling system window insets.
 * @param windowSizeClass The [WindowSizeClass] for adapting the layout to different screen sizes.
 * @param modifier The [Modifier] to be applied to the root container of the home screen.
 */
@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalSharedTransitionApi::class
)
@Composable
fun AppHomeScreen(
    homeScreenState: HomeScreenState,
    listState: LazyListState,
    preferencesState: PreferencesState,
    feedState: FeedState,
    recentLangs: List<String>,
    floatingToolbarScrollBehaviour: FloatingToolbarScrollBehavior?,
    feedListState: LazyListState,
    imageLoader: ImageLoader,
    languageSearchStr: String,
    languageSearchQuery: String,
    showLanguageSheet: Boolean,
    enableScrollButton: Boolean,
    deepLinkHandled: Boolean,
    onImageClick: () -> Unit,
    onGalleryImageClick: (String, String) -> Unit,
    setShowArticleLanguageSheet: (Boolean) -> Unit,
    onAction: (HomeAction) -> Unit,
    onSettingsAction: (SettingsAction) -> Unit,
    insets: PaddingValues,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    val photo = homeScreenState.photo
    val photoDesc = homeScreenState.photoDesc
    val fontSize = preferencesState.fontSize
    val fontFamily = remember(preferencesState.fontStyle) {
        if (preferencesState.fontStyle == "sans") FontFamily.SansSerif
        else FontFamily.Serif
    }

    val pullToRefreshState = rememberPullToRefreshState()

    var isRefreshing by remember { mutableStateOf(false) }

    val sendIntent: Intent = remember(homeScreenState.title, preferencesState.lang) {
        Intent()
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
    }
    val shareIntent = remember(homeScreenState.title, preferencesState.lang) {
        Intent.createChooser(sendIntent, null)
    }

    val context = LocalContext.current
    val systemBars = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()

    val lang = preferencesState.lang
    val pageId = homeScreenState.pageId

    if (showLanguageSheet && homeScreenState.status == WRStatus.SUCCESS)
        ArticleLanguageBottomSheet(
            langs = homeScreenState.langs ?: emptyList(),
            recentLangs = recentLangs,
            currentLang = WikiLang(preferencesState.lang, homeScreenState.title),
            searchStr = languageSearchStr,
            searchQuery = languageSearchQuery,
            setShowSheet = setShowArticleLanguageSheet,
            setLang = { onSettingsAction(SettingsAction.SaveLang(it)) },
            loadPage = { onAction(HomeAction.LoadPage(it)) },
            setSearchStr = { onAction(HomeAction.UpdateLanguageSearchStr(it)) }
        )
    else if (showLanguageSheet)
        LanguageBottomSheet(
            recentLangs = recentLangs,
            lang = preferencesState.lang,
            searchStr = languageSearchStr,
            searchQuery = languageSearchQuery,
            setShowSheet = setShowArticleLanguageSheet,
            setLang = {
                onSettingsAction(SettingsAction.SaveLang(it))
                if (homeScreenState.status in listOf(
                        WRStatus.FEED_LOADED,
                        WRStatus.FEED_NETWORK_ERROR
                    )
                )
                    onAction(HomeAction.LoadFeed())
                else
                    onAction(HomeAction.ReloadPage(true))
            },
            setSearchStr = { onAction(HomeAction.UpdateLanguageSearchStr(it)) }
        )

    if (homeScreenState.showRef) // Reference bottom sheet
        ModalBottomSheet(
            onDismissRequest = { onAction(HomeAction.HideRef) },
        ) {
            SelectionContainer {
                Column(Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.reference),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(
                            onClick = {
                                scope.launch {
                                    clipboard.setClipEntry(
                                        ClipEntry(
                                            ClipData.newPlainText(
                                                homeScreenState.ref,
                                                homeScreenState.ref
                                            )
                                        )
                                    )
                                }
                            },
                            shapes = IconButtonDefaults.shapes()
                        ) {
                            Icon(
                                painterResource(R.drawable.copy),
                                contentDescription = "Copy reference text"
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .clip(shapes.large)
                            .background(colorScheme.surface)
                            .fillMaxWidth()
                    ) {
                        Text(
                            homeScreenState.ref, Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }

    val pagerState = if (feedState.mostReadArticles != null)
        rememberPagerState { feedState.mostReadArticles.size / 5 }
    else null

    val newsCarouselState = if (feedState.news != null)
        rememberCarouselState(0) { feedState.news.size }
    else null

    val otdCarouselState = if (feedState.onThisDay != null)
        rememberCarouselState(0) { feedState.onThisDay.size }
    else null

    SharedTransitionLayout {
        val condition1 = homeScreenState.status != WRStatus.UNINITIALIZED &&
                homeScreenState.status != WRStatus.FEED_LOADED &&
                homeScreenState.status != WRStatus.FEED_NETWORK_ERROR
        val condition2 =
            (homeScreenState.status == WRStatus.UNINITIALIZED) && !preferencesState.dataSaver && preferencesState.feedEnabled
        val condition3 =
            homeScreenState.status == WRStatus.FEED_NETWORK_ERROR || homeScreenState.status == WRStatus.UNINITIALIZED

        Box(modifier = modifier) { // The container for all the composables in the home screen
            AnimatedVisibility(
                condition1,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LaunchedEffect(isRefreshing) {
                    delay(3000)
                    isRefreshing = false
                } // hide refresh indicator after a delay
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    state = pullToRefreshState,
                    onRefresh = {
                        if (homeScreenState.status == WRStatus.FEED_NETWORK_ERROR)
                            onAction(HomeAction.LoadFeed())
                        else
                            onAction(HomeAction.ReloadPage(true))
                        isRefreshing = true
                    },
                    indicator = {
                        LoadingIndicator(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = insets.calculateTopPadding()),
                            isRefreshing = isRefreshing,
                            state = pullToRefreshState
                        )
                    }
                ) {
                    LazyColumn( // The article
                        state = listState,
                        contentPadding = insets,
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        item { // Title + Image/description
                            SelectionContainer {
                                Column {
                                    Text(
                                        text = homeScreenState.title,
                                        style = MaterialTheme.typography.displaySmallEmphasized,
                                        fontFamily = FontFamily.Serif,
                                        modifier = Modifier
                                            .sharedBounds(
                                                sharedContentState = rememberSharedContentState(
                                                    homeScreenState.title
                                                ),
                                                animatedVisibilityScope = this@AnimatedVisibility,
                                                zIndexInOverlay = 1f
                                            )
                                            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                                            .animateContentSize(motionScheme.defaultSpatialSpec())
                                    )
                                    if (photoDesc != null) {
                                        Text(
                                            text = photoDesc,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = colorScheme.onSurfaceVariant,
                                            fontFamily = FontFamily.Serif,
                                            modifier = Modifier
                                                .sharedBounds(
                                                    sharedContentState = rememberSharedContentState(
                                                        photoDesc
                                                    ),
                                                    animatedVisibilityScope = this@AnimatedVisibility,
                                                    zIndexInOverlay = 1f
                                                )
                                                .padding(
                                                    start = 16.dp,
                                                    end = 16.dp,
                                                    top = 4.dp,
                                                    bottom = 16.dp
                                                )
                                                .fillMaxWidth()
                                        )
                                    }
                                    if (photoDesc != null) {
                                        ImageCard(
                                            photo = photo,
                                            title = homeScreenState.title,
                                            imageLoader = imageLoader,
                                            animatedVisibilityScope = this@AnimatedVisibility,
                                            showPhoto = !preferencesState.dataSaver,
                                            onClick = onImageClick,
                                            background = preferencesState.imageBackground,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                        item { // Main description
                            if (homeScreenState.extract.isNotEmpty())
                                SelectionContainer {
                                    ParsedBodyText(
                                        body = homeScreenState.extract[0],
                                        lang = homeScreenState.currentLang ?: "en",
                                        fontSize = fontSize,
                                        fontFamily = fontFamily,
                                        renderMath = preferencesState.renderMath,
                                        imageLoader = imageLoader,
                                        darkTheme = colorScheme.isDark(),
                                        dataSaver = preferencesState.dataSaver,
                                        background = preferencesState.imageBackground,
                                        checkFirstImage = true,
                                        onLinkClick = { onAction(HomeAction.LoadPage(it)) },
                                        onGalleryImageClick = onGalleryImageClick,
                                        showRef = { onAction(HomeAction.UpdateRef(it)) },
                                        pageImageUri = homeScreenState.photo?.source
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
                                        lang = homeScreenState.currentLang ?: "en",
                                        fontSize = fontSize,
                                        fontFamily = fontFamily,
                                        imageLoader = imageLoader,
                                        expanded = preferencesState.expandedSections,
                                        darkTheme = colorScheme.isDark(),
                                        dataSaver = preferencesState.dataSaver,
                                        renderMath = preferencesState.renderMath,
                                        imageBackground = preferencesState.imageBackground,
                                        onLinkClick = { onAction(HomeAction.LoadPage(it)) },
                                        onGalleryImageClick = onGalleryImageClick,
                                        showRef = { onAction(HomeAction.UpdateRef(it)) }
                                    )
                                }
                        }
                        item {
                            Spacer(Modifier.height(156.dp))
                        }
                    }
                }
            }

            AnimatedVisibility(
                !condition1 && condition2,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                AnimatedShimmer {
                    FeedLoader(brush = it, insets = insets)
                }
            }

            AnimatedVisibility(
                !condition1 && !condition2 && condition3,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    painterResource(R.drawable.ic_launcher_monochrome),
                    contentDescription = null,
                    modifier = Modifier
                        .size(400.dp)
                        .align(Alignment.Center)
                )
            }

            AnimatedVisibility(
                !condition1 && !condition2 && !condition3,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ArticleFeed(
                    feedState = feedState,
                    pagerState = pagerState,
                    newsCarouselState = newsCarouselState,
                    otdCarouselState = otdCarouselState,
                    imageLoader = imageLoader,
                    insets = insets,
                    loadPage = { onAction(HomeAction.LoadPage(it)) },
                    refreshFeed = { onAction(HomeAction.LoadFeed()) },
                    onImageClick = onImageClick,
                    listState = feedListState,
                    windowSizeClass = windowSizeClass,
                    animatedVisibilityScope = this@AnimatedVisibility,
                    imageBackground = preferencesState.imageBackground
                )
            }

            AnimatedVisibility( // The linear progress bar that shows up when the article is loading
                visible = homeScreenState.isLoading,
                enter = expandVertically(expandFrom = Alignment.Top),
                exit = shrinkVertically(shrinkTowards = Alignment.Top),
                modifier = Modifier.padding(top = (max(systemBars, insets.calculateTopPadding())))
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

            HorizontalFloatingToolbar(
                expanded = true,
                scrollBehavior = floatingToolbarScrollBehaviour,
                colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
                floatingActionButton = {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                        tooltip = { PlainTooltip { Text(stringResource(R.string.search)) } },
                        state = rememberTooltipState()
                    ) {
                        FloatingToolbarDefaults.VibrantFloatingActionButton(
                            onClick = { onAction(HomeAction.FocusSearchBar) }
                        ) {
                            Icon(Icons.Outlined.Search, stringResource(R.string.search))
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp)
                    .offset(y = -(insets.calculateBottomPadding()))
            ) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                    tooltip = { PlainTooltip { Text(stringResource(R.string.settingWikipediaLanguage)) } },
                    state = rememberTooltipState()
                ) {
                    IconButton(
                        onClick = { setShowArticleLanguageSheet(true) },
                        enabled = homeScreenState.status in listOf(
                            WRStatus.FEED_LOADED,
                            WRStatus.FEED_NETWORK_ERROR
                        ) || homeScreenState.langs?.isEmpty() == false
                    ) {
                        Icon(
                            painterResource(R.drawable.translate),
                            stringResource(R.string.settingWikipediaLanguage)
                        )
                    }
                }

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                    tooltip = { PlainTooltip { Text(stringResource(R.string.sharePage)) } },
                    state = rememberTooltipState()
                ) {
                    IconButton(
                        enabled = homeScreenState.status == WRStatus.SUCCESS,
                        onClick = remember(
                            homeScreenState.title,
                            preferencesState.lang
                        ) {
                            { context.startActivity(shareIntent) }
                        }
                    ) {
                        Icon(
                            painterResource(R.drawable.share),
                            contentDescription = stringResource(R.string.sharePage)
                        )
                    }
                }

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text(
                                when (homeScreenState.savedStatus) {
                                    SavedStatus.SAVED -> stringResource(R.string.deleteArticle)
                                    else -> stringResource(R.string.downloadArticle)
                                }
                            )
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    FilledTonalIconToggleButton(
                        checked = homeScreenState.savedStatus == SavedStatus.SAVED,
                        enabled = homeScreenState.status == WRStatus.SUCCESS,
                        colors = IconButtonDefaults.filledTonalIconToggleButtonColors(
                            containerColor = vibrantFloatingToolbarColors().toolbarContainerColor,
                            contentColor = vibrantFloatingToolbarColors().toolbarContentColor,
                            checkedContainerColor = colorScheme.surfaceContainer,
                            checkedContentColor = colorScheme.onSurface,
                            disabledContainerColor = vibrantFloatingToolbarColors().toolbarContainerColor,
                            disabledContentColor = colorScheme.onPrimaryContainer.copy(alpha = 0.38f)
                        ),
                        onCheckedChange = {
                            onAction(
                                HomeAction.SaveArticle(
                                    preferencesState.lang,
                                    context.getString(R.string.snackbarUnableToSave),
                                    context.getString(R.string.snackbarUnableToDelete)
                                )
                            )
                        }
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

                                SavedStatus.SAVING -> LoadingIndicator()

                                else ->
                                    Icon(
                                        painterResource(R.drawable.download),
                                        contentDescription = stringResource(R.string.downloadArticle)
                                    )
                            }
                        }
                    }
                }

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                    tooltip = { PlainTooltip { Text(stringResource(R.string.scroll_to_top)) } },
                    state = rememberTooltipState()
                ) {
                    IconButton(
                        onClick = { onAction(HomeAction.ScrollToTop) },
                        enabled = enableScrollButton
                    ) {
                        Icon(
                            painterResource(R.drawable.upward),
                            contentDescription = stringResource(R.string.scroll_to_top)
                        )
                    }
                }

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                    tooltip = { PlainTooltip { Text(stringResource(R.string.randomArticle)) } },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = { onAction(HomeAction.LoadRandom) }) {
                        Icon(
                            painterResource(R.drawable.shuffle),
                            contentDescription = stringResource(R.string.randomArticle)
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(homeScreenState.status) {
        if (homeScreenState.status == WRStatus.FEED_NETWORK_ERROR && !deepLinkHandled)
            onAction(
                HomeAction.ShowFeedErrorSnackBar(
                    context.getString(R.string.snackbarUnableToLoadFeed)
                )
            )
    }
}
