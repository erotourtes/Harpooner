package com.github.erotourtes.jetbrainsharpoon.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

@State(name = "Harpoon_State", storages = [Storage("harpoon_state.xml")])
@Service(Service.Level.PROJECT)
class HarpoonService(val project: Project) : PersistentStateComponent<HarpoonService.State> {
    data class File(
        var path: String,
        var line: Int,
        var column: Int,
    )
    data class State(
        var data: ArrayList<File> = ArrayList()
    )

    private var state = State()

    override fun getState(): State {
        return state;
    }

    override fun loadState(state: State) {
        this.state = state
    }

    fun addFile(path: String, line: Int, column: Int) {
        state.data += File(path, line, column)
    }
}