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
    table.split("|-").forEachIndexed { i, it ->
        if (i != 0) {
            val inlineSeparator =
                if (it.trim(' ', '\n').startsWith('!')) it.contains("!!") else it.contains("||")
            val row = mutableListOf<AnnotatedString>()
            val separator = if (inlineSeparator) {
                if (i == 1) "!!"
                else "||"
            } else "\n"
            it.trim('\n').split(separator).forEach {
                val curr = it.trim()
                if (!inlineSeparator) {
                    if (curr.startsWith('|')) {
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
                    } else if (curr.startsWith('!')) {
                        if (curr.substringBefore('|').contains("=\"")) {
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
                        } else row.add(
                            curr.substringAfter('!')
                                .toWikitextAnnotatedString(
                                    colorScheme,
                                    typography,
                                    loadPage,
                                    fontSize
                                )
                        )
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
            if (out.isEmpty() || row.size == out.getOrNull(0)?.size)
                out.add(row.toList())
        }
    }
    return out
}