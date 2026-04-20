package com.github.erotourtes.harpoon.services

import com.github.erotourtes.harpoon.listeners.FilesRenameListener
import com.github.erotourtes.harpoon.settings.SettingsChangeListener
import com.github.erotourtes.harpoon.settings.SettingsState
import com.github.erotourtes.harpoon.utils.FocusListener
import com.github.erotourtes.harpoon.utils.State
import com.github.erotourtes.harpoon.utils.menu.QuickMenu
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.*
import org.jetbrains.annotations.TestOnly

// TODO: folding builder
// TODO: fix bug with folds not closing after opening a file
// TODO: allow working with multiple file system protocols (temp/jar/file etc.)


@Service(Service.Level.PROJECT)
class HarpoonService(
    project: Project,
    private val scope: CoroutineScope,
) : Disposable {
    private val menu = QuickMenu(project, SettingsState.getInstance())
    private var state = State()
    private val fileEditorManager = FileEditorManager.getInstance(project)
    private val log = Logger.getInstance(HarpoonService::class.java)

    suspend fun init() {
        FilesRenameListener(this) { oldPath, newPath ->
            launch {
                onRenameFile(oldPath, newPath)
            }
        }
        FocusListener(this, menu::isMenuEditor)
        SettingsChangeListener(this) {
            launch {
                log.info("Settings changed")
                menu.updateSettings(it)
                menu.updateFile(getPaths())
            }
        }
        syncWithMenu()
    }

    suspend fun openMenu() = withSync {
        menu.open(getPaths())
    }

    suspend fun closeMenu() = withSync(syncWithMenuForce = true) {
        menu.close()
    }

    suspend fun toggleMenu() {
        if (menu.isOpen()) closeMenu() else openMenu()
    }

    suspend fun clearMenu() = withSync(syncWithMenu = false) {
        state.clear()
    }

    suspend fun addFile(file: VirtualFile) = withSync {
        state.add(file.path)
    }

    suspend fun removeFile(file: VirtualFile) = withSync {
        state.remove(file.path)
    }

    suspend fun toggleFile(file: VirtualFile) {
        val path = file.path
        if (state.includes(path)) {
            removeFile(file)
        } else {
            addFile(file)
        }
    }

    suspend fun openFile(index: Int) = withSync {
        openFileWithoutSync(index)
    }

    suspend fun replaceFile(index: Int, file: VirtualFile) = withSync {
        state.replace(index, file.path)
    }

    suspend fun nextFile() = withSync {
        val path = currentFilePath()
        val nextFileIndex = state.getNextIndexOf(path)
        if (nextFileIndex != -1) {
            openFileWithoutSync(nextFileIndex)
        }
    }

    suspend fun previousFile() = withSync {
        val path = currentFilePath()
        val nextFileIndex = state.getPrevIndexOf(path)
        if (nextFileIndex != -1) {
            openFileWithoutSync(nextFileIndex)
        }
    }

    suspend fun syncWithMenu() {
        val paths = menu.readLines()
        setPaths(paths)
    }

    fun getPaths(): List<String> = state.paths

    fun launch(action: suspend HarpoonService.() -> Unit): Job {
        val job = scope.launch {
            try {
                action()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log.error("Unknown error happened", e)
            }
        }
        return job
    }

    /**
     * Doesn't get the latest state from the menu contrary to {@link [openFile]}
     *
     * @throws [Exception] if file is not found or can't be opened
     */
    private suspend fun openFileWithoutSync(index: Int) {
        val file = getFile(index) ?: throw Exception("Can't find file")
        try {
            if (file.path == menu.virtualFile.path) {
                openMenu()
            } else {
                withContext(Dispatchers.EDT) {
                    fileEditorManager.openFile(file, true)
                }
            }
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

    private suspend fun onRenameFile(oldPath: String, newPath: String?) {
        val isDeleteEvent = newPath == null
        if (isDeleteEvent) {
            state.remove(oldPath)
        } else if (state.update(oldPath, newPath)) {
            menu.updateFile(getPaths())
        }
    }

    private suspend fun <T> withSync(
        syncWithMenu: Boolean = true,
        syncWithMenuForce: Boolean = false,
        updateMenu: Boolean = true,
        action: suspend () -> T,
    ): Result<T> {
        try {
            if (syncWithMenuForce || (syncWithMenu && menu.isMenuFileOpenedWithCurEditor())) {
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
            launch {
                menu.updateFile(getPaths())
            }
        } catch (e: Exception) {
            log.error("Filed to dispose the plugin", e)
        }
    }

    @TestOnly
    fun getMenVf(): VirtualFile {
        return menu.virtualFile
    }
}