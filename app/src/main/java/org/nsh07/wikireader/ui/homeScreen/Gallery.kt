package org.nsh07.wikireader.ui.homeScreen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import kotlinx.coroutines.launch
import org.nsh07.wikireader.R
import org.nsh07.wikireader.parser.toWikitextAnnotatedString
import org.nsh07.wikireader.ui.image.FeedImage

@Composable
fun Gallery(
    text: String,
    fontSize: Int,
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
            .animateContentSize()
    ) {
        val uri = remember(text) {
            "https://commons.wikimedia.org/wiki/Special:FilePath/${
                content[it].substringBefore('|')
            }"
        }
        val description = remember(text) { content[it].substringAfter('|') }
        OutlinedCard(
            shape = shapes.extraLarge,
            modifier = Modifier.padding(16.dp),
            onClick = { onClick(uri, description) }
        ) {
            FeedImage(
                source = uri,
                description = description,
                width = 1,
                height = 1,
                imageLoader = imageLoader,
                loadingIndicator = false
            )
            Text(
                description.toWikitextAnnotatedString(
                    colorScheme = colorScheme,
                    fontSize = fontSize,
                    loadPage = onLinkClick,
                    typography = typography
                ),
                modifier = Modifier.padding(20.dp)
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        OutlinedCard(shape = shapes.extraLarge) {
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
                        R.string.imageCounter,
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