package org.nsh07.wikireader.ui.homeScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nsh07.wikireader.R
import org.nsh07.wikireader.ui.theme.WikiReaderTheme

@Composable
fun ExpandableSection(
    title: String,
    body: String,
    fontSize: Int,
    expanded: Boolean,
    renderMath: Boolean,
    darkTheme: Boolean,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(expanded) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .clickable(onClick = { expanded = !expanded })
        ) {
            if (expanded) Icon(
                Icons.Outlined.KeyboardArrowUp,
                contentDescription = stringResource(R.string.collapse_section),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            else Icon(
                Icons.Outlined.KeyboardArrowDown,
                contentDescription = stringResource(R.string.expand_section),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
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
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
        ) {
            ParsedBodyText(
                body = body,
                fontSize = fontSize,
                onLinkClick = onLinkClick,
                renderMath = renderMath,
                darkTheme = darkTheme
            )
        }
    }
}

@Preview
@Composable
fun ExpandableSectionPreview() {
    WikiReaderTheme {
        ExpandableSection(
            title = "Title",
            body = "Lorem\nIpsum\nBig\nHonkin\nBody\nText",
            fontSize = 16,
            onLinkClick = {},
            expanded = false,
            renderMath = true,
            darkTheme = false
        )
    }
}
