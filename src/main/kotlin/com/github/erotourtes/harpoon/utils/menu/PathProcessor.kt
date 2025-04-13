package com.github.erotourtes.harpoon.utils.menu

import com.github.erotourtes.harpoon.settings.SettingsState
import kotlin.io.path.Path

class PathsProcessor(
    private val projectInfo: ProjectInfo, private var settings: Settings
) {

    fun process(paths: List<String>): List<String> = paths.map { process(it) }

    private fun process(path: String): String {
        var updatedPath = path

        if (!settings.showProjectPath) {
            updatedPath = updatedPath.removePrefix(projectInfo.pathWithSlashAtEnd)
        }

        return updatedPath
    }

    fun unprocess(path: String): String {
        var updatedPath = path.trim()

        val isAbsolute = Path(path).isAbsolute
        if (!settings.showProjectPath && updatedPath.isNotEmpty() && !isAbsolute) {
            updatedPath = "${projectInfo.pathWithSlashAtEnd}${updatedPath}"
        }

        return updatedPath
    }

    fun updateSettings(newState: Settings) {
        settings = newState
    }

    data class Settings(
        var showProjectPath: Boolean = true
    ) {
        companion object {
            fun from(settings: SettingsState): Settings {
                val newState = Settings()
                newState.showProjectPath = settings.showProjectPath
                return newState
            }
        }
    }
}

fun SettingsState.toProcessorSettings(): PathsProcessor.Settings {
    return PathsProcessor.Settings.from(this)
}
