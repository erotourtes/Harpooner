package com.github.erotourtes.harpoon.services.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "com.github.erotourtes.harpoon.services.AppSettingsState",
    storages = [Storage("HarpoonerSettings.xml")]
)
class SettingsState : PersistentStateComponent<SettingsState> {
    // Display Project Path
    var showProjectPath = false
    var foldProjectPath = true

    // Folding options
    var numberOfSlashes = 3

    override fun getState(): SettingsState = this
    override fun loadState(state: SettingsState) = XmlSerializerUtil.copyBean(state, this)

    companion object {
        fun getInstance(): SettingsState =
            ApplicationManager.getApplication().getService(SettingsState::class.java)
    }
}