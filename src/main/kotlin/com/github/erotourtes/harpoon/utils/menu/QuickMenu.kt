package com.github.erotourtes.harpoon.utils.menu

import com.github.erotourtes.harpoon.settings.SettingsState
import com.github.erotourtes.harpoon.utils.IDEA_PROJECT_FOLDER
import com.github.erotourtes.harpoon.utils.MENU_NAME
import com.github.erotourtes.harpoon.utils.openFileInEditor
import com.intellij.codeInsight.folding.CodeFoldingManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileOpenedSyncListener
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.ex.FileEditorWithProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
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
    private var foldingSettings = settings.toFoldsSettings()
    private var processor: PathsProcessor
    private val foldingPolicy = MenuFoldingPolicy(project, parentDisposable)
    private val foldingDiagnostics = MenuFoldingDiagnostics(parentDisposable)
    private val fileEditorManager: FileEditorManager get() = FileEditorManager.getInstance(project)

    init {
        initMenuFile()
        projectInfo = ProjectInfo.from(virtualFile.path)

        processor = PathsProcessor(projectInfo, settings.toProcessorSettings())
        Disposer.register(parentDisposable, Disposable { virtualFile.putUserData(MenuFoldingBuilder.CONTEXT_KEY, null) })

        project.messageBus.connect(parentDisposable).subscribe(FileOpenedSyncListener.TOPIC, object : FileOpenedSyncListener {
            override fun fileOpenedSync(
                source: FileEditorManager,
                file: VirtualFile,
                editorsWithProviders: List<FileEditorWithProvider>,
            ) {
                if (file != virtualFile) return
                // Observe native fold initialization; the folding builder owns the regions.
                for ((fileEditor, _) in editorsWithProviders) {
                    val editor = (fileEditor as? TextEditor)?.editor ?: continue
                    foldingPolicy.attach(editor)
                    foldingDiagnostics.record(editor, "fileOpenedSync before caret")
                    setCursorToEnd(editor)
                    foldingDiagnostics.record(editor, "fileOpenedSync after caret")
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
            foldingDiagnostics.record(editor, "navigation returned")
            fileEditorManager.runWhenLoaded(editor) {
                if (!editor.isDisposed) {
                    foldingDiagnostics.record(editor, "runWhenLoaded before caret")
                    setCursorToEnd(editor)
                    foldingDiagnostics.record(editor, "runWhenLoaded after caret")
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
            getMenuEditor()?.let { foldingDiagnostics.record(it, "close requested") }
            fileEditorManager.closeFile(virtualFile)
        }
    }

    suspend fun updateSettings(settings: SettingsState) = withContext(Dispatchers.EDT) {
        val newFoldingSettings = settings.toFoldsSettings()
        if (newFoldingSettings != foldingSettings) {
            foldingSettings = newFoldingSettings
            virtualFile.putUserData(MenuFoldingBuilder.CONTEXT_KEY, MenuFoldingBuilder.Context(projectInfo, foldingSettings))
            getMenuEditor()?.let { CodeFoldingManager.getInstance(project).scheduleAsyncFoldingUpdate(it) }
        }
        processor.updateSettings(settings.toProcessorSettings())
    }

    suspend fun updateFile(content: List<String>): QuickMenu {
        val processedContent = processor.process(content)
        withContext(Dispatchers.EDT) {
            WriteCommandAction.runWriteCommandAction(project) {
                val docManager = FileDocumentManager.getInstance()
                val document = docManager.getDocument(virtualFile) ?: return@runWriteCommandAction

                val text = processedContent.joinToString("\n")
                getMenuEditor()?.let {
                    foldingDiagnostics.record(it, "updateFile before replace changed=${document.text != text}")
                }
                if (document.text != text) {
                    document.replaceString(0, document.textLength, text)
                }
                getMenuEditor()?.let {
                    foldingDiagnostics.record(it, "updateFile after content")
                }
            }
        }

        return this
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
        if (this::virtualFile.isInitialized) virtualFile.putUserData(MenuFoldingBuilder.CONTEXT_KEY, null)
        menuFile = getMenuFile()
        virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(menuFile)
            ?: throw Exception("File is not found, this should not happen")
        virtualFile.putUserData(MenuFoldingBuilder.CONTEXT_KEY,
            MenuFoldingBuilder.Context(ProjectInfo.from(virtualFile.path), foldingSettings))
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
