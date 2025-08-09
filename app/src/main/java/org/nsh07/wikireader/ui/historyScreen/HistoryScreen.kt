package org.nsh07.wikireader.ui.historyScreen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.ImageLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.nsh07.wikireader.R
import org.nsh07.wikireader.data.ViewHistoryItem
import org.nsh07.wikireader.ui.historyScreen.viewModel.HistoryAction
import org.nsh07.wikireader.ui.historyScreen.viewModel.HistoryViewModel
import org.nsh07.wikireader.ui.image.FeedImage
import org.nsh07.wikireader.ui.theme.CustomTopBarColors.topBarColors
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.bottomListItemShape
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.middleListItemShape
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.topListItemShape
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun HistoryScreenRoot(
    imageLoader: ImageLoader,
    imageBackground: Boolean,
    openArticle: (String, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
) {
    val viewHistory by viewModel.viewHistoryFlow.collectAsStateWithLifecycle(emptyList())

    HistoryScreen(
        viewHistory = viewHistory,
        imageLoader = imageLoader,
        imageBackground = imageBackground,
        openArticle = openArticle,
        onAction = viewModel::onAction,
        onBack = onBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HistoryScreen(
    viewHistory: List<ViewHistoryItem>,
    imageLoader: ImageLoader,
    imageBackground: Boolean,
    openArticle: (String, String) -> Unit,
    onAction: (HistoryAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = LocalConfiguration.current.locales.get(0)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val zone = ZoneId.systemDefault()
    val snackbarHostState = remember { SnackbarHostState() }

    var lastDeleted: ViewHistoryItem?
    var deletedItems: List<ViewHistoryItem>?

    val dtf = remember {
        DateTimeFormatter
            .ofLocalizedDate(FormatStyle.LONG)
            .withLocale(locale)
    }
    val tf = remember {
        DateTimeFormatter
            .ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(locale)
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val groupedHistory =
        remember(viewHistory) {
            viewHistory.groupBy {
                Instant.ofEpochMilli(it.time).atZone(zone).format(dtf)
            }
        }

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.history)) },
                subtitle = { Text(stringResource(R.string.items, viewHistory.size)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Icon(
                            painterResource(R.drawable.arrow_back),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    FilledTonalIconButton(
                        onClick = {
                            deletedItems = viewHistory
                            onAction(HistoryAction.RemoveAll)
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.allHistoryDeleted),
                                    actionLabel = context.getString(R.string.undo)
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    deletedItems.fastForEach {
                                        onAction(HistoryAction.InsertItem(it))
                                        delay(10)
                                    }
                                }
                            }
                        },
                        enabled = viewHistory.isNotEmpty(),
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Icon(painterResource(R.drawable.delete), stringResource(R.string.deleteAll))
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = topBarColors
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { insets ->
        if (viewHistory.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = insets,
                modifier = Modifier
                    .fillMaxSize()
                    .background(topBarColors.containerColor)
            ) {
                groupedHistory.forEach { item ->
                    item {
                        Text(
                            item.key,
                            style = typography.titleSmall,
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 14.dp)
                        )
                    }
                    itemsIndexed(
                        item.value,
                        key = { index: Int, it: ViewHistoryItem -> it.time }
                    ) { index, it ->
                        ListItem(
                            leadingContent = if (it.description != null) {
                                {
                                    FeedImage(
                                        source = it.thumbnail,
                                        description = it.description,
                                        imageLoader = imageLoader,
                                        loadingIndicator = true,
                                        background = imageBackground,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(shapes.large)
                                    )
                                }
                            } else {
                                {
                                    Spacer(Modifier.width(56.dp))
                                }
                            },
                            headlineContent = {
                                Text(
                                    it.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            supportingContent = if (it.description != null) {
                                {
                                    Text(
                                        it.description,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            } else null,
                            trailingContent = {
                                Text(
                                    remember {
                                        Instant.ofEpochMilli(it.time).atZone(zone).format(tf)
                                    }
                                )
                            },
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .clip(
                                    if (item.value.size == 1) shapes.large
                                    else if (index == 0) topListItemShape
                                    else if (index == item.value.lastIndex) bottomListItemShape
                                    else middleListItemShape
                                )
                                .combinedClickable(
                                    onClick = { openArticle(it.title, it.lang) },
                                    onLongClick = {
                                        lastDeleted = it
                                        onAction(HistoryAction.RemoveItem(it))
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = context.getString(
                                                    R.string.deletedFromHistory,
                                                    it.title
                                                ),
                                                actionLabel = context.getString(R.string.undo),
                                                duration = SnackbarDuration.Long
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                onAction(HistoryAction.InsertItem(lastDeleted))
                                            }
                                        }
                                    }
                                )
                                .animateItem()
                        )
                    }
                }
            }
        } else {
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
                            painterResource(R.drawable.history_huge),
                            contentDescription = null,
                            tint = colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .padding(32.dp)
                                .size(100.dp)
                        )
                    }
                    Text(
                        stringResource(R.string.noHistoryItems),
                        textAlign = TextAlign.Center,
                        style = typography.titleLarge,
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(
                        stringResource(R.string.noHistoryItemsDesc),
                        textAlign = TextAlign.Center,
                        style = typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 48.dp)
                    )
                }
            }
        }
    }
}