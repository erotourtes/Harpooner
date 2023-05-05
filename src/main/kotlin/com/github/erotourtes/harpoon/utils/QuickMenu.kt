package com.github.erotourtes.harpoon.utils

import com.github.erotourtes.harpoon.listeners.FileEditorListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.messages.MessageBusConnection
import java.io.File

class QuickMenu {
    private var menuFile: File
    private var virtualFile: VirtualFile
    private var connection: MessageBusConnection? = null

    init {
        menuFile = createMenuFile()
        virtualFile = LocalFileSystem.getInstance().findFileByIoFile(menuFile)!!
    }

    fun readLines(): List<String> {
        return menuFile.readLines().filter { it.isNotEmpty() }
    }

    fun isMenuFile(path: String): Boolean {
        return path == menuFile.path
    }

    fun connectListener(): QuickMenu {
        if (connection != null) return this
        connection = ApplicationManager.getApplication().messageBus.connect()
        connection!!.subscribe(
                FileEditorManagerListener.FILE_EDITOR_MANAGER, FileEditorListener()
        )

        return this
    }

    fun disconnectListener(): QuickMenu {
        connection?.disconnect()
        connection = null

        return this
    }

    fun open(project: Project): QuickMenu {
        val fileManager = FileEditorManager.getInstance(project)
        fileManager.openFile(virtualFile, true)
        return this
    }

    fun updateFile(content: List<String>) {
        val writer = menuFile.bufferedWriter()
        content.forEach { writer.write(it + "\n") }
        writer.close()
    }

    private fun createMenuFile(): File {
        return File.createTempFile("harpoon", ".txt")
    }
}