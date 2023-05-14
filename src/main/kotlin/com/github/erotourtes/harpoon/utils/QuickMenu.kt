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

class QuickMenu(private val project: Project) {
    private lateinit var menuFile: File
    private lateinit var virtualFile: VirtualFile
    private var connection: MessageBusConnection? = null
    private val name = "Harpooner Menu"
    private val projectInfo = ProjectInfo.from(project.projectFilePath)

    init {
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

    fun open(): QuickMenu {
        val fileManager = FileEditorManager.getInstance(project)
        val harpoonService = project.getService(HarpoonService::class.java)
        if (!virtualFile.isValid) {
            initMenuFile()
            updateFile(harpoonService.getPaths())
        }
        fileManager.openFile(virtualFile, true)
        updateFile(harpoonService.getPaths())
        collapseAllFolds()

        return this
    }

    fun updateFile(content: List<String>): QuickMenu {
        ApplicationManager.getApplication().runWriteAction {
            val docManager = FileDocumentManager.getInstance()
            val document = docManager.getDocument(virtualFile) ?: return@runWriteAction
            content.joinToString("\n").let { document.setText(it) }

            content.forEachIndexed { index, it ->
                val line = document.getLineStartOffset(index)
                foldLine(line, it)
            }
        }

        return this
    }

    fun addToFile(str: String) {
        ApplicationManager.getApplication().runWriteAction {
            val docManager = FileDocumentManager.getInstance()
            val document = docManager.getDocument(virtualFile) ?: return@runWriteAction
            try {
                val endLine = document.getLineEndOffset(document.lineCount - 1)
                CommandProcessor.getInstance().executeCommand(
                    project, {
                        WriteCommandAction.runWriteCommandAction(project) {
                            document.insertString(endLine, "\n" + str)
                        }
                    }, name, null
                )
            } catch (e: Exception) {
                updateFile(listOf(str))
            }
        }
    }

    private fun collapseAllFolds() {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val foldingModel = editor.foldingModel

        foldingModel.runBatchFoldingOperation {
            foldingModel.allFoldRegions.forEach { it.isExpanded = false }
        }
    }

    private fun foldLine(line: Int, str: String) {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val editorFilePath = FileDocumentManager.getInstance().getFile(editor.document)?.path ?: return
        if (editorFilePath != virtualFile.path) return

        val foldingModel = editor.foldingModel
        val folds = getFoldsFrom(line, str)

        foldingModel.runBatchFoldingOperation {
            for ((start, end, placeHolder) in folds) {
                val foldRegion =
                    foldingModel.addFoldRegion(start, end, placeHolder) ?: return@runBatchFoldingOperation
                foldRegion.isExpanded = false
            }
        }
    }

    private fun getFoldsFrom(line: Int, str: String): List<Triple<Int, Int, String>> {
        val folds = ArrayList<Triple<Int, Int, String>>()
        var lastFoldIndex = 0
        if (str.startsWith(projectInfo.path)) {
            val endIndex = projectInfo.path.length
            folds.push(Triple(line, line + endIndex , projectInfo.name))
            lastFoldIndex += endIndex
        } else if (str.contains(projectInfo.name, false)) { // in case there is a symbolic links
            val endIndex = str.indexOf(projectInfo.name) + projectInfo.name.length
            folds.push(Triple(line, line + endIndex, projectInfo.name))
            lastFoldIndex += endIndex
        }

        var count = 0
        for (index in str.length - 1 downTo lastFoldIndex) {
            if (str[index] == '/') count++
            if (count == 3) {
                folds.push(Triple(line + lastFoldIndex, line + index + 1, ".../"))
                break
            }
        }

        return folds
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
                val name = projectPath.substring(projectNameIndex(projectPath))
                return ProjectInfo(name, projectPath)
            }

            private fun projectNameIndex(projectPath: String): Int {
                var lastIndex = -1
                for (index in projectPath.length - 2 downTo 0) {
                    if (projectPath[index] != '/') continue
                    lastIndex = index + 1
                    break
                }

                return lastIndex
            }

        }
    }
}