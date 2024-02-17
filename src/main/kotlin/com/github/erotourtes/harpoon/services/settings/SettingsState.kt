package com.github.erotourtes.harpoon.services.settings

import com.github.erotourtes.harpoon.utils.Observable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil


data class HarpoonSettings(
    var showProjectPath: Boolean = false,
    var numberOfSlashes: Int = 3,
    var showNotifications: Boolean = true,
    var isSavingOnTyping: Boolean = false
)

@State(
    name = "com.github.erotourtes.harpoon.services.AppSettingsState", storages = [Storage("HarpoonerSettings.xml")]
)
class SettingsState : PersistentStateComponent<SettingsState>, Observable<SettingsState>() {
    var settings = HarpoonSettings()

    val showProjectPath: Boolean
        get() = settings.showProjectPath

    val numberOfSlashes: Int
        get() = settings.numberOfSlashes

    val showNotifications: Boolean
        get() = settings.showNotifications

    val isSavingOnTyping: Boolean
        get() = settings.isSavingOnTyping

    override fun getState(): SettingsState = this
    override fun loadState(state: SettingsState) = XmlSerializerUtil.copyBean(state, this)

    fun snapshot(): SettingsState {
        val snapshot = SettingsState()
        snapshot.settings = this.settings.copy()
        return snapshot
    }

    companion object {
        fun getInstance(): SettingsState = ApplicationManager.getApplication().getService(SettingsState::class.java)
    }
}