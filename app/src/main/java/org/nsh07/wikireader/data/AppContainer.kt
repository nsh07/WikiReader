package org.nsh07.wikireader.data

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.nsh07.wikireader.BuildConfig
import org.nsh07.wikireader.network.HostSelectionInterceptor
import org.nsh07.wikireader.network.WikipediaApiService
import org.nsh07.wikireader.ui.settingsScreen.viewModel.PreferencesState
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

interface AppContainer {
    val appStatus: MutableStateFlow<AppStatus>
    val preferencesStateMutableFlow: MutableStateFlow<PreferencesState>
    val interceptor: HostSelectionInterceptor
    val wikipediaRepository: WikipediaRepository
    val appPreferencesRepository: AppPreferencesRepository
    val appDatabaseRepository: AppDatabaseRepository
}

class DefaultAppContainer(context: Context) : AppContainer {
    private val baseUrl = "https://en.wikipedia.org"
    private val json = Json { ignoreUnknownKeys = true }

    override val interceptor = HostSelectionInterceptor()

    override val appStatus = MutableStateFlow(AppStatus.NOT_INITIALIZED)
    override val preferencesStateMutableFlow = MutableStateFlow(PreferencesState())

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addNetworkInterceptor { chain ->
                chain.proceed(
                    chain.request()
                        .newBuilder()
                        .header(
                            "User-Agent",
                            "WikiReader/${BuildConfig.VERSION_NAME} (https://github.com/nsh07/wikireader; nishant.28@outlook.com) okhttp/5.1.0 retrofit/3.0.0"
                        )
                        .build()
                )
            }
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
        AppPreferencesRepository(
            Dispatchers.IO,
            AppDatabase.getDatabase(context).preferenceDao()
        )
    }

    override val appDatabaseRepository: AppDatabaseRepository by lazy {
        AppDatabaseRepository(
            searchHistoryDao = AppDatabase.getDatabase(context).searchHistoryDao(),
            savedArticleDao = AppDatabase.getDatabase(context).savedArticleDao(),
            viewHistoryDao = AppDatabase.getDatabase(context).viewHistoryDao(),
            userLanguageDao = AppDatabase.getDatabase(context).userLanguageDao()
        )
    }
}