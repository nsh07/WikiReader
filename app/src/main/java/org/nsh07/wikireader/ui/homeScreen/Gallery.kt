package org.nsh07.wikireader.ui.homeScreen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import kotlinx.coroutines.launch
import org.nsh07.wikireader.R
import org.nsh07.wikireader.parser.toWikitextAnnotatedString
import org.nsh07.wikireader.ui.image.FeedImage

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Gallery(
    text: String,
    lang: String,
    fontSize: Int,
    background: Boolean,
    imageLoader: ImageLoader,
    onLinkClick: (String) -> Unit,
    onClick: (String, String) -> Unit
) {
    val content = remember(text) { text.substringAfter('>').trim(' ', '\n').lines() }
    val pagerState = rememberPagerState { content.size }
    val coroutineScope = rememberCoroutineScope()

    HorizontalPager(
        state = pagerState,
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(motionScheme.defaultSpatialSpec())
    ) {
        val uriLow = remember(text) {
            "https://$lang.wikipedia.org/wiki/Special:FilePath/${
                content[it].substringBefore('|')
            }?width=720"
        }
        val uriHigh = remember(text) {
            "https://$lang.wikipedia.org/wiki/Special:FilePath/${
                content[it].substringBefore('|')
            }"
        }
        val description = remember(text) { content[it].substringAfter('|') }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 16.dp)
        ) {
            FeedImage(
                source = uriLow,
                description = description,
                imageLoader = imageLoader,
                loadingIndicator = false,
                background = background,
                modifier = Modifier
                    .clip(shapes.large)
                    .clickable(onClick = { onClick(uriHigh, description) })
            )
            Text(
                description.toWikitextAnnotatedString(
                    colorScheme = colorScheme,
                    fontSize = fontSize - 2,
                    loadPage = onLinkClick,
                    typography = typography,
                    showRef = {}
                ),
                fontSize = (fontSize - 2).sp,
                lineHeight = (24 * ((fontSize - 2) / 16.0)).toInt().sp,
                textAlign = TextAlign.Center,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                    R.string.imageCounter,
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