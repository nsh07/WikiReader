package org.nsh07.wikireader.parser

/**
 * Remove/simplify parts of wikitext
 *
 * @param wikitext Source Wikitext to clean up
 */
fun cleanUpWikitext(wikitext: String): String {
    return expandEpisodeTables(wikitext)
        .replace("<!--.+?-->".toRegex(), "")
        .replace("</?onlyinclude>".toRegex(RegexOption.IGNORE_CASE), "")
        .replace("</?noinclude>".toRegex(RegexOption.IGNORE_CASE), "")
        .replace(
            "<section\\s+(?:begin|end)\\s*=.*?/>".toRegex(RegexOption.IGNORE_CASE),
            ""
        )
        .replace("== \n", "==\n")
        // Convert colon-indented math to display math for proper block rendering
        // This ensures math like ": <math>formula</math>" is extracted as a block element
        .replace(
            "^:+\\s*<math(?![^>]*display)".toRegex(RegexOption.MULTILINE),
            "<math display=\"block\""
        )
        .replace(
            "\\{\\{nobility table header.*?\\}\\}"
                .toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)),
            "{| class=\"wikitable\"\n"
        )
}

/**
 * Converts [Episode table](https://en.wikipedia.org/wiki/Template:Episode_table) templates (used
 * on television season pages) into standard wikitables that the app can render.
 */
private fun expandEpisodeTables(wikitext: String): String {
    var result = wikitext
    var start = result.indexOf("{{Episode table", ignoreCase = true)

    while (start >= 0) {
        val template = result.substringMatchingParen('{', '}', start)
        if (!template.endsWith("}}")) break

        val table = buildString {
            append("{| class=\"wikitable\"\n")
            append("! No. !! Title !! Directed by !! Written by !! Original air date\n")

            var episodeStart = template.indexOf("{{Episode list", ignoreCase = true)
            while (episodeStart >= 0) {
                val episode = template.substringMatchingParen('{', '}', episodeStart)
                if (!episode.endsWith("}}")) break

                val params = episode.removePrefix("{{").removeSuffix("}}")
                    .splitTemplateParameters()
                    .mapNotNull {
                        val parts = it.split('=', limit = 2)
                        if (parts.size == 2) parts[0].trim().lowercase() to parts[1].trim()
                        else null
                    }
                    .toMap()

                val overall = params["episodenumber"] ?: ""
                val inSeason = params["episodenumber2"] ?: ""
                val number =
                    if (inSeason.isNotEmpty() && inSeason != overall) "$overall ($inSeason)"
                    else overall
                val title = params["title"] ?: ""

                append("|-\n")
                append("| $number\n")
                append("| ${if (title.isNotEmpty()) "\"$title\"" else ""}\n")
                append("| ${params["directedby"] ?: ""}\n")
                append("| ${params["writtenby"] ?: ""}\n")
                append("| ${params["originalairdate"] ?: ""}\n")

                episodeStart = template.indexOf(
                    "{{Episode list",
                    episodeStart + episode.length,
                    ignoreCase = true
                )
            }
            append("|}")
        }
        result = result.replaceRange(start, start + template.length, table)
        start = result.indexOf("{{Episode table", start + table.length, ignoreCase = true)
    }

    return result
}

/**
 * Splits template parameters on `|`, ignoring pipes nested in `{{...}}` templates and
 * `[[...]]` links.
 */
private fun String.splitTemplateParameters(): List<String> {
    val out = mutableListOf<String>()
    var braceDepth = 0
    var bracketDepth = 0
    var curr = StringBuilder()

    for (c in this) {
        when (c) {
            '{' -> braceDepth++
            '}' -> braceDepth--
            '[' -> bracketDepth++
            ']' -> bracketDepth--
        }
        if (c == '|' && braceDepth == 0 && bracketDepth == 0) {
            out.add(curr.toString())
            curr = StringBuilder()
        } else curr.append(c)
    }
    if (curr.isNotEmpty()) out.add(curr.toString())

    return out
}
