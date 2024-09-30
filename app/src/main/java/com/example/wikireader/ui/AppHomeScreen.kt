package com.example.wikireader.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.wikireader.R
import kotlinx.coroutines.launch

@Composable
private fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

@Composable
fun AppHomeScreen(
    homeScreenState: HomeScreenState,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val photo = homeScreenState.photo
    val photoDesc = homeScreenState.photoDesc

    val coroutineScope = rememberCoroutineScope()

    val index by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val offset by remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }
    val extendedFab = (listState.isScrollingUp() && offset > 1024)

    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = homeScreenState.isLoading,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize()
        ) {
            item { // Title
                Text(
                    text = homeScreenState.title,
                    style = MaterialTheme.typography.displayMedium,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.padding(16.dp)
                )
            }
            item { // Image/description display logic
                if (photoDesc != null) {
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .animateContentSize(tween(easing = FastOutSlowInEasing))
                        ) {
                            if (photo != null) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(photo.source)
                                        .crossfade(true)
                                        .build(),
                                    loading = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(
                                                    photo.width.toFloat() / photo.height.toFloat()
                                                )
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }
                                    },
                                    error = {
                                        Icon(
                                            painterResource(R.drawable.error),
                                            contentDescription = "Error loading image",
                                            modifier = Modifier.padding(vertical = 16.dp)
                                        )
                                    },
                                    contentDescription = photoDesc.description[0],
                                    imageLoader = ImageLoader.Builder(LocalContext.current)
                                        .components {
                                            add(SvgDecoder.Factory())
                                        }
                                        .build(),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Text(
                                text = photoDesc.label[0],
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 16.dp, bottom = 8.dp)
                                    .fillMaxWidth()
                            )
                            Text(
                                text = photoDesc.description[0],
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 16.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
            item { // Body ("extract")
                Text(
                    text = homeScreenState.extract,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        AnimatedVisibility(
            index > 1,
            enter = scaleIn(),
            exit = scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            ExtendedFloatingActionButton(
                onClick = { coroutineScope.launch { listState.animateScrollToItem(0) } },
                icon = {
                    Icon(
                        Icons.Rounded.KeyboardArrowUp,
                        contentDescription = stringResource(R.string.up_arrow)
                    )
                },
                text = { Text("Scroll to top") },
                expanded = extendedFab,
                modifier = Modifier
            )
        }
    }
}
