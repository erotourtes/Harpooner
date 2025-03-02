package com.github.erotourtes.harpoon.actions.navigate

import com.github.erotourtes.harpoon.utils.notify
import com.github.erotourtes.harpoon.services.HarpoonService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service


class NextFileAction: AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val harpoonService = project.service<HarpoonService>()

        try {
            harpoonService.nextFile()
        } catch (e: Exception) {
            notify(e.message ?: "Error opening file")
        }
    }
}