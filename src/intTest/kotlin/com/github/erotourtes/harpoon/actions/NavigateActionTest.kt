package com.github.erotourtes.harpoon.actions

import com.github.erotourtes.harpoon.HarpoonTestCase
import com.github.erotourtes.harpoon.helpers.HarpoonActions
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import java.io.File
import kotlin.io.path.createTempFile
import kotlin.io.path.pathString

@Suppress("FunctionName")
class NavigateActionTest : HarpoonTestCase() {
    var tempFiles: Array<VirtualFile> = emptyArray()

    override fun setUp() {
        super.setUp()

        tempFiles = dummyFiles.map {
            val tempFile = createTempFile(prefix = it.relativeFilePath)
            val vf = LocalFileSystem.getInstance().findFileByPath(tempFile.pathString)!!
            fixture.configureFromExistingVirtualFile(vf)

            performHarpoonAction(HarpoonActions.FileAdd)
            return@map vf
        }.toTypedArray()

        performHarpoonAction(HarpoonActions.QuickMenuOpen)
    }

    override fun tearDown() {
        tempFiles.forEach {
            File(it.path).delete()
        }

        super.tearDown()
    }

    fun `test(FileXOpen) - should open the file`() {
        val actions = arrayOf(
            HarpoonActions.File0Open,
            HarpoonActions.File1Open,
            HarpoonActions.File2Open,
            HarpoonActions.File3Open,
            HarpoonActions.File4Open,
            HarpoonActions.File5Open,
            HarpoonActions.File6Open,
            HarpoonActions.File7Open,
            HarpoonActions.File8Open,
            HarpoonActions.File9Open,
        )

        actions.forEachIndexed { index, action ->
            performHarpoonAction(action)
            curOpenedFilePath shouldBe tempFiles[index].path
        }
    }

    fun `test(FileOpenNext) - should open the next file`() {
        for (i in tempFiles.indices) {
            performHarpoonAction(HarpoonActions.FileOpenNext)

            curOpenedFilePath shouldBe tempFiles[i].path
        }

        performHarpoonAction(HarpoonActions.FileOpenNext)

        curOpenedFilePath shouldBe tempFiles[0].path
    }

    fun `test(FileOpenNext) - should open the next file ignoring empty records`() {
        fixture.configureFromExistingVirtualFile(tempFiles[1])
        performHarpoonAction(HarpoonActions.File2Replace)
        harpoonService.getPaths()[1].shouldBeEmpty()


        fixture.configureFromExistingVirtualFile(tempFiles[0])
        curOpenedFilePath shouldBe tempFiles[0].path


        performHarpoonAction(HarpoonActions.FileOpenNext)
        curOpenedFilePath shouldBe tempFiles[1].path
    }

    fun `test(FileOpenPrevious) - should open the previous file`() {
        for (i in tempFiles.indices) {
            performHarpoonAction(HarpoonActions.FileOpenPrevious)

            curOpenedFilePath shouldBe tempFiles[9 - i].path
        }

        performHarpoonAction(HarpoonActions.FileOpenPrevious)

        curOpenedFilePath shouldBe tempFiles[9].path
    }

    fun `test(FileOpenPrevious) - should open the previous file ignoring empty records`() {
        fixture.configureFromExistingVirtualFile(tempFiles[1])
        performHarpoonAction(HarpoonActions.File2Replace)
        harpoonService.getPaths()[1].shouldBeEmpty()


        fixture.configureFromExistingVirtualFile(tempFiles[1])
        curOpenedFilePath shouldBe tempFiles[1].path


        performHarpoonAction(HarpoonActions.FileOpenPrevious)
        curOpenedFilePath shouldBe tempFiles[0].path
    }
}