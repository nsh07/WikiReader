package org.nsh07.wikireader.ui.savedArticlesScreen.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.nsh07.wikireader.WikiReaderApplication
import org.nsh07.wikireader.data.AppDatabaseRepository
import org.nsh07.wikireader.data.WRStatus

class SavedArticlesViewModel(
    private val appDatabaseRepository: AppDatabaseRepository
) : ViewModel() {

    val savedArticleLangsFlow =
        appDatabaseRepository.getSavedArticleLanguages().distinctUntilChanged()
    val savedArticlesFlow = appDatabaseRepository.getSavedArticles().distinctUntilChanged()

    /**
     * Deletes the current article
     *
     * @return A [WRStatus] enum value indicating the status of the delete operation
     */
    fun deleteArticle(pageId: Int, lang: String): WRStatus {
        viewModelScope.launch(Dispatchers.IO) {
            appDatabaseRepository.deleteSavedArticle(pageId, lang)
        }

        return WRStatus.SUCCESS
    }

    fun deleteAllArticles(): WRStatus {
        viewModelScope.launch(Dispatchers.IO) {
            appDatabaseRepository.deleteAllSavedArticles()
        }
        return WRStatus.SUCCESS
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as WikiReaderApplication)
                val appHistoryRepository = application.container.appDatabaseRepository
                SavedArticlesViewModel(appDatabaseRepository = appHistoryRepository)
            }
        }
    }

}