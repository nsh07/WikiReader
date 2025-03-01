package org.nsh07.wikireader.ui.shimmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.nsh07.wikireader.ui.theme.WikiReaderTheme

@Composable
fun FeedLoader(brush: Brush, insets: PaddingValues) {
    val xl = MaterialTheme.shapes.extraLarge
    val l = MaterialTheme.shapes.large
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(
                rememberScrollState()
            )
    ) {
        Spacer(
            Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
                .size(256.dp, 56.dp)
                .clip(xl)
                .background(brush)
        )
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(256.dp)
                .padding(horizontal = 16.dp)
                .clip(l)
                .background(brush)
        )
        Spacer(
            Modifier
                .padding(horizontal = 16.dp)
                .size(200.dp, 52.dp)
                .clip(xl)
                .background(brush)
        )
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(horizontal = 16.dp)
                .clip(l)
                .background(brush)
        )
        Spacer(
            Modifier
                .padding(horizontal = 16.dp)
                .size(200.dp, 52.dp)
                .clip(xl)
                .background(brush)
        )
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(256.dp)
                .padding(horizontal = 16.dp)
                .clip(l)
                .background(brush)
        )
        for (i in 0..1) {
            Spacer(
                Modifier
                    .padding(horizontal = 16.dp)
                    .size(200.dp, 52.dp)
                    .clip(xl)
                    .background(brush)
            )
            Row {
                Spacer(
                    Modifier
                        .weight(8f)
                        .height(256.dp)
                        .padding(start = 16.dp)
                        .padding(end = 8.dp)
                        .clip(xl)
                        .background(brush)
                )
                Spacer(
                    Modifier
                        .weight(2f)
                        .height(256.dp)
                        .padding(end = 16.dp)
                        .clip(xl)
                        .background(brush)
                )
            }
        }
        Spacer(Modifier.height(insets.calculateBottomPadding() + 152.dp))
    }
}

@Preview
@Composable
fun FeedShimmerPreview() {
    WikiReaderTheme {
        Surface {
            AnimatedShimmer {
                FeedLoader(it, PaddingValues(bottom = 16.dp))
            }
        }
    }
}