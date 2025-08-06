package org.nsh07.wikireader.ui.homeScreen

import android.content.ClipData
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.core.text.parseAsHtml
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import coil3.ImageLoader
import kotlinx.coroutines.launch
import org.nsh07.wikireader.R
import org.nsh07.wikireader.data.SavedStatus
import org.nsh07.wikireader.data.WikiPhoto
import org.nsh07.wikireader.ui.homeScreen.viewModel.HomeAction
import org.nsh07.wikireader.ui.homeScreen.viewModel.HomeScreenState
import org.nsh07.wikireader.ui.homeScreen.viewModel.HomeSubscreen
import org.nsh07.wikireader.ui.image.FullScreenArticleImage
import org.nsh07.wikireader.ui.image.FullScreenImage
import org.nsh07.wikireader.ui.settingsScreen.LanguageBottomSheet
import org.nsh07.wikireader.ui.settingsScreen.viewModel.PreferencesState
import org.nsh07.wikireader.ui.settingsScreen.viewModel.SettingsAction
import org.nsh07.wikireader.ui.shimmer.AnimatedShimmer
import org.nsh07.wikireader.ui.shimmer.FeedLoader

/**
 * The main composable function for the app's home screen.
 *
 * @param backStack The navigation back stack for the home screen, managing [HomeSubscreen]s.
 * @param homeScreenState The current state of the home screen, containing article data and UI status.
 * @param preferencesState The current state of user preferences.
 * @param recentLangs A list of recently used language codes.
 * @param floatingToolbarScrollBehaviour The [FloatingToolbarScrollBehavior] for the floating action toolbar.
 * @param imageLoader The app-wide [ImageLoader] used for loading images.
 * @param languageSearchStr The current search string for languages in the language bottom sheet.
 * @param languageSearchQuery The current search query for languages after debouncing.
 * @param showLanguageSheet A boolean indicating whether the language selection bottom sheet should be shown.
 * @param deepLinkHandled A boolean indicating if a deep link has been processed for the initial feed load.
 * @param setShowArticleLanguageSheet A lambda function to control the visibility of the article language bottom sheet.
 * @param onAction A lambda function to dispatch [HomeAction] events to the ViewModel.
 * @param onSettingsAction A lambda function to dispatch [SettingsAction] events to the SettingsViewModel.
 * @param insets The [PaddingValues] for handling system window insets.
 * This is used to adjust UI elements to avoid overlapping with system bars.
 * @param windowSizeClass The [WindowSizeClass] for adapting the layout to different screen sizes.
 * @param modifier The [Modifier] to be applied to the root container of the home screen.
 */
