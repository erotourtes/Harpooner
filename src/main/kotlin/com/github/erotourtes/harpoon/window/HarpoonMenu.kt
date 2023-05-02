package com.github.erotourtes.harpoon.window

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.EditorTextField
import javax.swing.JComponent

class HarpoonMenu(private val text: String, private val fontSize: Int) : DialogWrapper(true) {
    private var textField: EditorTextField? = null
    init {
        setSize(500, 300)
        title = "Harpoon Menu"
        setUndecorated(false)
//        this.rootPane
        init()
    }
    override fun createCenterPanel(): JComponent? {
        textField = EditorTextField(text)
        textField!!.setOneLineMode(false)
        textField!!.addSettingsProvider { provider ->
            provider.setFontSize(fontSize)
            provider.settings.isLineNumbersShown = true
        }
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