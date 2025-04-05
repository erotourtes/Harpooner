package com.github.erotourtes.harpoon.utils

import com.github.erotourtes.harpoon.listeners.MenuChangeListener
import com.github.erotourtes.harpoon.services.HarpoonService
import com.github.erotourtes.harpoon.settings.SettingsState
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile

class FileTypingChangeHandler(
    private val harpoonService: HarpoonService,
    private val getVirtualFile: () -> VirtualFile
) :
    Disposable {

    init {
        Disposer.register(harpoonService, this)
    }

    init {
        val settings = SettingsState.getInstance()
        listenToMenuTypingChange(settings)
    }

    private fun listenToMenuTypingChange(settings: SettingsState) {
        val documentListener = runReadAction {
            val virtualFile = getVirtualFile()
            val menuDocument = FileDocumentManager.getInstance().getDocument(virtualFile)
                ?: throw Error("Can't get document of the ${virtualFile.path} file")
            return@runReadAction MenuChangeListener(harpoonService, menuDocument)
        }
        Disposer.register(this, documentListener)

        val updateTypingListener = { newSettings: SettingsState ->
            if (newSettings.isSavingOnTyping) documentListener.attach()
            else documentListener.detach()
        }

        updateTypingListener(settings)

        val settingsDisposable = settings.addObserver { updateTypingListener(it) }
        Disposer.register(this) { settingsDisposable() }
    }

    override fun dispose() {}
}