package org.nsh07.wikireader.ui.homeScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.tomtung.latex2unicode.LaTeX2Unicode
import kotlin.text.Typography.nbsp

@Composable
fun ParsedBodyText(
    body: List<AnnotatedString>,
    fontSize: Int,
    renderMath: Boolean,
    darkTheme: Boolean
) {
    if (renderMath) {
        val context = LocalContext.current
        val dpi = context.resources.displayMetrics.density
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            for (i in 0..body.lastIndex) {
                if (i % 2 == 0) {
                    Text(
                        text = body[i],
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
                        latex = body[i].toString(),
                        fontSize = fontSize,
                        darkTheme = darkTheme
                    )
                }
            }
        }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            for (i in 0..body.lastIndex) {
                if (i % 2 == 0)
                    Text(
                        text = body[i],
                        style = typography.bodyLarge.copy(hyphens = Hyphens.Auto),
                        fontSize = fontSize.sp,
                        lineHeight = (24 * (fontSize / 16.0)).toInt().sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                else
                    Text(
                        text = LaTeX2Unicode.convert(body[i].toString())
                                .replace(' ', nbsp),
                        fontFamily = FontFamily.Serif,
                        fontSize = (fontSize + 4).sp,
                        lineHeight = (24 * (fontSize / 16.0) + 4).toInt().sp,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                    )
            }
        }
    }
}
