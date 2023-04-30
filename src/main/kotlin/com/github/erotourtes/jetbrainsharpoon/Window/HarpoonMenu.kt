package com.github.erotourtes.jetbrainsharpoon.Window

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.EditorTextField
import javax.swing.JComponent

class HarpoonMenu(private val text: String) : DialogWrapper(true) {
    private var textField: EditorTextField? = null
    init {
        setSize(500, 300)
        title = "Harpoon Menu"
        init()
    }
    override fun createCenterPanel(): JComponent? {
        textField = EditorTextField(text)
        textField!!.setOneLineMode(false)

        return textField
    }

    override fun createSouthPanel(): JComponent? {
        return null
    }

    fun getPaths(): List<String> {
        val str = textField?.text?.trim() ?: return listOf()
        return str.split("\n").map { it.trim() }
    }
}