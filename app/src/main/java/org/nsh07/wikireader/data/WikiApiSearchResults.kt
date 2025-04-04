package org.nsh07.wikireader.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WikiApiSearchResults(
    val query: WikiSearchResultsQuery = WikiSearchResultsQuery()
)

@Serializable
data class WikiSearchResultsQuery(
    val pages: List<WikiSearchResult> = emptyList()
)

@Serializable
data class WikiSearchResult(
    val ns: Int = 0,
    val title: String,
    @SerialName("titlesnippet") val titleSnippet: String,
    @SerialName("pageid") val pageId: Int,
    val snippet: String,
    val index: Int,
    @SerialName("redirecttitle") val redirectTitle: String? = null,
    val thumbnail: WikiPhoto? = null
)

@Serializable
data class WikiApiPrefixSearchResults(
    val query: WikiPrefixSearchResultsQuery = WikiPrefixSearchResultsQuery()
)

@Serializable
data class WikiPrefixSearchResultsQuery(
    val pages: List<WikiPrefixSearchResult> = emptyList()
)

@Serializable
data class WikiPrefixSearchResult(
    @SerialName("pageid") val pageId: Int,
    val ns: Int = 0,
    val title: String,
    val index: Int,
    val thumbnail: WikiPhoto? = null,
    val terms: WikiPrefixSearchPageTerms? = null
)

@Serializable
data class WikiPrefixSearchPageTerms(
    val description: List<String>
)