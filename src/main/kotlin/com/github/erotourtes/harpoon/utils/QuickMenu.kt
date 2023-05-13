package com.github.erotourtes.harpoon.utils

import com.github.erotourtes.harpoon.listeners.FileEditorListener
import com.github.erotourtes.harpoon.services.HarpoonService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.io.createFile
import com.intellij.util.messages.MessageBusConnection
import java.io.File
import kotlin.io.path.Path

class QuickMenu(projectPath: String?) {
    private lateinit var menuFile: File
    private lateinit var virtualFile: VirtualFile
    private var connection: MessageBusConnection? = null
    private val name = "Harpooner Menu"
    private val ideaProjectFolder = ".idea"
    private val projectPath: String
    private val projectPathIndicator = "\$PROJECT_DIR\$/"

    init {
        this.projectPath = projectPath?.substring(0, projectPath.lastIndexOf(ideaProjectFolder)) ?: ""
        initMenuFile()
    }

    fun readLines(): List<String> {
        val docManager = FileDocumentManager.getInstance()
        val document = docManager.getDocument(virtualFile) ?: throw Error("Can't read file")

        return document.text.split("\n").map { it.replaceFirst(projectPathIndicator, projectPath) }
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
        if (!virtualFile.isValid) {
            initMenuFile()
            val harpoonService = project.getService(HarpoonService::class.java)
            updateFile(harpoonService.getPaths())
        }
        fileManager.openFile(virtualFile, true)
        return this
    }

    fun updateFile(content: List<String>): QuickMenu {
        ApplicationManager.getApplication().runWriteAction {
            val docManager = FileDocumentManager.getInstance()
            val document = docManager.getDocument(virtualFile) ?: return@runWriteAction
            content.joinToString("\n") { formatPath(it) }.let { document.setText(it) }
        }

        return this
    }

    fun addToFile(str: String, project: Project) {
        ApplicationManager.getApplication().runWriteAction {
            val docManager = FileDocumentManager.getInstance()
            val document = docManager.getDocument(virtualFile) ?: return@runWriteAction
            try {
                val endLine = document.getLineEndOffset(document.lineCount - 1)
                CommandProcessor.getInstance().executeCommand(
                    project, {
                        WriteCommandAction.runWriteCommandAction(project) {
                            document.insertString(endLine, "\n" + formatPath(str))
                        }
                    }, "Harpooner", null
                )
            } catch (e: Exception) {
                updateFile(listOf(str))
            }
        }
    }

    private fun formatPath(path: String): String {
        return path.replaceFirst(projectPath, projectPathIndicator)
    }

    private fun initMenuFile() {
        menuFile = getMenuFile()
        virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(menuFile)
            ?: throw Exception("File not found, this should not happen")
    }

    private fun getMenuFile(): File {
        if (projectPath.isEmpty()) return File.createTempFile(name, null)

        val projectPath = projectPath + ideaProjectFolder
        val menuPath = projectPath.plus("/$name")

        val menu = File(menuPath)
        return if (menu.exists()) menu else createMenuFile(menuPath)
    }

    private fun createMenuFile(path: String): File {
        return Path(path).createFile().toFile()
    }
}