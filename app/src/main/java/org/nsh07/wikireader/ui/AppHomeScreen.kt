package org.nsh07.wikireader.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.nsh07.wikireader.R
import org.nsh07.wikireader.ui.viewModel.HomeScreenState

/**
 * The app home screen composable.
 *
 * @param homeScreenState A [HomeScreenState] object provided by the app's ViewModel
 * @param listState A [LazyListState] object provided by the app's ViewModel
 */

@Composable
fun AppHomeScreen(
    homeScreenState: HomeScreenState,
    listState: LazyListState,
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val photo = homeScreenState.photo
    val photoDesc = homeScreenState.photoDesc

    Box(modifier = modifier) { // The container for all the composables in the home screen
        AnimatedVisibility( // The linear progress bar that shows up when the article is loading
            visible = homeScreenState.isLoading,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (homeScreenState.title != "") {
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
                            photoDesc = photoDesc,
                            onClick = onImageClick
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
        } else {
            Icon(
                painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(0.75f)
            )
        }
    }
}

