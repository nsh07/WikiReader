package org.nsh07.wikireader.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParsedBodyText(
    title: String,
    pageTitle: String,
    body: String,
    fontSize: Int,
    description: String,
    intro: Boolean = false,
    onLinkClick: (String) -> Unit
) {
    if (!description.contains("disambiguation") && title != "See also")
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = fontSize.sp,
            lineHeight = (24 * (fontSize / 16.0)).toInt().sp,
            modifier = Modifier.padding(16.dp)
        )
    else
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            body.split('\n').forEachIndexed { ind, it ->
                if (it.contains("===") || it.trim() == "" || it.last() == ':' || (intro && ind == 0))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = fontSize.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                else if (it.substringBefore(',').trim('"') == pageTitle)
                    FilledTonalButton(onClick = { onLinkClick(it) }) {
                        Text(
                            text = it,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                else
                    FilledTonalButton(onClick = { onLinkClick(it.substringBefore(',')) }) {
                        Text(
                            text = it.substringBefore(','),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
            }
        }
}