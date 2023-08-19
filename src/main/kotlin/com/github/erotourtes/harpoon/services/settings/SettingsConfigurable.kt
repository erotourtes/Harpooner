package com.github.erotourtes.harpoon.services.settings

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

    override fun getDisplayName(): String = "Harpooner Settings"

    override fun isModified(): Boolean = settingsComponent!!.state.isModified(SettingsState.getInstance())
}