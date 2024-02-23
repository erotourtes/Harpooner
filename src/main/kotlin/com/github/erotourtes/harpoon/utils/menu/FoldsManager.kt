package com.github.erotourtes.harpoon.utils.menu

import com.github.erotourtes.harpoon.services.settings.SettingsState
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import org.intellij.markdown.lexer.push

class FoldsManager(private val menu: QuickMenu, private val project: Project) {
    private val projectInfo = menu.projectInfo
    private var settings = SettingsState.getInstance()

    fun updateFoldsAt(line: Int, str: String) {
        removeFoldsFrom(line, line + str.length)
        addFoldsToLine(line, str)
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


    private fun removeFoldsFrom(start: Int, end: Int) {
        if (!menu.isMenuFileOpenedWithCurEditor()) return

        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val foldingModel = editor.foldingModel

        foldingModel.runBatchFoldingOperation {
            val foldRegions = foldingModel.allFoldRegions.filter {
                it.startOffset >= start && it.endOffset <= end
            }
            foldRegions.forEach { foldingModel.removeFoldRegion(it) }
        }
    }

    private fun addFoldsToLine(line: Int, str: String) {
        if (!menu.isMenuFileOpenedWithCurEditor()) return

        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val foldingModel = editor.foldingModel
        val folds = getFoldsFrom(line, str)

        foldingModel.runBatchFoldingOperation {
            for ((start, end, placeHolder) in folds) {
                if (start == end) continue
                val foldRegion =
                    foldingModel.addFoldRegion(start, end, placeHolder) ?: return@runBatchFoldingOperation
                foldRegion.isExpanded = false
            }
        }
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
                val placeholder = if (str[0] == '/') "/../" else ".../"
                folds.push(Triple(line + lastFoldIndex, line + index + 1, placeholder))
                break
            }
        }

        return folds
    }
}