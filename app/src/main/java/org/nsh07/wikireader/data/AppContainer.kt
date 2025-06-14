package org.nsh07.wikireader.data

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.nsh07.wikireader.network.HostSelectionInterceptor
import org.nsh07.wikireader.network.WikipediaApiService
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

interface AppContainer {
    val interceptor: HostSelectionInterceptor
    val wikipediaRepository: WikipediaRepository
    val appPreferencesRepository: AppPreferencesRepository
    val appHistoryRepository: AppHistoryRepository
}

class DefaultAppContainer(context: Context) : AppContainer {
    private val baseUrl = "https://en.wikipedia.org"
    private val json = Json { ignoreUnknownKeys = true }

    override val interceptor = HostSelectionInterceptor()

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    private val wikipediaRetrofit = Retrofit.Builder()
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .build()

    private val wikipediaPageRetrofit = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .build()

    private val wikipediaRetrofitService: WikipediaApiService by lazy {
        wikipediaRetrofit.create(WikipediaApiService::class.java)
    }

    private val wikipediaPageRetrofitService: WikipediaApiService by lazy {
        wikipediaPageRetrofit.create(WikipediaApiService::class.java)
    }

    override val wikipediaRepository: WikipediaRepository by lazy {
        NetworkWikipediaRepository(
            wikipediaRetrofitService,
            wikipediaPageRetrofitService,
            Dispatchers.IO
        )
    }

    override val appPreferencesRepository: AppPreferencesRepository by lazy {
        AppPreferencesRepository(context, Dispatchers.IO)
    }

    override val appHistoryRepository: AppHistoryRepository by lazy {
        AppHistoryRepository(AppDatabase.getDatabase(context).historyDao())
    }
}