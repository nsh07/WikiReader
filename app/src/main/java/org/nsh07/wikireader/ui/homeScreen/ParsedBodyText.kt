package org.nsh07.wikireader.ui.homeScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import com.github.tomtung.latex2unicode.LaTeX2Unicode
import kotlin.text.Typography.nbsp

@Composable
fun ParsedBodyText(
    body: List<AnnotatedString>,
    fontSize: Int,
    fontFamily: FontFamily,
    imageLoader: ImageLoader,
    background: Boolean,
    renderMath: Boolean,
    darkTheme: Boolean,
    dataSaver: Boolean,
    onLinkClick: (String) -> Unit,
    onGalleryImageClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dpi = LocalDensity.current.density

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        body.forEach {
            if (it.startsWith("[[File:")) {
                if (!dataSaver) {
                    ImageWithCaption(
                        text = it.toString(),
                        fontSize = fontSize,
                        imageLoader = imageLoader,
                        onLinkClick = onLinkClick,
                        onClick = onGalleryImageClick,
                        darkTheme = darkTheme,
                        background = background
                    )
                }
            } else if (it.startsWith("<gallery")) {
                if (!dataSaver) {
                    Gallery(
                        text = it.toString(),
                        fontSize = fontSize,
                        imageLoader = imageLoader,
                        onClick = onGalleryImageClick,
                        onLinkClick = onLinkClick,
                        background = background
                    )
                }
            } else if (it.startsWith("<math")) {
                if (renderMath) {
                    EquationImage(
                        context = context,
                        dpi = dpi,
                        latex = remember { it.toString().substringAfter('>') },
                        fontSize = fontSize,
                        darkTheme = darkTheme
                    )
                } else {
                    Text(
                        text = LaTeX2Unicode.convert(it.toString())
                            .replace(' ', nbsp).substringAfter('>'),
                        fontFamily = FontFamily.Serif,
                        fontSize = (fontSize + 4).sp,
                        lineHeight = (24 * (fontSize / 16.0) + 4).toInt().sp,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                    )
                }
            } else if (it.startsWith("{|")) {
                AsyncWikitable(
                    text = it.toString(),
                    fontSize = fontSize,
                    onLinkClick = onLinkClick
                )
            } else {
                Text(
                    text = it,
                    style = typography.bodyLarge.copy(hyphens = Hyphens.Auto),
                    fontSize = fontSize.sp,
                    fontFamily = fontFamily,
                    lineHeight = (24 * (fontSize / 16.0)).toInt().sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }
}
