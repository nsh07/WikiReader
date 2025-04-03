package org.nsh07.wikireader.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.nsh07.wikireader.network.WikipediaApiService
import java.time.LocalDate
import java.time.format.DateTimeFormatter

interface WikipediaRepository {
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
            wikipediaPageApiService.getPageContent(title)
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