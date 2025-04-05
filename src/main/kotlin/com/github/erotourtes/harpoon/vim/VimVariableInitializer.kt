package com.github.erotourtes.harpoon.vim

import com.github.erotourtes.harpoon.utils.PLUGIN_NAME
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.startup.ProjectActivity
import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.newapi.vim
import com.maddyhome.idea.vim.vimscript.model.datatypes.VimInt
import com.maddyhome.idea.vim.vimscript.services.VimRcService.executeIdeaVimRc

class VimVariableInitializer : ProjectActivity {
    private val log = Logger.getInstance(VimVariableInitializer::class.java)

    override suspend fun execute(project: Project) {
        ApplicationManager.getApplication().invokeLater {
            try {
                setGlobalVariable()
                reloadVimRc()
            } catch (e: Exception) {
                log.error("Couldn't set variable", e)
            }
        }
    }


    private fun setGlobalVariable() {
        log.info("Setting global variable")
        val variableService = VimPlugin.getVariableService()
        variableService.storeGlobalVariable(VARIABLE, VimInt(1))
        log.info("Set global variable")
    }

    private fun reloadVimRc() {
        log.info("Reloading .ideavimrc")
        val projects = ProjectManager.getInstance().openProjects
        for (project in projects) {
            log.info("Reloading .ideavimrc for project ${project.name}")
            val editor = FileEditorManager.getInstance(project).selectedTextEditor?.vim ?: continue
            executeIdeaVimRc(editor)
        }

        log.info("Reloaded .ideavimrc")
    }

    companion object {
        private const val VARIABLE = PLUGIN_NAME
    }
}