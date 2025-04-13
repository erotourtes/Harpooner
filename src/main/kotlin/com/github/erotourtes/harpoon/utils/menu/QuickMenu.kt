package com.github.erotourtes.harpoon.utils.menu

import com.github.erotourtes.harpoon.settings.SettingsState
import com.github.erotourtes.harpoon.utils.IDEA_PROJECT_FOLDER
import com.github.erotourtes.harpoon.utils.MENU_NAME
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.File


class QuickMenu(private val project: Project, settings: SettingsState) {
    val projectInfo: ProjectInfo
    private lateinit var menuFile: File
    lateinit var virtualFile: VirtualFile
        private set
    private val foldsManager: FoldsManager
    private var processor: PathsProcessor
    private val fileEditorManager: FileEditorManager get() = FileEditorManager.getInstance(project)

    init {
        initMenuFile()
        projectInfo = ProjectInfo.from(virtualFile.path)

        foldsManager = FoldsManager(
            projectInfo, ::isMenuFileOpenedWithCurEditor, {
                val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return@FoldsManager null
                val foldingModel = editor.foldingModel
                return@FoldsManager foldingModel
            }, settings.toFoldsSettings()
        )
        processor = PathsProcessor(projectInfo, settings.toProcessorSettings())
    }

    fun readLines(): List<String> = runReadAction {
        val docManager = FileDocumentManager.getInstance()
        val document = docManager.getDocument(virtualFile) ?: throw Error("Can't read file")

        return@runReadAction document.text.split("\n").map { processor.unprocess(it) }
    }

    fun open(paths: List<String>): QuickMenu {
        val fileManager = FileEditorManager.getInstance(project)

        if (!virtualFile.isValid) initMenuFile()

        fileManager.openFile(virtualFile, true)
        updateFile(paths)
//        foldsManager.collapseAllFolds()
        setCursorToEnd()

        return this
    }

    fun isOpen(): Boolean {
        return fileEditorManager.isFileOpen(virtualFile)
    }

    fun close() {
        fileEditorManager.closeFile(virtualFile)
    }

    fun updateSettings(settings: SettingsState) {
        foldsManager.updateSettings(settings.toFoldsSettings())
        processor.updateSettings(settings.toProcessorSettings())
    }

    fun updateFile(content: List<String>): QuickMenu {
        val processedContent = processor.process(content)
        runWriteAction {
            val docManager = FileDocumentManager.getInstance()
            val document = docManager.getDocument(virtualFile) ?: return@runWriteAction

            processedContent.joinToString("\n").let { document.setText(it) }
            processedContent.forEachIndexed { index, it ->
                val line = document.getLineStartOffset(index)
                foldsManager.updateFoldsAt(line, it)
            }
        }

        return this
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
        return isMenuEditor(editor)
    }

    fun isMenuEditor(editor: Editor): Boolean {
        val editorFilePath = FileDocumentManager.getInstance().getFile(editor.document)?.path ?: return false
        return editorFilePath == virtualFile.path
    }

    private fun initMenuFile() {
        menuFile = getMenuFile()
        virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(menuFile)
            ?: throw Exception("File is not found, this should not happen")
    }

    private fun getMenuFile(): File {
        val tmpProjectInfo = ProjectInfo.from(project.projectFilePath)
        if (tmpProjectInfo.pathWithSlashAtEnd.isEmpty()) return File.createTempFile(MENU_NAME, null)

        val projectPath = tmpProjectInfo.pathWithSlashAtEnd + IDEA_PROJECT_FOLDER
        val menuPath = projectPath.plus("/$MENU_NAME")

        val menu = File(menuPath)
        menu.createNewFile() // create file if it doesn't exist
        return menu
    }
}