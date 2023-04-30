package com.github.erotourtes.jetbrainsharpoon.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import  com.github.erotourtes.jetbrainsharpoon.window.HarpoonMenu
import com.github.erotourtes.jetbrainsharpoon.services.HarpoonService
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service

class QuickMenuAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val harpoonService = project.service<HarpoonService>()
        val savedPaths = harpoonService.getPaths()

        val pathsStr = savedPaths.joinToString(separator = "\n")
        val fontSize = e.getRequiredData(CommonDataKeys.EDITOR).colorsScheme.editorFontSize
        val menu = HarpoonMenu(pathsStr, fontSize)
        menu.showAndGet()
        var paths = menu.getPaths()

        if (paths != savedPaths)
            harpoonService.setPaths(paths)
    }
}