package org.nsh07.wikireader.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.nsh07.wikireader.R
import org.nsh07.wikireader.ui.theme.WikiReaderTheme

@Composable
fun ExpandableSection(title: String, body: String, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ),
        modifier = modifier.animateContentSize(tween(easing = FastOutSlowInEasing))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(onClick = { expanded = !expanded })
                .clip(RoundedCornerShape(12.dp))
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp, horizontal = 16.dp)
            )

            if (expanded) Icon(
                Icons.Outlined.KeyboardArrowUp,
                contentDescription = stringResource(R.string.collapse_section),
                modifier = Modifier.padding(end = 16.dp)
            )
            else Icon(
                Icons.Outlined.KeyboardArrowDown,
                contentDescription = stringResource(R.string.expand_section),
                modifier = Modifier.padding(end = 16.dp)
            )

        }

        if (expanded) {
            HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
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
            body = "Lorem\nIpsum\nBig\nHonkin\nBody\nText"
        )
    }
}
