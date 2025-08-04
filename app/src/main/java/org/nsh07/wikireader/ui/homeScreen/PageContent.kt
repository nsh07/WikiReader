package org.nsh07.wikireader.ui.homeScreen

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import coil3.ImageLoader
import kotlinx.coroutines.delay
import org.nsh07.wikireader.data.WikiLang
import org.nsh07.wikireader.ui.homeScreen.viewModel.HomeAction
import org.nsh07.wikireader.ui.homeScreen.viewModel.HomeSubscreen
import org.nsh07.wikireader.ui.image.ImageCard
import org.nsh07.wikireader.ui.settingsScreen.viewModel.PreferencesState
import org.nsh07.wikireader.ui.theme.isDark

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun PageContent(
    content: HomeSubscreen.Article,
    sharedScope: SharedTransitionScope,
    preferencesState: PreferencesState,
    insets: PaddingValues,
    imageLoader: ImageLoader,
    showLanguageSheet: Boolean,
    recentLangs: List<String>,
    languageSearchStr: String,
    languageSearchQuery: String,
    setShowArticleLanguageSheet: (Boolean) -> Unit,
    setLang: (String) -> Unit,
    loadPage: (String) -> Unit,
    onImageClick: () -> Unit,
    onGalleryImageClick: (String, String) -> Unit,
    setSearchStr: (String) -> Unit,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val photo = content.photo
    val photoDesc = content.photoDesc
    val fontSize = preferencesState.fontSize
    val fontFamily = remember(preferencesState.fontStyle) {
        if (preferencesState.fontStyle == "sans") FontFamily.SansSerif
        else FontFamily.Serif
    }
    val lang = preferencesState.lang
    val pageId = content.pageId

    val pullToRefreshState = rememberPullToRefreshState()

    var isRefreshing by remember { mutableStateOf(false) }

    if (showLanguageSheet)
        ArticleLanguageBottomSheet(
            langs = content.langs ?: emptyList(),
            recentLangs = recentLangs,
            currentLang = WikiLang(preferencesState.lang, content.title),
            searchStr = languageSearchStr,
            searchQuery = languageSearchQuery,
            setShowSheet = setShowArticleLanguageSheet,
            setLang = setLang,
            loadPage = loadPage,
            setSearchStr = setSearchStr
        )

    LaunchedEffect(isRefreshing) {
        delay(3000)
        isRefreshing = false
    } // hide refresh indicator after a delay

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        state = pullToRefreshState,
        onRefresh = {
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
            state = content.listState,
            contentPadding = insets,
            modifier = modifier
                .fillMaxSize()
        ) {
            item { // Title + Image/description
                with(sharedScope) {
                    SelectionContainer {
                        Column {
                            Text(
                                text = content.title,
                                style = MaterialTheme.typography.displaySmallEmphasized,
                                fontFamily = FontFamily.Serif,
                                modifier = Modifier
                                    .sharedBounds(
                                        sharedContentState = rememberSharedContentState(
                                            content.title
                                        ),
                                        animatedVisibilityScope = LocalNavAnimatedContentScope.current,
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
                                            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
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
                                    title = content.title,
                                    imageLoader = imageLoader,
                                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                    showPhoto = !preferencesState.dataSaver,
                                    onClick = onImageClick,
                                    background = preferencesState.imageBackground,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
            item { // Main description
                if (content.extract.isNotEmpty())
                    SelectionContainer {
                        ParsedBodyText(
                            body = content.extract[0],
                            lang = content.currentLang ?: "en",
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
                            pageImageUri = content.photo?.source
                        )
                    }
            }
            itemsIndexed(
                content.extract,
                key = { i, it -> "$pageId.$lang#$i" }
            ) { i: Int, it: List<AnnotatedString> ->// Expandable sections logic
                if (i % 2 == 1)
                    SelectionContainer {
                        ExpandableSection(
                            title = content.extract[i],
                            body = content.extract.getOrElse(i + 1) { emptyList() },
                            lang = content.currentLang ?: "en",
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