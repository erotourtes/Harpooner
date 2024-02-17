package com.github.erotourtes.harpoon.listeners

import com.github.erotourtes.harpoon.utils.*
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.startup.StartupActivity

class ProjectOnStartListener : StartupActivity {
    override fun runActivity(project: Project) {
        val path = getGitignorePath(project) ?: return

        val gitignoreVF = LocalFileSystem.getInstance().findFileByPath(path) ?: return
        val gitignoreDocument = FileDocumentManager.getInstance().getDocument(gitignoreVF) ?: return

        if (gitignoreDocument.text.contains(MENU_NAME)) return

        try {
            val endLine = gitignoreDocument.getLineEndOffset(gitignoreDocument.lineCount - 1)
            CommandProcessor.getInstance().executeCommand(
                project, {
                    WriteCommandAction.runWriteCommandAction(project) {
                        val message = "\n# $PLUGIN_NAME\n$XML_HARPOONER_FILE_NAME\n$MENU_NAME"
                        gitignoreDocument.insertString(endLine, message)
                    }
                }, PLUGIN_NAME, null
            )
        } catch (e: Exception) {
            println(e.toString())
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