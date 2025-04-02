package org.nsh07.wikireader.parser

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.github.tomtung.latex2unicode.LaTeX2Unicode
import org.nsh07.wikireader.data.langCodeToName
import kotlin.math.min
import kotlin.text.Typography.bullet
import kotlin.text.Typography.nbsp

/**
 * Converts Wikitext source code into an [AnnotatedString] that can be rendered by [androidx.compose.material3.Text]
 */
fun String.toWikitextAnnotatedString(
    colorScheme: ColorScheme,
    typography: Typography,
    performSearch: (String) -> Unit,
    fontSize: Int,
    newLine: Boolean = true,
    inIndentCode: Boolean = false
): AnnotatedString {
    // TODO: Implement more Wikitext features
    val input = this
    var i = 0
    return buildAnnotatedString {
        while (i < input.length) {
            when (input[i]) {
                ' ' ->
                    if ((getOrNull(i - 1) == '\n' || i == 0) && !inIndentCode) {
                        val curr = substring(i + 1).substringBefore('\n')
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                color = colorScheme.onSurfaceVariant
                            )
                        ) {
                            append(
                                curr.toWikitextAnnotatedString(
                                    colorScheme,
                                    typography,
                                    performSearch,
                                    fontSize,
                                    inIndentCode = true
                                )
                            )
                        }
                        i += curr.length
                    } else append(input[i])

                '<' -> {
                    val currSubstring = input.substring(i)
                    if (currSubstring.startsWith("<code>")) {
                        val curr = currSubstring.substringBefore("</code>").substringAfter('>')
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                color = colorScheme.onSurfaceVariant
                            )
                        ) {
                            append(
                                curr.toWikitextAnnotatedString(
                                    colorScheme,
                                    typography,
                                    performSearch,
                                    fontSize,
                                    newLine = false
                                )
                            )
                        }
                        i += 6 + curr.length + 6
                    } else if (currSubstring.startsWith("<sub>")) {
                        val curr = currSubstring.substringBefore("</sub>").substringAfter('>')
                        withStyle(
                            SpanStyle(
                                baselineShift = BaselineShift.Subscript
                            )
                        ) {
                            append(
                                curr.toWikitextAnnotatedString(
                                    colorScheme,
                                    typography,
                                    performSearch,
                                    fontSize,
                                    newLine = false
                                )
                            )
                        }
                        i += 5 + curr.length + 5
                    } else if (currSubstring.startsWith("<sup>")) {
                        val curr = currSubstring.substringBefore("</sup>").substringAfter('>')
                        withStyle(
                            SpanStyle(
                                baselineShift = BaselineShift.Superscript
                            )
                        ) {
                            append(
                                curr.toWikitextAnnotatedString(
                                    colorScheme,
                                    typography,
                                    performSearch,
                                    fontSize,
                                    newLine = false
                                )
                            )
                        }
                        i += 5 + curr.length + 5
                    } else if (currSubstring.startsWith("<math>")) {
                        val curr = currSubstring.substringBefore("</math>").substringAfter('>')
                        withStyle(SpanStyle(fontFamily = FontFamily.Serif)) {
                            append(LaTeX2Unicode.convert(curr).replace(' ', nbsp))
                        }
                        i += 6 + curr.length + 6
                    } else if (currSubstring.startsWith("<math display")) {
                        val curr = currSubstring.substringBefore("</math>").substringAfter('>')
                        append("\t\t")
                        withStyle(SpanStyle(fontFamily = FontFamily.Serif)) {
                            append(LaTeX2Unicode.convert(curr).replace(' ', nbsp))
                        }
                        i += currSubstring.substringBefore('>').length + curr.length + "</math>".length
                    } else if (currSubstring.startsWith("<blockquote")) {
                        val curr =
                            currSubstring.substringBefore("</blockquote>").substringAfter('>')
                        withStyle(
                            ParagraphStyle(textIndent = TextIndent(restLine = 12.sp))
                        ) {
                            append("\t ")
                            append(
                                curr.toWikitextAnnotatedString(
                                    colorScheme,
                                    typography,
                                    performSearch,
                                    fontSize
                                )
                            )
                        }
                        i += 12 + curr.length + 13
                    } else {
                        append(input[i])
                    }
                }

                '{' ->
                    if (input.getOrNull(i + 1) == '{') {
                        val currSubstring =
                            substringMatchingParen('{', '}', i).substringBeforeLast("}}")
                        if (currSubstring.startsWith("{{mono", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|')
                            withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                                append(
                                    curr.toWikitextAnnotatedString(
                                        colorScheme,
                                        typography,
                                        performSearch,
                                        fontSize
                                    )
                                )
                            }
                        } else if (currSubstring.startsWith("{{math", ignoreCase = true) ||
                            currSubstring.startsWith("{{mvar", ignoreCase = true)
                        ) {
                            val curr = currSubstring.substringAfter('|')
                            withStyle(SpanStyle(fontFamily = FontFamily.Serif)) {
                                append(
                                    curr.replace(' ', nbsp).toWikitextAnnotatedString(
                                        colorScheme,
                                        typography,
                                        performSearch,
                                        fontSize
                                    )
                                )
                            }
                        } else if (currSubstring.startsWith("{{main", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|')
                            val splitList = curr.split('|')
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                append("Main article")
                                if (splitList.size > 1) append("s: ")
                                else append(": ")
                                splitList.forEachIndexed { index, it ->
                                    append(
                                        "[[$it]]".toWikitextAnnotatedString(
                                            colorScheme,
                                            typography,
                                            performSearch,
                                            fontSize
                                        )
                                    )
                                    if (index == splitList.size - 2 && splitList.size > 1) append(", and ")
                                    else if (index < splitList.size - 1) append(", ")
                                }
                            }
                        } else if (currSubstring.startsWith("{{see also", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|')
                            val splitList = curr.split('|')
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                append("See also: ")
                                splitList.forEachIndexed { index, it ->
                                    append(
                                        "[[$it]]".toWikitextAnnotatedString(
                                            colorScheme,
                                            typography,
                                            performSearch,
                                            fontSize
                                        )
                                    )
                                    if (index == splitList.size - 2 && splitList.size > 1) append(", and ")
                                    else if (index < splitList.size - 1) append(", ")
                                }
                            }
                        } else if (currSubstring.startsWith("{{IPAc-en", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|')
                            append("/${curr.replace("|", "").replace(' ', nbsp)}/")
                        } else if (currSubstring.startsWith("{{lang|")) {
                            val curr = currSubstring.substringAfter('|')
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                append(
                                    curr.substringAfter('|').substringBefore('|')
                                        .toWikitextAnnotatedString(
                                            colorScheme,
                                            typography,
                                            performSearch,
                                            fontSize
                                        )
                                )
                            }
                        } else if (currSubstring.startsWith("{{IPA", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|')
                            withStyle(SpanStyle(fontSize = (fontSize - 2).sp)) {
                                append("${langCodeToName(curr.substringBefore('|'))}: ")
                            }
                            append(
                                "[${
                                    curr.substringAfter('|').substringBefore("|")
                                        .replace("|", "").replace(' ', nbsp)
                                }]"
                            )
                        } else if (currSubstring.startsWith("{{respell", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|')
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                append(curr.replace('|', '-'))
                            }
                        } else if (currSubstring.startsWith("{{BCE", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|').substringBefore('|')
                            append(curr)
                            append(nbsp)
                            append("BCE")
                        } else if (currSubstring.startsWith("{{blockquote", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|')
                            withStyle(
                                ParagraphStyle(textIndent = TextIndent(restLine = 12.sp))
                            ) {
                                append("\t ")
                                append(
                                    curr.toWikitextAnnotatedString(
                                        colorScheme,
                                        typography,
                                        performSearch,
                                        fontSize
                                    )
                                )
                            }
                        } else if (currSubstring.startsWith("{{further", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|')
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                append(
                                    "Further reading: [[${
                                        curr.substringBefore('|').substringBefore('#')
                                    }]]\n".toWikitextAnnotatedString(
                                        colorScheme,
                                        typography,
                                        performSearch,
                                        fontSize
                                    )
                                )
                            }
                        } else if (currSubstring.startsWith("{{as of", ignoreCase = true)) {
                            // TODO: Complete this
                            val curr = currSubstring.substringAfter("{{")
                            append(curr.substringBefore('|'))
                            append(' ')
                            var date = ""
                            curr.substringAfter('|').split('|').forEach {
                                if (it.toIntOrNull() != null) {
                                    date += it
                                    date += '/'
                                }
                            }
                            append(date.trim('/'))
                        } else if (currSubstring.startsWith("{{Nihongo", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|')
                            append(
                                curr.toWikitextAnnotatedString(
                                    colorScheme,
                                    typography,
                                    performSearch,
                                    fontSize
                                )
                            )
                        }
                        i += currSubstring.length + 1
                    } else append(input[i])

                '&' ->
                    if (input.substring(i).substringBefore(';') == "&nbsp") {
                        append(nbsp)
                        i += 5
                    } else append(input[i])

                '*' ->
                    if ((i == 0 || input[i - 1] == '\n') && newLine) {
                        val bulletCount =
                            input.substring(i).substringBefore(' ').count { it == '*' }
                        val curr =
                            if (input[i + bulletCount] == ' ') {
                                input.substring(i + bulletCount + 1).substringBefore('\n')
                            } else input.substring(i + bulletCount).substringBefore('\n')
                        withStyle(
                            ParagraphStyle(textIndent = TextIndent(restLine = (12 * bulletCount).sp))
                        ) {
                            append("\t\t".repeat(bulletCount - 1))
                            append(bullet)
                            append("\t\t")
                            append(
                                curr.toWikitextAnnotatedString(
                                    colorScheme,
                                    typography,
                                    performSearch,
                                    fontSize
                                )
                            )
                        }
                        i += bulletCount + curr.length + 1
                    } else append(input[i])

                '=' ->
                    if (input.getOrNull(i + 1) == '=' && input.getOrNull(i + 2) == '=') {
                        if (input.getOrNull(i + 3) == '=') { // h4
                            val curr = input.substring(i + 4).substringBefore("====")
                            withStyle(typography.titleLarge.toSpanStyle()) {
                                append(
                                    "${curr.trim()}\n".toWikitextAnnotatedString(
                                        colorScheme,
                                        typography,
                                        performSearch,
                                        fontSize
                                    )
                                )
                            }
                            i += 3 + curr.length + 4
                        } else { // h3
                            val curr = input.substring(i + 3).substringBefore("===")
                            withStyle(typography.headlineSmall.toSpanStyle()) {
                                append(
                                    "${curr.trim()}\n".toWikitextAnnotatedString(
                                        colorScheme,
                                        typography,
                                        performSearch,
                                        fontSize
                                    )
                                )
                            }
                            i += 2 + curr.length + 3
                        }
                    } else append(input[i])

                '\'' ->
                    if (input.getOrNull(i + 1) == '\'' && input.getOrNull(i + 2) == '\'') {
                        val subs = input.substring(i + 3)
                        val curr = subs.substring(
                            0,
                            min(
                                subs.length,
                                ("'''(?!')".toRegex().find(subs)?.range?.start ?: subs.length) + 2
                            )
                        )
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(
                                curr.toWikitextAnnotatedString(
                                    colorScheme,
                                    typography,
                                    performSearch,
                                    fontSize
                                )
                            )
                        }
                        i += curr.length + 3
                    } else if (input.getOrNull(i + 1) == '\'') {
                        val curr = input.substring(i + 2).substringBefore("''")
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(
                                curr.toWikitextAnnotatedString(
                                    colorScheme,
                                    typography,
                                    performSearch,
                                    fontSize
                                )
                            )
                        }
                        i += 1 + curr.length + 2
                    } else append(input[i])

                '[' ->
                    if (input.getOrNull(i + 1) == '[') {
                        val curr = input.substring(i + 2).substringBefore("]]")
                        if (!curr.startsWith("File:", ignoreCase = true)) {
                            withLink(
                                LinkAnnotation.Url(
                                    "",
                                    TextLinkStyles(
                                        SpanStyle(color = colorScheme.primary)
                                    )
                                ) {
                                    performSearch(curr.substringBefore('|').substringBefore('#'))
                                }
                            ) { append(curr.substringAfter('|')) }
                            i += 1 + curr.length + 2
                        } else i += substringMatchingParen('[', ']', i).length
                    } else append(input[i])

                else -> append(input[i])
            }
            i++
        }
    }
}

fun String.substringMatchingParen(
    openingParen: Char,
    closingParen: Char,
    startIndex: Int = 0
): String {
    var i = startIndex
    var stack = 0
    while (i < length) {
        if (this[i] == openingParen) stack++
        else if (this[i] == closingParen) stack--

        if (stack == 0) break

        i++
    }

    return if (i < length) this.substring(startIndex, i + 1)
    else this
}