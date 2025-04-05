package com.github.erotourtes.harpoon.services

import com.github.erotourtes.harpoon.listeners.FilesRenameListener
import com.github.erotourtes.harpoon.settings.SettingsChangeListener
import com.github.erotourtes.harpoon.utils.FileTypingChangeHandler
import com.github.erotourtes.harpoon.utils.FocusListener
import com.github.erotourtes.harpoon.utils.menu.QuickMenu
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile

// TODO: optimise live save of the menu
// TODO: folding builder
// TODO: optimise live save + editor focus close trigger 2 saves
// TODO: fix rare bug with menu is overwriting itself
// TODO: fix bug with folds not closing after opening a file


@Service(Service.Level.PROJECT)
class HarpoonService(project: Project) : Disposable {
    private val menu = QuickMenu(project)
    private var state = State()
    private val fileEditorManager = FileEditorManager.getInstance(project)
    private val log = Logger.getInstance(HarpoonService::class.java)

    init {
        FilesRenameListener(::onRenameFile, this)
        FocusListener(this, menu::isMenuEditor)
        SettingsChangeListener(this) {
            log.info("Settings changed")
            menu.updateSettings(it)
            menu.updateFile(getPaths())
        }
        FileTypingChangeHandler(this) { menu.virtualFile }
        syncWithMenu()
    }

    fun openMenu() {
        menu.open(getPaths())
    }

    fun closeMenu() {
        menu.close()
    }

    fun toggleMenu() {
        if (menu.isOpen()) closeMenu() else openMenu()
    }

    fun syncWithMenu() {
        setPaths(menu.readLines())
    }

    fun getPaths(): List<String> = state.paths

    fun addFile(file: VirtualFile): Unit = state.add(file.path)

    fun removeFile(file: VirtualFile): Unit = state.remove(file.path)

    fun toggleFile(file: VirtualFile) {
        val path = file.path
        if (state.includes(path)) {
            state.remove(path)
        } else {
            state.add(path)
        }
    }

    /**
     * @throws Exception if file is not found or can't be opened
     */
    fun openFile(index: Int) {
        val file = getFile(index) ?: throw Exception("Can't find file")
        try {
            if (file.path == menu.virtualFile.path) openMenu()
            else fileEditorManager.openFile(file, true)
        } catch (e: Exception) {
            throw Exception("Can't find file. It might be deleted")
        }
    }

    fun nextFile() {
        val currentIndex = currentIndex()
        if (currentIndex != -1 && state.size() > 0) {
            val nextIndex = (currentIndex + 1) % state.size()
            openFile(nextIndex)
        }
    }

    fun previousFile() {
        val currentIndex = currentIndex()
        if (currentIndex != -1 && state.size() > 0) {
            val prevIndex = (currentIndex - 1 + state.size()) % state.size()
            openFile(prevIndex)
        }
    }

    private fun currentIndex(): Int {
        val currentFile = fileEditorManager.selectedEditor?.file
        val currentFilePath = currentFile?.path
        val currentIndex = state.paths.indexOf(currentFilePath)
        return currentIndex
    }

    private fun setPaths(paths: List<String>): Unit = state.set(paths)

    private fun getFile(index: Int): VirtualFile? = state.getFile(index)

    private fun onRenameFile(oldPath: String, newPath: String?) {
        val isDeleteEvent = newPath == null
        if (isDeleteEvent) state.remove(oldPath)
        else if (state.update(
                oldPath, newPath
            )
        ) // TODO: somehow rename listener can go crazy and spam file change events
            menu.updateFile(getPaths())
    }

    class State {
        private var data: ArrayList<String> = ArrayList()
        private val virtualFiles = mutableMapOf<String, VirtualFile?>()

        val paths: List<String> get() = data.toList()

        fun getFile(index: Int): VirtualFile? {
            val path = data.getOrNull(index) ?: return null
            return virtualFiles.getOrPut(path) { LocalFileSystem.getInstance().findFileByPath(path) }
        }

        fun set(newPaths: List<String>) {
            val filtered = newPaths.filter { it.isNotEmpty() }.distinct()
            data = ArrayList(filtered)
        }

        fun add(path: String) {
            if (data.contains(path)) return
            data.add(path)
            virtualFiles[path] = LocalFileSystem.getInstance().findFileByPath(path)
        }

        fun remove(path: String) {
            val index = data.indexOf(path)
            if (index == -1) return
            data.removeAt(index)
            virtualFiles.remove(path)
        }

        fun includes(path: String): Boolean = data.indexOf(path) != -1

        fun update(oldPath: String, newPath: String?): Boolean {
            val index = data.indexOf(oldPath)
            if (index == -1) return false

            data[index] = newPath!!
            virtualFiles[newPath] = virtualFiles.remove(oldPath)
            return true
        }

        fun size(): Int = data.size

    }

    companion object {
        fun getInstance(project: Project): HarpoonService {
            return project.service<HarpoonService>()
        }
    }

    // Needs for other classes to be able to register in Disposer
    override fun dispose() {}
}