package org.nsh07.wikireader.ui.historyScreen.viewModel

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
import org.nsh07.wikireader.data.ViewHistoryItem

class HistoryViewModel(
    private val appDatabaseRepository: AppDatabaseRepository
) : ViewModel() {

    val viewHistoryFlow = appDatabaseRepository.getViewHistory().distinctUntilChanged()

    fun onAction(action: HistoryAction) {
        when (action) {
            is HistoryAction.InsertItem -> insertViewHistoryItem(action.item)
            is HistoryAction.RemoveItem -> removeViewHistoryItem(action.item)
            is HistoryAction.RemoveAll -> removeViewHistoryItem(null)
        }
    }

    private fun insertViewHistoryItem(item: ViewHistoryItem) =
        viewModelScope.launch(Dispatchers.IO) {
            appDatabaseRepository.insertViewHistory(item)
        }

    private fun removeViewHistoryItem(item: ViewHistoryItem?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (item != null) appDatabaseRepository.deleteViewHistory(item)
            else appDatabaseRepository.deleteAllViewHistory()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as WikiReaderApplication)
                val appHistoryRepository = application.container.appDatabaseRepository
                HistoryViewModel(appDatabaseRepository = appHistoryRepository)
            }
        }
    }

}