@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalSharedTransitionApi::class
)
@Composable
fun AppHomeScreen(
    backStack: SnapshotStateList<HomeSubscreen>,
    homeScreenState: HomeScreenState,
    preferencesState: PreferencesState,
    recentLangs: List<String>,
    floatingToolbarScrollBehaviour: FloatingToolbarScrollBehavior?,
    imageLoader: ImageLoader,
    languageSearchStr: String,
    languageSearchQuery: String,
    showLanguageSheet: Boolean,
    deepLinkHandled: Boolean,
    setShowArticleLanguageSheet: (Boolean) -> Unit,
    onAction: (HomeAction) -> Unit,
    onSettingsAction: (SettingsAction) -> Unit,
    insets: PaddingValues,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboard.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val motionScheme = motionScheme

    val sendIntent: Intent = remember(backStack.last(), preferencesState.lang) {
        Intent()
            .apply {
                val isArticle = backStack.last() is HomeSubscreen.Article
                action = Intent.ACTION_SEND
                putExtra(
                    Intent.EXTRA_TEXT,
                    if (isArticle) "https://${preferencesState.lang}.wikipedia.org/wiki/${
                        (backStack.last() as HomeSubscreen.Article).title.replace(' ', '_')
                    }" else "https://${preferencesState.lang}.wikipedia.org/wiki/Main_Page"
                )
                type = "text/plain"
            }
    }
    val shareIntent = remember(backStack.last(), preferencesState.lang) {
        Intent.createChooser(sendIntent, null)
    }

    val context = LocalContext.current
    val systemBars = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()

    if (homeScreenState.showRef && backStack.last() is HomeSubscreen.Article) // Reference bottom sheet
        ModalBottomSheet(
            onDismissRequest = { onAction(HomeAction.HideRef) },
        ) {
            val content = homeScreenState.ref
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
                                                content,
                                                content
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
                            content, Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }

    val pagerState =
        if (backStack[0] is HomeSubscreen.Feed && (backStack[0] as HomeSubscreen.Feed).mostReadArticles != null)
            rememberPagerState { (backStack[0] as HomeSubscreen.Feed).mostReadArticles!!.size / 5 }
        else null

    val newsCarouselState =
        if (backStack[0] is HomeSubscreen.Feed && (backStack[0] as HomeSubscreen.Feed).news != null)
            rememberCarouselState(0) { (backStack[0] as HomeSubscreen.Feed).news!!.size }
        else null

    val otdCarouselState =
        if (backStack[0] is HomeSubscreen.Feed && (backStack[0] as HomeSubscreen.Feed).onThisDay != null)
            rememberCarouselState(0) { (backStack[0] as HomeSubscreen.Feed).onThisDay!!.size }
        else null

    Box(modifier = modifier) { // The container for all the composables in the home screen
        SharedTransitionLayout {
            NavDisplay(
                backStack = backStack,
                onBack = {
                    onAction(HomeAction.StopAll)
                    repeat(it) { backStack.removeAt(backStack.lastIndex) }
                },
                transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                popTransitionSpec = { fadeIn().togetherWith(fadeOut()) },
                predictivePopTransitionSpec = {
                    if (backStack.size > 2 && backStack.last() !is HomeSubscreen.Image)
                        (slideInHorizontally(
                            initialOffsetX = { -it / 4 },
                            animationSpec = motionScheme.defaultSpatialSpec()
                        ) + fadeIn()).togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { it / 4 },
                                animationSpec = motionScheme.fastSpatialSpec()
                            ) + fadeOut()
                        )
                    else fadeIn().togetherWith(fadeOut())
                },
                entryProvider = entryProvider {
                    entry<HomeSubscreen.Logo> {
                        Logo()
                    }

                    entry<HomeSubscreen.FeedLoader> {
                        FeedShimmer(insets = insets)
                    }

                    entry<HomeSubscreen.Feed> { entry ->
                        if (showLanguageSheet)
                            LanguageBottomSheet(
                                recentLangs = recentLangs,
                                lang = preferencesState.lang,
                                searchStr = languageSearchStr,
                                searchQuery = languageSearchQuery,
                                setShowSheet = setShowArticleLanguageSheet,
                                setLang = {
                                    onSettingsAction(SettingsAction.SaveLang(it))
                                    onAction(HomeAction.LoadFeed())
                                },
                                setSearchStr = { onAction(HomeAction.UpdateLanguageSearchStr(it)) }
                            )
                        ArticleFeed(
                            feedContent = entry,
                            pagerState = pagerState,
                            newsCarouselState = newsCarouselState,
                            otdCarouselState = otdCarouselState,
                            imageLoader = imageLoader,
                            insets = insets,
                            loadPage = { onAction(HomeAction.LoadPage(it)) },
                            refreshFeed = { onAction(HomeAction.LoadFeed()) },
                            onImageClick = {
                                backStack.add(
                                    HomeSubscreen.Image.FullScreenImage(
                                        photo = WikiPhoto(
                                            source = entry.image?.thumbnail?.source ?: "",
                                            width = entry.image?.thumbnail?.width ?: 1,
                                            height = entry.image?.thumbnail?.height ?: 1
                                        ),
                                        photoDesc = entry.image?.description?.text?.parseAsHtml()
                                            .toString(),
                                        title = entry.image?.title ?: "",
                                        imageLoader = imageLoader,
                                        link = entry.image?.filePage,
                                        background = preferencesState.imageBackground,
                                        onBack = backStack::removeLastOrNull
                                    )
                                )
                            },
                            windowSizeClass = windowSizeClass,
                            sharedScope = this@SharedTransitionLayout,
                            imageBackground = preferencesState.imageBackground
                        )
                    }

                    entry<HomeSubscreen.Article> { entry ->
                        PageContent(
                            content = entry,
                            sharedScope = this@SharedTransitionLayout,
                            preferencesState = preferencesState,
                            insets = insets,
                            imageLoader = imageLoader,
                            showLanguageSheet = showLanguageSheet,
                            recentLangs = recentLangs,
                            languageSearchStr = languageSearchStr,
                            languageSearchQuery = languageSearchQuery,
                            setShowArticleLanguageSheet = setShowArticleLanguageSheet,
                            setLang = { onSettingsAction(SettingsAction.SaveLang(it)) },
                            loadPage = { onAction(HomeAction.LoadPage(it)) },
                            onImageClick = {
                                backStack.add(
                                    HomeSubscreen.Image.FullScreenImage(
                                        photo = entry.photo,
                                        photoDesc = entry.photoDesc,
                                        title = entry.title,
                                        imageLoader = imageLoader,
                                        background = preferencesState.imageBackground,
                                        link = entry.photo?.source,
                                        onBack = backStack::removeLastOrNull
                                    )
                                )
                            },
                            onGalleryImageClick = { uri, description ->
                                backStack.add(
                                    HomeSubscreen.Image.FullScreenArticleImage(
                                        uri = uri,
                                        description = description,
                                        imageLoader = imageLoader,
                                        link = uri,
                                        background = preferencesState.imageBackground,
                                        onBack = backStack::removeLastOrNull
                                    )
                                )
                            },
                            setSearchStr = { onAction(HomeAction.UpdateLanguageSearchStr(it)) },
                            onAction = onAction
                        )
                    }

                    entry<HomeSubscreen.Image.FullScreenImage> {
                        FullScreenImage(
                            photo = it.photo,
                            photoDesc = it.photoDesc,
                            sharedScope = this@SharedTransitionLayout,
                            title = it.title,
                            background = it.background,
                            imageLoader = it.imageLoader,
                            link = it.link,
                            onBack = it.onBack
                        )
                    }

                    entry<HomeSubscreen.Image.FullScreenArticleImage> {
                        FullScreenArticleImage(
                            uri = it.uri,
                            description = it.description,
                            sharedScope = this@SharedTransitionLayout,
                            imageLoader = it.imageLoader,
                            background = it.background,
                            link = it.link,
                            onBack = it.onBack
                        )
                    }
                }
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

        AnimatedVisibility(
            backStack.last() !is HomeSubscreen.Image,
            enter = slideInVertically(
                motionScheme.defaultSpatialSpec(),
                initialOffsetY = {
                    with(density) {
                        it + insets.calculateBottomPadding().roundToPx() + 1
                    }
                }
            ),
            exit = slideOutVertically(
                motionScheme.defaultSpatialSpec(),
                targetOffsetY = {
                    with(density) {
                        it + insets.calculateBottomPadding().roundToPx() + 1
                    }
                }
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            HorizontalFloatingToolbar(
                expanded = true,
                scrollBehavior = floatingToolbarScrollBehaviour,
                colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
                floatingActionButton = {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                            TooltipAnchorPosition.Above
                        ),
                        tooltip = { PlainTooltip { Text(stringResource(R.string.search)) } },
                        state = rememberTooltipState()
                    ) {
                        FloatingToolbarDefaults.VibrantFloatingActionButton(
                            onClick = { onAction(HomeAction.FocusSearchBar) }
                        ) {
                            Icon(
                                painterResource(R.drawable.search),
                                stringResource(R.string.search)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = -(insets.calculateBottomPadding()))
            ) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        TooltipAnchorPosition.Above
                    ),
                    tooltip = { PlainTooltip { Text(stringResource(R.string.settingWikipediaLanguage)) } },
                    state = rememberTooltipState()
                ) {
                    IconButton(
                        onClick = { setShowArticleLanguageSheet(true) },
                        enabled = backStack.last() is HomeSubscreen.Feed ||
                                (backStack.last() is HomeSubscreen.Article && (backStack.last() as HomeSubscreen.Article).langs?.isEmpty() == false)
                    ) {
                        Icon(
                            painterResource(R.drawable.translate),
                            stringResource(R.string.settingWikipediaLanguage)
                        )
                    }
                }

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        TooltipAnchorPosition.Above
                    ),
                    tooltip = { PlainTooltip { Text(stringResource(R.string.sharePage)) } },
                    state = rememberTooltipState()
                ) {
                    IconButton(
                        enabled = backStack.last() is HomeSubscreen.Article,
                        onClick = remember(
                            backStack.last(),
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
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        TooltipAnchorPosition.Above
                    ),
                    tooltip = {
                        PlainTooltip {
                            Text(
                                when ((backStack.last() as HomeSubscreen.Article).savedStatus) {
                                    SavedStatus.SAVED -> stringResource(R.string.deleteArticle)
                                    else -> stringResource(R.string.downloadArticle)
                                }
                            )
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    FilledTonalIconToggleButton(
                        checked = backStack.last() is HomeSubscreen.Article && (backStack.last() as HomeSubscreen.Article).savedStatus == SavedStatus.SAVED,
                        enabled = backStack.last() is HomeSubscreen.Article,
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
                            if (backStack.last() is HomeSubscreen.Article)
                                (backStack.last() as HomeSubscreen.Article).savedStatus
                            else SavedStatus.NOT_SAVED,
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
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        TooltipAnchorPosition.Above
                    ),
                    tooltip = { PlainTooltip { Text(stringResource(R.string.scroll_to_top)) } },
                    state = rememberTooltipState()
                ) {
                    IconButton(
                        onClick = { onAction(HomeAction.ScrollToTop) },
                        enabled = true
                    ) {
                        Icon(
                            painterResource(R.drawable.upward),
                            contentDescription = stringResource(R.string.scroll_to_top)
                        )
                    }
                }

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        TooltipAnchorPosition.Above
                    ),
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

    LaunchedEffect(backStack.last()) {
        if (backStack.last() is HomeSubscreen.Logo && !deepLinkHandled && preferencesState.feedEnabled)
            onAction(
                HomeAction.ShowFeedErrorSnackBar(
                    context.getString(R.string.snackbarUnableToLoadFeed)
                )
            )
    }
}

@Composable
fun FeedShimmer(
    insets: PaddingValues,
    modifier: Modifier = Modifier
) {
    AnimatedShimmer {
        FeedLoader(brush = it, insets = insets, modifier = modifier)
    }
}

@Composable
fun BoxScope.Logo(modifier: Modifier = Modifier) {
    Icon(
        painterResource(R.drawable.ic_launcher_monochrome),
        contentDescription = null,
        modifier = modifier
            .size(400.dp)
            .align(Alignment.Center)
    )
}
