package org.nsh07.wikireader.ui.homeScreen

import android.icu.text.CompactDecimalFormat
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.CarouselDefaults
import androidx.compose.material3.carousel.CarouselState
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.core.text.parseAsHtml
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.window.core.layout.WindowSizeClass
import coil3.ImageLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.nsh07.wikireader.R
import org.nsh07.wikireader.ui.homeScreen.viewModel.HomeSubscreen
import org.nsh07.wikireader.ui.image.FeedImage
import org.nsh07.wikireader.ui.theme.CustomTopBarColors.cardColors
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.bottomListItemShape
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.cardShape
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.middleListItemShape
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.topListItemShape
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.absoluteValue
import kotlin.math.min

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class
)
@Composable
fun ArticleFeed(
    feedContent: HomeSubscreen.Feed,
    sharedScope: SharedTransitionScope,
    pagerState: PagerState?,
    newsCarouselState: CarouselState?,
    otdCarouselState: CarouselState?,
    imageLoader: ImageLoader,
    insets: PaddingValues,
    windowSizeClass: WindowSizeClass,
    imageBackground: Boolean,
    loadPage: (String) -> Unit,
    refreshFeed: () -> Unit,
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()
    val listState = feedContent.listState

    val expanded =
        remember { windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) }
    val df = remember {
        CompactDecimalFormat.getInstance(
            configuration.getLocales().get(0),
            CompactDecimalFormat.CompactStyle.SHORT
        )
    }
    val dtf = remember {
        DateTimeFormatter
            .ofLocalizedDate(FormatStyle.LONG)
            .withLocale(configuration.getLocales().get(0))
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
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = insets.calculateTopPadding()),
                isRefreshing = isRefreshing,
                state = pullToRefreshState
            )
        },
        modifier = modifier
    ) {
        LazyColumn(
            state = listState,
            contentPadding = insets,
            modifier = modifier.fillMaxSize()
        ) {
            if (feedContent.tfa != null) {
                item {
                    with(sharedScope) {
                        Text(
                            stringResource(R.string.featuredArticle),
                            style = typography.headlineMedium,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 16.dp)
                        )
                        Text(
                            text = remember { LocalDate.now().format(dtf) },
                            style = typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Card(
                            onClick = { loadPage(feedContent.tfa.titles?.canonical ?: "") },
                            shape = cardShape,
                            colors = cardColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            if (!expanded) {
                                FeedImage(
                                    source = feedContent.tfa.originalImage?.source,
                                    description = feedContent.tfa.titles?.normalized,
                                    width = feedContent.tfa.originalImage?.width ?: 1,
                                    height = feedContent.tfa.originalImage?.height ?: 1,
                                    imageLoader = imageLoader,
                                    background = imageBackground,
                                    loadingIndicator = false,
                                    modifier = Modifier
                                        .sharedBounds(
                                            sharedContentState = rememberSharedContentState(
                                                feedContent.tfa.originalImage?.source ?: "imgsrc"
                                            ),
                                            animatedVisibilityScope = LocalNavAnimatedContentScope.current
                                        )
                                        .clip(cardShape)
                                )
                                Text(
                                    feedContent.tfa.titles?.normalized ?: "",
                                    style = typography.headlineMedium,
                                    fontFamily = FontFamily.Serif,
                                    modifier = Modifier
                                        .sharedBounds(
                                            sharedContentState = rememberSharedContentState(
                                                feedContent.tfa.titles?.normalized ?: "title"
                                            ),
                                            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                            zIndexInOverlay = 1f
                                        )
                                        .padding(horizontal = 16.dp)
                                        .padding(top = 16.dp)
                                )
                                Text(
                                    feedContent.tfa.description ?: "",
                                    style = typography.bodyMedium,
                                    color = colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .sharedBounds(
                                            sharedContentState = rememberSharedContentState(
                                                feedContent.tfa.description ?: "desc"
                                            ),
                                            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                            zIndexInOverlay = 1f
                                        )
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                                Text(
                                    feedContent.tfa.extract ?: "",
                                    maxLines = 5,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .padding(top = 8.dp, bottom = 16.dp)
                                )
                            } else {
                                Row {
                                    FeedImage(
                                        source = feedContent.tfa.originalImage?.source,
                                        description = feedContent.tfa.titles?.normalized,
                                        width = feedContent.tfa.originalImage?.width ?: 1,
                                        height = feedContent.tfa.originalImage?.height ?: 1,
                                        imageLoader = imageLoader,
                                        background = imageBackground,
                                        loadingIndicator = false,
                                        modifier = Modifier
                                            .sharedBounds(
                                                sharedContentState = rememberSharedContentState(
                                                    feedContent.tfa.originalImage?.source
                                                        ?: "imgsrc"
                                                ),
                                                animatedVisibilityScope = LocalNavAnimatedContentScope.current
                                            )
                                            .weight(1f)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            feedContent.tfa.titles?.normalized ?: "",
                                            style = typography.headlineMedium,
                                            fontFamily = FontFamily.Serif,
                                            modifier = Modifier
                                                .sharedBounds(
                                                    sharedContentState = rememberSharedContentState(
                                                        feedContent.tfa.titles?.normalized
                                                            ?: "title"
                                                    ),
                                                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                                    zIndexInOverlay = 1f
                                                )
                                                .padding(horizontal = 16.dp)
                                                .padding(top = 16.dp)
                                        )
                                        Text(
                                            feedContent.tfa.description ?: "",
                                            style = typography.bodyMedium,
                                            color = colorScheme.onSurfaceVariant,
                                            modifier = Modifier
                                                .sharedBounds(
                                                    sharedContentState = rememberSharedContentState(
                                                        feedContent.tfa.description ?: "desc"
                                                    ),
                                                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                                    zIndexInOverlay = 1f
                                                )
                                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                        )
                                        Text(
                                            feedContent.tfa.extract ?: "",
                                            maxLines = 5,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .padding(horizontal = 16.dp)
                                                .padding(top = 8.dp, bottom = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (feedContent.mostReadArticles != null) {
                item {
                    with(sharedScope) {
                        Text(
                            stringResource(R.string.trendingArticles),
                            style = typography.titleLarge,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 32.dp)
                        )
                        Text(
                            stringResource(R.string.topArticlesOTD),
                            style = typography.bodyMedium,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                        )

                        HorizontalPager(
                            state = pagerState!!, // PagerState is not null when mostReadArticles is not null
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(motionScheme.defaultSpatialSpec())
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                for (i in it * 5..it * 5 + 4) {
                                    key(i) {
                                        Row(
                                            modifier = Modifier
                                                .clip(
                                                    when (i) {
                                                        it * 5 -> topListItemShape
                                                        it * 5 + 4 -> bottomListItemShape
                                                        else -> middleListItemShape
                                                    }
                                                )
                                                .background(colorScheme.surfaceContainer)
                                                .clickable(
                                                    onClick = {
                                                        loadPage(
                                                            feedContent.mostReadArticles[i].titles?.normalized
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
                                                    feedContent.mostReadArticles[i].titles?.normalized
                                                        ?: "",
                                                    style = typography.titleMedium,
                                                    modifier = Modifier
                                                        .sharedBounds(
                                                            sharedContentState = rememberSharedContentState(
                                                                feedContent.mostReadArticles[i].titles?.normalized
                                                                    ?: "title"
                                                            ),
                                                            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                                            zIndexInOverlay = 1f
                                                        )
                                                        .padding(top = 16.dp)
                                                )
                                                Text(
                                                    feedContent.mostReadArticles[i].description
                                                        ?: "",
                                                    style = typography.bodyMedium,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.sharedBounds(
                                                        sharedContentState = rememberSharedContentState(
                                                            feedContent.mostReadArticles[i].description
                                                                ?: "desc"
                                                        ),
                                                        animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                                        zIndexInOverlay = 1f
                                                    )
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(
                                                        bottom = 8.dp,
                                                        top = 4.dp
                                                    )
                                                ) {
                                                    ArticleViewsGraph(
                                                        remember {
                                                            feedContent.mostReadArticles[i].viewHistory?.map {
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
                                                        df.format(feedContent.mostReadArticles[i].views),
                                                        style = typography.bodyMedium,
                                                        color = colorScheme.primary
                                                    )
                                                }
                                            }
                                            if (feedContent.mostReadArticles[i].thumbnail != null)
                                                FeedImage(
                                                    source = feedContent.mostReadArticles[i].thumbnail?.source,
                                                    description = feedContent.mostReadArticles[i].titles?.normalized,
                                                    imageLoader = imageLoader,
                                                    loadingIndicator = true,
                                                    background = imageBackground,
                                                    modifier = Modifier
                                                        .sharedBounds(
                                                            sharedContentState = rememberSharedContentState(
                                                                feedContent.mostReadArticles[i].originalImage?.source
                                                                    ?: "imgsrc"
                                                            ),
                                                            animatedVisibilityScope = LocalNavAnimatedContentScope.current
                                                        )
                                                        .padding(16.dp)
                                                        .clip(shapes.large)
                                                        .size(80.dp)
                                                )
                                        }
                                    }
                                }
                            }
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(shapes.large)
                                    .background(colorScheme.surfaceContainer)
                            ) {
                                IconButton(
                                    shapes = IconButtonDefaults.shapes(),
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
                                        painterResource(R.drawable.keyboard_arrow_left),
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
                                    shapes = IconButtonDefaults.shapes(),
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
                                        painterResource(R.drawable.keyboard_arrow_right),
                                        contentDescription = stringResource(R.string.scrollRight)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (feedContent.image != null) {
                item {
                    Text(
                        stringResource(R.string.picOfTheDay),
                        style = typography.titleLarge,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 32.dp)
                    )
                    Card(
                        onClick = onImageClick,
                        shape = cardShape,
                        colors = cardColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        if (!expanded) {
                            FeedImage(
                                source = feedContent.image.thumbnail?.source,
                                description = feedContent.image.description?.text,
                                width = feedContent.image.thumbnail?.width ?: 1,
                                height = feedContent.image.thumbnail?.height ?: 1,
                                imageLoader = imageLoader,
                                background = imageBackground,
                                loadingIndicator = false,
                                modifier = Modifier.clip(cardShape)
                            )
                            Text(
                                feedContent.image.description?.text?.parseAsHtml().toString(),
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 16.dp)
                            )
                            Text(
                                (feedContent.image.artist?.name
                                    ?: feedContent.image.artist?.text)?.substringBefore('\n') +
                                        " (" + feedContent.image.credit?.text?.substringBefore(';') + ")",
                                style = typography.bodyMedium,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 8.dp, bottom = 16.dp)
                            )
                        } else {
                            Row {
                                FeedImage(
                                    source = feedContent.image.image?.source,
                                    description = feedContent.image.description?.text,
                                    width = feedContent.image.image?.width ?: 1,
                                    height = feedContent.image.image?.height ?: 1,
                                    imageLoader = imageLoader,
                                    background = imageBackground,
                                    loadingIndicator = false,
                                    modifier = Modifier.weight(1f)
                                )
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        feedContent.image.description?.text?.parseAsHtml()
                                            .toString(),
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                            .padding(top = 16.dp)
                                    )
                                    Text(
                                        (feedContent.image.artist?.name
                                            ?: feedContent.image.artist?.text)?.substringBefore('\n') +
                                                " (" + feedContent.image.credit?.text?.substringBefore(
                                            ';'
                                        ) + ")",
                                        style = typography.bodyMedium,
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                            .padding(top = 8.dp, bottom = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (feedContent.news != null) {
                item {
                    with(sharedScope) {
                        Text(
                            stringResource(R.string.inTheNews),
                            style = typography.titleLarge,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 32.dp)
                        )
                        HorizontalMultiBrowseCarousel(
                            state = newsCarouselState!!,
                            itemSpacing = 8.dp,
                            flingBehavior = CarouselDefaults.multiBrowseFlingBehavior(
                                newsCarouselState
                            ),
                            modifier = if (!expanded)
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
                                    source = feedContent.news[i].links
                                        ?.find { it.thumbnail != null }
                                        ?.thumbnail?.source,
                                    description = null,
                                    imageLoader = imageLoader,
                                    loadingIndicator = false,
                                    background = imageBackground,
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
                                        feedContent.news[i].story?.parseAsHtml().toString(),
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
                                        feedContent.news[i].links
                                            ?.subList(
                                                0,
                                                min(3, feedContent.news[i].links?.size ?: 0)
                                            )
                                            ?.fastForEach {
                                                OutlinedButton(
                                                    shapes = ButtonDefaults.shapes(),
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
                                                        color = Color.White,
                                                        modifier = Modifier.sharedBounds(
                                                            sharedContentState = rememberSharedContentState(
                                                                it.titles?.normalized ?: "title"
                                                            ),
                                                            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                                            zIndexInOverlay = 1f
                                                        )
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
            if (feedContent.onThisDay != null) {
                item {
                    with(sharedScope) {
                        Text(
                            stringResource(R.string.onThisDay),
                            style = typography.titleLarge,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 32.dp)
                        )
                        HorizontalMultiBrowseCarousel(
                            state = otdCarouselState!!,
                            itemSpacing = 8.dp,
                            flingBehavior = CarouselDefaults.multiBrowseFlingBehavior(
                                otdCarouselState
                            ),
                            modifier =
                                if (!expanded)
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
                                    if ((feedContent.onThisDay[i].year ?: 9999) > 1) {
                                        feedContent.onThisDay[i].year.toString()
                                    } else {
                                        feedContent.onThisDay[i].year?.absoluteValue.toString() + " BC"
                                    },
                                    style = typography.titleLarge,
                                    color = colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                Box {
                                    FeedImage(
                                        source = feedContent.onThisDay[i].pages
                                            ?.find { it.thumbnail != null }
                                            ?.thumbnail?.source,
                                        description = null,
                                        imageLoader = imageLoader,
                                        loadingIndicator = false,
                                        background = imageBackground,
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
                                            feedContent.onThisDay[i].text ?: "",
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
                                            feedContent.onThisDay[i].pages
                                                ?.subList(
                                                    0,
                                                    min(
                                                        3,
                                                        feedContent.onThisDay[i].pages?.size ?: 0
                                                    )
                                                )
                                                ?.fastForEach {
                                                    OutlinedButton(
                                                        shapes = ButtonDefaults.shapes(),
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
                                                            color = Color.White,
                                                            modifier = Modifier.sharedBounds(
                                                                sharedContentState = rememberSharedContentState(
                                                                    it.titles?.normalized ?: "title"
                                                                ),
                                                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                                                zIndexInOverlay = 1f
                                                            )
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
            }
            item {
                Spacer(Modifier.height(156.dp))
            }
        }
    }
}
