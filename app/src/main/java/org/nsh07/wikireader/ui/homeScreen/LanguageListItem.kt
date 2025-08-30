package org.nsh07.wikireader.ui.homeScreen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.nsh07.wikireader.R
import org.nsh07.wikireader.ui.theme.CustomColors.listItemColors

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LanguageListItem(
    headlineContent: @Composable (() -> Unit),
    selected: Boolean,
    items: Int,
    index: Int,
    supportingContent: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val top by animateDpAsState(
        if (isPressed) 36.dp
        else {
            if (items == 1 || index == 0) 20.dp
            else 4.dp
        },
        motionScheme.fastSpatialSpec()
    )
    val bottom by animateDpAsState(
        if (isPressed) 36.dp
        else {
            if (items == 1 || index == items - 1) 20.dp
            else 4.dp
        },
        motionScheme.fastSpatialSpec()
    )

    ListItem(
        headlineContent = headlineContent,
        supportingContent = supportingContent,
        leadingContent = if (selected) {
            {
                Icon(
                    painterResource(R.drawable.check),
                    contentDescription = stringResource(R.string.selectedLabel)
                )
            }
        } else null,
        colors =
            if (selected) ListItemDefaults.colors(
                containerColor = colorScheme.primaryContainer.copy(
                    alpha = 0.3f
                ), leadingIconColor = colorScheme.primary
            )
            else listItemColors,
        modifier = Modifier
            .clip(
                if (selected) CircleShape
                else RoundedCornerShape(
                    topStart = top,
                    topEnd = top,
                    bottomStart = bottom,
                    bottomEnd = bottom
                )
            )
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource
            )
    )
}