package org.nsh07.wikireader.ui.savedArticlesScreen.viewModel

sealed class SavedArticlesAction {
    data class Delete(val pageId: Int, val lang: String) : SavedArticlesAction()
    object DeleteAll : SavedArticlesAction()
}