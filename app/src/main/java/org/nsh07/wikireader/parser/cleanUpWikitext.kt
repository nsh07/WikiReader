package org.nsh07.wikireader.parser

/**
 * Remove parts of a Wikitext section that are not to be rendered
 *
 * @param wikitext Source Wikitext to clean up
 */
fun cleanUpWikitext(wikitext: String): String {
    var bodyText = wikitext
//        .replace("<!--.+?-->\n".toRegex(), "")
        .replace(
            "<ref[^/]*?>.+?</ref>|<ref.*?/>".toRegex(RegexOption.DOT_MATCHES_ALL),
            ""
        )
    bodyText = bodyText.replace("<var>|</var>".toRegex(), "''")
    return bodyText
}