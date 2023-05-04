package com.github.erotourtes.harpoon.actions

import com.github.erotourtes.harpoon.factories.notify
import com.github.erotourtes.harpoon.services.HarpoonService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service

class AddFileAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val harpoonService = event.project?.service<HarpoonService>()
        val file = event.dataContext.getData(CommonDataKeys.PSI_FILE)

        if (harpoonService == null || file == null)
            return notify("File or project is not defined")

        harpoonService.addFile(file.virtualFile)
    }
}