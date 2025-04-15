package org.nsh07.wikireader.ui.homeScreen

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.nsh07.wikireader.parser.parseWikitable
import kotlin.math.max

@Composable
fun Wikitable(
    text: String,
    fontSize: Int,
    loadPage: (String) -> Unit,
) {
    val rows = parseWikitable(text, colorScheme, typography, loadPage, fontSize)
    Box(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .border(Dp.Hairline, color = colorScheme.onSurface)
    ) {
        Table(
            rows.size,
            rows.getOrNull(0)?.size ?: 0,
            verticalAlignment = Alignment.CenterVertically
        ) { row, column ->
            if (row == 0 || column == 0)
                Text(
                    rows[row][column],
                    modifier = Modifier
                        .padding(8.dp)
                        .widthIn(max = 256.dp),
                    fontWeight = FontWeight.Bold
                )
            else
                Text(
                    rows[row][column],
                    modifier = Modifier
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .widthIn(max = 256.dp)
                )
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