package com.github.erotourtes.harpoon.utils

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Paths

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
        val filtered = newPaths.take(lastIndexOfNotEmptyPath + 1).filter {
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
        val record = data.find { it?.isTheSameAs(path) ?: false }
        if (record == null) return
        data.remove(record)
    }

    fun clear() {
        data.clear()
    }

    fun replace(atIndex: Int, withPath: String) {
        if (atIndex < 0 || withPath.isEmpty()) {
            return
        }

        data.replaceAll {
            if (it?.isTheSameAs(withPath) == true) null else it
        }

        val record = PathRecord.from(withPath) ?: return
        while (data.size <= atIndex) {
            data.add(null)
        }
        data[atIndex] = record

        while (data.isNotEmpty() && data.last() == null) {
            data.removeLast()
        }
    }

    fun getNextIndexOf(path: String?): Int {
        val index = getRelativeIndexOf(path, forward = true)
        return index
    }

    fun getPrevIndexOf(path: String?): Int {
        val index = getRelativeIndexOf(path, forward = false)
        return index
    }

    private fun getRelativeIndexOf(path: String?, forward: Boolean): Int {
        val index = getIndexOf(path).let {
            when {
                it != -1 -> it
                data.size == 0 -> -1
                forward -> -1
                else -> data.size
            }
        }

        for (step in 1..data.size) {
            val offset = if (forward) step else -step
            val nextIndex = (index + offset + data.size) % data.size
            if (data[nextIndex] != null) {
                return nextIndex
            }
        }

        return index
    }

    private fun getIndexOf(path: String?): Int {
        if (path.isNullOrEmpty()) {
            return -1
        }
        val index = data.indexOfFirst { it?.isTheSameAs(path) ?: false }
        return index
    }

    fun includes(path: String): Boolean = getIndexOf(path) != -1

    fun update(oldPath: String, newPath: String): Boolean {
        val oldRecordIndex = data.indexOfFirst { it?.isTheSameAs(oldPath) ?: false }
        if (oldRecordIndex == -1) {
            return false
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

        fun isTheSameAs(other: String): Boolean {
            val result = runCatching {
                val otherPath = Paths.get(other).toRealPath()
                val thisPath = Paths.get(this.path).toRealPath()
                val isEqual = otherPath == thisPath
                return@runCatching isEqual
            }
            return result.getOrElse { false }
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

