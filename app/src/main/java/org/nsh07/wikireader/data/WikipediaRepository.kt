package org.nsh07.wikireader.data

import org.nsh07.wikireader.network.WikipediaApiService

interface WikipediaRepository {
    suspend fun getSearchResult(query: String): WikiApiResponse
    suspend fun getRandomResult(): WikiApiResponse
}

class NetworkWikipediaRepository(
    private val wikipediaApiService: WikipediaApiService
) : WikipediaRepository {
    override suspend fun getSearchResult(query: String): WikiApiResponse {
        return wikipediaApiService.getSearchResult(query)
    }

    override suspend fun getRandomResult(): WikiApiResponse {
        return wikipediaApiService.getRandomResult()
    }
}