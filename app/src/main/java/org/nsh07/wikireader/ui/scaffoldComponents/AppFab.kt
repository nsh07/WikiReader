package org.nsh07.wikireader.ui.scaffoldComponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.nsh07.wikireader.R

@Composable
fun AppFab(
    focusSearch: () -> Unit,
    scrollToTop: () -> Unit,
    index: Int,
    fabEnter: EnterTransition,
    fabExit: ExitTransition
) {
    Column(horizontalAlignment = Alignment.End) {
        AnimatedVisibility(
            index > 1,
            enter = fabEnter,
            exit = fabExit
        ) {
            SmallFloatingActionButton(
                onClick = scrollToTop
            ) {
                Icon(
                    Icons.Outlined.KeyboardArrowUp,
                    contentDescription = stringResource(R.string.scroll_to_top)
                )
            }
        }

        FloatingActionButton(
            onClick = focusSearch,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = stringResource(R.string.search)
            )
        }
    }
}
