package com.github.erotourtes.harpoon.listeners

import com.intellij.openapi.Disposable
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent

class FileRenameListener(
    private val file: VirtualFile,
    private val ds: Disposable,
    private val callback: (oldName: String, newName: String) -> Unit
) {
    private val vfm = VirtualFileManager.getInstance()
    private val listener = AsyncListener()

    init {
        vfm.addAsyncFileListener(listener, ds)
    }

    inner class AsyncListener : AsyncFileListener {
        override fun prepareChange(p0: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier? {
            for (event in p0) {
                val changeEvent = event as? VFilePropertyChangeEvent ?: continue
                if (changeEvent.propertyName != "name") continue

                val oldName = changeEvent.oldValue as? String ?: continue
                val newName = changeEvent.newValue as? String ?: continue
                callback(oldName, newName)
            }

            return null
        }
    }
}