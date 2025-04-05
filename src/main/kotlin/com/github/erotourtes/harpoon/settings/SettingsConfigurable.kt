package com.github.erotourtes.harpoon.settings

import com.github.erotourtes.harpoon.utils.SETTINGS_HEADER
import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class SettingsConfigurable : Configurable {
    private var settingsComponent: SettingsComponent? = null

    override fun getPreferredFocusedComponent(): JComponent = settingsComponent!!.getPreferredFocusedComponent()

    override fun createComponent(): JComponent {
        settingsComponent = SettingsComponent()
        return settingsComponent!!.panelUI
    }

    override fun apply() = settingsComponent!!.state.apply(SettingsState.getInstance())
    override fun reset() = settingsComponent!!.state.reset(SettingsState.getInstance())

    override fun disposeUIResources() {
        settingsComponent = null
    }

    override fun getDisplayName(): String = SETTINGS_HEADER

    override fun isModified(): Boolean = settingsComponent!!.state.isModified(SettingsState.getInstance())
}