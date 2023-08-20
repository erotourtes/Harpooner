package com.github.erotourtes.harpoon.utils.menu

import com.github.erotourtes.harpoon.services.settings.SettingsState

class PathsProcessor(private val menu: QuickMenu) {
    private val settings = SettingsState.getInstance()

    fun process(paths: List<String>): List<String> = paths.map { process(it) }

    private fun process(path: String): String {
        var updatedPath = path

        if (!settings.showProjectPath)
            updatedPath = updatedPath.removePrefix(menu.projectInfo.pathWithSlashAtEnd)

        return updatedPath
    }

    fun unprocess(path: String): String {
        val settings = SettingsState.getInstance()
        var updatedPath = path

        if (!settings.showProjectPath)
            updatedPath = "${menu.projectInfo.pathWithSlashAtEnd}${updatedPath}"

        return updatedPath
    }
}