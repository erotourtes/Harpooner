package com.github.erotourtes.harpoon.actions

import com.github.erotourtes.harpoon.services.HarpoonService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.messages.MessageBusConnection
import java.io.File

// It was not registered in plugin.xml as it shouldn't start with the IDE
// TODO: does this violate stateless principle?
class FileEditorListener(
    private val tmpFile: File,
    private val connection: MessageBusConnection
) : FileEditorManagerListener {
    override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
        if (file.path != tmpFile.path) return

        val project = source.project
        val harpoonService = project.service<HarpoonService>()
        val paths = tmpFile.readLines()
        harpoonService.setPaths(paths)

        tmpFile.delete()
        connection.disconnect()
    }
}

class QuickMenuAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val harpoonService = project.service<HarpoonService>()

        val savedPaths = harpoonService.getPaths()
        val tempFile = createTmpFile(savedPaths)
        openTmpFile(tempFile, project)

        val connection = ApplicationManager.getApplication().messageBus.connect()
        connection.subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER, FileEditorListener(tempFile, connection)
        )
    }

    private fun createTmpFile(content: List<String>): File {
        val tempFile = File.createTempFile("harpoon", ".txt")
        val writer = tempFile.bufferedWriter()
        content.forEach { writer.write(it + "\n") }
        writer.close()

        return tempFile
    }

    private fun openTmpFile(tempFile: File, project: Project) {
        val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(tempFile) ?: return
        val fileManager = FileEditorManager.getInstance(project)
        fileManager.openFile(virtualFile, true)
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