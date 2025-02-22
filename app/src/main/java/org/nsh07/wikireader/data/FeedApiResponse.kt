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
    val titles: Titles,
    val thumbnail: Image,
    @SerialName("originalimage") val originalImage: Image,
    val lang: String,
    val description: String,
    val extract: String,
    val timestamp: String
)

@Serializable
data class FeedApiMostRead(
    val date: String,
    val articles: List<MostReadArticle>
)

@Serializable
data class FeedApiImage(
    val title: String,
    val thumbnail: Image,
    val image: Image,
    val artist: Artist,
    val credit: Credit,
    val description: Description,
    @SerialName("file_page") val filePage: String
)

@Serializable
data class FeedApiNews(
    val links: List<Article>,
    val story: String
)

@Serializable
data class FeedApiOTD(
    val text: String,
    val pages: List<Article>,
    val year: Int?
)

@Serializable
data class Titles(
    val canonical: String,
    val normalized: String
)

@Serializable
data class Image(
    val source: String,
    val width: Int,
    val height: Int
)

@Serializable
data class MostReadArticle(
    val views: Int,
    val rank: Int,
    @SerialName("view_history") val viewHistory: List<ViewHistory>,
    val titles: Titles,
    val thumbnail: Image? = null,
    @SerialName("originalimage") val originalImage: Image? = null,
    val lang: String,
    val description: String,
    val extract: String
)

@Serializable
data class Article(
    val titles: Titles,
    val thumbnail: Image? = null,
    @SerialName("originalimage") val originalImage: Image? = null,
    val lang: String,
    val description: String? = null,
    val extract: String
)

@Serializable
data class ViewHistory(
    val date: String,
    val views: Int
)

@Serializable
data class Artist(
    val text: String? = null,
    val name: String? = null
)

@Serializable
data class Credit(
    val text: String
)

@Serializable
data class Description(
    val text: String,
    val lang: String
)