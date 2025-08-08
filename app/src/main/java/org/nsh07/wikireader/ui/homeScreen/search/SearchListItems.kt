package org.nsh07.wikireader.ui.homeScreen.search

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import org.nsh07.wikireader.R
import org.nsh07.wikireader.data.SearchHistoryItem
import org.nsh07.wikireader.data.WikiPrefixSearchResult
import org.nsh07.wikireader.data.WikiSearchResult
import org.nsh07.wikireader.ui.homeScreen.viewModel.HomeAction
import org.nsh07.wikireader.ui.image.FeedImage

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LazyItemScope.SearchHistoryListItem(
    index: Int,
    items: Int,
    currentItem: SearchHistoryItem,
    haptic: HapticFeedback,
    onAction: (HomeAction) -> Unit,
    removeHistoryItem: (SearchHistoryItem) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentText = currentItem.query
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
        leadingContent = {
            Icon(
                painterResource(R.drawable.history),
                contentDescription = null
            )
        },
        headlineContent = {
            Text(
                currentText,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailingContent = {
            IconButton(
                shapes = IconButtonDefaults.shapes(),
                onClick = { onAction(HomeAction.SetQuery(currentText)) },
                modifier = Modifier.wrapContentSize()
            ) {
                Icon(
                    painterResource(R.drawable.north_west),
                    contentDescription = null
                )
            }
        },
        modifier = modifier
            .padding(horizontal = 16.dp)
            .clip(
                RoundedCornerShape(
                    topStart = top,
                    topEnd = top,
                    bottomStart = bottom,
                    bottomEnd = bottom
                )
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(
                        HapticFeedbackType.LongPress
                    )
                    removeHistoryItem(currentItem)
                },
                interactionSource = interactionSource
            )
            .animateItem()
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LazyItemScope.PrefixSearchResultListItem(
    item: WikiPrefixSearchResult,
    dataSaver: Boolean,
    imageBackground: Boolean,
    imageLoader: ImageLoader,
    onSearchBarExpandedChange: (Boolean) -> Unit,
    onAction: (HomeAction) -> Unit,
    items: Int,
    index: Int,
    modifier: Modifier = Modifier
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
        headlineContent = {
            Text(
                item.title,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = if (item.terms != null) {
            {
                Text(
                    item.terms.description[0],
                    softWrap = true,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else null,
        trailingContent = {
            if (item.thumbnail != null && !dataSaver)
                FeedImage(
                    source = item.thumbnail.source,
                    imageLoader = imageLoader,
                    contentScale = ContentScale.Crop,
                    loadingIndicator = true,
                    background = imageBackground,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .size(56.dp)
                        .clip(shapes.large)
                )
            else null
        },
        modifier = modifier
            .padding(horizontal = 16.dp)
            .clip(
                RoundedCornerShape(
                    topStart = top,
                    topEnd = top,
                    bottomStart = bottom,
                    bottomEnd = bottom
                )
            )
            .clickable(
                onClick = {
                    onSearchBarExpandedChange(false)
                    onAction(HomeAction.LoadPage(item.title))
                },
                interactionSource = interactionSource
            )
            .animateItem()
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LazyItemScope.SearchResultListItem(
    it: WikiSearchResult,
    index: Int,
    items: Int,
    dataSaver: Boolean,
    imageBackground: Boolean,
    imageLoader: ImageLoader,
    onSearchBarExpandedChange: (Boolean) -> Unit,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier
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
        overlineContent = if (it.redirectTitle != null) {
            {
                Text(
                    stringResource(
                        R.string.redirectedFrom,
                        it.redirectTitle
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else null,
        headlineContent = {
            Text(
                AnnotatedString.fromHtml(
                    it.titleSnippet.ifEmpty { it.title }
                ),
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                AnnotatedString.fromHtml(it.snippet),
                softWrap = true,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailingContent = {
            if (it.thumbnail != null && !dataSaver)
                FeedImage(
                    source = it.thumbnail.source,
                    imageLoader = imageLoader,
                    contentScale = ContentScale.Crop,
                    loadingIndicator = true,
                    background = imageBackground,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .size(56.dp)
                        .clip(shapes.large)
                )
            else null
        },
        modifier = modifier
            .padding(horizontal = 16.dp)
            .clip(
                RoundedCornerShape(
                    topStart = top,
                    topEnd = top,
                    bottomStart = bottom,
                    bottomEnd = bottom
                )
            )
            .clickable(
                onClick = {
                    onSearchBarExpandedChange(false)
                    onAction(HomeAction.LoadPage(it.title))
                },
                interactionSource = interactionSource
            )
            .animateItem()
    )
}
