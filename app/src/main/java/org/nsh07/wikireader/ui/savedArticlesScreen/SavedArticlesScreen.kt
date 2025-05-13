package org.nsh07.wikireader.ui.savedArticlesScreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import org.nsh07.wikireader.ui.viewModel.SavedArticlesState

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class
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

    if (showArticleDeleteDialog)
        DeleteArticleDialog(
            articleFileName = toDelete,
            setShowDeleteDialog = { showArticleDeleteDialog = it },
            deleteArticle = deleteArticle,
            deleteAll = deleteAll,
            showSnackbar = { coroutineScope.launch { snackBarHostState.showSnackbar(it) } }
        )

    Scaffold(
        topBar = { SavedArticlesTopBar(scrollBehavior = scrollBehavior, onBack = onBack) },
        snackbarHost = { SnackbarHost(snackBarHostState) },
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { insets ->
        if (savedArticlesState.savedArticles.isNotEmpty())
            LazyColumn(
                contentPadding = insets,
                modifier = Modifier
                    .fillMaxHeight()
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(
                                R.string.articlesSize,
                                savedArticlesState.savedArticles.size,
                                bytesToHumanReadableSize(
                                    savedArticlesState.articlesSize.toDouble()
                                )
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                        Spacer(Modifier.weight(1f))
                        TextButton(
                            onClick = {
                                toDelete = null
                                showArticleDeleteDialog = true
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) { Text(stringResource(R.string.deleteAll)) }
                    }
                }
                if (savedArticlesState.languageFilters.size > 1)
                    item {
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
                items(savedArticlesState.savedArticles.filter {
                    selectedLangs.contains(
                        it.substringAfterLast(
                            '.'
                        )
                    )
                }, key = { it }) {
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
                        modifier = Modifier
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painterResource(R.drawable.save),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(100.dp)
                    )
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