package org.nsh07.wikireader.data

import android.util.Log
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
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
import kotlin.math.min
import kotlin.text.Typography.bullet
import kotlin.text.Typography.nbsp

/**
 * Extension function for [String] to convert it to a [Color]
 *
 * The base string must be of the format produced by [Color.toString],
 * i.e, the color black with 100% opacity in sRGB would be represented by:
 *
 *      Color(0.0, 0.0, 0.0, 1.0, sRGB IEC61966-2.1)
 */
fun String.toColor(): Color {
    // Sample string: Color(0.0, 0.0, 0.0, 1.0, sRGB IEC61966-2.1)
    Log.d("Color", this)
    val comma1 = this.indexOf(',')
    val comma2 = this.indexOf(',', comma1 + 1)
    val comma3 = this.indexOf(',', comma2 + 1)
    val comma4 = this.indexOf(',', comma3 + 1)

    val r = this.substringAfter('(').substringBefore(',').toFloat()
    val g = this.slice(comma1 + 1..comma2 - 1).toFloat()
    val b = this.slice(comma2 + 1..comma3 - 1).toFloat()
    val a = this.slice(comma3 + 1..comma4 - 1).toFloat()
    return Color(r, g, b, a)
}

fun String.toWikitextAnnotatedString(
    colorScheme: ColorScheme,
    typography: Typography,
    performSearch: (String) -> Unit,
    fontSize: Int,
    newLine: Boolean = true,
    inIndentCode: Boolean = false
): AnnotatedString {
    // TODO: Complete this
    // TODO: Add <math> using inlinecontent
    // TODO: Implement more Wikitext features
    val input = this

    var i = 0
    return buildAnnotatedString {
        while (i < input.length) {
            when (input[i]) {
                ' ' ->
                    if ((input.getOrNull(i - 1) == '\n' || i == 0) && !inIndentCode) {
                        val curr = input.substring(i + 1).substringBefore('\n')
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
                        append(LaTeX2Unicode.convert(curr))
                        i += 6 + curr.length + 6
                    } else if (currSubstring.startsWith("<math display")) {
                        val curr = currSubstring.substringAfter('>').substringBefore("</math>")
                        append("\t\t")
                        append(LaTeX2Unicode.convert(curr))
                        i += currSubstring.substringBefore('>').length + curr.length + "</math>".length
                    } else {
                        append(input[i])
                    }
                }

                '{' ->
                    if (input.getOrNull(i + 1) == '{') {
                        val currSubstring = input.substring(i).substringBefore("}}")
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
                            i += 6 + curr.length + 2
                        } else if (currSubstring.startsWith("{{math", ignoreCase = true) ||
                            currSubstring.startsWith("{{mvar", ignoreCase = true)
                        ) {
                            val curr = currSubstring.substringAfter('|')
                            withStyle(SpanStyle(fontFamily = FontFamily.Serif)) {
                                append(
                                    curr.toWikitextAnnotatedString(
                                        colorScheme,
                                        typography,
                                        performSearch,
                                        fontSize
                                    )
                                )
                            }
                            i += 6 + curr.length + 2
                        } else if (currSubstring.startsWith("{{main", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|')
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                append(
                                    "Main article: [[${curr.substringBefore('#')}]]\n".toWikitextAnnotatedString(
                                        colorScheme,
                                        typography,
                                        performSearch,
                                        fontSize
                                    )
                                )
                            }
                            i += 6 + curr.length + 2
                        } else if (currSubstring.startsWith("{{IPAc-en", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|')
                            append("/${curr.replace("|", "").replace(' ', nbsp)}/")
                            i += 9 + curr.length + 2
                        } else if (currSubstring.startsWith("{{IPA", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|')
                            withStyle(SpanStyle(fontSize = (fontSize - 2).sp)) {
                                append("${langCodeToName(curr.substringBefore('|'))}: ")
                            }
                            append(
                                "[${
                                    curr.substringAfter('|').substringBefore("|lang")
                                        .replace("|", "").replace(' ', nbsp)
                                }]"
                            )
                            i += 5 + curr.length + 2
                        } else if (currSubstring.startsWith("{{respell", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|')
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                append(curr.replace('|', '-'))
                            }
                            i += 9 + curr.length + 2
                        } else if (currSubstring.startsWith("{{blockquote", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|')
                            withStyle(
                                ParagraphStyle(textIndent = TextIndent(restLine = 12.sp))
                            ) {
                                append("\t ")
                                append(curr)
                            }
                            i += 12 + curr.length + 2
                        } else if (currSubstring.startsWith("{{NoteTag")) {
                            i += 2 + currSubstring.length + 2
                        }
                        else append(input[i])
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
                        } else i += 3 + curr.length + 2
                    } else append(input[i])

                else -> append(input[i])
            }
            i++
        }
    }
}

