package com.github.erotourtes.harpoon.utils

import com.github.erotourtes.harpoon.services.HarpoonService
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.FocusChangeListener

class FocusListener(private val harpoonService: HarpoonService, private val isMenuFile: (Editor) -> Boolean) : FocusChangeListener {
    private var isHarpoonerPrevFocused = false

    override fun focusGained(editor: Editor) {
        super.focusGained(editor)

        val isRefocusOnMenu = isHarpoonerPrevFocused && isMenuFile(editor)
        if (!isHarpoonerPrevFocused || isRefocusOnMenu) return

        harpoonService.syncWithMenu()
        harpoonService.closeMenu()
        isHarpoonerPrevFocused = false
    }


    override fun focusLost(editor: Editor) {
        // can't directly call, because it's fired on every focus lost (even if I didn't switch the editor e.x. focused the tree view)
        super.focusLost(editor)
        isHarpoonerPrevFocused = isMenuFile(editor)
    }
}