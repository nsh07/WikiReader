package org.nsh07.wikireader.ui.homeScreen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.nsh07.wikireader.R

@Composable
fun AppFab(
    index: Int,
    showSmallFAB: Boolean,
    focusSearch: () -> Unit,
    scrollToTop: () -> Unit,
    performRandomPageSearch: () -> Unit
) {
    Column(horizontalAlignment = Alignment.End) {
        if (showSmallFAB) {
            SmallFloatingActionButton(
                onClick = {
                    if (index > 1) scrollToTop()
                    else performRandomPageSearch()
                }
            ) {
                Crossfade(targetState = index > 1, label = "FAB Icon Crossfade") { isScrolled ->
                    if (isScrolled) {
                        Icon(
                            painterResource(R.drawable.upward),
                            contentDescription = stringResource(R.string.scroll_to_top)
                        )
                    } else {
                        Icon(
                            painterResource(R.drawable.shuffle),
                            contentDescription = "Random article"
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        FloatingActionButton(
            onClick = focusSearch
        ) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = stringResource(R.string.search)
            )
        }
    }
}
