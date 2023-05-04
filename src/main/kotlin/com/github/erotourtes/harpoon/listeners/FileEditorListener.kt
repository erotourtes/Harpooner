package com.github.erotourtes.harpoon.listeners

import com.github.erotourtes.harpoon.services.HarpoonService
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.messages.MessageBusConnection
import java.io.File

// It was not registered in plugin.xml as it shouldn't start with the IDE
// TODO: does this violate stateless principle?
class FileEditorListener(
    private val tmpFile: File,
    private val connection: MessageBusConnection
) : FileEditorManagerListener {
    override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
        if (file.path != tmpFile.path) return

        val project = source.project
        val harpoonService = project.service<HarpoonService>()
        val paths = tmpFile.readLines()
        harpoonService.setPaths(paths)

        tmpFile.delete()
        connection.disconnect()
    }
}
