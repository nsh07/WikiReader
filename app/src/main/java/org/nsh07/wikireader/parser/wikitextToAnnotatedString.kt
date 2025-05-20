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
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.github.tomtung.latex2unicode.LaTeX2Unicode
import org.nsh07.wikireader.data.langCodeToName
import kotlin.math.min
import kotlin.text.Typography.bullet
import kotlin.text.Typography.nbsp
import kotlin.text.Typography.ndash

private const val MAGIC_SEP = "{{!}}"

/**
 * Converts Wikitext source code into an [AnnotatedString] that can be rendered by [androidx.compose.material3.Text]
 */
fun String.toWikitextAnnotatedString(
    colorScheme: ColorScheme,
    typography: Typography,
    loadPage: (String) -> Unit,
    fontSize: Int,
    newLine: Boolean = true,
    inIndentCode: Boolean = false
): AnnotatedString {
    val input = this
    var i = 0

    val twas: String.() -> AnnotatedString = {
        this.toWikitextAnnotatedString(colorScheme, typography, loadPage, fontSize)
    }

    val twasNoNewline: String.() -> AnnotatedString = {
        this.toWikitextAnnotatedString(colorScheme, typography, loadPage, fontSize, newLine = false)
    }

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
                                    loadPage,
                                    fontSize,
                                    inIndentCode = true
                                )
                            )
                        }
                        i += curr.length
                    } else append(input[i])

                '<' -> {
                    val currSubstring = input.substring(i)
                    if (currSubstring.startsWith("<nowiki>")) {
                        val curr = currSubstring.substringBefore("</nowiki>").substringAfter('>')
                        append(curr)
                        i += 8 + curr.length + 8
                    } else if (currSubstring.startsWith("<br", ignoreCase = true)) {
                        append('\n')
                        i += currSubstring.substringBefore('>').length
                    } else if (currSubstring.startsWith("<u>")) {
                        val curr = currSubstring.substringBefore("</u>").substringAfter('>')
                        withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                            append(curr.twasNoNewline())
                        }
                        i += 3 + curr.length + 3
                    } else if (currSubstring.startsWith("<div")) {
                        val divTag = currSubstring.substringBefore('>')
                        val curr = currSubstring.substringBefore("</div>").substringAfter('>')
                        append(
                            curr.twasNoNewline()
                        )
                        i += divTag.length + 1 + curr.length + 5
                    } else if (currSubstring.startsWith("<section")) {
                        val curr = currSubstring.substringBefore('>')
                        i += curr.length
                    } else if (currSubstring.startsWith("<noinclude>")) {
                        val curr = currSubstring.substringBefore("</noinclude>").substringAfter('>')
                        append(curr.twasNoNewline())
                        i += 11 + curr.length + 11
                    } else if (currSubstring.startsWith("<code>")) {
                        val curr = currSubstring.substringBefore("</code>").substringAfter('>')
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                color = colorScheme.onSurfaceVariant
                            )
                        ) { append(curr.twasNoNewline()) }
                        i += 6 + curr.length + 6
                    } else if (currSubstring.startsWith("<syntaxhighlight")) {
                        val curr =
                            currSubstring.substringBefore("</syntaxhighlight>").substringAfter('>')
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                color = colorScheme.onSurfaceVariant
                            )
                        ) { append(curr) }
                        i += currSubstring.substringBefore('>').length + curr.length + 18
                    } else if (currSubstring.startsWith("<sub>")) {
                        val curr = currSubstring.substringBefore("</sub>").substringAfter('>')
                        withStyle(
                            SpanStyle(
                                baselineShift = BaselineShift.Subscript,
                                fontSize = (fontSize - 2).sp
                            )
                        ) { append(curr.twasNoNewline()) }
                        i += 5 + curr.length + 5
                    } else if (currSubstring.startsWith("<sup>")) {
                        val curr = currSubstring.substringBefore("</sup>").substringAfter('>')
                        withStyle(
                            SpanStyle(
                                baselineShift = BaselineShift.Superscript,
                                fontSize = (fontSize - 2).sp
                            )
                        ) { append(curr.twasNoNewline()) }
                        i += 5 + curr.length + 5
                    } else if (currSubstring.startsWith("<small>")) {
                        val curr = currSubstring.substringBefore("</small>").substringAfter('>')
                        withStyle(SpanStyle(fontSize = (fontSize - 2).sp)) {
                            append(curr.twasNoNewline())
                        }
                        i += 7 + curr.length + 7
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
                            ParagraphStyle(
                                textIndent = TextIndent(
                                    firstLine = 16.sp,
                                    restLine = 16.sp
                                )
                            )
                        ) {
                            withStyle(SpanStyle(color = colorScheme.onSurfaceVariant)) {
                                append('\n')
                                append(curr.twas())
                                append('\n')
                            }
                        }
                        i += 12 + curr.length + 13
                    } else if (currSubstring.startsWith("<!--")) {
                        val curr = currSubstring.substringMatchingParen('<', '>')
                        i += curr.length - 1
                    } else {
                        append(input[i])
                    }
                }

                '{' ->
                    if (input.getOrNull(i + 1) == '{') {
                        val currSubstring =
                            substringMatchingParen('{', '}', i).substringBeforeLast("}}")
                        if (currSubstring.startsWith("{{nowrap", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|')
                            append(curr.replace(' ', nbsp).twas())
                        } else if (currSubstring.startsWith("{{abbr", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|').substringBefore('|')
                            append(curr.twas())
                        } else if (currSubstring.startsWith("{{TableTBA", ignoreCase = true)) {
                            append("<small>TBA</small>".twas())
                        } else if (currSubstring.startsWith("{{convert", ignoreCase = true) ||
                            currSubstring.startsWith("{{cvt", ignoreCase = true)
                        ) {
                            val curr = currSubstring.substringAfter('|')
                            val currSplit = curr.split('|')
                            var toAdd = ""
                            toAdd += currSplit[0]
                            toAdd += if (currSplit[1] in listOf("-", "to", "and")) {
                                " " + currSplit[1] + " " + currSplit[2] + " " + currSplit[3]
                            } else {
                                " " + currSplit[1]
                            }
                            append(toAdd)
                        } else if (currSubstring.startsWith("{{mono", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|')
                            withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                                append(curr.twas())
                            }
                        } else if (currSubstring.startsWith("{{math", ignoreCase = true) ||
                            currSubstring.startsWith("{{mvar", ignoreCase = true)
                        ) {
                            val curr = currSubstring.substringAfter(
                                '|',
                                currSubstring.substringAfter("{{").substringBefore("}}")
                            )
                            withStyle(SpanStyle(fontFamily = FontFamily.Serif)) {
                                append(curr.replace(' ', nbsp).twas())
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
                                        "[[${it.substringBefore(MAGIC_SEP)}|${
                                            it.substringAfter(MAGIC_SEP).replace(
                                                "#",
                                                " § "
                                            )
                                        }]]".twas()
                                    )
                                    if (index == splitList.size - 2 && splitList.size > 1) append(", and ")
                                    else if (index < splitList.size - 1) append(", ")
                                }
                            }
                            append('\n')
                        } else if (currSubstring.startsWith("{{see also", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|')
                            val splitList = curr.split('|').filterNot { it.startsWith('#') }
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                append("See also: ")
                                splitList.forEachIndexed { index, it ->
                                    append(
                                        "[[${it.substringBefore(MAGIC_SEP)}|${
                                            it.substringAfter((MAGIC_SEP)).replace("#", " § ")
                                        }]]".twas()
                                    )
                                    if (index == splitList.size - 2 && splitList.size > 1) append(", and ")
                                    else if (index < splitList.size - 1) append(", ")
                                }
                            }
                        } else if (currSubstring.startsWith("{{distinguish", ignoreCase = true)) {
                            val textSpecified =
                                currSubstring.contains("text=") || currSubstring.contains("text =")
                            val curr =
                                if (textSpecified) currSubstring.substringAfter('=').trim()
                                else currSubstring.substringAfter('|')
                            val splitList =
                                if (textSpecified) listOf(curr)
                                else curr.split('|')
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                append("Not to be confused with ")
                                if (!textSpecified) splitList.forEachIndexed { index, it ->
                                    append(
                                        "[[${it.substringBefore(MAGIC_SEP)}|${
                                            it.substringAfter(
                                                MAGIC_SEP
                                            ).replace("#", " § ")
                                        }]]".twas()
                                    )
                                    if (index == splitList.size - 2 && splitList.size > 1) append(", or ")
                                    else if (index < splitList.size - 1) append(", ")
                                } else append(splitList[0].twas())
                                append('.')
                            }
                        } else if (currSubstring.startsWith("{{hatnote", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|')
                            append("''$curr''".twas())
                        } else if (currSubstring.startsWith("{{IPAc-en", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|')
                            append("/${curr.replace("|", "").replace(' ', nbsp)}/")
                        } else if (currSubstring.startsWith("{{langx", ignoreCase = true)) {
                            val lang = langCodeToName(
                                currSubstring.substringAfter('|').substringBefore('|').trim()
                            )
                            val text = currSubstring.substringAfter('|').substringAfter('|')
                                .substringBefore('|').trim()
                            append("[[$lang|$lang]]: ".twas())
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                append(
                                    text.twas()
                                )
                            }
                        } else if (currSubstring.startsWith("{{lang|", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|')
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                append(curr.substringAfter('|').substringBefore('|').twas())
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
                                ParagraphStyle(
                                    textIndent = TextIndent(
                                        firstLine = 16.sp,
                                        restLine = 16.sp
                                    )
                                )
                            ) {
                                withStyle(SpanStyle(color = colorScheme.onSurfaceVariant)) {
                                    append('\n')
                                    append(
                                        curr.substringAfter("text=")
                                            .substringBefore('=')
                                            .substringBeforeLast('|')
                                            .twas()
                                    )
                                    append('\n')
                                }
                            }
                        } else if (currSubstring.startsWith("{{further", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|')
                            val topic = curr.substringAfter("topic=", "").substringBefore('|')
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                append("Further")
                                if (topic.isNotEmpty()) {
                                    append(" information on $topic")
                                    append(
                                        ": [[${
                                            curr.substringAfter('|').substringBefore('|')
                                                .substringBefore('#')
                                        }]]\n".twas()
                                    )
                                } else {
                                    append(" reading")
                                    append(
                                        ": [[${
                                            curr.substringAfter('|').substringBefore('|')
                                                .substringBefore('#')
                                        }]]\n".twas()
                                    )
                                }
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
                        } else if (currSubstring.startsWith("{{unichar", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|').substringBefore('|')
                            append("<code>U+$curr</code> ".twas())
                            try {
                                append(Character.toString(curr.toInt(16)))
                            } catch (_: Exception) {
                            }
                        } else if (currSubstring.startsWith("{{char", ignoreCase = true)) {
                            append(currSubstring.substringAfter('|').twas())
                        } else if (currSubstring.startsWith("{{Nihongo", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|')
                            append(curr.twas())
                        } else if (currSubstring.startsWith("{{flagg", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|').substringAfter('|')
                                .substringBefore('|')
                            append(curr.twas())
                        } else if (currSubstring.startsWith("{{noflag", ignoreCase = true)) {
                            val curr = currSubstring.substringAfter('|')
                            append(curr.twas())
                        } else if (currSubstring.startsWith(
                                "{{spaced en dash",
                                ignoreCase = true
                            ) ||
                            currSubstring.startsWith("{{snd", ignoreCase = true)
                        ) {
                            append(nbsp)
                            append(ndash)
                            append(' ')
                        } else if (currSubstring.startsWith("{{empty section", ignoreCase = true)) {
                            withStyle(
                                SpanStyle(
                                    fontStyle = FontStyle.Italic,
                                    color = colorScheme.onSurfaceVariant
                                )
                            ) {
                                append("This section is empty. You can help by adding to it on Wikipedia.")
                            }
                        } else {
                            val curr = input.getOrNull(i + 1 + currSubstring.length + 1)
                            if (curr == '\n') i++
                        }
                        i += currSubstring.length + 1
                    } else append(input[i])

                '&' ->
                    if (input.substring(i, min(i + 10, input.length)).contains(';')) {
                        val curr = input.substring(i, input.indexOf(';', i) + 1)
                        append(AnnotatedString.fromHtml(curr))
                        i += curr.length - 1
                    } else append(input[i])

                '*' ->
                    if ((i == 0 || input[i - 1] == '\n') && newLine) {
                        val bulletCount =
                            input.substring(i).substringBefore(' ').count { it == '*' }
                        val curr = input.substring(i).substringBefore('\n')
                        withStyle(
                            ParagraphStyle(
                                textIndent = TextIndent(restLine = (12 * bulletCount).sp),
                                lineHeight = (24 * (fontSize / 16.0)).toInt().sp,
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.None
                                )
                            )
                        ) {
                            append("\t\t".repeat(bulletCount - 1))
                            append(bullet)
                            append("\t\t")
                            append(curr.substringAfterLast('*').trim().twas())
                        }
                        i += curr.length
                    } else append(input[i])

                '=' ->
                    if (input.getOrNull(i + 1) == '=' && input.getOrNull(i + 2) == '=') {
                        if (input.getOrNull(i + 3) == '=') {
                            if (input.getOrNull(i + 4) == '=') { // h5
                                val curr = input.substring(i + 5).substringBefore("=====")
                                withStyle(typography.titleMedium.toSpanStyle()) {
                                    append(curr.trim().twas())
                                }
                                i += 4 + curr.length + 5
                            } else { // h4
                                val curr = input.substring(i + 4).substringBefore("====")
                                withStyle(typography.titleLarge.toSpanStyle()) {
                                    append("${curr.trim()}\n".twas())
                                }
                                i += 3 + curr.length + 4
                            }
                        } else { // h3
                            val curr = input.substring(i + 3).substringBefore("===")
                            withStyle(typography.headlineSmall.toSpanStyle()) {
                                append("${curr.trim()}\n".twas())
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
                            append(curr.twas())
                        }
                        i += curr.length + 3
                    } else if (input.getOrNull(i + 1) == '\'') {
                        val curr = input.substring(i + 2).substringBefore("''")
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(curr.twas())
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
                                    loadPage(curr.substringBefore('|').substringBefore('#'))
                                }
                            ) {
                                append(curr.substringAfter('|').trim().twas())
                            }
                            i += 1 + curr.length + 2
                        } else i += substringMatchingParen('[', ']', i).length - 1
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