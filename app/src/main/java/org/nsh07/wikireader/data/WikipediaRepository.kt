package org.nsh07.wikireader.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.nsh07.wikireader.network.WikipediaApiService
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal val sectionTransclusion =
    "\\{\\{#section:\\s*([^|}]+)\\|\\s*([^}]+)\\}\\}".toRegex(RegexOption.IGNORE_CASE)

internal val pageTransclusion = "\\{\\{:\\s*([^{}|#]+?)\\s*\\}\\}".toRegex()

interface WikipediaRepository {
    suspend fun getPrefixSearchResults(query: String): WikiApiPrefixSearchResults
    suspend fun getSearchResults(query: String): WikiApiSearchResults
    suspend fun getPageData(query: String): WikiApiPageData
    suspend fun getPageContent(title: String): String
    suspend fun getRandomResult(): WikiApiPageData
    suspend fun getFeed(
        date: String = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
    ): FeedApiResponse
}

class NetworkWikipediaRepository(
    private val wikipediaApiService: WikipediaApiService,
    private val wikipediaPageApiService: WikipediaApiService,
    private val ioDispatcher: CoroutineDispatcher
) : WikipediaRepository {
    override suspend fun getPrefixSearchResults(query: String): WikiApiPrefixSearchResults =
        withContext(ioDispatcher) {
            wikipediaApiService.getPrefixSearchResults(query)
        }

    override suspend fun getSearchResults(query: String): WikiApiSearchResults =
        withContext(ioDispatcher) {
            wikipediaApiService.getSearchResults(query)
        }
    override suspend fun getPageData(query: String): WikiApiPageData =
        withContext(ioDispatcher) {
            wikipediaApiService.getPageData(query)
        }

    override suspend fun getPageContent(title: String): String =
        withContext(ioDispatcher) {
            val fetch: suspend (String) -> String = { wikipediaPageApiService.getPageContent(it) }
            expandPageTransclusions(
                expandSectionTransclusions(wikipediaPageApiService.getPageContent(title), fetch),
                fetch
            )
        }

    override suspend fun getRandomResult(): WikiApiPageData =
        withContext(ioDispatcher) {
            wikipediaApiService.getRandomResult()
        }

    override suspend fun getFeed(
        date: String
    ): FeedApiResponse =
        withContext(ioDispatcher) {
            wikipediaApiService.getFeed(date)
        }
}

internal suspend fun expandSectionTransclusions(
    wikitext: String,
    getPageContent: suspend (String) -> String
): String {
    val pages = mutableMapOf<String, String>()

    return buildString {
        var lastIndex = 0
        sectionTransclusion.findAll(wikitext).forEach { match ->
            append(wikitext, lastIndex, match.range.first)
            val page = match.groupValues[1].trim().replace(' ', '_')
            val section = match.groupValues[2].trim()
            val pageContent = pages[page] ?: runCatching {
                getPageContent(page)
            }.getOrNull()?.also { pages[page] = it }
            append(pageContent?.extractSection(section) ?: match.value)
            lastIndex = match.range.last + 1
        }
        append(wikitext, lastIndex, wikitext.length)
    }
}

/**
 * Replaces whole-page transclusions (`{{:Page name}}`) with the target page's `<onlyinclude>`
 * content, mirroring MediaWiki's transclusion behaviour for pages such as TV season articles.
 */
internal suspend fun expandPageTransclusions(
    wikitext: String,
    getPageContent: suspend (String) -> String
): String {
    val pages = mutableMapOf<String, String>()

    return buildString {
        var lastIndex = 0
        pageTransclusion.findAll(wikitext).forEach { match ->
            append(wikitext, lastIndex, match.range.first)
            val page = match.groupValues[1].trim().replace(' ', '_')
            val pageContent = pages[page] ?: runCatching {
                getPageContent(page)
            }.getOrNull()?.also { pages[page] = it }
            append(pageContent?.extractOnlyInclude() ?: "")
            lastIndex = match.range.last + 1
        }
        append(wikitext, lastIndex, wikitext.length)
    }
}

internal fun String.extractOnlyInclude(): String? {
    val blocks = "<onlyinclude>(.*?)</onlyinclude>"
        .toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        .findAll(this)
        .map { it.groupValues[1] }
        .toList()
    return if (blocks.isEmpty()) null else blocks.joinToString("\n")
}

internal fun String.extractSection(name: String): String? {
    val escapedName = Regex.escape(name)
    val begin = "<section\\s+begin\\s*=\\s*[\"']?$escapedName[\"']?\\s*/>"
        .toRegex(RegexOption.IGNORE_CASE).find(this) ?: return null
    val end = "<section\\s+end\\s*=\\s*[\"']?$escapedName[\"']?\\s*/>"
        .toRegex(RegexOption.IGNORE_CASE).find(this, begin.range.last + 1) ?: return null
    return substring(begin.range.last + 1, end.range.first)
}