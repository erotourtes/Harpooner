package com.github.erotourtes.harpoon.listeners

import com.intellij.openapi.Disposable
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent

class FilesRenameListener(
    private val callback: (oldPath: String, newPath: String?) -> Unit,
    parentDisposable: Disposable,
) {
    private val vfm = VirtualFileManager.getInstance()
    private val listener = AsyncListener()

    init {
        vfm.addAsyncFileListener(listener, parentDisposable)
    }

    inner class AsyncListener : AsyncFileListener {
        override fun prepareChange(p0: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier? {
            for (event in p0) {

                when (event) {
                    is VFilePropertyChangeEvent -> {
                        val oldPath = event.oldPath as? String ?: continue
                        val newPath = event.newPath as? String ?: continue
                        if (oldPath != newPath) callback(oldPath, newPath)
                    }

                    is VFileMoveEvent -> {
                        val oldPath = event.oldPath
                        val newPath = event.newPath
                        if (oldPath != newPath) callback(oldPath, newPath)
                    }

                    is VFileDeleteEvent -> {
                        val oldPath = event.file.path
                        val newPath = null
                        callback(oldPath, newPath)
                    }
                }
            }

            return null
        }
    }
}