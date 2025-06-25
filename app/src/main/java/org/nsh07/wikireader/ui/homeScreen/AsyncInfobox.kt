package org.nsh07.wikireader.ui.homeScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import coil3.ImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.nsh07.wikireader.R
import org.nsh07.wikireader.parser.parseInfobox
import org.nsh07.wikireader.ui.theme.CustomTopBarColors.cardColors

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun AsyncInfobox(
    text: String,
    lang: String,
    fontSize: Int,
    darkTheme: Boolean,
    background: Boolean,
    imageLoader: ImageLoader,
    onImageClick: (String, String) -> Unit,
    onLinkClick: (String) -> Unit,
    showRef: (String) -> Unit
) {
    val context = LocalContext.current
    val colorScheme = colorScheme
    val typography = typography
    val extensions = listOf(".jpg", ".jpeg", ".png", ".svg", ".gif")
    val scope = rememberCoroutineScope()
    var infobox by remember { mutableStateOf(emptyList<Pair<AnnotatedString, AnnotatedString>>()) }
    var title: AnnotatedString? by remember { mutableStateOf(AnnotatedString(context.getString(R.string.infobox))) }

    LaunchedEffect(text) {
        scope.launch(Dispatchers.IO) {
            infobox = parseInfobox(text, colorScheme, typography, onLinkClick, showRef, fontSize)
            title = infobox.find { it.first.toString() == "Name" }?.second
        }
    }

    var expanded by remember { mutableStateOf(false) }

    SharedTransitionLayout {
        Card(
            colors = cardColors,
            shape = shapes.largeIncreased,
            modifier = Modifier
                .widthIn(max = 512.dp)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                ListItem(
                    leadingContent = {
                        FilledTonalIconButton(
                            onClick = { expanded = !expanded },
                            shapes = IconButtonDefaults.shapes()
                        ) {
                            Icon(
                                if (expanded) Icons.Outlined.KeyboardArrowUp
                                else Icons.Outlined.KeyboardArrowDown,
                                contentDescription = stringResource(R.string.expand_section)
                            )
                        }
                    },
                    headlineContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.infobox), fontWeight = FontWeight.Medium)
                            Spacer(Modifier.width(8.dp))
                            AnimatedVisibility(!expanded) {
                                Text(
                                    title ?: AnnotatedString(""),
                                    color = colorScheme.outline,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .sharedBounds(
                                            sharedContentState = rememberSharedContentState(
                                                title ?: AnnotatedString("")
                                            ), animatedVisibilityScope = this
                                        )
                                )
                            }
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = colorScheme.surfaceContainer),
                    modifier = Modifier
                        .clip(shapes.largeIncreased)
                        .padding(vertical = 8.dp)
                        .clickable(onClick = { expanded = !expanded })
                )

                AnimatedVisibility(
                    expanded,
                    enter = expandVertically(expandFrom = Alignment.CenterVertically) + fadeIn(),
                    exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically) + fadeOut()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (title != null)
                            Text(
                                title ?: AnnotatedString(""),
                                style = typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .sharedBounds(
                                        sharedContentState = rememberSharedContentState(
                                            key = title ?: AnnotatedString("")
                                        ),
                                        animatedVisibilityScope = this@AnimatedVisibility
                                    )
                                    .padding(vertical = 8.dp)
                            )
                        infobox.fastForEach { item ->
                            Row(Modifier.fillMaxWidth()) {
                                Text(
                                    item.first,
                                    fontSize = fontSize.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = (24 * (fontSize / 16.0)).toInt().sp,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .widthIn(max = 256.dp)
                                        .weight(1f)
                                )
                                if (item.second.matches("\\[\\[.{1,6}:.+]]".toRegex()) ||
                                    extensions.fastAny { item.second.endsWith(it) }
                                ) {
                                    ImageWithCaption(
                                        item.second.toString(),
                                        fontSize,
                                        lang,
                                        darkTheme,
                                        background,
                                        false,
                                        imageLoader,
                                        onLinkClick = onLinkClick,
                                        onClick = onImageClick,
                                        showCaption = false,
                                        modifier = Modifier
                                            .weight(2f)
                                    )
                                } else {
                                    Text(
                                        item.second,
                                        fontSize = fontSize.sp,
                                        lineHeight = (24 * (fontSize / 16.0)).toInt().sp,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .widthIn(max = 256.dp)
                                            .weight(2f)
                                    )
                                }
                            }
                        }
                        FilledTonalIconButton(
                            onClick = { expanded = false },
                            shapes = IconButtonDefaults.shapes(),
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(16.dp)
                                .width(52.dp)
                        ) {
                            Icon(
                                Icons.Outlined.KeyboardArrowUp,
                                contentDescription = stringResource(R.string.collapse_section)
                            )
                        }
                    }
                }
            }
        }
    }
}