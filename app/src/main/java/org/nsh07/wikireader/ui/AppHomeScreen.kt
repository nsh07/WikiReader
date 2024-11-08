package org.nsh07.wikireader.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.unit.sp
import org.nsh07.wikireader.R
import org.nsh07.wikireader.ui.viewModel.HomeScreenState
import org.nsh07.wikireader.ui.viewModel.PreferencesState

/**
 * The app home screen composable.
 *
 * @param homeScreenState A [HomeScreenState] object provided by the app's ViewModel
 * @param listState A [LazyListState] object provided by the app's ViewModel
 * @param onImageClick A lambda that is called when the image in the home screen is clicked
 * @param insets A [PaddingValues] object provided by the parent [androidx.compose.material3.Scaffold]
 * @param modifier Self explanatory
 */

@Composable
fun AppHomeScreen(
    homeScreenState: HomeScreenState,
    listState: LazyListState,
    preferencesState: PreferencesState,
    onImageClick: () -> Unit,
    insets: PaddingValues,
    modifier: Modifier = Modifier
) {
    val photo = homeScreenState.photo
    val photoDesc = homeScreenState.photoDesc
    val fontSize = preferencesState.fontSize

    var s = homeScreenState.extract.size
    if (s > 1) s -= 2
    else s = 0

    Box(modifier = modifier) { // The container for all the composables in the home screen
        if (homeScreenState.title != "") {
            LazyColumn( // The article
                state = listState,
                modifier = Modifier.fillMaxSize()
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
                        ImageCard(
                            photo = photo,
                            photoDesc = photoDesc,
                            onClick = onImageClick
                        )
                    }
                }
                item { // Main description
                    SelectionContainer {
                        Text(
                            text = homeScreenState.extract[0],
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = fontSize.sp,
                            lineHeight = (24 * (fontSize / 16.0)).toInt().sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                for (i in 1..s) {
                    if (i % 2 == 1) // Elements at odd indices are titles
                        item { // Expandable sections logic
                            SelectionContainer {
                                ExpandableSection(
                                    title = homeScreenState.extract[i],
                                    body = homeScreenState.extract[i + 1],
                                    fontSize = fontSize,
                                    expanded = preferencesState.expandedSections
                                )
                            }
                        }
                }

                item {
                    Spacer(Modifier.height(insets.calculateBottomPadding() + 152.dp))
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

        AnimatedVisibility( // The linear progress bar that shows up when the article is loading
            visible = homeScreenState.isLoading,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

