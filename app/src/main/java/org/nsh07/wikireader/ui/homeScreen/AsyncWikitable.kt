package org.nsh07.wikireader.ui.homeScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.nsh07.wikireader.parser.parseWikitable
import kotlin.math.max

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AsyncWikitable(
    text: String,
    fontSize: Int,
    onLinkClick: (String) -> Unit
) {
    val colorScheme = colorScheme
    val typography = typography
    val coroutineScope = rememberCoroutineScope()

    var rows by remember {
        mutableStateOf(
            Pair(
                AnnotatedString(""),
                emptyList<List<AnnotatedString>>()
            )
        )
    }
    var expanded by remember { mutableStateOf(false) }
    val tableTitle =
        buildAnnotatedString { withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Table") } }

    LaunchedEffect(text) {
        coroutineScope.launch(Dispatchers.IO) {
            rows = parseWikitable(text, colorScheme, typography, onLinkClick, fontSize)
        }
    }

    OutlinedCard(
        onClick = { expanded = !expanded },
        border = BorderStroke(1.dp, colorScheme.outline),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                shapes = IconButtonDefaults.shapes(),
                onClick = { expanded = !expanded },
                modifier = Modifier.padding(8.dp)
            ) {
                if (!expanded)
                    Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null)
                else
                    Icon(Icons.Outlined.KeyboardArrowUp, contentDescription = null)
            }
            Text(
                if (rows.first != AnnotatedString("")) rows.first
                else tableTitle,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp)
                    .padding(end = 16.dp)
            )
        }
    }

    AnimatedVisibility(
        expanded,
        enter = expandVertically(expandFrom = Alignment.CenterVertically) + fadeIn(),
        exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .border(1.dp, color = colorScheme.outline, shape = shapes.large)
        ) {
            Table(
                rows.second.size,
                rows.second.getOrNull(0)?.size ?: 0,
                verticalAlignment = Alignment.CenterVertically
            ) { row, column ->
                Text(
                    rows.second[row][column],
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


@Composable
fun Table(
    rows: Int,
    columns: Int,
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    cell: @Composable (row: Int, column: Int) -> Unit
) {
    val measurePolicy =
        remember(rows, columns, verticalAlignment, horizontalAlignment) {
            TableMeasurePolicy(
                rows,
                columns,
                verticalAlignment,
                horizontalAlignment
            )
        }
    Layout(content = {
        repeat(rows) { row ->
            repeat(columns) { column ->
                cell(row, column)
            }
        }
    }, measurePolicy = measurePolicy, modifier = modifier)
}

private class TableMeasurePolicy(
    private val rows: Int,
    private val columns: Int,
    private val verticalAlignment: Alignment.Vertical,
    private val horizontalAlignment: Alignment.Horizontal
) : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints
    ): MeasureResult {
        val measured = measurables.map { it.measure(constraints) }

        val columnWidths = List(columns) { column ->
            var maxCellWidth = 0
            repeat(rows) { row ->
                val i = (row * columns) + column
                val placeable = measured[i]
                maxCellWidth = max(placeable.width, maxCellWidth)
            }
            maxCellWidth
        }

        val rowHeights = List(rows) { row ->
            var maxCellHeight = 0
            repeat(columns) { column ->
                val i = (row * columns) + column
                val placeable = measured[i]
                maxCellHeight = max(placeable.height, maxCellHeight)
            }
            maxCellHeight
        }

        val tableWidth = columnWidths.sum()
        val tableHeight = rowHeights.sum()

        return layout(tableWidth, tableHeight) {
            var y = 0
            repeat(rows) { row ->
                var x = 0
                val rowHeight = rowHeights[row]
                repeat(columns) { column ->
                    val i = row * columns + column
                    val placeable = measured[i]
                    val columnWidth = columnWidths[column]
                    val yOffset = verticalAlignment.align(placeable.height, rowHeight)
                    val xOffset =
                        horizontalAlignment.align(placeable.width, columnWidth, layoutDirection)
                    placeable.placeRelative(x + xOffset, y + yOffset)

                    x += columnWidth
                }
                y += rowHeight
            }
        }
    }
}