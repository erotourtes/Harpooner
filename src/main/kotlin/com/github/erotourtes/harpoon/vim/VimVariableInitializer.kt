package com.github.erotourtes.harpoon.vim

import com.github.erotourtes.harpoon.utils.PLUGIN_NAME
import com.intellij.openapi.project.Project
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.startup.ProjectActivity
import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.vimscript.model.datatypes.VimInt

class VimVariableInitializer : ProjectActivity {
    private val log = Logger.getInstance(VimVariableInitializer::class.java)

    private fun runUnsafe() {
        if (!VimPlugin.isEnabled()) {
            log.info("Vim plugin is not enabled")
            return
        }
        val variableService = VimPlugin.getVariableService()
        variableService.storeGlobalVariable(PLUGIN_NAME, VimInt(1))
    }

    override suspend fun execute(project: Project) {
        runCatching(::runUnsafe).onFailure { log.error(it.message) }
    }
}