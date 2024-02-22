package com.github.erotourtes.harpoon.services

import com.github.erotourtes.harpoon.listeners.FilesRenameListener
import com.github.erotourtes.harpoon.utils.menu.QuickMenu
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile

// TODO: optimise live save of the menu
// TODO: optimise live save + editor focus close trigger 2 saves

@Service(Service.Level.PROJECT)
class HarpoonService(project: Project) : Disposable {
    private val menu = QuickMenu(project, this)
    private var state = State()
    private val fileEditorManager = FileEditorManager.getInstance(project)

    init {
        FilesRenameListener(::onRenameFile, this)
        syncWithMenu()
    }

    fun openMenu() {
        menu.open()
    }

    fun onMenuClose() {
        syncWithMenu()
    }

    fun syncWithMenuSafe() {
        ApplicationManager.getApplication().invokeLater {
            syncWithMenu()
        }
    }

    fun syncWithMenu() {
        setPaths(menu.readLines())
    }

    fun isMenuFile(path: String): Boolean = menu.isMenuFile(path)

    fun getPaths(): List<String> = state.paths

    fun addFile(file: VirtualFile): Unit = state.add(file.path)

    /**
     * @throws Exception if file is not found or can't be opened
     */
    fun openFile(index: Int) {
        val file = getFile(index) ?: throw Exception("Can't find file")
        try {
            fileEditorManager.openFile(file, true)
        } catch (e: Exception) {
            throw Exception("Can't find file. It might be deleted")
        }
    }

    fun setPaths(paths: List<String>): Unit = state.set(paths)

    val menuVF: VirtualFile get() = menu.virtualFile

    private fun getFile(index: Int): VirtualFile? = state.getFile(index)

    private fun onRenameFile(oldPath: String, newPath: String?) {
        val isDeleteEvent = newPath == null
        if (isDeleteEvent) state.remove(oldPath)
        else state.update(oldPath, newPath)

        menu.syncWithService()
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

        fun update(oldPath: String, newPath: String?) {
            val index = data.indexOf(oldPath)
            if (index == -1) return

            data[index] = newPath!!
            virtualFiles[newPath] = virtualFiles.remove(oldPath)
        }
    }

    companion object {
        fun getInstance(project: Project): HarpoonService {
            return project.service<HarpoonService>()
        }
    }

    // Needs for other classes to be able to register in Disposer
    override fun dispose() {}
}