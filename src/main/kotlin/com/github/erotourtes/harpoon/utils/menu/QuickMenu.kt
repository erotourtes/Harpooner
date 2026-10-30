package com.github.erotourtes.harpoon.utils.menu

import com.github.erotourtes.harpoon.settings.SettingsState
import com.github.erotourtes.harpoon.utils.IDEA_PROJECT_FOLDER
import com.github.erotourtes.harpoon.utils.MENU_NAME
import com.github.erotourtes.harpoon.utils.openFileInEditor
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.FoldingModel
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileOpenedSyncListener
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.ex.FileEditorWithProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


class QuickMenu(private val project: Project, settings: SettingsState, parentDisposable: Disposable) {
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
            projectInfo, {
                val editor = getMenuEditor() ?: return@FoldsManager null
                val foldingModel = editor.foldingModel
                return@FoldsManager foldingModel
            }, settings.toFoldsSettings()
        )
        processor = PathsProcessor(projectInfo, settings.toProcessorSettings())

        project.messageBus.connect(parentDisposable).subscribe(FileOpenedSyncListener.TOPIC, object : FileOpenedSyncListener {
            override fun fileOpenedSync(
                source: FileEditorManager,
                file: VirtualFile,
                editorsWithProviders: List<FileEditorWithProvider>,
            ) {
                if (file != virtualFile) return
                // This callback runs in the editor-opening EDT event, before it can repaint.
                for ((fileEditor, _) in editorsWithProviders) {
                    val editor = (fileEditor as? TextEditor)?.editor ?: continue
                    setCursorToEnd(editor)
                    updateFolds(editor.document, editor.foldingModel)
                }
            }
        })
    }

    suspend fun readLines(): List<String> = readAction {
        val docManager = FileDocumentManager.getInstance()
        val document = docManager.getDocument(virtualFile) ?: throw Error("Can't read file")

        return@readAction document.text.split("\n").map { processor.unprocess(it) }
    }

    suspend fun open(content: List<String>): QuickMenu {
        if (!virtualFile.isValid) initMenuFile()

        // Populate the document before opening it so the editor starts with current content.
        updateFile(content)
        openFileInEditor(project, virtualFile)
        withContext(Dispatchers.EDT) {
            val editor = getMenuEditor() ?: return@withContext
            fileEditorManager.runWhenLoaded(editor) {
                if (!editor.isDisposed) {
                    setCursorToEnd(editor)
                    updateFolds(editor.document)
                    // Disabled until the cause of automatic re-expansion is understood.
                    // foldsManager.collapseAllFolds()
                }
            }
        }

        return this
    }

    suspend fun isOpen(): Boolean = withContext(Dispatchers.EDT) {
        return@withContext fileEditorManager.isFileOpen(virtualFile)
    }

    suspend fun close() {
        withContext(Dispatchers.EDT) {
            fileEditorManager.closeFile(virtualFile)
        }
    }

    fun updateSettings(settings: SettingsState) {
        foldsManager.updateSettings(settings.toFoldsSettings())
        processor.updateSettings(settings.toProcessorSettings())
    }

    suspend fun updateFile(content: List<String>): QuickMenu {
        val processedContent = processor.process(content)
        withContext(Dispatchers.EDT) {
            WriteCommandAction.runWriteCommandAction(project) {
                val docManager = FileDocumentManager.getInstance()
                val document = docManager.getDocument(virtualFile) ?: return@runWriteCommandAction

                val text = processedContent.joinToString("\n")
                if (document.text != text) {
                    document.replaceString(0, document.textLength, text)
                }
                updateFolds(document)
            }
        }

        return this
    }

    private fun updateFolds(document: Document, foldingModel: FoldingModel? = getMenuEditor()?.foldingModel) {
        if (foldingModel == null) return
        for (line in 0 until document.lineCount) {
            val start = document.getLineStartOffset(line)
            val end = document.getLineEndOffset(line)
            val str = document.charsSequence.subSequence(start, end).toString()
            foldsManager.updateFoldsAt(start, str, foldingModel)
        }
    }

    private fun setCursorToEnd(editor: Editor) {
        val caretModel = editor.caretModel
        val currentLineNumber = caretModel.logicalPosition.line
        val currentLineEndOffset = editor.document.getLineEndOffset(currentLineNumber)
        caretModel.moveToOffset(currentLineEndOffset)
    }

    suspend fun isMenuFileOpenedWithCurEditor(): Boolean = withContext(Dispatchers.EDT) {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return@withContext false
        return@withContext isMenuEditor(editor)
    }

    fun isMenuEditor(editor: Editor): Boolean {
        val editorFilePath = FileDocumentManager.getInstance().getFile(editor.document)?.path ?: return false
        return editorFilePath == virtualFile.path
    }

    private fun getMenuEditor(): Editor? {
        return fileEditorManager.getEditors(virtualFile)
            .filterIsInstance<TextEditor>()
            .firstOrNull()
            ?.editor
    }

    private fun initMenuFile() {
        menuFile = getMenuFile()
        virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(menuFile)
            ?: throw Exception("File is not found, this should not happen")
    }

    private fun getMenuFile(): File {
        return getOrCreateMenuFile(project.projectFilePath)
    }
}

internal fun getOrCreateMenuFile(projectFilePath: String?): File {
    val tmpProjectInfo = ProjectInfo.from(projectFilePath)
    if (tmpProjectInfo.pathWithSlashAtEnd.isEmpty()) {
        return File.createTempFile(MENU_NAME, null)
    }

    val projectDir = File(tmpProjectInfo.pathWithSlashAtEnd, IDEA_PROJECT_FOLDER)
    if (!projectDir.exists()) {
        return File.createTempFile(MENU_NAME, null)
    }

    try {
        val menu = File(projectDir, MENU_NAME)
        if (!menu.exists()) {
            menu.createNewFile()
        }
        return menu
    } catch (e: Exception) {
        println(e)
        return File.createTempFile(MENU_NAME, null)
    }
}
