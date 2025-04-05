package com.github.erotourtes.harpoon.vim

import com.github.erotourtes.harpoon.utils.PLUGIN_NAME
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.ProjectManager
import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.extension.VimExtension
import com.maddyhome.idea.vim.newapi.vim
import com.maddyhome.idea.vim.vimscript.model.datatypes.VimInt
import com.maddyhome.idea.vim.vimscript.services.VimRcService.executeIdeaVimRc

class VimVariableInitializer : VimExtension {
    private val log = Logger.getInstance(VimVariableInitializer::class.java)

    override fun getName(): String = PLUGIN_NAME

    override fun init() {
        ApplicationManager.getApplication().invokeLater {
            try {
                if (isVariableSet()) {
                    log.info("Variable already set")
                    return@invokeLater
                }
                setGlobalVariable(true)
                reloadVimRc()
            } catch (e: Exception) {
                log.error("Couldn't set global variable", e)
            }
        }
    }

    override fun dispose() {
        log.info("Dispose")
        ApplicationManager.getApplication().invokeLater {
            setGlobalVariable(false)
        }
    }

    private fun isVariableSet(): Boolean {
        val variableService = VimPlugin.getVariableService()
        val variable = variableService.getGlobalVariableValue(VARIABLE) ?: return false
        val value = variable.asBoolean()
        return value
    }

    private fun setGlobalVariable(isEnabled: Boolean) {
        log.info("Setting global variable")
        val variableService = VimPlugin.getVariableService()
        variableService.storeGlobalVariable(VARIABLE, VimInt(if (isEnabled) 1 else 0))
        log.info("Set global variable")
    }

    private fun reloadVimRc() {
        log.info("Reloading .ideavimrc")
        val projects = ProjectManager.getInstance().openProjects
        for (project in projects) {
            log.info("Reloading .ideavimrc for project ${project.name}")
            val editor = FileEditorManager.getInstance(project).selectedTextEditor?.vim ?: continue
            executeIdeaVimRc(editor);
        }

        log.info("Reloaded .ideavimrc")
    }

    companion object {
        private const val VARIABLE = PLUGIN_NAME
    }
}