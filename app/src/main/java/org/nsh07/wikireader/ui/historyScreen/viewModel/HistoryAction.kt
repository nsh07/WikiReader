package org.nsh07.wikireader.ui.historyScreen.viewModel

import org.nsh07.wikireader.data.ViewHistoryItem

sealed class HistoryAction {
    data class InsertItem(val item: ViewHistoryItem) : HistoryAction()
    data class RemoveItem(val item: ViewHistoryItem) : HistoryAction()
    object RemoveAll : HistoryAction()
}