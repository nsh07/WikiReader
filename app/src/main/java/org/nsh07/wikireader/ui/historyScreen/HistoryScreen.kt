package org.nsh07.wikireader.ui.historyScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import org.nsh07.wikireader.R
import org.nsh07.wikireader.data.ViewHistoryItem
import org.nsh07.wikireader.ui.image.FeedImage
import org.nsh07.wikireader.ui.theme.CustomTopBarColors.topBarColors
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.bottomListItemShape
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.middleListItemShape
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.topListItemShape
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HistoryScreen(
    viewHistory: List<ViewHistoryItem>,
    imageLoader: ImageLoader,
    imageBackground: Boolean,
    openArticle: (String, String) -> Unit,
    deleteHistoryItem: (ViewHistoryItem?) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = LocalConfiguration.current.locales.get(0)
    val zone = ZoneId.systemDefault()

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
                title = { Text("History") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = topBarColors
            )
        },
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { insets ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = insets,
            modifier = Modifier
                .fillMaxHeight()
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
                                        .size(56.dp)
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
                                remember { Instant.ofEpochMilli(it.time).atZone(zone).format(tf) }
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
//                                    toDelete = Pair(it.pageId, it.lang)
//                                    toDeleteTitle = it.title
//                                    showArticleDeleteDialog = true
                                }
                            )
                            .animateItem()
                    )
                }
            }
        }
    }
}