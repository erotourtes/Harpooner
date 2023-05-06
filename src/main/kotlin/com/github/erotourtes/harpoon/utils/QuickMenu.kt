package com.github.erotourtes.harpoon.utils

import com.github.erotourtes.harpoon.listeners.FileEditorListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.io.createFile
import com.intellij.util.messages.MessageBusConnection
import java.io.File
import java.io.FileWriter
import kotlin.io.path.Path

class QuickMenu(projectPath: String?) {
    private var menuFile: File
    private var virtualFile: VirtualFile
    private var connection: MessageBusConnection? = null
    private val name = "Harpooner Menu"

    init {
        menuFile = getMenuFile(projectPath)
        virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(menuFile)
            ?: throw Exception("File not found, this should not happen")
    }

    fun readLines(): List<String> {
        return menuFile.readLines()
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

    fun addToFile(str: String) {
        FileWriter(menuFile, true).buffered().use { writer ->
            writer.write(str + "\n")
        }
    }

    private fun getMenuFile(path: String?): File {
        if (path == null) return File.createTempFile(name, null)

        val projectPath = path.substring(0, path.indexOf(".idea") + 5)
        val menuPath = projectPath.plus("/$name")

        val menu = File(menuPath)
        return if (menu.exists()) menu else createMenuFile(menuPath)
    }

    private fun createMenuFile(path: String): File {
        return Path(path).createFile().toFile()
    }
}