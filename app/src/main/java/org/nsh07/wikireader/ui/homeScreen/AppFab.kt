package org.nsh07.wikireader.ui.homeScreen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
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
    var expanded by remember { mutableStateOf(false) }

    BackHandler(expanded && visible) { expanded = false }

    FloatingActionButtonMenu(
        expanded = expanded,
        button = {
            ToggleFloatingActionButton(
                checked = expanded,
                onCheckedChange = { expanded = !expanded }
            ) {
                val imageVector by remember {
                    derivedStateOf {
                        if (checkedProgress > 0.5f) Icons.Outlined.Close else Icons.Outlined.Add
                    }
                }
                Icon(
                    painter = rememberVectorPainter(imageVector),
                    contentDescription = "Floating action button icon",
                    modifier = Modifier.animateIcon({ checkedProgress })
                )
            }
        },
        modifier = Modifier
            .semantics {
                traversalIndex = -1f
                stateDescription = if (expanded) "Expanded" else "Collapsed"
                contentDescription = "Toggle menu"
            }
            .animateFloatingActionButton(
                visible = visible || expanded,
                alignment = Alignment.BottomEnd
            )
    ) {
        FloatingActionButtonMenuItem(
            onClick = {
                expanded = false
                performRandomPageSearch()
            },
            text = { Text(stringResource(R.string.randomArticle)) },
            icon = { Icon(painterResource(R.drawable.shuffle), null) },
            modifier = Modifier.semantics { isTraversalGroup = true },
        )
        AnimatedVisibility(
            index > 1,
            enter = expandVertically(motionScheme.fastSpatialSpec()),
            exit = shrinkVertically(motionScheme.fastSpatialSpec())
        ) {
            FloatingActionButtonMenuItem(
                onClick = {
                    expanded = false
                    scrollToTop()
                },
                text = { Text(stringResource(R.string.scroll_to_top)) },
                icon = { Icon(painterResource(R.drawable.upward), null) },
                modifier = Modifier.semantics { isTraversalGroup = true },
            )
        }
        FloatingActionButtonMenuItem(
            onClick = {
                expanded = false
                focusSearch()
            },
            text = { Text(stringResource(R.string.search)) },
            icon = { Icon(Icons.Outlined.Search, null) },
            modifier = Modifier.semantics {
                isTraversalGroup = true
                customActions =
                    listOf(
                        CustomAccessibilityAction(
                            label = "Close menu",
                            action = {
                                expanded = false
                                true
                            }
                        )
                    )
            },
        )
    }
}
