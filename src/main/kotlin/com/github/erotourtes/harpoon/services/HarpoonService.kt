package com.github.erotourtes.harpoon.services

import com.github.erotourtes.harpoon.listeners.FilesRenameListener
import com.github.erotourtes.harpoon.settings.SettingsChangeListener
import com.github.erotourtes.harpoon.settings.SettingsState
import com.github.erotourtes.harpoon.utils.FocusListener
import com.github.erotourtes.harpoon.utils.State
import com.github.erotourtes.harpoon.utils.menu.QuickMenu
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.*
import org.jetbrains.annotations.TestOnly
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.coroutineContext

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
    private val dispatcher = Dispatchers.Default.limitedParallelism(1)
    @Volatile
    private var pathsSnapshot: List<String> = emptyList()
    private val fileEditorManager = FileEditorManager.getInstance(project)
    private val log = Logger.getInstance(HarpoonService::class.java)

    suspend fun init() = withServiceContext {
        withContext(Dispatchers.EDT) {
            FilesRenameListener(this@HarpoonService) { oldPath, newPath ->
                launch {
                    onRenameFile(oldPath, newPath)
                }
            }
            FocusListener(this@HarpoonService, menu::isMenuEditor)
            SettingsChangeListener(this@HarpoonService) {
                launch {
                    log.info("Settings changed")
                    menu.updateSettings(it)
                    menu.updateFile(getPaths())
                }
            }
        }
        syncWithMenu()
    }

    suspend fun openMenu() = withServiceContext {
        withSync {
            menu.open(getPaths())
        }
    }

    suspend fun closeMenu() = withServiceContext {
        withSync(syncWithMenuForce = true) {
            menu.close()
        }
    }

    suspend fun toggleMenu() = withServiceContext {
        if (menu.isOpen()) closeMenu() else openMenu()
    }

    suspend fun clearMenu() = withServiceContext {
        withSync(syncWithMenu = false) {
            state.clear()
            refreshPathsSnapshot()
        }
    }

    suspend fun addFile(file: VirtualFile) = withServiceContext {
        withSync {
            state.add(file.path)
            refreshPathsSnapshot()
        }
    }

    suspend fun removeFile(file: VirtualFile) = withServiceContext {
        withSync {
            state.remove(file.path)
            refreshPathsSnapshot()
        }
    }

    suspend fun toggleFile(file: VirtualFile) = withServiceContext {
        val path = file.path
        if (state.includes(path)) {
            removeFile(file)
        } else {
            addFile(file)
        }
    }

    suspend fun openFile(index: Int) = withServiceContext {
        withSync {
            openFileWithoutSync(index)
        }
    }

    suspend fun replaceFile(index: Int, file: VirtualFile) = withServiceContext {
        withSync {
            state.replace(index, file.path)
            refreshPathsSnapshot()
        }
    }

    suspend fun nextFile() = withServiceContext {
        withSync {
            val path = currentFilePath()
            val nextFileIndex = state.getNextIndexOf(path)
            if (nextFileIndex != -1) {
                openFileWithoutSync(nextFileIndex)
            }
        }
    }

    suspend fun previousFile() = withServiceContext {
        withSync {
            val path = currentFilePath()
            val nextFileIndex = state.getPrevIndexOf(path)
            if (nextFileIndex != -1) {
                openFileWithoutSync(nextFileIndex)
            }
        }
    }

    suspend fun syncWithMenu() {
        val paths = menu.readLines()
        setPaths(paths)
    }

    fun getPaths(): List<String> = pathsSnapshot

    fun launch(action: suspend HarpoonService.() -> Unit): Job {
        val job = scope.launch(dispatcher) {
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


    private suspend fun currentFilePath(): String? = withContext(Dispatchers.EDT) {
        val currentFile = fileEditorManager.selectedEditor?.file
        return@withContext currentFile?.path
    }

    private fun setPaths(paths: List<String>) {
        state.set(paths)
        refreshPathsSnapshot()
    }

    private fun refreshPathsSnapshot() {
        pathsSnapshot = state.paths
    }

    private fun getFile(index: Int): VirtualFile? = state.getFile(index)

    private suspend fun onRenameFile(oldPath: String, newPath: String?) {
        val isDeleteEvent = newPath == null
        if (isDeleteEvent) {
            state.remove(oldPath)
            refreshPathsSnapshot()
        } else if (state.update(oldPath, newPath)) {
            refreshPathsSnapshot()
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
            val application = ApplicationManager.getApplication()
            val paths = getPaths()
            if (application.isDispatchThread) {
                menu.updateFileOnEdt(paths)
            } else {
                application.invokeAndWait {
                    menu.updateFileOnEdt(paths)
                }
            }
        } catch (e: Exception) {
            log.error("Filed to dispose the plugin", e)
        }
    }

    @TestOnly
    fun getMenVf(): VirtualFile {
        return menu.virtualFile
    }

    @TestOnly
    suspend fun awaitIdle() = withServiceContext {
        Unit
    }

    private suspend fun <T> withServiceContext(action: suspend HarpoonService.() -> T): T {
        if (coroutineContext[ContinuationInterceptor] === dispatcher) {
            return action()
        }
        return withContext(dispatcher) {
            action()
        }
    }
}
