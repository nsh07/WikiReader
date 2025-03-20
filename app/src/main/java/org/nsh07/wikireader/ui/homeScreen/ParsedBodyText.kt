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
        var math = false
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            parsed.split('\n').forEach {
                if (it == "<math display=\"block\">" || it == "<math display=block>") {
                    math = true
                } else if (it == "</math>") {
                    math = false
                } else if (it.startsWith("<math display=block>")) {
                    EquationImage(
                        context = context,
                        dpi = dpi,
                        latex = it.substringAfter('>').substringBeforeLast('<'),
                        fontSize = fontSize,
                        darkTheme = darkTheme
                    )
                } else if (!math) {
                    if (it.trim() != "")
                        Text(
                            text = it.toWikitextAnnotatedString(
                                colorScheme = colorScheme,
                                typography = typography,
                                performSearch = onLinkClick
                            ),
                            style = typography.bodyLarge.copy(hyphens = Hyphens.Auto),
                            fontSize = fontSize.sp,
                            lineHeight = (24 * (fontSize / 16.0)).toInt().sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                } else {
                    EquationImage(
                        context = context,
                        dpi = dpi,
                        latex = it,
                        fontSize = fontSize,
                        darkTheme = darkTheme
                    )
                }
            }
        }
    } else {
        Text(
            text = parsed.toWikitextAnnotatedString(
                colorScheme = colorScheme,
                typography = typography,
                performSearch = onLinkClick
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
