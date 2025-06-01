package org.nsh07.wikireader.parser

/**
 * Remove parts of a Wikitext section that are not to be rendered
 *
 * @param wikitext Source Wikitext to clean up
 */
fun cleanUpWikitext(wikitext: String): String {
    return wikitext
        .replace(
            "<ref[^/]*?>.+?</ref>|<ref.*?/>".toRegex(RegexOption.DOT_MATCHES_ALL),
            ""
        )
        .replace(
            "\\{\\{nobility table header.*?\\}\\}"
                .toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)),
            "{| class=\"wikitable\"\n"
        )
}