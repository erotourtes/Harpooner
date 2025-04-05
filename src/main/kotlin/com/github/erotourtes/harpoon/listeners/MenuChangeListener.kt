package com.github.erotourtes.harpoon.listeners

import com.github.erotourtes.harpoon.services.HarpoonService
import com.github.erotourtes.harpoon.utils.Debouncer
import com.github.erotourtes.harpoon.utils.TYPING_DEBOUNCE_MS
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener

class MenuChangeListener(
    private val harpoonService: HarpoonService,
    private val document: Document
) : DocumentListener, Disposable {
    private val log = Logger.getInstance(MenuChangeListener::class.java)
    private val debouncer = Debouncer(this, TYPING_DEBOUNCE_MS)
    private var isAttached = false

    fun attach() {
        if (isAttached) return
        document.addDocumentListener(this)
        isAttached = true
        log.info("Attached")
    }

    fun detach() {
        if (!isAttached) return
        document.removeDocumentListener(this)
        isAttached = false
        log.info("Detached")
    }

    override fun documentChanged(event: DocumentEvent) {
        debouncer.debounce {
            harpoonService.syncWithMenuSafe()
        }
    }

    override fun dispose() {
        detach()
        log.info("Disposed")
    }
}