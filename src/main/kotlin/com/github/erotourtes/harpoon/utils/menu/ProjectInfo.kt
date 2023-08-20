package com.github.erotourtes.harpoon.utils.menu

import com.github.erotourtes.harpoon.utils.IDEA_PROJECT_FOLDER

data class ProjectInfo(val name: String, val pathWithSlashAtEnd: String) {
    companion object {
        fun from(path: String?): ProjectInfo {
            val projectPath = path?.substring(0, path.lastIndexOf(IDEA_PROJECT_FOLDER)) ?: ""
            val name = projectPath.substring(projectNameIndex(projectPath))
            return ProjectInfo(name, projectPath)
        }

        private fun projectNameIndex(projectPath: String): Int {
            var lastIndex = -1
            for (index in projectPath.length - 2 downTo 0) {
                if (projectPath[index] != '/') continue
                lastIndex = index + 1
                break
            }

            return lastIndex
        }
    }
}
