package com.github.erotourtes.harpoon.utils.menu

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class PathsProcessorTest {
    private val list = listOf(
        "/home/user/Projects/MyProject/src/file1.txt",
        "/home/user/Projects/MyProject/test/file2.png",
        "/home/user/Projects/MyProject/src/com/github/package/file3.txt",
    )

    private lateinit var pathsProcessor: PathsProcessor

    @BeforeEach
    fun setUp() {
        pathsProcessor = PathsProcessor(
            ProjectInfo.from("/home/user/Projects/MyProject/.idea/Harpooner Menu"),
            PathsProcessor.Settings().apply { showProjectPath = true }
        )
    }

    @Test
    fun `should not remove project path`() {
        val processed = pathsProcessor.process(list)

        list.mapIndexed { index, path -> assertEquals(path, processed[index]) }
    }

    @Test
    fun `should remove project path`() {
        pathsProcessor.updateSettings(PathsProcessor.Settings().apply { showProjectPath = false })

        val processed = pathsProcessor.process(list)

        assertEquals("src/file1.txt", processed[0])
        assertEquals("test/file2.png", processed[1])
        assertEquals("src/com/github/package/file3.txt", processed[2])
    }

    @Test
    fun `should not add project path`() {
        val path = list[0]
        val processed = pathsProcessor.process(listOf(path)).first()

        val unprocessed = pathsProcessor.unprocess(processed)

        assertEquals(path, unprocessed)
    }

    @Test
    fun `should not add project path on blank`() {
        val path = "       "
        val processed = pathsProcessor.process(listOf(path)).first()

        val unprocessed = pathsProcessor.unprocess(processed)

        assertEquals("", unprocessed)
    }

    @Test
    fun `should add project path`() {
        pathsProcessor.updateSettings(PathsProcessor.Settings().apply { showProjectPath = false })
        val path = list[0]
        val processed = pathsProcessor.process(listOf(path)).first()

        val unprocessed = pathsProcessor.unprocess(processed)

        assertEquals(path, unprocessed)
    }
}