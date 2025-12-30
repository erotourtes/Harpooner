package com.github.erotourtes.harpoon.services

import com.github.erotourtes.harpoon.listeners.FilesRenameListener
import com.github.erotourtes.harpoon.settings.SettingsChangeListener
import com.github.erotourtes.harpoon.settings.SettingsState
import com.github.erotourtes.harpoon.utils.FocusListener
import com.github.erotourtes.harpoon.utils.State
import com.github.erotourtes.harpoon.utils.menu.QuickMenu
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.TestOnly

// TODO: folding builder
// TODO: fix bug with folds not closing after opening a file
// TODO: allow working with multiple file system protocols (temp/jar/file etc.)


@Service(Service.Level.PROJECT)
class HarpoonService(project: Project) : Disposable {
    private val menu = QuickMenu(project, SettingsState.getInstance())
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
        syncWithMenu()
    }

    fun openMenu() = withSync {
        menu.open(getPaths())
    }

    fun closeMenu() = withSync {
        menu.close()
    }

    fun toggleMenu() {
        if (menu.isOpen()) closeMenu() else openMenu()
    }

    fun clearMenu() = withSync(syncWithMenu = false) {
        state.clear()
    }

    fun addFile(file: VirtualFile) = withSync {
        state.add(file.path)
    }

    fun removeFile(file: VirtualFile) = withSync {
        state.remove(file.path)
    }

    fun toggleFile(file: VirtualFile) {
        val path = file.path
        if (state.includes(path)) {
            removeFile(file)
        } else {
            addFile(file)
        }
    }

    fun openFile(index: Int) = withSync {
        openFileWithoutSync(index)
    }

    fun replaceFile(index: Int, file: VirtualFile) = withSync {
        state.replace(index, file.path)
    }

    fun nextFile() = withSync {
        val path = currentFilePath()
        val nextFileIndex = state.getNextIndexOf(path)
        if (nextFileIndex != -1) {
            openFileWithoutSync(nextFileIndex)
        }
    }

    fun previousFile() = withSync {
        val path = currentFilePath()
        val nextFileIndex = state.getPrevIndexOf(path)
        if (nextFileIndex != -1) {
            openFileWithoutSync(nextFileIndex)
        }
    }

    fun syncWithMenu() {
        val paths = menu.readLines()
        setPaths(paths)
    }

    fun getPaths(): List<String> = state.paths

    /**
     * Doesn't get the latest state from the menu contrary to {@link [openFile]}
     *
     * @throws [Exception] if file is not found or can't be opened
     */
    private fun openFileWithoutSync(index: Int) {
        val file = getFile(index) ?: throw Exception("Can't find file")
        try {
            if (file.path == menu.virtualFile.path) openMenu()
            else fileEditorManager.openFile(file, true)
        } catch (e: Exception) {
            throw Exception("Can't find file. It might be deleted", e)
        }
    }


    private fun currentFilePath(): String? {
        val currentFile = fileEditorManager.selectedEditor?.file
        val currentFilePath = currentFile?.path
        return currentFilePath
    }

    private fun setPaths(paths: List<String>): Unit = state.set(paths)

    private fun getFile(index: Int): VirtualFile? = state.getFile(index)

    private fun onRenameFile(oldPath: String, newPath: String?) {
        val isDeleteEvent = newPath == null
        if (isDeleteEvent) {
            state.remove(oldPath)
        } else if (state.update(oldPath, newPath)) {
            menu.updateFile(getPaths())
        }
    }

    private fun <T> withSync(
        syncWithMenu: Boolean = true,
        updateMenu: Boolean = true,
        action: () -> T,
    ): Result<T> {
        try {
            if (syncWithMenu && menu.isMenuFileOpenedWithCurEditor()) {
                syncWithMenu()
            }
        } catch (e: Exception) {
            log.error("Could not sync with menu", e)
        }

        val result = runCatching {
            action()
        }

        try {
            if (updateMenu && menu.isMenuFileOpenedWithCurEditor()) {
                menu.updateFile(getPaths())
            }
        } catch (e: Exception) {
            log.error("Could not update menu file", e)
        }

        return result
    }

    companion object {
        fun getInstance(project: Project): HarpoonService {
            return project.service<HarpoonService>()
        }
    }

    override fun dispose() {
        log.debug("dispose")
        try {
            menu.updateFile(getPaths())
        } catch (e: Exception) {
            log.error("Filed to dispose the plugin", e)
        }
    }

    @TestOnly
    fun getMenVf(): VirtualFile {
        return menu.virtualFile
    }
}