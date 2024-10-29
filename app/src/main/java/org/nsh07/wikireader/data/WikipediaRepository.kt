package org.nsh07.wikireader.data

import org.nsh07.wikireader.network.WikipediaApi

interface WikipediaRepository {
    suspend fun searchWikipedia(query: String): WikiApiResponse
}

class NetworkWikipediaRepository() : WikipediaRepository {
    override suspend fun searchWikipedia(query: String): WikiApiResponse {
        return WikipediaApi.retrofitService.searchWikipedia(query)
    }
}