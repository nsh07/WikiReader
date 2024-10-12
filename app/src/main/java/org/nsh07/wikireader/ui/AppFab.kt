package org.nsh07.wikireader.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
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
    extendedFab: Boolean,
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
                onClick = focusSearch
            ) {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = stringResource(R.string.search)
                )
            }
        }

        AnimatedVisibility(
            index > 1,
            enter = fabEnter,
            exit = fabExit,
            modifier = Modifier
                .padding(top = 24.dp)
        ) {
            ExtendedFloatingActionButton(
                onClick = scrollToTop,
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