package org.nsh07.wikireader.data

import org.nsh07.wikireader.network.WikipediaApiService
import java.time.LocalDate
import java.time.format.DateTimeFormatter

interface WikipediaRepository {
    suspend fun getSearchResult(query: String): WikiApiResponse
    suspend fun getPageContent(title: String): String
    suspend fun getRandomResult(): WikiApiResponse
    suspend fun getFeed(
        date: String = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
    ): FeedApiResponse
}

class NetworkWikipediaRepository(
    private val wikipediaApiService: WikipediaApiService,
    private val wikipediaPageApiService: WikipediaApiService,
) : WikipediaRepository {
    override suspend fun getSearchResult(query: String): WikiApiResponse {
        return wikipediaApiService.getSearchResult(query)
    }

    override suspend fun getPageContent(title: String): String {
        return wikipediaPageApiService.getPageContent(title)
    }

    override suspend fun getRandomResult(): WikiApiResponse {
        return wikipediaApiService.getRandomResult()
    }

    override suspend fun getFeed(
        date: String
    ): FeedApiResponse {
        return wikipediaApiService.getFeed(date)
    }
}