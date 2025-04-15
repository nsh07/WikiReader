package org.nsh07.wikireader.parser

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.ui.text.AnnotatedString

fun parseWikitable(
    table: String, colorScheme: ColorScheme,
    typography: Typography,
    loadPage: (String) -> Unit,
    fontSize: Int
): List<List<AnnotatedString>> {
    val out = mutableListOf<List<AnnotatedString>>()
    val rowSpan = mutableMapOf<Int, Pair<Int, String>>()

    table.split("\\|-.*\n".toRegex()).forEachIndexed { i, it ->
        if (i != 0) {
            val inlineSeparator =
                if (it.trim(' ', '\n').startsWith('!')) it.contains("!!") else it.contains("||")
            val row = mutableListOf<AnnotatedString>()
            val separator = if (inlineSeparator) {
                if (it.trim(' ', '\n').startsWith('!')) "!!"
                else "||"
            } else "\n"
            it.trim('\n').split(separator).forEachIndexed { i, it ->
                val curr = it.trim()
                if (rowSpan[i] != null) {
                    if (rowSpan[i]!!.first - 1 > 0) {
                        row.add(AnnotatedString(""))
                        rowSpan[i] = Pair(rowSpan[i]!!.first - 1, rowSpan[i]!!.second)
                    } else
                        rowSpan.remove(i)
                }
                if (!inlineSeparator) {
                    if (curr.startsWith('|')) {
                        if (curr.contains("rowspan=")) {
                            rowSpan[i] = Pair(
                                curr.substringAfter("rowspan=").substringBefore('|')
                                    .substringBefore(' ').trim('"')
                                    .toIntOrNull() ?: 0,
                                curr.substringAfter('|').substringAfter('|').trim()
                            )
                            row.add(
                                curr.substringAfter('|')
                                    .substringAfter('|')
                                    .trim()
                                    .toWikitextAnnotatedString(
                                        colorScheme,
                                        typography,
                                        loadPage,
                                        fontSize
                                    )
                            )
                        } else if (curr.contains("colspan=")) {
                            val colSpan = curr.substringAfter("colspan=").substringBefore('|')
                                .substringBefore(' ').trim('"')
                                .toIntOrNull() ?: 1
                            val substr = curr.substringAfter('|').substringAfter('|').trim()
                            row.add(
                                substr.toWikitextAnnotatedString(
                                    colorScheme,
                                    typography,
                                    loadPage,
                                    fontSize
                                )
                            )
                            repeat(colSpan - 1) { row.add(AnnotatedString("")) }
                        } else if (curr.contains("=\"")) {
                            row.add(
                                curr.substringAfter('|')
                                    .substringAfter('|')
                                    .trim()
                                    .toWikitextAnnotatedString(
                                        colorScheme,
                                        typography,
                                        loadPage,
                                        fontSize
                                    )
                            )
                        } else {
                            row.add(
                                curr.substringAfter('|')
                                    .trim()
                                    .toWikitextAnnotatedString(
                                        colorScheme,
                                        typography,
                                        loadPage,
                                        fontSize
                                    )
                            )
                        }
                    } else if (curr.startsWith('!')) {
                        if (curr.contains("rowspan=")) {
                            rowSpan[i] = Pair(
                                curr.substringAfter("rowspan=").substringBefore('|')
                                    .substringBefore(' ').trim('"')
                                    .toIntOrNull() ?: 0,
                                curr.substringAfter('!').substringAfter('|').trim()
                            )
                            row.add(
                                curr.substringAfter('!')
                                    .substringAfter('|')
                                    .trim()
                                    .toWikitextAnnotatedString(
                                        colorScheme,
                                        typography,
                                        loadPage,
                                        fontSize
                                    )
                            )
                        } else if (curr.contains("colspan=")) {
                            val colSpan =
                                curr.substringAfter("colspan=").substringBefore('|')
                                    .substringBefore(' ').trim('"')
                                    .toIntOrNull() ?: 1
                            val substr = curr.substringAfter('!').substringAfter('|').trim()
                            row.add(
                                substr.toWikitextAnnotatedString(
                                    colorScheme,
                                    typography,
                                    loadPage,
                                    fontSize
                                )
                            )
                            repeat(colSpan - 1) { row.add(AnnotatedString("")) }
                        } else if (curr.substringBefore('|').contains("=\"")) {
                            row.add(
                                curr.substringAfter('!')
                                    .substringAfter('|')
                                    .trim()
                                    .toWikitextAnnotatedString(
                                        colorScheme,
                                        typography,
                                        loadPage,
                                        fontSize
                                    )
                            )
                        } else {
                            row.add(
                                curr.substringAfter('!')
                                    .trim()
                                    .toWikitextAnnotatedString(
                                        colorScheme,
                                        typography,
                                        loadPage,
                                        fontSize
                                    )
                            )
                        }
                    } else {
                        if (row.lastIndex != -1)
                            row[row.lastIndex] =
                                row[row.lastIndex] + "\n$it".toWikitextAnnotatedString(
                                    colorScheme,
                                    typography,
                                    loadPage,
                                    fontSize
                                )
                    }
                } else {
                    if (curr.contains("rowspan=")) {
                        rowSpan[i] = Pair(
                            curr.substringAfter("rowspan=").substringBefore('|')
                                .substringBefore(' ').trim('"').toIntOrNull() ?: 0,
                            curr.trim('!', '|', ' ')
                        )
                    }
                    curr.split(separator).forEach {
                        if (it.contains('=') && it.contains('|'))
                            row.add(
                                curr.substringAfterLast('"').trim('!', '|', ' ').toWikitextAnnotatedString(
                                    colorScheme,
                                    typography,
                                    loadPage,
                                    fontSize
                                )
                            )
                        else
                            row.add(
                                curr.trim('!', '|', ' ').toWikitextAnnotatedString(
                                    colorScheme,
                                    typography,
                                    loadPage,
                                    fontSize
                                )
                            )
                    }
                }
            }
            if (out.isEmpty() || row.size == out.getOrNull(0)?.size)
                out.add(row.toList())
        }
    }
    return out
}