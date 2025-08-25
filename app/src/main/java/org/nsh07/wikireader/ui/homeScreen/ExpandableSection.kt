package org.nsh07.wikireader.ui.homeScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import org.nsh07.wikireader.R
import org.nsh07.wikireader.ui.theme.WRShapeDefaults.cardShape
import org.nsh07.wikireader.ui.theme.WikiReaderTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ExpandableSection(
    title: List<AnnotatedString>,
    body: List<AnnotatedString>,
    lang: String,
    fontSize: Int,
    fontFamily: FontFamily,
    sharedScope: SharedTransitionScope,
    expanded: Boolean,
    renderMath: Boolean,
    darkTheme: Boolean,
    dataSaver: Boolean,
    imageBackground: Boolean,
    modifier: Modifier = Modifier,
    onLinkClick: (String) -> Unit,
    onGalleryImageClick: (String, String) -> Unit,
    showRef: (String) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(expanded) }
    val arrowRotation by animateFloatAsState(
        if (expanded) 180f else 0f,
        animationSpec = motionScheme.defaultSpatialSpec()
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ),
        shape = cardShape,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .clickable(onClick = { expanded = !expanded })
        ) {
            Icon(
                painterResource(R.drawable.keyboard_arrow_down),
                contentDescription =
                    if (expanded) stringResource(R.string.collapse_section)
                    else stringResource(R.string.expand_section),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .graphicsLayer {
                        rotationZ = arrowRotation
                    }
            )

            Text(
                text = remember {
                    var out = ""
                    title.fastForEach {
                        out += it
                    }
                    out.replace("<.+>".toRegex(), "")
                },
                style = MaterialTheme.typography.headlineMediumEmphasized,
                fontFamily = FontFamily.Serif,
                fontSize = (28 * (fontSize / 16.0)).toInt().sp,
                lineHeight = (36 * (fontSize / 16.0)).toInt().sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 16.dp, bottom = 16.dp, end = 16.dp)
            )
        }

        AnimatedVisibility(
            expanded,
            enter = expandVertically(expandFrom = Alignment.CenterVertically) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically) + fadeOut()
        ) {
            ParsedBodyText(
                body = body,
                lang = lang,
                sharedScope = sharedScope,
                fontSize = fontSize,
                fontFamily = fontFamily,
                renderMath = renderMath,
                darkTheme = darkTheme,
                dataSaver = dataSaver,
                background = imageBackground,
                onLinkClick = onLinkClick,
                onGalleryImageClick = onGalleryImageClick,
                showRef = showRef
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
fun ExpandableSectionPreview() {
    WikiReaderTheme {
        SharedTransitionLayout {
            ExpandableSection(
                title = listOf(buildAnnotatedString { append("Title") }),
                body = listOf(buildAnnotatedString { append("Lorem\nIpsum\nBig\nHonking\nBody\nText") }),
                lang = "en",
                fontSize = 16,
                fontFamily = FontFamily.SansSerif,
                sharedScope = this@SharedTransitionLayout,
                expanded = false,
                renderMath = true,
                darkTheme = false,
                dataSaver = false,
                imageBackground = false,
                onLinkClick = {},
                onGalleryImageClick = { _, _ -> },
                showRef = {}
            )
        }
    }
}
