package com.github.erotourtes.harpoon.utils.menu

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

/** Lets the platform initialize and restore path folds as ordinary, default-collapsed folds. */
class MenuFoldingBuilder : FoldingBuilderEx(), DumbAware {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val context = root.containingFile?.virtualFile?.getUserData(CONTEXT_KEY) ?: return FoldingDescriptor.EMPTY_ARRAY
        val folds = FoldsManager(context.projectInfo, context.settings)
        return (0 until document.lineCount).flatMap { line ->
            val start = document.getLineStartOffset(line)
            val end = document.getLineEndOffset(line)
            folds.getFoldsFrom(start, document.charsSequence.subSequence(start, end).toString())
        }.filter { it.start < it.end }.map { fold ->
            FoldingDescriptor(root.node, TextRange(fold.start, fold.end), null, fold.placeholder, true, emptySet())
        }.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String = ".../"

    override fun isCollapsedByDefault(node: ASTNode): Boolean = true

    data class Context(val projectInfo: ProjectInfo, val settings: FoldsManager.Settings)

    companion object {
        val CONTEXT_KEY: Key<Context> = Key.create("harpoon.menu.folding.context")
    }
}
