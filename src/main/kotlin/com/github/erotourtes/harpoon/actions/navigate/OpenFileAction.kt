package com.github.erotourtes.harpoon.actions.navigate

import com.github.erotourtes.harpoon.services.HarpoonService
import com.github.erotourtes.harpoon.utils.notify
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

abstract class OpenFileAction : AnAction() {
    abstract fun index(): Int
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val harpoonService = project.service<HarpoonService>()

        harpoonService.openFile(index()).onFailure {
            notify(it.message ?: "Error opening file")
        }
    }
}

class OpenFileAction0 : OpenFileAction() {
    override fun index(): Int = 0
}

class OpenFileAction1 : OpenFileAction() {
    override fun index(): Int = 1
}

class OpenFileAction2 : OpenFileAction() {
    override fun index(): Int = 2
}

class OpenFileAction3 : OpenFileAction() {
    override fun index(): Int = 3
}

class OpenFileAction4 : OpenFileAction() {
    override fun index(): Int = 4
}

class OpenFileAction5 : OpenFileAction() {
    override fun index(): Int = 5
}

class OpenFileAction6 : OpenFileAction() {
    override fun index(): Int = 6
}

class OpenFileAction7 : OpenFileAction() {
    override fun index(): Int = 7
}

class OpenFileAction8 : OpenFileAction() {
    override fun index(): Int = 8
}

class OpenFileAction9 : OpenFileAction() {
    override fun index(): Int = 9
}