package com.github.erotourtes.harpoon.menu

import com.github.erotourtes.harpoon.HarpoonTestCase
import com.github.erotourtes.harpoon.helpers.HarpoonActions
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.io.File
import kotlin.io.path.createTempFile
import kotlin.io.path.pathString

@Suppress("FunctionName")
class MenuTest : HarpoonTestCase() {
    var tempFiles: Array<VirtualFile> = emptyArray()

    override fun setUp() {
        super.setUp()

        tempFiles = dummyFiles.map {
            val tempFile = createTempFile(prefix = it.relativeFilePath)
            val vf = LocalFileSystem.getInstance().findFileByPath(tempFile.pathString)!!
            return@map vf
        }.toTypedArray()
    }

    override fun tearDown() {
        tempFiles.forEach {
            File(it.path).delete()
        }

        super.tearDown()
    }

    fun `test(FileAdd) - should update menu with one file`() {
        tempFiles[0].configureFixture()
        performHarpoonAction(HarpoonActions.FileAdd)

        performHarpoonAction(HarpoonActions.QuickMenuOpen)
        getMenuHelper().text shouldBe tempFiles[0].path
    }

    fun `test(FileAdd) - should update menu with two file`() {
        tempFiles[0].configureFixture()
        performHarpoonAction(HarpoonActions.FileAdd)
        tempFiles[1].configureFixture()
        performHarpoonAction(HarpoonActions.FileAdd)

        performHarpoonAction(HarpoonActions.QuickMenuOpen)
        getMenuHelper().text shouldBe "${tempFiles[0].path}\n${tempFiles[1].path}"
    }

    fun `test(FileAdd) - should update menu when it's opened`() {
        tempFiles[0].configureFixture()
        performHarpoonAction(HarpoonActions.QuickMenuOpen)
        performHarpoonAction(HarpoonActions.FileAdd)
        getMenuHelper().text shouldBe getMenuHelper().path
    }

    fun `test(FileXReplace) - should update state from menu when it's opened`() {
        (0..3).forEach {
            tempFiles[it].configureFixture()
            performHarpoonAction(HarpoonActions.FileAdd)
        }

        performHarpoonAction(HarpoonActions.QuickMenuOpen)
        performHarpoonAction(HarpoonActions.File0Replace)
        performHarpoonAction(HarpoonActions.File1Replace)
        performHarpoonAction(HarpoonActions.File2Replace)
        getMenuHelper().text shouldBe "\n\n${getMenuHelper().path}\n${tempFiles[3].path}"
    }

    fun `test(FileXOpen) - should open the correct files, once menu is modified and opened`() {
        (0..3).forEach {
            tempFiles[it].configureFixture()
            performHarpoonAction(HarpoonActions.FileAdd)
        }

        performHarpoonAction(HarpoonActions.QuickMenuOpen)
        curOpenedFilePath shouldBe getMenuHelper().path

        getMenuHelper().updateText(
            """
            ${tempFiles[1].path}
            ${tempFiles[0].path}
            """.trimIndent()
        )

        performHarpoonAction(HarpoonActions.File1Open)
        curOpenedFilePath shouldBe tempFiles[0].path

        performHarpoonAction(HarpoonActions.File0Open)
        curOpenedFilePath shouldBe tempFiles[1].path
    }

    fun `test(FileOpenNext) - should open the next file, once menu is modified and opened`() {
        (0..3).forEach {
            tempFiles[it].configureFixture()
            performHarpoonAction(HarpoonActions.FileAdd)
        }

        performHarpoonAction(HarpoonActions.QuickMenuOpen)
        curOpenedFilePath shouldBe getMenuHelper().path

        getMenuHelper().updateText(
            """
            ${tempFiles[2].path}
            ${tempFiles[1].path}
            """.trimIndent()
        )

        performHarpoonAction(HarpoonActions.FileOpenNext)
        curOpenedFilePath shouldBe tempFiles[2].path
    }

    fun `test(FileOpenPrevious) - should open the previous file, once menu is modified and opened`() {
        (0..3).forEach {
            tempFiles[it].configureFixture()
            performHarpoonAction(HarpoonActions.FileAdd)
        }

        performHarpoonAction(HarpoonActions.QuickMenuOpen)
        curOpenedFilePath shouldBe getMenuHelper().path

        getMenuHelper().updateText(
            """
            ${tempFiles[5].path}
            ${tempFiles[4].path}
            """.trimIndent()
        )

        performHarpoonAction(HarpoonActions.FileOpenPrevious)
        curOpenedFilePath shouldBe tempFiles[4].path
    }

    private fun VirtualFile.configureFixture() {
        fixture.configureFromExistingVirtualFile(this@configureFixture)
    }

    fun `test() - should update menu if it's updated and closed`() {
        (0..3).forEach {
            tempFiles[it].configureFixture()
            performHarpoonAction(HarpoonActions.FileAdd)
        }

        performHarpoonAction(HarpoonActions.QuickMenuOpen)
        curOpenedFilePath shouldBe getMenuHelper().path

        getMenuHelper().updateText(
            """
            ${tempFiles[7].path}
            ${tempFiles[8].path}
            """.trimIndent()
        )

        fixture.configureFromExistingVirtualFile(tempFiles[0])
        curOpenedFilePath shouldBe tempFiles[0].path

        harpoonService.closeMenu()

        harpoonService.getPaths() shouldContainExactly listOf(tempFiles[7].path, tempFiles[8].path)
    }
}