package com.github.erotourtes.harpoon.actions

import com.github.erotourtes.harpoon.services.HarpoonService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class QuickMenuAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val harpoonService = project.service<HarpoonService>()
        harpoonService.menu.open().connectListener()
    }
}


/*
*  Document listener (listen to changes in the file)
*
        val documentListener = object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                super.documentChanged(event)
                val paths = event.document.text.split("\n")
                println("document changed")
            }
        }

        val document = fileManager.selectedTextEditor?.document ?: return
        document.addDocumentListener(documentListener)
//        document.removeDocumentListener(documentListener)
* */


/*
*  Bulk file listener (listen to changes in the file (on save))
*
project.messageBus.connect().subscribe(
    VirtualFileManager.VFS_CHANGES,
    object : BulkFileListener {
        override fun after(events: MutableList<out VFileEvent>) {
            println("event fired")
            for (event in events) {
                if (event.file != virtualFile) continue

                val paths = tempFile.readLines()
                harpoonService.setPaths(paths)
                virtualFile.delete(null)
            }
        }
    })
 */