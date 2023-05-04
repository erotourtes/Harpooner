package com.github.erotourtes.harpoon.actions

import com.github.erotourtes.harpoon.factories.notify
import com.github.erotourtes.harpoon.services.HarpoonService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager

abstract class OpenFileAction : AnAction() {
    abstract fun index(): Int
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val harpoonService = project.service<HarpoonService>()
        val file = harpoonService?.getFile(index()) ?: return notify("Can't find file")
        
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