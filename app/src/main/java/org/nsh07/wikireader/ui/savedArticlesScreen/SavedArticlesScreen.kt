package org.nsh07.wikireader.ui.savedArticlesScreen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.nsh07.wikireader.R
import org.nsh07.wikireader.data.ArticleInfo
import org.nsh07.wikireader.data.LanguageInfo
import org.nsh07.wikireader.data.WRStatus
import org.nsh07.wikireader.ui.image.FeedImage
import org.nsh07.wikireader.ui.savedArticlesScreen.viewModel.SavedArticlesAction
import org.nsh07.wikireader.ui.savedArticlesScreen.viewModel.SavedArticlesViewModel
import org.nsh07.wikireader.ui.theme.CustomTopBarColors.topBarColors
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.bottomListItemShape
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.middleListItemShape
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.topListItemShape

@Composable
fun SavedArticlesScreenRoot(
    imageBackground: Boolean,
    openSavedArticle: (Int, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SavedArticlesViewModel = viewModel(factory = SavedArticlesViewModel.Factory)
) {
    val savedArticles by viewModel.savedArticlesFlow.collectAsState(emptyList())
    val savedArticleLangs by viewModel.savedArticleLangsFlow.collectAsState(emptyList())

    SavedArticlesScreen(
        savedArticles = savedArticles,
        savedArticleLangs = savedArticleLangs,
        imageBackground = imageBackground,
        openSavedArticle = openSavedArticle,
        onAction = viewModel::onAction,
        onBack = onBack,
        modifier = modifier
    )
}

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun SavedArticlesScreen(
    savedArticles: List<ArticleInfo>,
    savedArticleLangs: List<LanguageInfo>,
    imageBackground: Boolean,
    openSavedArticle: (Int, String) -> Unit,
    onAction: (SavedArticlesAction) -> WRStatus,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackBarHostState = remember { SnackbarHostState() }
    var toDelete: Pair<Int, String>? by remember { mutableStateOf(null) }
    var toDeleteTitle: String? by remember { mutableStateOf(null) }
    var showArticleDeleteDialog by remember { mutableStateOf(false) }
    var ct by remember { mutableIntStateOf(0) }

    val languageFilters = remember(savedArticleLangs) {
        savedArticleLangs.map { LanguageFilterOption(it.langName, it.lang) }
    }

    val selectedLanguages = remember(ct, languageFilters) {
        languageFilters.fastFilter { it.selected }.map { it.option }
    }

    val groupedArticles = remember(savedArticles, selectedLanguages) {
        savedArticles.groupBy { it.langName }
            .filterKeys { selectedLanguages.isEmpty() || it in selectedLanguages }
    }

    if (showArticleDeleteDialog)
        DeleteArticleDialog(
            pageId = toDelete?.first,
            lang = toDelete?.second,
            title = toDeleteTitle,
            setShowDeleteDialog = { showArticleDeleteDialog = it },
            deleteArticle = { pageId, lang ->
                onAction(SavedArticlesAction.Delete(pageId, lang))
            },
            deleteAll = { onAction(SavedArticlesAction.DeleteAll) },
            showSnackbar = { coroutineScope.launch { snackBarHostState.showSnackbar(it) } }
        )

    Scaffold(
        topBar = {
            SavedArticlesTopBar(
                articlesInfo = stringResource(
                    R.string.articlesSize,
                    savedArticles.size,
                    remember(groupedArticles) { groupedArticles.size }
                ),
                scrollBehavior = scrollBehavior,
                deleteEnabled = savedArticles.isNotEmpty(),
                onBack = onBack,
                onDeleteAll = {
                    toDelete = null
                    toDeleteTitle = null
                    showArticleDeleteDialog = true
                }
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { insets ->
        if (savedArticles.isNotEmpty())
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = insets,
                modifier = Modifier
                    .fillMaxHeight()
                    .background(topBarColors.containerColor)
            ) {
                item {
                    if (savedArticleLangs.size > 1)
                        FlowRow(
                            Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            languageFilters.fastForEach { filterOption ->
                                FilterChip(
                                    selected = filterOption.selected,
                                    onClick = {
                                        filterOption.selected = !filterOption.selected
                                        ct++
                                    },
                                    label = { Text(filterOption.option) },
                                    leadingIcon =
                                        if (filterOption.selected) {
                                            {
                                                Icon(
                                                    painterResource(R.drawable.check),
                                                    contentDescription = null
                                                )
                                            }
                                        } else null
                                )
                            }
                        }
                }
                groupedArticles.forEach { item ->
                    if (groupedArticles.size > 1) item(key = item.key + "-lang") {
                        Text(
                            item.key,
                            style = typography.titleSmall,
                            modifier = Modifier
                                .padding(horizontal = 32.dp, vertical = 14.dp)
                                .animateItem()
                        )
                    }
                    itemsIndexed(
                        item.value,
                        key = { index: Int, it: ArticleInfo -> it.pageId.toString() + it.lang }
                    ) { index: Int, it: ArticleInfo ->
                        ListItem(
                            leadingContent = if (it.thumbnail != null) {
                                {
                                    FeedImage(
                                        source = it.thumbnail,
                                        description = it.description,
                                        loadingIndicator = true,
                                        background = imageBackground,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(shapes.large)
                                    )
                                }
                            } else {
                                {
                                    Spacer(Modifier.width(64.dp))
                                }
                            },
                            headlineContent = {
                                Text(
                                    it.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            supportingContent =
                                if (it.description != null) {
                                    {
                                        Text(
                                            it.description,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                } else null,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .clip(
                                    if (item.value.size == 1) shapes.large
                                    else if (index == 0) topListItemShape
                                    else if (index == item.value.lastIndex) bottomListItemShape
                                    else middleListItemShape
                                )
                                .combinedClickable(
                                    onClick = { openSavedArticle(it.pageId, it.lang) },
                                    onLongClick = {
                                        toDelete = Pair(it.pageId, it.lang)
                                        toDeleteTitle = it.title
                                        showArticleDeleteDialog = true
                                    }
                                )
                                .animateItem()
                        )
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        else
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(topBarColors.containerColor),
                contentAlignment = Alignment.Center
            ) {
                val transition = rememberInfiniteTransition(
                    label = "Cookie rotate"
                )
                val angle by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 10000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "Cookie animation"
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center) {
                        Spacer(
                            Modifier
                                .graphicsLayer {
                                    rotationZ = angle
                                }
                                .clip(MaterialShapes.Cookie12Sided.toShape())
                                .background(colorScheme.primaryContainer)
                                .padding(32.dp)
                                .size(100.dp)
                        )
                        Icon(
                            painterResource(R.drawable.save),
                            contentDescription = null,
                            tint = colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .padding(32.dp)
                                .size(100.dp)
                        )
                    }
                    Text(
                        stringResource(R.string.noSavedArticles),
                        textAlign = TextAlign.Center,
                        style = typography.titleLarge,
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(
                        stringResource(R.string.noSavedArticlesDesc),
                        textAlign = TextAlign.Center,
                        style = typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 48.dp)
                    )
                }
            }
    }
}