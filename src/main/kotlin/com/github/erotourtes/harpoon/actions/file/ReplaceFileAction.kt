package com.github.erotourtes.harpoon.actions.file

import com.github.erotourtes.harpoon.services.HarpoonService
import com.github.erotourtes.harpoon.utils.notify
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service

abstract class ReplaceFileAction : AnAction() {
    abstract fun index(): Int
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val harpoonService = project.service<HarpoonService>()
        val file = event.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return notify("File or project is not defined")

        harpoonService.replaceFile(index(), file.virtualFile)
    }
}

class ReplaceFileAction0 : ReplaceFileAction() {
    override fun index(): Int = 0
}

class ReplaceFileAction1 : ReplaceFileAction() {
    override fun index(): Int = 1
}

class ReplaceFileAction2 : ReplaceFileAction() {
    override fun index(): Int = 2
}

class ReplaceFileAction3 : ReplaceFileAction() {
    override fun index(): Int = 3
}

class ReplaceFileAction4 : ReplaceFileAction() {
    override fun index(): Int = 4
}

class ReplaceFileAction5 : ReplaceFileAction() {
    override fun index(): Int = 5
}

class ReplaceFileAction6 : ReplaceFileAction() {
    override fun index(): Int = 6
}

class ReplaceFileAction7 : ReplaceFileAction() {
    override fun index(): Int = 7
}

class ReplaceFileAction8 : ReplaceFileAction() {
    override fun index(): Int = 8
}

class ReplaceFileAction9 : ReplaceFileAction() {
    override fun index(): Int = 9
}
