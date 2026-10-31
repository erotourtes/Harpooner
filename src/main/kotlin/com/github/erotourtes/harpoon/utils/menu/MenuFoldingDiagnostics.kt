package com.github.erotourtes.harpoon.utils.menu

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.FoldRegion
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.editor.ex.FoldingListener
import com.intellij.openapi.editor.ex.FoldingModelEx
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import java.util.IdentityHashMap

/** Temporary, bounded diagnostics for the menu's unexpected fold expansion. */
class MenuFoldingDiagnostics(
    private val parentDisposable: Disposable,
    private val output: (String, Throwable?) -> Unit = { message, trace -> LOG.info(message, trace) },
) {
    private val traceKey = Key.create<Trace>("harpoon.menu.folding.trace")

    init {
        EditorFactory.getInstance().addEditorFactoryListener(object : EditorFactoryListener {
            override fun editorReleased(event: EditorFactoryEvent) {
                val trace = event.editor.getUserData(traceKey) ?: return
                trace.write("EDITOR_RELEASED")
                Disposer.dispose(trace.disposable)
                event.editor.putUserData(traceKey, null)
            }
        }, parentDisposable)
    }

    fun record(editor: Editor, phase: String) {
        if (editor.isDisposed) return
        val trace = editor.getUserData(traceKey) ?: Trace(editor).also {
            editor.putUserData(traceKey, it)
        }
        trace.write("PHASE $phase folds=" + editor.foldingModel.allFoldRegions.take(20).joinToString { region ->
            "${identity(region)}:${region.startOffset}..${region.endOffset}:${if (region.isExpanded) "open" else "closed"}"
        })
    }

    private inner class Trace(private val editor: Editor) : FoldingListener {
        val disposable = Disposer.newDisposable("Harpooner menu fold diagnostics")
        private val states = IdentityHashMap<FoldRegion, Boolean>()
        private var remainingEvents = 100
        private var remainingStacks = 12
        private val started = System.nanoTime()

        init {
            Disposer.register(parentDisposable, disposable)
            Disposer.register(disposable, Disposable {
                editor.putUserData(traceKey, null)
                states.clear()
            })
            editor.foldingModel.allFoldRegions.forEach { states[it] = it.isExpanded }
            (editor.foldingModel as? FoldingModelEx)?.addListener(this, disposable)
        }

        override fun onFoldRegionStateChange(region: FoldRegion) {
            val previous = states.put(region, region.isExpanded)
            val event = when {
                previous == null -> "ADDED"
                previous == region.isExpanded -> return
                region.isExpanded -> "EXPANDED"
                else -> "COLLAPSED"
            }
            writeRegion(event, region, event == "EXPANDED")
        }

        override fun beforeFoldRegionRemoved(region: FoldRegion) {
            writeRegion("REMOVED", region, true)
            states.remove(region)
        }

        override fun beforeFoldRegionDisposed(region: FoldRegion) {
            if (states.remove(region) != null) writeRegion("DISPOSED", region, true)
        }

        override fun onFoldInitializationStatusChanged(initInProgress: Boolean) {
            write("INITIALIZATION inProgress=$initInProgress")
        }

        private fun writeRegion(event: String, region: FoldRegion, stack: Boolean) {
            write("$event fold=${identity(region)} range=${region.startOffset}..${region.endOffset} " +
                "expanded=${region.isExpanded} valid=${region.isValid}", stack)
        }

        fun write(event: String, stack: Boolean = false) {
            if (remainingEvents-- <= 0) return
            val context = if (editor.isDisposed) "disposed=true" else {
                "stamp=${editor.document.modificationStamp} foldCount=${editor.foldingModel.allFoldRegions.size} " +
                    "carets=${editor.caretModel.allCarets.joinToString {
                        if (it.isUpToDate) "${it.offset}@line${it.logicalPosition.line + 1}" else "updating"
                    }}"
            }
            val trace = if (stack && remainingStacks-- > 0) Throwable("Fold change caller") else null
            val elapsed = (System.nanoTime() - started) / 1_000_000
            output("[Harpooner folds] editor=${identity(editor)} +${elapsed}ms $event $context", trace)
            if (remainingEvents == 0) output("[Harpooner folds] editor=${identity(editor)} event limit reached", null)
        }
    }

    private companion object {
        val LOG = Logger.getInstance(MenuFoldingDiagnostics::class.java)
        fun identity(value: Any): String = Integer.toHexString(System.identityHashCode(value))
    }
}
