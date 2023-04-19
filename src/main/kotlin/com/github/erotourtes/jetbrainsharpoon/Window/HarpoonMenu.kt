package com.github.erotourtes.jetbrainsharpoon.Window

import com.github.erotourtes.jetbrainsharpoon.services.HarpoonService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.EditorTextField
import com.intellij.ui.content.ContentFactory
import javax.swing.JComponent

class HarpoonMenu(val text: String) : DialogWrapper(true) {
    init {
        setSize(500, 500)
        title = "Harpoon Menu"
        init()
    }
    override fun createCenterPanel(): JComponent? {
        val textField = EditorTextField(text)
        textField.setOneLineMode(false)

        return textField
    }
}