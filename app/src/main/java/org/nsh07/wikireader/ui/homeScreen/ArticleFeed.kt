package org.nsh07.wikireader.ui.homeScreen

import android.icu.text.CompactDecimalFormat
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.LoadingIndicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.text.parseAsHtml
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import coil3.ImageLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.nsh07.wikireader.R
import org.nsh07.wikireader.ui.image.FeedImage
import org.nsh07.wikireader.ui.viewModel.FeedState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.absoluteValue
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun ArticleFeed(
    feedState: FeedState,
    imageLoader: ImageLoader,
    insets: PaddingValues,
    listState: LazyListState,
    windowSizeClass: WindowSizeClass,
    loadPage: (String) -> Unit,
    refreshFeed: () -> Unit,
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()
    val wide = remember { windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED }
    val df = remember {
        CompactDecimalFormat.getInstance(
            context.resources.configuration.getLocales().get(0),
            CompactDecimalFormat.CompactStyle.SHORT
        )
    }
    val dtf = remember {
        DateTimeFormatter
            .ofLocalizedDate(FormatStyle.LONG)
            .withLocale(context.resources.configuration.getLocales().get(0))
    }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isRefreshing) {
        delay(3000)
        isRefreshing = false
    } // hide refresh indicator after a while

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        state = pullToRefreshState,
        onRefresh = {
            isRefreshing = true
            refreshFeed()
        },
        indicator = {
            LoadingIndicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = isRefreshing,
                state = pullToRefreshState
            )
        },
        modifier = modifier
    ) {
        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize()
        ) {
            if (feedState.tfa != null) {
                item {
                    Text(
                        stringResource(R.string.featuredArticle),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp)
                    )
                    Text(
                        text = remember { LocalDate.now().format(dtf) },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
            if (feedState.tfa != null) {
                item {
                    ElevatedCard(
                        onClick = { loadPage(feedState.tfa.titles?.canonical ?: "") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        FeedImage(
                            source = feedState.tfa.originalImage?.source,
                            description = feedState.tfa.titles?.normalized,
                            width = feedState.tfa.originalImage?.width ?: 1,
                            height = feedState.tfa.originalImage?.height ?: 1,
                            imageLoader = imageLoader,
                            loadingIndicator = false
                        )
                        Text(
                            feedState.tfa.titles?.normalized ?: "",
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = FontFamily.Serif,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 16.dp)
                        )
                        Text(
                            feedState.tfa.description ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        Text(
                            feedState.tfa.extract ?: "",
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 8.dp, bottom = 16.dp)
                        )
                    }
                }
            }
            if (feedState.mostReadArticles != null) {
                item {
                    Text(
                        stringResource(R.string.trendingArticles),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 32.dp)
                    )
                    Text(
                        stringResource(R.string.topArticlesOTD),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                    )
                    val pagerState = rememberPagerState { feedState.mostReadArticles.size / 5 }
                    HorizontalPager(
                        state = pagerState,
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            for (i in it * 5..it * 5 + 4) {
                                key(i) {
                                    Row(
                                        modifier = Modifier
                                            .clickable(
                                                onClick = {
                                                    loadPage(
                                                        feedState.mostReadArticles[i].titles?.normalized
                                                            ?: ""
                                                    )
                                                }
                                            )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(start = 16.dp)
                                        ) {
                                            Text(
                                                feedState.mostReadArticles[i].titles?.normalized
                                                    ?: "",
                                                style = MaterialTheme.typography.titleMedium,
                                                modifier = Modifier.padding(top = 16.dp)
                                            )
                                            Text(
                                                feedState.mostReadArticles[i].description
                                                    ?: "",
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            ) {
                                                ArticleViewsGraph(
                                                    remember {
                                                        feedState.mostReadArticles[i].viewHistory?.map {
                                                            it.views ?: 0
                                                        } ?: emptyList()
                                                    },
                                                    modifier = Modifier
                                                        .size(width = 96.dp, height = 32.dp)
                                                        .padding(
                                                            horizontal = 16.dp,
                                                            vertical = 8.dp
                                                        )
                                                )
                                                Text(
                                                    df.format(feedState.mostReadArticles[i].views),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        if (feedState.mostReadArticles[i].thumbnail != null)
                                            FeedImage(
                                                source = feedState.mostReadArticles[i].thumbnail?.source,
                                                description = feedState.mostReadArticles[i].titles?.normalized,
                                                imageLoader = imageLoader,
                                                loadingIndicator = true,
                                                modifier = Modifier
                                                    .padding(16.dp)
                                                    .clip(shapes.large)
                                                    .size(80.dp, 80.dp)
                                            )
                                    }
                                    if (i != it * 5 + 4) HorizontalDivider()
                                }
                            }
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        ElevatedCard(shape = shapes.extraLarge) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(
                                                pagerState.currentPage - 1
                                            )
                                        }
                                    },
                                    enabled = pagerState.currentPage != 0
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                                        contentDescription = stringResource(R.string.scrollLeft)
                                    )
                                }
                                Text(
                                    stringResource(
                                        R.string.pageIndicator,
                                        pagerState.currentPage + 1,
                                        pagerState.pageCount
                                    )
                                )
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(
                                                pagerState.currentPage + 1
                                            )
                                        }
                                    },
                                    enabled = pagerState.currentPage != pagerState.pageCount - 1
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                        contentDescription = stringResource(R.string.scrollRight)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (feedState.image != null) {
                item {
                    Text(
                        stringResource(R.string.picOfTheDay),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 32.dp)
                    )
                    ElevatedCard(
                        onClick = onImageClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        FeedImage(
                            source = feedState.image.image?.source,
                            description = feedState.image.description?.text,
                            width = feedState.image.image?.width ?: 1,
                            height = feedState.image.image?.height ?: 1,
                            imageLoader = imageLoader,
                            loadingIndicator = false
                        )
                        Text(
                            feedState.image.description?.text?.parseAsHtml().toString(),
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 16.dp)
                        )
                        Text(
                            (feedState.image.artist?.name
                                ?: feedState.image.artist?.text)?.substringBefore('\n') +
                                    " (" + feedState.image.credit?.text?.substringBefore(';') + ")",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 8.dp, bottom = 16.dp)
                        )
                    }
                }
            }
            if (feedState.news != null) {
                item {
                    val carouselState = rememberCarouselState(0) { feedState.news.size }
                    Text(
                        stringResource(R.string.inTheNews),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 32.dp)
                    )
                    HorizontalMultiBrowseCarousel(
                        state = carouselState,
                        itemSpacing = 8.dp,
                        modifier = if (!wide)
                            Modifier
                                .padding(16.dp)
                                .aspectRatio(1f)
                        else
                            Modifier
                                .padding(16.dp)
                                .height(512.dp),
                        preferredItemWidth = 512.dp
                    ) { i ->
                        Box {
                            FeedImage(
                                source = feedState.news[i].links
                                    ?.find { it.thumbnail != null }
                                    ?.thumbnail?.source,
                                description = null,
                                imageLoader = imageLoader,
                                loadingIndicator = false,
                                modifier = Modifier
                                    .maskClip(shapes.extraLarge)
                            )
                            Box(
                                modifier = Modifier
                                    .maskClip(shapes.extraLarge)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black
                                            )
                                        )
                                    )
                                    .fillMaxSize()
                            ) {}
                            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                                Text(
                                    feedState.news[i].story?.parseAsHtml().toString(),
                                    maxLines = 10,
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color.White,
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .fillMaxWidth()
                                )
                                FlowRow(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            top = 8.dp,
                                            bottom = 16.dp,
                                            start = 16.dp,
                                            end = 16.dp
                                        )
                                        .wrapContentHeight(align = Alignment.Top),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    feedState.news[i].links
                                        ?.subList(0, min(3, feedState.news[i].links?.size ?: 0))
                                        ?.forEach {
                                            OutlinedButton(
                                                border = BorderStroke(
                                                    width = ButtonDefaults.outlinedButtonBorder().width,
                                                    color = Color.LightGray
                                                ),
                                                onClick = {
                                                    loadPage(
                                                        it.titles?.canonical ?: ""
                                                    )
                                                }
                                            ) {
                                                Text(
                                                    it.titles?.normalized ?: "",
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                }
                            }
                        }
                    }
                }
            }
            if (feedState.onThisDay != null) {
                item {
                    val carouselState = rememberCarouselState(0) { feedState.onThisDay.size }
                    Text(
                        stringResource(R.string.onThisDay),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 32.dp)
                    )
                    HorizontalMultiBrowseCarousel(
                        state = carouselState,
                        itemSpacing = 8.dp,
                        modifier =
                            if (!wide)
                                Modifier
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .aspectRatio(0.94f)
                            else
                                Modifier
                                    .padding(16.dp)
                                    .height(512.dp),
                        preferredItemWidth = 512.dp
                    ) { i ->
                        Column {
                            Text(
                                if ((feedState.onThisDay[i].year ?: 9999) > 1) {
                                    feedState.onThisDay[i].year.toString()
                                } else {
                                    feedState.onThisDay[i].year?.absoluteValue.toString() + " BC"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Box {
                                FeedImage(
                                    source = feedState.onThisDay[i].pages
                                        ?.find { it.thumbnail != null }
                                        ?.thumbnail?.source,
                                    description = null,
                                    imageLoader = imageLoader,
                                    loadingIndicator = false,
                                    modifier = Modifier
                                        .maskClip(shapes.extraLarge)
                                )
                                Box(
                                    modifier = Modifier
                                        .maskClip(shapes.extraLarge)
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Black
                                                )
                                            )
                                        )
                                        .fillMaxSize()
                                ) {}
                                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                                    Text(
                                        feedState.onThisDay[i].text ?: "",
                                        maxLines = 10,
                                        overflow = TextOverflow.Ellipsis,
                                        color = Color.White,
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                            .fillMaxWidth()
                                    )
                                    FlowRow(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                top = 8.dp,
                                                bottom = 16.dp,
                                                start = 16.dp,
                                                end = 16.dp
                                            )
                                            .wrapContentHeight(align = Alignment.Top),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        feedState.onThisDay[i].pages
                                            ?.subList(
                                                0,
                                                min(3, feedState.onThisDay[i].pages?.size ?: 0)
                                            )
                                            ?.forEach {
                                                OutlinedButton(
                                                    border = BorderStroke(
                                                        width = ButtonDefaults.outlinedButtonBorder().width,
                                                        color = Color.LightGray
                                                    ),
                                                    onClick = {
                                                        loadPage(
                                                            it.titles?.canonical ?: ""
                                                        )
                                                    }
                                                ) {
                                                    Text(
                                                        it.titles?.normalized ?: "",
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(insets.calculateBottomPadding() + 152.dp))
            }
        }
    }
}
