package org.nsh07.wikireader.ui.homeScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.nsh07.wikireader.parser.parseInfobox
import org.nsh07.wikireader.ui.theme.CustomTopBarColors.cardColors

@Composable
fun AsyncInfobox(
    text: String,
    fontSize: Int,
    onLinkClick: (String) -> Unit,
    showRef: (String) -> Unit
) {
    val colorScheme = colorScheme
    val typography = typography
    val scope = rememberCoroutineScope()
    var infobox by remember { mutableStateOf(emptyList<Pair<AnnotatedString, AnnotatedString>>()) }
    var title: AnnotatedString? by remember { mutableStateOf(AnnotatedString("Infobox")) }

    LaunchedEffect(text) {
        scope.launch(Dispatchers.IO) {
            infobox = parseInfobox(text, colorScheme, typography, onLinkClick, showRef, fontSize)
            title = infobox.firstOrNull()?.second
        }
    }

    Card(
        colors = cardColors,
        shape = shapes.large,
        modifier = Modifier
            .widthIn(max = 512.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (title != null)
                Text(
                    title ?: AnnotatedString(""),
                    style = typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            Table(
                rows = infobox.size,
                columns = 2
            ) { row, column ->
                if (column == 0) {
                    Text(
                        infobox[row].first,
                        fontSize = fontSize.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = (24 * (fontSize / 16.0)).toInt().sp,
                        modifier = Modifier
                            .padding(8.dp)
                            .widthIn(max = 256.dp)
                    )
                } else {
                    Text(
                        infobox[row].second,
                        fontSize = fontSize.sp,
                        lineHeight = (24 * (fontSize / 16.0)).toInt().sp,
                        modifier = Modifier
                            .padding(8.dp)
                            .widthIn(max = 256.dp)
                    )
                }
            }
        }
    }
}