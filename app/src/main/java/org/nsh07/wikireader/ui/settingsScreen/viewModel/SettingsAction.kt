package org.nsh07.wikireader.ui.settingsScreen.viewModel

sealed class SettingsAction {
    data class SaveColorScheme(val value: String) : SettingsAction()
    data class SaveFontStyle(val value: String) : SettingsAction()
    data class SaveLang(val value: String) : SettingsAction()
    data class SaveTheme(val value: String) : SettingsAction()

    data class SaveFontSize(val value: Int) : SettingsAction()

    data class SaveBlackTheme(val value: Boolean) : SettingsAction()
    data class SaveDataSaver(val value: Boolean) : SettingsAction()
    data class SaveExpandedSections(val value: Boolean) : SettingsAction()
    data class SaveFeedEnabled(val value: Boolean) : SettingsAction()
    data class SaveHistory(val value: Boolean) : SettingsAction()
    data class SaveImageBackground(val value: Boolean) : SettingsAction()
    data class SaveImmersiveMode(val value: Boolean) : SettingsAction()
    data class SaveRenderMath(val value: Boolean) : SettingsAction()
    data class SaveSearchHistory(val value: Boolean) : SettingsAction()

    object ResetSettings : SettingsAction()
}