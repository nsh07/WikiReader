package org.nsh07.wikireader.data

import org.nsh07.wikireader.network.WikipediaApiService

interface WikipediaRepository {
    suspend fun searchWikipedia(query: String): WikiApiResponse
}

class NetworkWikipediaRepository(
    private val wikipediaApiService: WikipediaApiService
) : WikipediaRepository {
    override suspend fun searchWikipedia(query: String): WikiApiResponse {
        return wikipediaApiService.searchWikipedia(query)
    }
}