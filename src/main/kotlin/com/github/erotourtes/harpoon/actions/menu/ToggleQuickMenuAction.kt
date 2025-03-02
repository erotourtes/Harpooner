package com.github.erotourtes.harpoon.actions.menu

import com.github.erotourtes.harpoon.services.HarpoonService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class ToggleQuickMenuAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val harpoonService = project.service<HarpoonService>()
        harpoonService.toggleMenu()
    }
}