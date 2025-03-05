package org.nsh07.wikireader.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedApiResponse(
    val tfa: FeedApiTFA? = null,
    @SerialName("mostread") val mostRead: FeedApiMostRead? = null,
    val image: FeedApiImage? = null,
    val news: List<FeedApiNews>? = null,
    @SerialName("onthisday") val onThisDay: List<FeedApiOTD>? = null,
)

@Serializable
data class FeedApiTFA(
    val titles: Titles? = null,
    val thumbnail: Image? = null,
    @SerialName("originalimage") val originalImage: Image? = null,
    val lang: String? = null,
    val description: String? = null,
    val extract: String? = null,
    val timestamp: String? = null
)

@Serializable
data class FeedApiMostRead(
    val date: String? = null,
    val articles: List<MostReadArticle>? = null
)

@Serializable
data class FeedApiImage(
    val title: String? = null,
    val thumbnail: Image? = null,
    val image: Image? = null,
    val artist: Artist? = null,
    val credit: Credit? = null,
    val description: Description? = null,
    @SerialName("file_page") val filePage: String? = null
)

@Serializable
data class FeedApiNews(
    val links: List<Article>? = null,
    val story: String? = null
)

@Serializable
data class FeedApiOTD(
    val text: String? = null,
    val pages: List<Article>? = null,
    val year: Int? = null
)

@Serializable
data class Titles(
    val canonical: String? = null,
    val normalized: String? = null
)

@Serializable
data class Image(
    val source: String? = null,
    val width: Int? = null,
    val height: Int? = null
)

@Serializable
data class MostReadArticle(
    val views: Int? = null,
    val rank: Int? = null,
    @SerialName("view_history") val viewHistory: List<ViewHistory>? = null,
    val titles: Titles? = null,
    val thumbnail: Image? = null,
    @SerialName("originalimage") val originalImage: Image? = null,
    val lang: String? = null,
    val description: String? = null,
    val extract: String? = null
)

@Serializable
data class Article(
    val titles: Titles? = null,
    val thumbnail: Image? = null,
    @SerialName("originalimage") val originalImage: Image? = null,
    val lang: String? = null,
    val description: String? = null,
    val extract: String? = null
)

@Serializable
data class ViewHistory(
    val date: String? = null,
    val views: Int? = null
)

@Serializable
data class Artist(
    val text: String? = null,
    val name: String? = null
)

@Serializable
data class Credit(
    val text: String? = null
)

@Serializable
data class Description(
    val text: String? = null,
    val lang: String? = null
)