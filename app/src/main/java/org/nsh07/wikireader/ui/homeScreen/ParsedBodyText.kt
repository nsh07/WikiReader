package org.nsh07.wikireader.ui.homeScreen

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Size
import org.nsh07.wikireader.R

@Composable
fun ParsedBodyText(
    title: String,
    pageTitle: String,
    body: String,
    fontSize: Int,
    description: String,
    renderMath: Boolean,
    darkTheme: Boolean,
    intro: Boolean = false,
    onLinkClick: (String) -> Unit
) {
    if (!description.contains("disambiguation") && title != "See also") {
        if (renderMath) { // Split content by newlines and render LaTeX if line starts with \{displaystyle
            val context = LocalContext.current
            val dpi = context.resources.displayMetrics.density
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                body.split('\n').forEach {
                    if (!it.startsWith("  ") && it.trim() != "")
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = fontSize.sp,
                            lineHeight = (24 * (fontSize / 16.0)).toInt().sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    else if (it.trimStart().startsWith("{\\displaystyle"))
                        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(
                                        "https://latex.codecogs.com/png.image?\\dpi{${
                                            (dpi * 160 * (fontSize / 16.0)).toInt()
                                        }}${
                                            it.trim()
                                        }"
                                    )
                                    .size(Size.ORIGINAL)
                                    .build(),
                                placeholder = painterResource(R.drawable.more_horiz),
                                error = painterResource(R.drawable.error),
                                contentDescription = null,
                                colorFilter = if (darkTheme) // Invert colors in dark theme
                                    PorterDuffColorFilter(
                                        0xffffffff.toInt(),
                                        PorterDuff.Mode.SRC_IN
                                    ).asComposeColorFilter()
                                else null,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                }
            }
        } else { // Clean up string and then display
            val pattern =
                remember { "\n {6}\n {4}\n {3} \\{\\\\displaystyle.*\\}\n {2}\n".toRegex() }
            val multipleSpaces = remember { "[^\\S\\n]{2,}".toRegex() }
            Text(
                text = body
                    .replace(pattern, "")
                    .replace("\n  \n    \n      ", "")
                    .replace("\n        ", "")
                    .replace(multipleSpaces, " "),
                style = MaterialTheme.typography.bodyLarge,
                fontSize = fontSize.sp,
                lineHeight = (24 * (fontSize / 16.0)).toInt().sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    } else
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
