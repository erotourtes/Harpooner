package com.github.erotourtes.harpoon.utils.menu

import com.github.erotourtes.harpoon.listeners.FileEditorListener
import com.github.erotourtes.harpoon.services.HarpoonService
import com.github.erotourtes.harpoon.services.settings.SettingsState
import com.github.erotourtes.harpoon.utils.IDEA_PROJECT_FOLDER
import com.github.erotourtes.harpoon.utils.MENU_NAME
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
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


// TODO: store a snapshot of the settings in the quick menu (now it is stored in processor and fold manager separately); + maybe implement a snapshot class
class QuickMenu(private val project: Project) {
    private lateinit var menuFile: File
    private lateinit var virtualFile: VirtualFile
    private var connection: MessageBusConnection? = null
    val projectInfo = ProjectInfo.from(project.projectFilePath)
    private val foldManager = FoldManager(this, project)
    private var processor = PathsProcessor(this)

    init {
        initMenuFile()
        SettingsState.getInstance().addObserver { updateSettings(it) }
    }

    fun readLines(): List<String> {
        val docManager = FileDocumentManager.getInstance()
        val document = docManager.getDocument(virtualFile) ?: throw Error("Can't read file")

        return document.text.split("\n").map { processor.unprocess(it) }
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

            val processedContent = processor.process(content)
            processedContent.joinToString("\n").let { document.setText(it) }

            processedContent.forEachIndexed { index, it ->
                val line = document.getLineStartOffset(index)
                foldManager.addFoldsToLine(line, it)
            }
        }

        return this
    }

    private fun updateSettings(settings: SettingsState) {
        foldManager.updateSettings(settings)

        processor = PathsProcessor(this)
        val harpoonService = HarpoonService.getInstance(project)
        updateFile(harpoonService.getPaths())
    }

//    private fun addToFile(str: String) {
//        ApplicationManager.getApplication().runWriteAction {
//            val docManager = FileDocumentManager.getInstance()
//            val document = docManager.getDocument(virtualFile) ?: return@runWriteAction
//            try {
//                val endLine = document.getLineEndOffset(document.lineCount - 1)
//                CommandProcessor.getInstance().executeCommand(
//                    project, {
//                        WriteCommandAction.runWriteCommandAction(project) {
//                            document.insertString(endLine, "\n" + str)
//                        }
//                    }, PLUGIN_NAME, null
//                )
//            } catch (e: Exception) {
//                updateFile(listOf(str))
//            }
//        }
//    }

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
            ?: throw Exception("File is not found, this should not happen")
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