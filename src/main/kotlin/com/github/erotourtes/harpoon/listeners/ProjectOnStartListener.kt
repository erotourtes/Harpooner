package com.github.erotourtes.harpoon.listeners

import com.github.erotourtes.harpoon.services.HarpoonService
import com.github.erotourtes.harpoon.settings.SettingsState
import com.github.erotourtes.harpoon.utils.GITIGNORE
import com.github.erotourtes.harpoon.utils.IDEA_PROJECT_FOLDER
import com.github.erotourtes.harpoon.utils.MENU_NAME
import com.github.erotourtes.harpoon.utils.PLUGIN_NAME
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.LocalFileSystem

class ProjectOnStartListener : ProjectActivity {
    private val log = Logger.getInstance(ProjectOnStartListener::class.java)

    override suspend fun execute(project: Project) {
        invokeLater {
            gitIgnoreMenuFiles(project)
            initServiceIfMenuIsOpened(project)
        }
    }

    private fun initServiceIfMenuIsOpened(project: Project) {
        val editors = FileEditorManager.getInstance(project).allEditors.filter {
            it.file.name == MENU_NAME
        }

        if (editors.isEmpty()) return
        // Idea services are loaded lazily, so we need to call getInstance to init the service
        HarpoonService.getInstance(project)
    }

    private fun gitIgnoreMenuFiles(project: Project) {
        if (!SettingsState.getInstance().adjustGitIgnore) return

        val path = getGitignorePath(project) ?: return

        val gitignoreVF = LocalFileSystem.getInstance().findFileByPath(path) ?: return
        val gitignoreDocument = runReadAction {
            return@runReadAction FileDocumentManager.getInstance().getDocument(gitignoreVF)
        } ?: return

        if (gitignoreDocument.text.contains(MENU_NAME)) return

        try {
            val endLine = gitignoreDocument.getLineEndOffset(gitignoreDocument.lineCount - 1)
            CommandProcessor.getInstance().executeCommand(
                project, {
                    WriteCommandAction.runWriteCommandAction(project) {
                        val message = """
                            # $PLUGIN_NAME
                            $MENU_NAME
                        """.trimIndent()
                        gitignoreDocument.insertString(endLine, message)
                    }
                }, PLUGIN_NAME, null
            )
        } catch (e: Exception) {
            log.error(e.toString())
        }
    }

    private fun getGitignorePath(project: Project): String? {
        val path = project.projectFilePath ?: return null
        val lastIndex = path.lastIndexOf(IDEA_PROJECT_FOLDER) - 1
        if (lastIndex < 0) return null
        val ideaPath = path.substring(0, lastIndex)
        if (ideaPath.isEmpty()) return null
        return "$ideaPath/$IDEA_PROJECT_FOLDER/$GITIGNORE"
    }
}