package com.github.erotourtes.harpoon.utils.menu

import com.github.erotourtes.harpoon.settings.SettingsState
import com.intellij.openapi.editor.FoldRegion
import com.intellij.openapi.editor.FoldingModel
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import org.intellij.markdown.lexer.push

class FoldsManager(private val menu: QuickMenu, private val project: Project) {
    private val projectInfo = menu.projectInfo
    private var settings = SettingsState.getInstance()

    fun updateFoldsAt(line: Int, str: String) {
        if (!menu.isMenuFileOpenedWithCurEditor()) return

        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val foldingModel = editor.foldingModel
        val newFolds = getFoldsFrom(line, str)

        foldingModel.runBatchFoldingOperation {
            val curLineFolds = getCurrentLineFolds(foldingModel, line, line + str.length)

            for ((start, end, placeHolder) in newFolds) {
                if (start == end) continue

                val foldAlreadyInLine = curLineFolds.find { it.startOffset == start && it.endOffset == end }
                if (foldAlreadyInLine != null) {
                    curLineFolds.remove(foldAlreadyInLine)
//                    foldAlreadyInLine.isExpanded = false
                    continue
                }

                val foldRegion =
                    foldingModel.addFoldRegion(start, end, placeHolder) ?: return@runBatchFoldingOperation
                foldRegion.isExpanded = false
            }

            // remove all folds that are not in the newFolds
            curLineFolds.forEach(foldingModel::removeFoldRegion)
        }
    }

    fun updateSettings(newState: SettingsState) {
        settings = newState
    }

    fun collapseAllFolds() {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val foldingModel = editor.foldingModel

        foldingModel.runBatchFoldingOperation {
            foldingModel.allFoldRegions.forEach { it.isExpanded = false }
        }
    }

    private fun getCurrentLineFolds(foldingModel: FoldingModel, start: Int, end: Int): MutableList<FoldRegion> {
        return foldingModel.allFoldRegions.filter {
            it.startOffset >= start && it.endOffset <= end
        }.toMutableList()
    }

    private fun getFoldsFrom(line: Int, str: String): List<Triple<Int, Int, String>> {
        val folds = ArrayList<Triple<Int, Int, String>>()
        var lastFoldIndex = 0
        if (settings.showProjectPath && str.startsWith(projectInfo.pathWithSlashAtEnd)) {
            val endIndex = projectInfo.pathWithSlashAtEnd.length
            folds.push(Triple(line, line + endIndex, projectInfo.name))
            lastFoldIndex += endIndex
        }

        var count = 0
        for (index in str.length - 1 downTo lastFoldIndex) {
            if (str[index] == '/') count++
            if (count == settings.numberOfSlashes && index != lastFoldIndex) {
                val placeholder = if (str[lastFoldIndex] == '/') "/../" else ".../"
                folds.push(Triple(line + lastFoldIndex, line + index + 1, placeholder))
                break
            }
        }

        return folds
    }
}