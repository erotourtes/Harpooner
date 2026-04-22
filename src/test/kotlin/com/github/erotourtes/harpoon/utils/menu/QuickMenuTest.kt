package com.github.erotourtes.harpoon.utils.menu

import com.github.erotourtes.harpoon.utils.IDEA_PROJECT_FOLDER
import com.github.erotourtes.harpoon.utils.MENU_NAME
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class QuickMenuTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `getOrCreateMenuFile falls back to temp file when idea directory is missing`() {
        val projectDir = tempDir.resolve("sample-project")
        val projectFilePath = projectDir.resolve(IDEA_PROJECT_FOLDER).resolve("workspace.xml").toString()

        val menuFile = getOrCreateMenuFile(projectFilePath)
        val tmpDirPath = System.getProperty("java.io.tmpdir")

        assertTrue(menuFile.exists())
        assertTrue(Files.notExists(projectDir.resolve(IDEA_PROJECT_FOLDER)))
        assertTrue(menuFile.path.startsWith(tmpDirPath))
        assertTrue(menuFile.name.startsWith(MENU_NAME))
    }

    @Test
    fun `getOrCreateMenuFile uses project idea directory when it exists`() {
        val projectDir = tempDir.resolve("sample-project")
        val ideaDir = projectDir.resolve(IDEA_PROJECT_FOLDER)
        Files.createDirectories(ideaDir)
        val projectFilePath = ideaDir.resolve("workspace.xml").toString()

        val menuFile = getOrCreateMenuFile(projectFilePath)
        val expectedPath = ideaDir.resolve(MENU_NAME).toString()

        assertTrue(menuFile.exists())
        assertEquals(expectedPath, menuFile.path)
    }
}
