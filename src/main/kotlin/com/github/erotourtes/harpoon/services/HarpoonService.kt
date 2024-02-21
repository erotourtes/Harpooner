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

// TODO: optimise live save of the meny
// TODO: optimise live save + editor focus close trigger 2 saves

@Service(Service.Level.PROJECT)
class HarpoonService(project: Project) : Disposable {
    private val menu = QuickMenu(project, this)
    private val virtualFiles = mutableMapOf<String, VirtualFile?>()
    private var state = State()
    private val fileEditorManager = FileEditorManager.getInstance(project)

    init {
        FilesRenameListener(::onRenameFile, this)
        connectListenersIfMenuIsOpened()
        syncWithMenu()
    }

    fun openMenu() {
        menu.open().connectListener()
    }

    fun onMenuClose() {
        syncWithMenu()
        menu.disconnectListener()
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

    fun getPaths(): List<String> = state.data.toList()

    fun addFile(file: VirtualFile) {
        val path = file.path
        if (virtualFiles[path] != null || state.data.any { it == path }) return
        state.data += path
        virtualFiles[path] = file
    }

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

    fun setPaths(paths: List<String>) {
        val filtered = paths.filter { it.isNotEmpty() }.distinct()
        if (filtered != state.data) state.data = ArrayList(filtered)
    }

    val menuVF: VirtualFile get() = menu.virtualFile

    private fun getFile(index: Int): VirtualFile? {
        val path = state.data.getOrNull(index) ?: return null
        if (path.isEmpty()) return null
        if (virtualFiles[path] == null) virtualFiles[path] = LocalFileSystem.getInstance().findFileByPath(path)

        return virtualFiles.getOrDefault(path, null)
    }

    private fun onRenameFile(oldPath: String, newPath: String?) {
        val index = state.data.indexOf(oldPath)
        if (index == -1) return

        val isDeleteEvent = newPath == null
        if (isDeleteEvent) {
            state.data.removeAt(index)
            virtualFiles.remove(oldPath)
            menu.syncWithService()
            return
        }

        state.data[index] = newPath!!
        virtualFiles[newPath] = virtualFiles.remove(oldPath)
        menu.syncWithService()
    }

    private fun connectListenersIfMenuIsOpened() {
        if (fileEditorManager.isFileOpen(menu.virtualFile)) {
            fileEditorManager.closeFile(menu.virtualFile)
            menu.connectListener()
        }
    }

    class State {
        var data: ArrayList<String> = ArrayList()
    }

    companion object {
        fun getInstance(project: Project): HarpoonService {
            return project.service<HarpoonService>()
        }
    }

    // Needs for other classes to be able to register in Disposer
    override fun dispose() {}
}