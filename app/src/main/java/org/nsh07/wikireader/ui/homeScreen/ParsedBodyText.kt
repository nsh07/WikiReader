package org.nsh07.wikireader.ui.homeScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nsh07.wikireader.data.cleanUpWikitext
import org.nsh07.wikireader.data.toWikitextAnnotatedString

@Composable
fun ParsedBodyText(
    body: String,
    fontSize: Int,
    renderMath: Boolean,
    darkTheme: Boolean,
    onLinkClick: (String) -> Unit
) {
    val parsed = cleanUpWikitext(body)
    if (renderMath) {
        val context = LocalContext.current
        val dpi = context.resources.displayMetrics.density
        var curr = ""
        var i = 0
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            while (i < parsed.length) {
                if (parsed[i] == '<') {
                    val currSubstring = parsed.substring(i)
                    if (currSubstring.startsWith("<math display")) {
                        Text(
                            text = curr.toWikitextAnnotatedString(
                                colorScheme = colorScheme,
                                typography = typography,
                                performSearch = onLinkClick,
                                fontSize = fontSize
                            ),
                            style = typography.bodyLarge.copy(hyphens = Hyphens.Auto),
                            fontSize = fontSize.sp,
                            lineHeight = (24 * (fontSize / 16.0)).toInt().sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                        curr = currSubstring.substringAfter('>').substringBefore("</math>")
                        EquationImage(
                            context = context,
                            dpi = dpi,
                            latex = curr,
                            fontSize = fontSize,
                            darkTheme = darkTheme
                        )
                        i += currSubstring.substringBefore('>').length + curr.length + "</math>".length
                        curr = ""
                    } else curr += parsed[i]
                } else curr += parsed[i]
                i++
            }
            Text(
                text = curr.toWikitextAnnotatedString(
                    colorScheme = colorScheme,
                    typography = typography,
                    performSearch = onLinkClick,
                    fontSize = fontSize
                ),
                style = typography.bodyLarge.copy(hyphens = Hyphens.Auto),
                fontSize = fontSize.sp,
                lineHeight = (24 * (fontSize / 16.0)).toInt().sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
    } else {
        Text(
            text = parsed.toWikitextAnnotatedString(
                colorScheme = colorScheme,
                typography = typography,
                performSearch = onLinkClick,
                fontSize = fontSize
            ),
            style = typography.bodyLarge.copy(hyphens = Hyphens.Auto),
            fontSize = fontSize.sp,
            lineHeight = (24 * (fontSize / 16.0)).toInt().sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
