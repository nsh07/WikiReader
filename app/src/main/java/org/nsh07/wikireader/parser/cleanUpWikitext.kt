package org.nsh07.wikireader.parser

/**
 * Remove parts of a Wikitext section that are not to be rendered
 *
 * @param wikitext Source Wikitext to clean up
 * @param index Index of the current section. 0 is the introduction, which contains Infoboxes and
 * other templates that are unnecessary, so initial template removal is performed on sections with
 * index 0
 */
fun cleanUpWikitext(wikitext: String, index: Int = 0): String {
    var stack = 0
    var i = 0
    var bodyText = wikitext
        .replace("<!--.+?-->\n".toRegex(), "")
        .replace(
            "<!--.+?-->|<ref[^/]*?>.+?</ref>|<ref.*?/>".toRegex(RegexOption.DOT_MATCHES_ALL),
            ""
        )
    if (index == 0)
        while (i < bodyText.length) {
            if (bodyText[i] == '{' && bodyText.getOrNull(i + 1) == '{') {
                stack++
                i++
            } else if (bodyText[i] == '}' && bodyText.getOrNull(i + 1) == '}') {
                stack--
                i++
            } else if (
                (bodyText[i] == '\n' || i == 0) &&
                (bodyText.getOrNull(i + 1) != '}' && bodyText.getOrNull(i + 1) != '}') &&
                (bodyText.getOrNull(i + 1) != '{' && bodyText.getOrNull(i + 1) != '{')
            ) {
                if (stack == 0) {
                    if (i != 0) i++
                    break
                }
            }
            i++
        }

    bodyText = bodyText
        .substring(i)
    // Remove references and comments
    //"\\{\\{(?![mM]ono)(?![Mm]ath)(?![Mm]var)(?![Mm]ain)(?!IPA)(?!respell)(?![Bb]lockquote)[^{}]+\\}\\}"
//    while (bodyText.contains(
//            "\\{\\{[Ii]nfobox[^{}]+\\}\\}".toRegex(
//                RegexOption.DOT_MATCHES_ALL
//            )
//        )
//    ) {
//        bodyText =
//            bodyText.replace(
//                "\\{\\{(?![mM]ono)(?![Mm]ath)(?![Mm]var)(?![Mm]ain)(?!IPA)(?!respell)(?![Bb]lockquote)[^{}]+\\}\\}".toRegex(
//                    RegexOption.DOT_MATCHES_ALL
//                ),
//                ""
//            )
//    }
//    bodyText = bodyText.replace(" {2,}".toRegex(), " ") // Remove double spaces
//    bodyText = bodyText.replace("\n{3,}".toRegex(), "\n") // Remove empty newlines
    bodyText = bodyText.replace("<var>|</var>".toRegex(), "''")
    return bodyText
}