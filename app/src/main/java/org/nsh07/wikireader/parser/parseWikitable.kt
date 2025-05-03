package org.nsh07.wikireader.parser

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.ui.text.AnnotatedString
import kotlin.math.max

fun parseWikitable(
    table: String, colorScheme: ColorScheme,
    typography: Typography,
    loadPage: (String) -> Unit,
    fontSize: Int
): Pair<AnnotatedString, List<List<AnnotatedString>>> {
    val rows = mutableListOf<MutableList<AnnotatedString>>()
    var caption = AnnotatedString("")
    val lines = table.lines().map { it.trim() }.filter { it.isNotEmpty() }
    val styling = "class=|align=|scope=|style=|rowspan=|colspan=|width=|nowrap".toRegex()

    var currentRow = mutableListOf<AnnotatedString>()
    val rowSpan = mutableMapOf<Int, Int>()
    var maxSize = 0
    var insideTable = false

    for (line in lines) {
        val currSize = currentRow.size
        if (rowSpan[currSize] != null) {
            if (rowSpan[currSize]!! - 1 > 0) {
                currentRow.add(AnnotatedString(""))
                rowSpan[currSize] = rowSpan[currSize]!! - 1
            } else
                rowSpan.remove(currSize)
        }
        when {
            line.startsWith("{|") -> {
                // Table begin
                insideTable = true
            }

            line.startsWith("|+") -> {
                // Table caption
                caption = "'''${line.removePrefix("|+").trim()}'''".toWikitextAnnotatedString(
                    colorScheme,
                    typography,
                    loadPage,
                    fontSize
                )
            }

            line.startsWith("|}") -> {
                // Table end
                if (currentRow.isNotEmpty()) {
                    rows.add(currentRow)
                    currentRow = mutableListOf()
                }
                insideTable = false
            }

            line.startsWith("|-") -> {
                // Row delimiter
                if (currentRow.isNotEmpty()) {
                    rows.add(currentRow)
                    maxSize = max(currentRow.size, maxSize)
                    currentRow = mutableListOf()
                }
            }

            line.startsWith("!") -> {
                // Header cells, can be split by '!!' or on multiple lines
                val content = line.removePrefix("!")
                val cells = mutableListOf<AnnotatedString>()
                if ("!!" in line) {
                    content.split("!!").forEach {
                        var colspan = 1
                        if (it.contains("colspan")) {
                            colspan = it.substringAfter("colspan=").substringBefore('|')
                                .substringBefore(' ').trim('"')
                                .toIntOrNull() ?: 1
                        }
                        val curr =
                            if (it.contains(styling)) {
                                it.substringAfter('|')
                            } else it

                        if (it.contains("rowspan")) { // Read rowspan count of cell
                            rowSpan[currentRow.size] =
                                it.substringAfter("rowspan=").substringBefore('|')
                                    .substringBefore(' ').trim('"')
                                    .toIntOrNull() ?: 1
                        }

                        cells.add(
                            ("'''$curr'''").trim()
                                .toWikitextAnnotatedString(
                                    colorScheme,
                                    typography,
                                    loadPage,
                                    fontSize
                                )
                        )
                        repeat(colspan - 1) {
                            cells.add(AnnotatedString(""))
                        }
                    }
                } else {
                    var colspan = 1
                    if (content.contains("colspan")) {
                        colspan = content.substringAfter("colspan=").substringBefore('|')
                            .substringBefore(' ').trim('"')
                            .toIntOrNull() ?: 1
                    }
                    val curr =
                        if (content.contains(styling)) {
                            content.substringAfter('|')
                        } else content

                    if (content.contains("rowspan")) { // Read rowspan count of cell
                        rowSpan[currentRow.size] =
                            content.substringAfter("rowspan=").substringBefore('|')
                                .substringBefore(' ').trim('"')
                                .toIntOrNull() ?: 1
                    }

                    cells.add(
                        ("'''$curr'''").trim()
                            .toWikitextAnnotatedString(
                                colorScheme,
                                typography,
                                loadPage,
                                fontSize
                            )
                    )
                    repeat(colspan - 1) {
                        cells.add(AnnotatedString(""))
                    }
                }
                currentRow.addAll(cells)
            }

            line.startsWith("|") -> {
                // Data cells, can be split by '||' or on multiple lines
                val content = line.removePrefix("|")
                val cells = mutableListOf<AnnotatedString>()
                if ("||" in content) {
                    content.split("||").forEach {
                        var colspan = 1
                        if (it.contains("colspan")) {
                            colspan = it.substringAfter("colspan=").substringBefore('|')
                                .substringBefore(' ').trim('"')
                                .toIntOrNull() ?: 1
                        }
                        val curr =
                            if (it.contains(styling)) {
                                it.substringAfter('|')
                            } else it

                        if (it.contains("rowspan")) { // Read rowspan count of cell
                            rowSpan[currentRow.size] =
                                it.substringAfter("rowspan=").substringBefore('|')
                                    .substringBefore(' ').trim('"')
                                    .toIntOrNull() ?: 1
                        }

                        cells.add(
                            curr.trim()
                                .toWikitextAnnotatedString(
                                    colorScheme,
                                    typography,
                                    loadPage,
                                    fontSize
                                )
                        )
                        repeat(colspan - 1) {
                            cells.add(AnnotatedString(""))
                        }
                    }
                } else {
                    var colspan = 1
                    if (content.contains("colspan")) {
                        colspan = content.substringAfter("colspan=").substringBefore('|')
                            .substringBefore(' ').trim('"')
                            .toIntOrNull() ?: 1
                    }
                    val curr =
                        if (content.contains(styling)) {
                            content.substringAfter('|')
                        } else content

                    if (content.contains("rowspan")) { // Read rowspan count of cell
                        rowSpan[currentRow.size] =
                            content.substringAfter("rowspan=").substringBefore('|')
                                .substringBefore(' ').trim('"')
                                .toIntOrNull() ?: 1
                    }

                    cells.add(
                        curr.trim()
                            .toWikitextAnnotatedString(
                                colorScheme,
                                typography,
                                loadPage,
                                fontSize
                            )
                    )
                    repeat(colspan - 1) {
                        cells.add(AnnotatedString(""))
                    }
                }
                currentRow.addAll(cells)
            }

            else -> {
                // Possibly a continuation of a multi-line cell?
                if (insideTable) {
                    val lastIndex = currentRow.lastIndex
                    currentRow[lastIndex] += line.trim()
                        .toWikitextAnnotatedString(colorScheme, typography, loadPage, fontSize)
                }
            }
        }
    }

    if (currentRow.isNotEmpty()) {
        rows.add(currentRow)
    }

    for (i in 0 until rows.size) {
        // Sanity check to ensure all rows are the same size
        if (rows[i].size < maxSize) {
            repeat(maxSize - rows[i].size) {
                rows[i].add(AnnotatedString(""))
            }
        }
    }

    return Pair(caption, rows)
}