fun cleanUpWikitext(text: String): String {
    var bodyText = text
        .replace(
            "<!--.+?-->|<ref[^/]*?>.+?</ref>|<ref.*?/>".toRegex(RegexOption.DOT_MATCHES_ALL),
            ""
        ) // Remove references and comments
    while (bodyText.contains(
            "\\{\\{(?![mM]ono)(?![Mm]ath)(?![Mm]var)(?![Mm]ain)(?!IPA)(?!respell)(?![Bb]lockquote)[^{}]+\\}\\}".toRegex(
                RegexOption.DOT_MATCHES_ALL
            )
        )
    ) {
        bodyText =
            bodyText.replace(
                "\\{\\{(?![mM]ono)(?![Mm]ath)(?![Mm]var)(?![Mm]ain)(?!IPA)(?!respell)(?![Bb]lockquote)[^{}]+\\}\\}".toRegex(
                    RegexOption.DOT_MATCHES_ALL
                ),
                ""
            )
    }
//    bodyText = bodyText.replace(" {2,}".toRegex(), " ") // Remove double spaces
    bodyText = bodyText.replace("\n{3,}".toRegex(), "\n") // Remove empty newlines
    bodyText = bodyText.replace("<var>|</var>".toRegex(), "''")
    return bodyText
}


/**
 * Function to parse a string returned by the Wikipedia API and convert it into a [List] of [String]s
 *
 * @param text The [String] returned by the Wikipedia API
 *
 * @return List of the format:
 *
 *      {intro text, heading1, body1, heading2, body2, ...}
 *
 * Note that subheadings are *not* parsed, and are left as is (surrounded by three '=' signs on
 * either side)
 */
fun parseSections(text: String): List<String> {
    val out = text.split("\n==(?!=)|(?<!=)==[\n ]|(?<!=)==(?=<!--)".toRegex()).toMutableList()

    for (i in out.lastIndex downTo 1) {
        val curr = out[i].trim()
        if (curr in listOf("References", "External links", "Gallery", "Footnotes")) {
            out.removeAt(i + 1)
            out.removeAt(i)
        } else if (i + 1 <= out.lastIndex) {
            if (out[i + 1].trim('\n') == "") {
                out.removeAt(i + 1)
                out.removeAt(i)
            }
        }
    }

    return out
}

fun bytesToHumanReadableSize(bytes: Double) = when {
    bytes >= 1 shl 30 -> "%.1f GB".format(bytes / (1 shl 30))
    bytes >= 1 shl 20 -> "%.1f MB".format(bytes / (1 shl 20))
    bytes >= 1 shl 10 -> "%.0f kB".format(bytes / (1 shl 10))
    else -> "$bytes bytes"
}

fun langCodeToName(langCode: String): String {
    try {
        return LanguageData.langNames[LanguageData.langCodes.binarySearch(langCode)]
    } catch (_: Exception) {
        Log.e("Language", "Unknown Language: $langCode")
        return langCode
    }
}

fun langCodeToWikiName(langCode: String): String {
    try {
        return LanguageData.wikipediaNames[LanguageData.langCodes.binarySearch(langCode)]
    } catch (_: Exception) {
        Log.e("Language", "Unknown Language: $langCode")
        return langCode
    }
}