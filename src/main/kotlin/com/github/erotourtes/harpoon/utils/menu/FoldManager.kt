package com.github.erotourtes.harpoon.utils.menu

import com.github.erotourtes.harpoon.services.settings.SettingsState
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import org.intellij.markdown.lexer.push

class FoldManager(private val menu: QuickMenu, private val project: Project) {
    private val projectInfo = menu.projectInfo
    private var settings = SettingsState.getInstance()

    fun addFoldsToLine(line: Int, str: String) {
        if (!menu.isMenuFileOpenedWithCurEditor()) return

        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val foldingModel = editor.foldingModel
        val folds = getFoldsFrom(line, str)

        foldingModel.runBatchFoldingOperation {
            for ((start, end, placeHolder) in folds) {
                val foldRegion =
                    foldingModel.addFoldRegion(start, end, placeHolder) ?: return@runBatchFoldingOperation
                foldRegion.isExpanded = false
            }
        }
    }

    private fun removeAllFolds() {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val foldingModel = editor.foldingModel

        foldingModel.runBatchFoldingOperation {
            foldingModel.allFoldRegions.forEach {
                foldingModel.removeFoldRegion(it)
            }
        }
    }

    fun updateSettings(newState: SettingsState) {
        removeAllFolds()
        settings = newState
    }

    fun collapseAllFolds() {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val foldingModel = editor.foldingModel

        foldingModel.runBatchFoldingOperation {
            foldingModel.allFoldRegions.forEach { it.isExpanded = false }
        }
    }

    private fun getFoldsFrom(line: Int, str: String): List<Triple<Int, Int, String>> {
        val folds = ArrayList<Triple<Int, Int, String>>()
        var lastFoldIndex = 0
        if (settings.showProjectPath && str.startsWith(projectInfo.path)) {
            val endIndex = projectInfo.path.length
            folds.push(Triple(line, line + endIndex, projectInfo.name))
            lastFoldIndex += endIndex
        } else if (settings.showProjectPath && str.contains(
                projectInfo.name,
                false
            )
        ) { // TODO: Think about symbolic links
            val endIndex = str.indexOf(projectInfo.name) + projectInfo.name.length
            folds.push(Triple(line, line + endIndex, projectInfo.name))
            lastFoldIndex += endIndex
        }

        var count = 0
        for (index in str.length - 1 downTo lastFoldIndex) {
            if (str[index] == '/') count++
            if (count == settings.numberOfSlashes) {
                folds.push(Triple(line + lastFoldIndex, line + index + 1, ".../"))
                break
            }
        }

        return folds
    }
}