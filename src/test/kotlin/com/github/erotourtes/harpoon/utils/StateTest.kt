package com.github.erotourtes.harpoon.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class StateTest {
    @Test
    fun `should set all the paths`() {
        val state = State()
        state.set(
            listOf(
                "",
                "",
                "file2.txt",
                "",
                "file4.txt",
                "file5.txt",
                "",
                "",
            )
        )

        assertArrayEquals(
            arrayOf("", "", "file2.txt", "", "file4.txt", "file5.txt"),
            state.paths.toTypedArray()
        )
    }

    @Test
    fun `should include the right files and not include empty files`() {
        val state = State()
        state.set(
            listOf(
                "file0.txt",
                "",
                "file2.txt",
                "file3.txt"
            )
        )

        assertTrue(state.includes("file0.txt"))
        assertFalse(state.includes(""))
        assertFalse(state.includes("non-existing.txt"))
    }

    @Test
    fun `should append entry`() {
        val state = State()

        state.add("file0.txt")

        assertArrayEquals(
            arrayOf("file0.txt"),
            state.paths.toTypedArray()
        )
    }

    @Test
    fun `should not append empty entry`() {
        val state = State()

        state.add("")

        assertArrayEquals(
            arrayOf(),
            state.paths.toTypedArray()
        )
    }

    @Test
    fun `should replace first empty entry`() {
        val state = State()
        state.set(
            listOf(
                "file0.txt",
                "",
                "file2.txt",
                "file3.txt"
            )
        )

        state.add("file4.txt")

        assertArrayEquals(
            arrayOf("file0.txt", "file4.txt", "file2.txt", "file3.txt"),
            state.paths.toTypedArray()
        )
    }

    @Test
    fun `should not append existing entry`() {
        val state = State()

        state.add("file0.txt")
        state.add("file0.txt")

        assertArrayEquals(
            arrayOf("file0.txt"),
            state.paths.toTypedArray()
        )
    }

    @Test
    fun `should remove entry`() {
        val state = State()
        state.set(
            listOf(
                "file0.txt",
                "",
                "file2.txt",
                "file3.txt"
            )
        )

        state.remove("file2.txt")

        assertArrayEquals(
            arrayOf("file0.txt", "", "file3.txt"),
            state.paths.toTypedArray()
        )
    }

    @Test
    fun `should not remove empty entries`() {
        val state = State()
        state.set(
            listOf(
                "file0.txt",
                "",
                "file2.txt",
                "file3.txt"
            )
        )

        state.remove("")

        assertEquals(4, state.paths.size)
        assertEquals("", state.paths[1])
    }

    @Test
    fun `should clear all files`() {
        val state = State()
        state.set(
            listOf(
                "file0.txt",
                "",
                "file2.txt",
                "file3.txt"
            )
        )
        assertFalse(state.paths.isEmpty())

        state.clear()

        assertTrue(state.paths.isEmpty())
    }

    @Test
    fun `should replace existing entry`() {
        val state = State()
        state.set(
            listOf(
                "file0.txt",
                "",
                "file2.txt",
                "file3.txt"
            )
        )

        val modified = state.update("file2.txt", "file-replaced.txt")

        assertTrue(modified)
        assertArrayEquals(
            arrayOf("file0.txt", "", "file-replaced.txt", "file3.txt"),
            state.paths.toTypedArray()
        )
    }

    @Test
    fun `should not replace non-existing entry`() {
        val state = State()
        state.set(
            listOf(
                "file0.txt",
                "",
                "file2.txt",
                "file3.txt"
            )
        )

        val modified = state.update("file-non-existing.txt", "file-replaced.txt")

        assertFalse(modified)
        assertArrayEquals(
            arrayOf("file0.txt", "", "file2.txt", "file3.txt"),
            state.paths.toTypedArray()
        )
    }
}