package org.nsh07.wikireader.ui.homeScreen.viewModel

import org.nsh07.wikireader.data.UserLanguage

sealed class HomeAction {
    data class DeleteUserLanguage(val lang: String) : HomeAction()
    data class InsertUserLanguage(val userLanguage: UserLanguage) : HomeAction()
    data class LoadFeed(val fromBack: Boolean = false) : HomeAction()
    data class LoadPage(
        val title: String?,
        val lang: String? = null,
        val random: Boolean = false,
    ) : HomeAction()

    data class LoadSavedArticle(val pageId: Int, val lang: String) : HomeAction()
    data class LoadSearch(val query: String) : HomeAction()
    data class LoadSearchResultsDebounced(val query: String) : HomeAction()
    data class MarkUserLanguageSelected(val lang: String) : HomeAction()
    data class ReloadPage(val persistLang: Boolean = false) : HomeAction()
    data class SaveArticle(
        val lang: String,
        val unableToSaveError: String,
        val unableToDeleteError: String
    ) : HomeAction()

    data class SetQuery(val text: String) : HomeAction()
    data class ShowFeedErrorSnackBar(val errorString: String) : HomeAction()
    data class UpdateLanguageSearchStr(val str: String) : HomeAction()
    data class UpdateRef(val ref: String) : HomeAction()
    object FocusSearchBar : HomeAction()
    object HideRef : HomeAction()
    object LoadRandom : HomeAction()
    object ScrollToTop : HomeAction()
    object StopAll : HomeAction()
}