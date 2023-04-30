package com.github.erotourtes.jetbrainsharpoon.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

@State(name = "Harpoon_State", storages = [Storage("harpoon_state.xml")])
@Service(Service.Level.PROJECT)
class HarpoonService(val project: Project) : PersistentStateComponent<HarpoonService.State> {
    class State {
        var data: ArrayList<String> = ArrayList()
    }

    private var state = State()
    override fun getState(): State {
        return state
    }

    override fun loadState(state: State) {
        this.state = state
    }

    fun addFile(path: String) {
        if (state.data.any { it == path }) return
        state.data += path;
    }

    fun getPaths(): List<String> {
        return state.data
    }

    fun getPath(index: Int): String? {
        return state.data.getOrNull(index)
    }

    fun setPaths(paths: List<String>) {
        state.data = ArrayList(paths)
    }
}