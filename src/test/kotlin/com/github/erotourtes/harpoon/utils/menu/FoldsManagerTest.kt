package com.github.erotourtes.harpoon.utils.menu

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FoldsManagerTest {
    private val projectInfo = ProjectInfo.from("/home/user/project/MyProject/.idea/Harpoon Menu")

    @Test
    fun `should fold the project prefix and intermediate directories`() {
        val folds = FoldsManager(projectInfo, FoldsManager.Settings(true, 2))
            .getFoldsFrom(0, "/home/user/project/MyProject/src/main/kotlin/com/example/MyClass.kt")

        assertEquals(listOf(
            FoldsManager.Fold(0, 29, "MyProject/"),
            FoldsManager.Fold(29, 49, ".../"),
        ), folds)
    }

    @Test
    fun `should fold an absolute path without the project prefix`() {
        val folds = FoldsManager(projectInfo, FoldsManager.Settings(false, 3))
            .getFoldsFrom(0, "/home/user/project/MyProject/src/main/kotlin/com/example/MyClass.kt")

        assertEquals(listOf(FoldsManager.Fold(0, 45, "/../")), folds)
    }

    @Test
    fun `should use the line start offset for a relative path`() {
        val folds = FoldsManager(projectInfo, FoldsManager.Settings(false, 3))
            .getFoldsFrom(10, "src/main/kotlin/com/example/MyClass.kt")

        assertEquals(listOf(FoldsManager.Fold(10, 26, ".../")), folds)
    }

    @Test
    fun `should not fold short paths or empty lines`() {
        val folds = FoldsManager(projectInfo, FoldsManager.Settings(false, 3))

        assertTrue(folds.getFoldsFrom(0, "src/File.kt").isEmpty())
        assertTrue(folds.getFoldsFrom(0, "").isEmpty())
    }

    @Test
    fun `should not create an empty project prefix for temporary menus`() {
        val folds = FoldsManager(ProjectInfo("", ""), FoldsManager.Settings(true, 3))
            .getFoldsFrom(0, "src/main/kotlin/com/example/MyClass.kt")

        assertEquals(listOf(FoldsManager.Fold(0, 16, ".../")), folds)
    }
}
