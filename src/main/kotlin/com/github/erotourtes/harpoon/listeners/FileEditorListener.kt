package com.github.erotourtes.harpoon.listeners

import com.github.erotourtes.harpoon.services.HarpoonService
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.vfs.VirtualFile

// It was not registered in plugin.xml as it shouldn't start with the IDE
class FileEditorListener : FileEditorManagerListener {
    override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
        val project = source.project
        val harpoonService = project.service<HarpoonService>()
        val menu = harpoonService.menu

        if (menu == null || !menu.isMenuFile(file.path)) return

        harpoonService.setPaths(menu.readLines())
        menu.disconnectListener()
    }
}
