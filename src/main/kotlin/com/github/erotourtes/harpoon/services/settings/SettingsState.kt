package com.github.erotourtes.harpoon.services.settings

import com.github.erotourtes.harpoon.utils.Observable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "com.github.erotourtes.harpoon.services.AppSettingsState",
    storages = [Storage("HarpoonerSettings.xml")]
)
class SettingsState : PersistentStateComponent<SettingsState>, Observable<SettingsState>() {
    // Display Project Path
    var showProjectPath = false

    // Folding options
    var numberOfSlashes = 3

    // Other
    var showNotifications = true

    override fun getState(): SettingsState = this
    override fun loadState(state: SettingsState) = XmlSerializerUtil.copyBean(state, this)

    fun snapshot(): SettingsState {
        val snapshot = SettingsState()
        snapshot.showProjectPath = showProjectPath
        snapshot.numberOfSlashes = numberOfSlashes
        snapshot.showNotifications = showNotifications
        return snapshot
    }

    companion object {
        fun getInstance(): SettingsState =
            ApplicationManager.getApplication().getService(SettingsState::class.java)
    }
}