package com.github.erotourtes.harpoon.settings

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer

class SettingsChangeListener(private val disposable: Disposable, private val updateSettings: (SettingsState) -> Unit) {
    init {
        val settings = SettingsState.getInstance()
        listenToSettingsChange(settings)
    }

    private fun listenToSettingsChange(settings: SettingsState) {
        val d = settings.addObserver { updateSettings(it) }
        Disposer.register(disposable, d)
    }
}