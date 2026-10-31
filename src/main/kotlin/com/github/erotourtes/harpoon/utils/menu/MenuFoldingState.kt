package com.github.erotourtes.harpoon.utils.menu

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros

@Service(Service.Level.PROJECT)
@State(name = "HarpoonerMenuFolding", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class MenuFoldingState : PersistentStateComponent<MenuFoldingState.StoredState> {
    data class StoredState(var expanded: MutableSet<String> = linkedSetOf())

    private var storedState = StoredState()

    override fun getState(): StoredState = storedState

    override fun loadState(state: StoredState) {
        storedState = state
    }
}
