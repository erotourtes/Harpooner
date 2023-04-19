package com.github.erotourtes.jetbrainsharpoon.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import javax.swing.JLabel
import  com.github.erotourtes.jetbrainsharpoon.Window.HarpoonMenu

class QuickMenuAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val menu = HarpoonMenu("Hello world")
        menu.showAndGet()


    }

}