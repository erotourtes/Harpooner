package com.github.erotourtes.harpoon.utils

import com.github.erotourtes.harpoon.services.HarpoonService
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.EditorEventMulticasterEx
import com.intellij.openapi.editor.ex.FocusChangeListener

class FocusListener(private val harpoonService: HarpoonService, private val isMenuFile: (Editor) -> Boolean) :
    FocusChangeListener {
    private val log = Logger.getInstance(FocusListener::class.java)
    private var isHarpoonerPrevFocused = false

    init {
        listenToEditorFocus()
    }

    private fun listenToEditorFocus() {
        val multicaster = EditorFactory.getInstance().eventMulticaster
        if (multicaster !is EditorEventMulticasterEx) {
            log.error("EditorEventMulticasterEx is not supported")
            return
        }

        multicaster.addFocusChangeListener(this, harpoonService)
    }

    override fun focusGained(editor: Editor) {
        log.info("Focus gained")
        val isRefocusOnMenu = isHarpoonerPrevFocused && isMenuFile(editor)
        if (!isHarpoonerPrevFocused || isRefocusOnMenu) return

        log.info("Close menu")
        harpoonService.closeMenu()
        isHarpoonerPrevFocused = false
    }


    override fun focusLost(editor: Editor) {
        // can't directly call, because it's fired on every focus lost (even if I didn't switch the editor e.x. focused the tree view)
        isHarpoonerPrevFocused = isMenuFile(editor)
        log.info("Focus lost")
    }
}