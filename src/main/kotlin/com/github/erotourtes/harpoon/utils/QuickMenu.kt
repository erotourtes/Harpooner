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
import org.intellij.markdown.lexer.push
import java.io.File
import kotlin.io.path.Path

class QuickMenu(projectPath: String?) {
    private lateinit var menuFile: File
    private lateinit var virtualFile: VirtualFile
    private var connection: MessageBusConnection? = null
    private val name = "Harpooner Menu"
    private val projectInfo: ProjectInfo

    init {
        projectInfo = ProjectInfo.from(projectPath)
        initMenuFile()
    }

    fun readLines(): List<String> {
        val docManager = FileDocumentManager.getInstance()
        val document = docManager.getDocument(virtualFile) ?: throw Error("Can't read file")

        return document.text.split("\n").map { it }
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
            updateFile(project, harpoonService.getPaths())
        }
        fileManager.openFile(virtualFile, true)
        return this
    }

    fun updateFile(project: Project, content: List<String>): QuickMenu {
        ApplicationManager.getApplication().runWriteAction {
            val docManager = FileDocumentManager.getInstance()
            val document = docManager.getDocument(virtualFile) ?: return@runWriteAction
            content.joinToString("\n") { formatPath(it) }.let { document.setText(it) }

            content.forEachIndexed { index, it ->
                val line = document.getLineStartOffset(index)
                foldLine(project, line, it)
            }
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
                foldLine(project, document.lineCount - 1, str)
            } catch (e: Exception) {
                updateFile(project, listOf(str))
            }
        }
    }

    private fun foldLine(project: Project, line: Int, str: String) {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val foldingModel = editor.foldingModel
        val foldings = ArrayList<Triple<Int, Int, String>>()

        var lastFoldIndex = line
        val lineLenLimit = 50

        if (str.startsWith(projectInfo.path)) {
            foldings.push(Triple(line, projectInfo.path.length, projectInfo.name))
            lastFoldIndex += projectInfo.path.length + 1
        } else if (str.contains(projectInfo.name, false)) {
            foldings.push(Triple(lastFoldIndex, projectInfo.path.length, "${projectInfo.name}/"))
            lastFoldIndex += projectInfo.path.length + 1
        }
        if (str.length - (lastFoldIndex - line) > lineLenLimit) {
            val index = str.indexOf("/", lastFoldIndex + lineLenLimit)
            foldings.push(Triple(lastFoldIndex, index, "..."))
        }

        foldingModel.runBatchFoldingOperation {
            for ((start, end, placeHolder) in foldings) {
                val foldRegion = foldingModel.addFoldRegion(start, end, placeHolder) ?: return@runBatchFoldingOperation
                foldRegion.isExpanded = false
            }
        }
    }

    private fun formatPath(path: String): String {
        return path
    }

    private fun initMenuFile() {
        menuFile = getMenuFile()
        virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(menuFile)
            ?: throw Exception("File not found, this should not happen")
    }

    private fun getMenuFile(): File {
        if (projectInfo.path.isEmpty()) return File.createTempFile(name, null)

        val projectPath = projectInfo.path + ProjectInfo.ideaProjectFolder
        val menuPath = projectPath.plus("/$name")

        val menu = File(menuPath)
        return if (menu.exists()) menu else createMenuFile(menuPath)
    }

    private fun createMenuFile(path: String): File {
        return Path(path).createFile().toFile()
    }

    data class ProjectInfo(val name: String, val path: String) {
        companion object {
            const val ideaProjectFolder = ".idea"
            fun from(path: String?): ProjectInfo {
                val projectPath = path?.substring(0, path.lastIndexOf(ideaProjectFolder)) ?: ""
                val name = projectPath.substring(projectPath.lastIndexOf("/") + 1)
                return ProjectInfo(name, projectPath)
            }

        }
    }
}