package com.github.erotourtes.harpoon.actions

import com.github.erotourtes.harpoon.utils.notify
import com.github.erotourtes.harpoon.services.HarpoonService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service

class AddFileAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val harpoonService = HarpoonService.getInstance(project)
        val file = event.dataContext.getData(CommonDataKeys.PSI_FILE)

        if (harpoonService == null || file == null)
            return notify("File or project is not defined")

        harpoonService.addFile(file.virtualFile)
    }
}