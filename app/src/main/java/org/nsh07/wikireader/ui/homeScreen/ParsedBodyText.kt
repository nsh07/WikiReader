package org.nsh07.wikireader.ui.homeScreen

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
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
        Text(
            buildAnnotatedString {
                body.split('\n').forEachIndexed { ind, curr ->
                    if (curr.contains("===") || curr.trim() == "" || curr.last() == ':' || (intro && ind == 0))
                        append(curr + "\n")
                    else {
                        val link =
                            LinkAnnotation.Url(
                                "",
                                TextLinkStyles(
                                    SpanStyle(
                                        color = MaterialTheme.colorScheme.primary,
                                        textDecoration = TextDecoration.Underline
                                    )
                                )
                            ) {
                                if (curr.substringBefore(',').trim('"') == pageTitle)
                                    onLinkClick(curr)
                                else
                                    onLinkClick(curr.substringBefore(','))
                            }

                        if (curr[0] == '"') {
                            withLink(link) { append(curr.substringBeforeLast('"') + '"') }
                            append(curr.substringAfterLast('"') + '\n')
                        } else if (curr.contains(')')) {
                            withLink(link) { append(curr.substringBefore(')') + ')') }
                            append(curr.substringAfter(')') + '\n')
                        } else {
                            withLink(link) { append(curr.substringBefore(',')) }
                            val appendText =
                                curr.substringAfter(',', missingDelimiterValue = "no-delimiter")
                            if (appendText != "no-delimiter") append(",$appendText\n")
                            else append('\n')
                        }
                    }
                }
            },
            style = MaterialTheme.typography.bodyLarge,
            fontSize = fontSize.sp,
            lineHeight = (24 * (fontSize / 16.0)).toInt().sp,
            modifier = Modifier.padding(16.dp)
        )
}
