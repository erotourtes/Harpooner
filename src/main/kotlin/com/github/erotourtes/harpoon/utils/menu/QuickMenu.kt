package com.github.erotourtes.harpoon.utils.menu

import com.github.erotourtes.harpoon.listeners.FileEditorListener
import com.github.erotourtes.harpoon.listeners.MenuChangeListener
import com.github.erotourtes.harpoon.services.HarpoonService
import com.github.erotourtes.harpoon.services.settings.SettingsState
import com.github.erotourtes.harpoon.utils.IDEA_PROJECT_FOLDER
import com.github.erotourtes.harpoon.utils.ListenerManager
import com.github.erotourtes.harpoon.utils.MENU_NAME
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.EditorEventMulticasterEx
import com.intellij.openapi.editor.ex.FocusChangeListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.messages.MessageBusConnection
import java.io.File


// TODO: think about settings encapsulation
class QuickMenu(private val project: Project, private val harpoonService: HarpoonService) : Disposable {
    val projectInfo: ProjectInfo
    private lateinit var menuFile: File
    lateinit var virtualFile: VirtualFile
        private set
    private val foldManager: FoldManager
    private var processor: PathsProcessor
    private val dbusListener = DbusListener()
    private val listenerManager = ListenerManager()

    init {
        Disposer.register(harpoonService, this)
    }

    init {
        initMenuFile()
        projectInfo = ProjectInfo.from(virtualFile.path)

        val settings = SettingsState.getInstance()
        listenToSettingsChange(settings)
        listenToMenuTypingChange(settings)
        listenToEditorFocus()

        foldManager = FoldManager(this, project)
        processor = PathsProcessor(projectInfo)
    }

    fun readLines(): List<String> {
        val docManager = FileDocumentManager.getInstance()
        val document = docManager.getDocument(virtualFile) ?: throw Error("Can't read file")

        return document.text.split("\n").map { processor.unprocess(it) }
    }

    fun isMenuFile(path: String): Boolean = path == menuFile.path

    fun connectListener(): QuickMenu {
        dbusListener.connect()
        return this
    }

    fun disconnectListener(): QuickMenu {
        dbusListener.disconnect()

        return this
    }

    fun open(): QuickMenu {
        val fileManager = FileEditorManager.getInstance(project)

        if (!virtualFile.isValid) initMenuFile()

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

    private fun listenToEditorFocus() {
        val multicaster = EditorFactory.getInstance().eventMulticaster
        val listener = FocusListener()
        if (multicaster !is EditorEventMulticasterEx) {
            println("EditorEventMulticasterEx is not supported")
            return
        }

        multicaster.addFocusChangeListener(listener, this)
    }

    private fun listenToMenuTypingChange(settings: SettingsState) {
        val menuDocument = FileDocumentManager.getInstance().getDocument(virtualFile)
            ?: throw Error("Can't get document of the ${virtualFile.path} file")
        val documentListener = MenuChangeListener(harpoonService, menuDocument)

        val updateTypingListener = { newSettings: SettingsState ->
            if (newSettings.isSavingOnTyping) documentListener.attach()
            else documentListener.detach()
        }

        updateTypingListener(settings)

        val settingsDisposable = settings.addObserver { updateTypingListener(it) }

        listenerManager.addDisposable {
            documentListener.dispose()
            settingsDisposable()
        }
    }

    private fun listenToSettingsChange(settings: SettingsState) {
        val disposable = settings.addObserver { updateSettings(it) }
        listenerManager.addDisposable(disposable)
    }

    private fun updateSettings(settings: SettingsState) {
        foldManager.updateSettings(settings)

        processor.updateSettings(settings)
        updateFile(harpoonService.getPaths())
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

    override fun dispose() {
        disconnectListener()
        listenerManager.disposeAllListeners()
    }

    private class DbusListener {
        private var connection: MessageBusConnection? = null
        private val listener by lazy { FileEditorListener() }

        fun connect() {
            if (connection != null) return
            connection = ApplicationManager.getApplication().messageBus.connect()
            connection!!.subscribe(
                FileEditorManagerListener.FILE_EDITOR_MANAGER, listener
            )
        }

        fun disconnect() {
            connection?.disconnect()
            connection = null
        }
    }

    private inner class FocusListener : FocusChangeListener {
        override fun focusLost(editor: Editor) {
            super.focusLost(editor)
            if (!isMenuFileOpenedWith(editor)) return

            harpoonService.syncWithMenu()
            foldManager.collapseAllFolds()
        }
    }
}