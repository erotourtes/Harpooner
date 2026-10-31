package com.github.erotourtes.harpoon.utils.menu

import com.intellij.codeInsight.folding.CodeFoldingManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.FoldRegion
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.editor.ex.FoldingListener
import com.intellij.openapi.editor.ex.FoldingModelEx
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import java.util.Collections
import java.util.IdentityHashMap

/** Applies the menu's initial defaults, including the caret line, without undoing manual expansion. */
class MenuFoldingPolicy(private val project: Project, private val parentDisposable: Disposable) {
    init {
        EditorFactory.getInstance().addEditorFactoryListener(object : EditorFactoryListener {
            override fun editorCreated(event: EditorFactoryEvent) {
                attach(event.editor)
            }

            override fun editorReleased(event: EditorFactoryEvent) {
                event.editor.getUserData(POLICY_KEY)?.let { Disposer.dispose(it.disposable) }
            }
        }, parentDisposable)
    }

    fun attach(editor: Editor) {
        if (project.isDisposed || editor.project != project || editor.isDisposed || editor.getUserData(POLICY_KEY) != null) return
        val file = FileDocumentManager.getInstance().getFile(editor.document) ?: return
        if (file.getUserData(MenuFoldingBuilder.CONTEXT_KEY) == null) return
        val model = editor.foldingModel as? FoldingModelEx ?: return
        val policy = Policy(editor)
        editor.putUserData(POLICY_KEY, policy)
        model.addListener(policy, policy.disposable)
        policy.onFoldProcessingEnd()
    }

    private inner class Policy(private val editor: Editor) : FoldingListener {
        val disposable = Disposer.newDisposable("Harpooner menu folding policy")
        private val initialized = Collections.newSetFromMap(IdentityHashMap<FoldRegion, Boolean>())
        private var applying = false

        init {
            Disposer.register(parentDisposable, disposable)
            Disposer.register(disposable, Disposable {
                editor.putUserData(POLICY_KEY, null)
                initialized.clear()
            })
        }

        override fun beforeFoldRegionDisposed(region: FoldRegion) {
            initialized.remove(region)
        }

        override fun beforeFoldRegionRemoved(region: FoldRegion) {
            initialized.remove(region)
        }

        override fun onFoldProcessingEnd() {
            if (applying || project.isDisposed || editor.isDisposed || !editor.caretModel.isUpToDate) return
            val file = FileDocumentManager.getInstance().getFile(editor.document) ?: return
            val context = file.getUserData(MenuFoldingBuilder.CONTEXT_KEY) ?: return
            val manager = CodeFoldingManager.getInstance(project)
            val regions = editor.foldingModel.allFoldRegions.filter { manager.isCollapsedByDefault(it) == true }
                .mapNotNull { region -> foldKey(region, context)?.let { region to it } }
            if (regions.isEmpty()) return
            val state = project.service<MenuFoldingState>().state
            val newRegions = regions.filter { (region, _) -> initialized.add(region) }
            if (newRegions.isNotEmpty()) {
                applying = true
                try {
                    editor.foldingModel.runBatchFoldingOperation {
                        for ((region, key) in newRegions) {
                            if (key !in state.expanded) {
                                for (caret in editor.caretModel.allCarets) {
                                    if (caret.offset > region.startOffset && caret.offset < region.endOffset) {
                                        val line = editor.document.getLineNumber(caret.offset)
                                        caret.moveToOffset(editor.document.getLineEndOffset(line))
                                    }
                                }
                            }
                            region.isExpanded = key in state.expanded
                        }
                    }
                    // Publish the chosen states to the same cache used by pending async restoration.
                    // Otherwise that restoration can overwrite the caret-line default after this batch.
                    manager.saveFoldingState(editor)
                } finally {
                    applying = false
                }
            }
            state.expanded = regions.filter { it.first.isExpanded }.mapTo(linkedSetOf()) { it.second }
        }

        private fun foldKey(region: FoldRegion, context: MenuFoldingBuilder.Context): String? {
            if (!region.isValid) return null
            val document = editor.document
            val line = document.getLineNumber(region.startOffset)
            val start = document.getLineStartOffset(line)
            val text = document.charsSequence.subSequence(start, document.getLineEndOffset(line)).toString()
            val expected = FoldsManager(context.projectInfo, context.settings).getFoldsFrom(start, text)
                .any { it.start == region.startOffset && it.end == region.endOffset && it.placeholder == region.placeholderText }
            if (!expected) return null
            val path = text.removePrefix(context.projectInfo.pathWithSlashAtEnd)
            val foldedText = document.charsSequence.subSequence(region.startOffset, region.endOffset)
            return "${path.length}:$path$foldedText"
        }
    }

    private companion object {
        val POLICY_KEY = Key.create<Policy>("harpoon.menu.folding.policy")
    }
}
