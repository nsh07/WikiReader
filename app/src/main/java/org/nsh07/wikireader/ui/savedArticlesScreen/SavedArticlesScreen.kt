package org.nsh07.wikireader.ui.savedArticlesScreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.nsh07.wikireader.R
import org.nsh07.wikireader.data.WRStatus
import org.nsh07.wikireader.data.bytesToHumanReadableSize
import org.nsh07.wikireader.data.langCodeToWikiName

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SavedArticlesScreen(
    modifier: Modifier = Modifier,
    loadArticles: () -> List<String>,
    articlesSize: () -> Long,
    openSavedArticle: (String) -> Unit,
    deleteArticle: (String) -> WRStatus,
    deleteAll: () -> WRStatus,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackBarHostState = remember { SnackbarHostState() }
    var savedArticles by remember { mutableStateOf(loadArticles()) }
    var savedArticlesSize by remember { mutableLongStateOf(articlesSize()) }
    var toDelete: String? by remember { mutableStateOf("") }
    var showArticleDeleteDialog by remember { mutableStateOf(false) }

    if (showArticleDeleteDialog)
        DeleteArticleDialog(
            articleFileName = toDelete,
            setShowDeleteDialog = { showArticleDeleteDialog = it },
            deleteArticle = {
                val status = deleteArticle(it)
                if (status == WRStatus.SUCCESS) {
                    savedArticles -= it
                    savedArticlesSize = articlesSize()
                }
                status
            },
            deleteAll = {
                val status = deleteAll()
                if (status == WRStatus.SUCCESS) {
                    savedArticles = emptyList()
                    savedArticlesSize = articlesSize()
                }
                status
            },
            showSnackbar = { coroutineScope.launch { snackBarHostState.showSnackbar(it) } }
        )

    Scaffold(
        topBar = { SavedArticlesTopBar(scrollBehavior = scrollBehavior, onBack = onBack) },
        snackbarHost = { SnackbarHost(snackBarHostState) },
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { insets ->
        if (savedArticles.isNotEmpty())
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = insets.calculateTopPadding())
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${savedArticles.size} articles, ${
                                bytesToHumanReadableSize(
                                    savedArticlesSize.toDouble()
                                )
                            } total",
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
                        ) { Text("Delete all") }
                    }
                    HorizontalDivider()
                }
                items(savedArticles, key = { it }) {
                    ListItem(
                        headlineContent = {
                            Text(
                                remember { it.substringBeforeLast(".").substringBeforeLast('.') },
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
                item {
                    Spacer(modifier.height(insets.calculateBottomPadding()))
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
                        "No saved articles",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(
                        "Click on the download button at the top of an article to get started",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 48.dp)
                    )
                }
            }
    }
}