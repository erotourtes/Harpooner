package com.github.erotourtes.harpoon.utils.menu

import com.github.erotourtes.harpoon.services.settings.SettingsState

class PathsProcessor(private val projectInfo: ProjectInfo) {
    private var settings = SettingsState.getInstance()

    fun process(paths: List<String>): List<String> = paths.map { process(it) }

    private fun process(path: String): String {
        var updatedPath = path

        if (!settings.showProjectPath)
            updatedPath = updatedPath.removePrefix(projectInfo.pathWithSlashAtEnd)

        return updatedPath
    }

    fun unprocess(path: String): String {
        var updatedPath = path.trim()

        if (!settings.showProjectPath && updatedPath.isNotEmpty())
            updatedPath = "${projectInfo.pathWithSlashAtEnd}${updatedPath}"

        return updatedPath
    }

    fun updateSettings(newState: SettingsState) {
        settings = newState
    }
}