package com.github.erotourtes.harpoon.utils.menu

import com.github.erotourtes.harpoon.services.settings.SettingsState
import kotlin.io.path.Path

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

        val isAbsolute = Path(path).isAbsolute
        if (!settings.showProjectPath && updatedPath.isNotEmpty() && !isAbsolute)
            updatedPath = "${projectInfo.pathWithSlashAtEnd}${updatedPath}"

        return updatedPath
    }

    fun updateSettings(newState: SettingsState) {
        settings = newState
    }
}