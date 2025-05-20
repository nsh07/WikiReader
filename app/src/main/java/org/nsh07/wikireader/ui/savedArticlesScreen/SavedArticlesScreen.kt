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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import kotlinx.coroutines.launch
import org.nsh07.wikireader.R
import org.nsh07.wikireader.data.WRStatus
import org.nsh07.wikireader.data.bytesToHumanReadableSize
import org.nsh07.wikireader.data.langCodeToWikiName
import org.nsh07.wikireader.ui.theme.ExpressiveListItemShapes.bottomListItemShape
import org.nsh07.wikireader.ui.theme.ExpressiveListItemShapes.middleListItemShape
import org.nsh07.wikireader.ui.theme.ExpressiveListItemShapes.topListItemShape
import org.nsh07.wikireader.ui.viewModel.SavedArticlesState

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun SavedArticlesScreen(
    savedArticlesState: SavedArticlesState,
    modifier: Modifier = Modifier,
    deleteAll: () -> WRStatus,
    onBack: () -> Unit,
    openSavedArticle: (String) -> Unit,
    deleteArticle: (String) -> WRStatus
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackBarHostState = remember { SnackbarHostState() }
    var toDelete: String? by remember { mutableStateOf("") }
    var showArticleDeleteDialog by remember { mutableStateOf(false) }

    var selectedLangs =
        savedArticlesState.languageFilters.filter { it.selected }.map { it.langCode }
    if (selectedLangs.isEmpty()) selectedLangs =
        savedArticlesState.languageFilters.map { it.langCode }

    val listColors = ListItemDefaults.colors(containerColor = colorScheme.surfaceContainer)

    if (showArticleDeleteDialog)
        DeleteArticleDialog(
            articleFileName = toDelete,
            setShowDeleteDialog = { showArticleDeleteDialog = it },
            deleteArticle = deleteArticle,
            deleteAll = deleteAll,
            showSnackbar = { coroutineScope.launch { snackBarHostState.showSnackbar(it) } }
        )

    Scaffold(
        topBar = {
            SavedArticlesTopBar(
                articlesInfo = stringResource(
                    R.string.articlesSize,
                    savedArticlesState.savedArticles.size,
                    bytesToHumanReadableSize(
                        savedArticlesState.articlesSize.toDouble()
                    )
                ),
                scrollBehavior = scrollBehavior,
                deleteEnabled = savedArticlesState.savedArticles.isNotEmpty(),
                onBack = onBack,
                onDeleteAll = {
                    toDelete = null
                    showArticleDeleteDialog = true
                }
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { insets ->
        if (savedArticlesState.savedArticles.isNotEmpty())
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = insets,
                modifier = Modifier
                    .fillMaxHeight()
            ) {
                item {
                    if (savedArticlesState.languageFilters.size > 1)
                        FlowRow(
                            Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            savedArticlesState.languageFilters.forEach { filterOption ->
                                FilterChip(
                                    selected = filterOption.selected,
                                    onClick = {
                                        filterOption.selected = !filterOption.selected
                                    },
                                    label = { Text(filterOption.option) },
                                    leadingIcon = if (filterOption.selected) {
                                        {
                                            Icon(
                                                Icons.Outlined.Check,
                                                contentDescription = null
                                            )
                                        }
                                    } else null
                                )
                            }
                        }
                }
                itemsIndexed(
                    savedArticlesState.savedArticles.filter {
                        selectedLangs.contains(
                            it.substringAfterLast(
                                '.'
                            )
                        )
                    },
                    key = { index: Int, it: String -> it }
                ) { index: Int, it: String ->
                    ListItem(
                        headlineContent = {
                            Text(
                                remember {
                                    it.substringBeforeLast('.').substringBeforeLast('.')
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        supportingContent = {
                            Text(remember {
                                langCodeToWikiName(
                                    it.substringAfterLast(
                                        '.'
                                    )
                                )
                            })
                        },
                        colors = listColors,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clip(
                                if (savedArticlesState.savedArticles.size == 1) shapes.large
                                else if (index == 0) topListItemShape
                                else if (index == savedArticlesState.savedArticles.lastIndex) bottomListItemShape
                                else middleListItemShape
                            )
                            .combinedClickable(
                                onClick = { openSavedArticle(it) },
                                onLongClick = {
                                    toDelete = it
                                    showArticleDeleteDialog = true
                                }
                            )
                            .animateItem()
                    )
                }
            }
        else
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(
                        stringResource(R.string.noSavedArticlesDesc),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 48.dp)
                    )
                }
            }
    }
}