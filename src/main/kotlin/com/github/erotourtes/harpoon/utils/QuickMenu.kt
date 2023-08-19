package com.github.erotourtes.harpoon.utils

import com.github.erotourtes.harpoon.listeners.FileEditorListener
import com.github.erotourtes.harpoon.services.HarpoonService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
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

class FoldManager(private val menu: QuickMenu, private val project: Project) {
    private val projectInfo = menu.projectInfo

    private fun getFoldsFrom(line: Int, str: String): List<Triple<Int, Int, String>> {
        val folds = ArrayList<Triple<Int, Int, String>>()
        var lastFoldIndex = 0
        if (str.startsWith(projectInfo.path)) {
            val endIndex = projectInfo.path.length
            folds.push(Triple(line, line + endIndex, projectInfo.name))
            lastFoldIndex += endIndex
        } else if (str.contains(
                projectInfo.name,
                false
            )
        ) { // TODO: in case there is a symbolic links, may be handle differently
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

    fun addFoldsToLine(line: Int, str: String) {
        if (!menu.isMenuFileOpenedWithCurEditor()) return

        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
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

    fun collapseAllFolds() {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val foldingModel = editor.foldingModel

        foldingModel.runBatchFoldingOperation {
            foldingModel.allFoldRegions.forEach { it.isExpanded = false }
        }
    }
}


class QuickMenu(private val project: Project) {
    private lateinit var menuFile: File
    private lateinit var virtualFile: VirtualFile
    private var connection: MessageBusConnection? = null
    val projectInfo = ProjectInfo.from(project.projectFilePath)
    private val foldManager = FoldManager(this, project)

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
        val harpoonService = HarpoonService.getInstance(project)

        if (!virtualFile.isValid)
            initMenuFile()

        fileManager.openFile(virtualFile, true)
        updateFile(harpoonService.getPaths())
        foldManager.collapseAllFolds()
        setCursorToEnd()

        return this
    }

    private fun updateFile(content: List<String>): QuickMenu {
        ApplicationManager.getApplication().runWriteAction {
            val docManager = FileDocumentManager.getInstance()
            val document = docManager.getDocument(virtualFile) ?: return@runWriteAction
            content.joinToString("\n").let { document.setText(it) }

            content.forEachIndexed { index, it ->
                val line = document.getLineStartOffset(index)
                foldManager.addFoldsToLine(line, it)
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
                    }, PLUGIN_NAME, null
                )
            } catch (e: Exception) {
                updateFile(listOf(str))
            }
        }
    }

    private fun setCursorToEnd() {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val caretModel = editor.caretModel
        val currentLineNumber = caretModel.logicalPosition.line
        val currentLineEndOffset = editor.document.getLineEndOffset(currentLineNumber)
        caretModel.moveToOffset(currentLineEndOffset)
    }

    fun isMenuFileOpenedWithCurEditor(): Boolean {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return false
        return isMenuFileOpenedWith(editor)
    }

    private fun isMenuFileOpenedWith(editor: Editor): Boolean {
        val editorFilePath = FileDocumentManager.getInstance().getFile(editor.document)?.path ?: return false
        return editorFilePath == virtualFile.path
    }

    private fun initMenuFile() {
        menuFile = getMenuFile()
        virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(menuFile)
            ?: throw Exception("File not found, this should not happen")
    }

    private fun getMenuFile(): File {
        if (projectInfo.path.isEmpty()) return File.createTempFile(MENU_NAME, null)

        val projectPath = projectInfo.path + IDEA_PROJECT_FOLDER
        val menuPath = projectPath.plus("/$MENU_NAME")

        val menu = File(menuPath)
        return if (menu.exists()) menu else createMenuFile(menuPath)
    }

    private fun createMenuFile(path: String): File {
        return Path(path).createFile().toFile()
    }
}