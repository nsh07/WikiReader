package org.nsh07.wikireader.ui.homeScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.nsh07.wikireader.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppFab(
    index: Int,
    visible: Boolean,
    focusSearch: () -> Unit,
    scrollToTop: () -> Unit,
    performRandomPageSearch: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.padding(
            end = WindowInsets
                .systemBars
                .asPaddingValues()
                .calculateEndPadding(LocalLayoutDirection.current)
        )
    ) {
        SmallFloatingActionButton(
            onClick = {
                if (index > 1) scrollToTop()
                else performRandomPageSearch()
            },
            modifier = Modifier.animateFloatingActionButton(
                visible,
                alignment = Alignment.BottomEnd
            )
        ) {
            AnimatedContent(targetState = index > 1, label = "FAB Icon Animation") { isScrolled ->
                when {
                    isScrolled -> {
                        Icon(
                            painterResource(R.drawable.upward),
                            contentDescription = stringResource(R.string.scroll_to_top)
                        )
                    }

                    else -> {
                        Icon(
                            painterResource(R.drawable.shuffle),
                            contentDescription = stringResource(R.string.randomArticle)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        FloatingActionButton(
            onClick = focusSearch,
            modifier = Modifier.animateFloatingActionButton(
                visible,
                alignment = Alignment.BottomEnd
            )
        ) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = stringResource(R.string.search)
            )
        }
    }
}
