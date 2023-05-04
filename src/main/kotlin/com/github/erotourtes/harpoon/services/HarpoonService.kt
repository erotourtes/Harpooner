package com.github.erotourtes.harpoon.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile

@State(name = "Harpoon_State", storages = [Storage("harpoon_state.xml")])
@Service(Service.Level.PROJECT)
class HarpoonService(val project: Project) : PersistentStateComponent<HarpoonService.State> {
    private val virtualFiles = mutableMapOf<String, VirtualFile?>()
    private var state = State()
    override fun getState(): State {
        return state
    }

    override fun loadState(state: State) {
        this.state = state
    }

    fun addFile(file: VirtualFile) {
        val path = file.path
        if (state.data.any { it == path }) return
        state.data += path;
        virtualFiles[path] = file
    }

    fun getPaths(): List<String> {
        return state.data
    }

    fun getFile(index: Int): VirtualFile? {
        val path = state.data.getOrNull(index) ?: return null
        if (path.isEmpty()) return null
        if (virtualFiles[path] == null)
            virtualFiles[path] = LocalFileSystem.getInstance().findFileByPath(path)

        return virtualFiles.getOrDefault(path, null)
    }

    fun setPaths(paths: List<String>) {
        if (paths != state.data)
            state.data = ArrayList(paths)
    }

    class State {
        var data: ArrayList<String> = ArrayList()
    }
}