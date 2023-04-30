package com.github.erotourtes.jetbrainsharpoon.actions

import com.github.erotourtes.jetbrainsharpoon.factories.notify
import com.github.erotourtes.jetbrainsharpoon.services.HarpoonService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.LocalFileSystem

abstract class OpenFileAction : AnAction() {
    abstract fun index(): Int
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val harpoonService = project.service<HarpoonService>()
        val filePath = harpoonService?.getPath(index()) ?: return notify("Out of bounds")
        if (filePath.isEmpty()) return notify("File path is empty")
        val file = LocalFileSystem.getInstance().findFileByPath(filePath) ?: return notify("File path is corrupted")
        val fileManager = FileEditorManager.getInstance(project)
        fileManager.openFile(file, true)
    }
}

class OpenFileAction0 : OpenFileAction() {
    override fun index(): Int {
        return 0
    }
}

class OpenFileAction1 : OpenFileAction() {
    override fun index(): Int {
        return 1
    }
}

class OpenFileAction2 : OpenFileAction() {
    override fun index(): Int {
        return 2
    }
}

class OpenFileAction3 : OpenFileAction() {
    override fun index(): Int {
        return 3
    }
}