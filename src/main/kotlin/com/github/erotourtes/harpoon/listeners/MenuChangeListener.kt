package com.github.erotourtes.harpoon.listeners

import com.github.erotourtes.harpoon.services.HarpoonService
import com.github.erotourtes.harpoon.utils.TYPING_DEBOUNCE_MS
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class Debouncer(
    private val ms: Long
) {
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var future: ScheduledFuture<*>? = null

    fun debounce(action: () -> Unit) {
        future?.cancel(false)
        future = executor.schedule(action, ms, TimeUnit.MILLISECONDS)
    }

    fun close() {
        executor.shutdown()
    }
}

class MenuChangeListener(
    private val harpoonService: HarpoonService,
    private val document: Document
) : DocumentListener, Disposable {
    private val debouncer = Debouncer(TYPING_DEBOUNCE_MS)

    private var isAttached = false

    fun attach() {
        if (isAttached) return
        document.addDocumentListener(this)
        isAttached = true
    }

    fun detach() {
        if (!isAttached) return
        document.removeDocumentListener(this)
        isAttached = false
    }

    override fun documentChanged(event: DocumentEvent) {
        debouncer.debounce {
            harpoonService.syncWithMenuSafe()
        }
    }

    override fun dispose() {
        detach()
        debouncer.close()
    }
}