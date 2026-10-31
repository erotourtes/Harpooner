package com.github.erotourtes.harpoon.utils.menu

import com.github.erotourtes.harpoon.settings.SettingsState

class FoldsManager(
    private val projectInfo: ProjectInfo,
    private val settings: Settings,
) {

    fun getFoldsFrom(startOffset: Int, text: String): List<Fold> {
        val folds = ArrayList<Fold>()
        var lastFoldIndex = 0
        if (settings.showProjectPath && projectInfo.pathWithSlashAtEnd.isNotEmpty() &&
            text.startsWith(projectInfo.pathWithSlashAtEnd)
        ) {
            val endIndex = projectInfo.pathWithSlashAtEnd.length
            folds.add(Fold(startOffset, startOffset + endIndex, projectInfo.name))
            lastFoldIndex += endIndex
        }

        var count = 0
        for (index in text.length - 1 downTo lastFoldIndex) {
            if (text[index] == '/') count++
            if (count == settings.numberOfSlashes && index != lastFoldIndex) {
                val placeholder = if (text[lastFoldIndex] == '/') "/../" else ".../"
                folds.add(Fold(startOffset + lastFoldIndex, startOffset + index + 1, placeholder))
                break
            }
        }

        return folds
    }


    data class Fold(val start: Int, val end: Int, val placeholder: String)

    data class Settings(
        val showProjectPath: Boolean,
        val numberOfSlashes: Int,
    ) {
        companion object {
            fun from(settings: SettingsState): Settings {
                return Settings(
                    showProjectPath = settings.showProjectPath,
                    numberOfSlashes = settings.numberOfSlashes,
                )
            }
        }
    }
}

fun SettingsState.toFoldsSettings(): FoldsManager.Settings {
    return FoldsManager.Settings.from(this)
}
