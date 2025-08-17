package com.github.erotourtes.harpoon.utils

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile

class State {
    private val data: ArrayList<PathRecord?> = ArrayList()

    val paths: List<String>
        get() = data.map { it?.path ?: "" }.toList()

    fun getFile(index: Int): VirtualFile? {
        val record = data.getOrNull(index) ?: return null
        val virtualFile = record.getVirtualFile()
        return virtualFile
    }

    fun set(newPaths: List<String>) {
        val seen = mutableSetOf<String>()
        val lastIndexOfNotEmptyPath = newPaths.indexOfLast { it.isNotEmpty() }
        val filtered = newPaths.subList(0, lastIndexOfNotEmptyPath + 1).filter {
            val shouldRemain = it.isEmpty() || !seen.contains(it)
            if (shouldRemain) {
                seen.add(it)
            }
            return@filter shouldRemain
        }.map { PathRecord.from(it) }
        data.clear()
        data.addAll(filtered)
    }

    fun add(path: String) {
        if (includes(path) || path.isEmpty()) return
        val record = PathRecord.from(path)
        val firstEmptyEntryIndex = data.indexOfFirst { it == null }
        when {
            firstEmptyEntryIndex != -1 -> data[firstEmptyEntryIndex] = record
            else -> data.add(record)
        }
    }

    fun remove(path: String) {
        val record = data.find { it?.path == path }
        if (record == null) return
        data.remove(record)
    }

    fun clear() {
        data.clear()
    }

    fun includes(path: String): Boolean = data.find { it?.path == path } != null

    fun update(oldPath: String, newPath: String): Boolean {
        val oldRecordIndex = data.indexOfFirst { it?.path == oldPath }
        if (oldRecordIndex == -1) {
            return false;
        }

        data[oldRecordIndex] = PathRecord.from(newPath)
        return true
    }

    fun size(): Int = data.size

    private class PathRecord private constructor(val path: String) {
        private val _virtualFile: VirtualFile? by lazy { findVirtualFile(path) }

        fun getVirtualFile(): VirtualFile? {
            val virtualFile = this._virtualFile
            if (virtualFile == null || !virtualFile.isValid) {
                return null
            }
            return virtualFile
        }

        companion object {
            fun from(path: String): PathRecord? {
                if (path.isEmpty()) {
                    return null
                }

                return PathRecord(path)
            }
        }
    }
}

private fun findVirtualFile(path: String): VirtualFile? {
    val file = LocalFileSystem.getInstance().findFileByPath(path)
    return file
}

