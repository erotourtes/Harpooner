package com.github.erotourtes.jetbrainsharpoon.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import  com.github.erotourtes.jetbrainsharpoon.Window.HarpoonMenu
import com.github.erotourtes.jetbrainsharpoon.services.HarpoonService
import com.intellij.openapi.components.service

class QuickMenuAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val harpoonService = project.service<HarpoonService>()
        val savedPaths = harpoonService.getPaths()

        val pathsStr = savedPaths.joinToString(separator = "\n")
        val menu = HarpoonMenu(pathsStr)
        menu.showAndGet()
        var paths = menu.getPaths()

        if (paths != savedPaths)
            harpoonService.setPaths(paths)
    }
}