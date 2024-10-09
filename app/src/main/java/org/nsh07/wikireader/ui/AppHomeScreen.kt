package org.nsh07.wikireader.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.nsh07.wikireader.R

/**
 * The app home screen composable.
 *
 * @param homeScreenState A [HomeScreenState] object provided by the app's ViewModel
 * @param listState A [LazyListState] object provided by the app's ViewModel
 * @param searchAction The lambda that gets executed when the search FAB is pressed
 */

@Composable
fun AppHomeScreen(
    homeScreenState: HomeScreenState,
    listState: LazyListState,
    searchAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val photo = homeScreenState.photo
    val photoDesc = homeScreenState.photoDesc

    val coroutineScope = rememberCoroutineScope()

    val index by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val extendedFab by remember {
        derivedStateOf {
            listState.lastScrolledBackward || !listState.canScrollForward
        }
    }

    val fabEnter = scaleIn(transformOrigin = TransformOrigin(1f, 1f)) + fadeIn()
    val fabExit = scaleOut(transformOrigin = TransformOrigin(1f, 1f)) + fadeOut()

    Box(modifier = modifier) { // The container for all the composables in the home screen
        AnimatedVisibility( // The linear progress bar that shows up when the article is loading
            visible = homeScreenState.isLoading,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        LazyColumn( // The article
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
            item { // Image/description
                if (photoDesc != null) {
                    WikiImageCard(
                        photo = photo,
                        photoDesc = photoDesc
                    )
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

        Column( // Floating action buttons
            horizontalAlignment = Alignment.End,
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            AnimatedVisibility(
                index > 1,
                enter = fabEnter,
                exit = fabExit,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = searchAction
                ) {
                    Icon(Icons.Rounded.Search, contentDescription = stringResource(R.string.search))
                }
            }

            AnimatedVisibility(
                index > 1,
                enter = fabEnter,
                exit = fabExit,
                modifier = Modifier
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
}

