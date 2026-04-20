package com.github.erotourtes.harpoon.actions

import com.github.erotourtes.harpoon.HarpoonTestCase
import com.github.erotourtes.harpoon.helpers.HarpoonActions
import io.kotest.matchers.collections.*
import io.kotest.matchers.shouldBe

@Suppress("FunctionName")
class FileActionTest : HarpoonTestCase() {
    fun `test(FileAdd) - should add file to the menu`() {
        paths() shouldHaveSize 0

        fixture.configureByFile(dummyFiles[0].relativeFilePath)
        performHarpoonAction(HarpoonActions.FileAdd)


        paths() shouldContainExactly listOf(
            dummyFiles[0].getProjectPath(),
        )
    }

    fun `test(FileAdd) - should not add duplicate files to the menu`() {
        paths() shouldHaveSize 0

        fixture.configureByFile(dummyFiles[0].relativeFilePath)
        performHarpoonAction(HarpoonActions.FileAdd)

        fixture.configureByFile(dummyFiles[1].relativeFilePath)
        performHarpoonAction(HarpoonActions.FileAdd)

        fixture.configureByFile(dummyFiles[0].relativeFilePath)
        performHarpoonAction(HarpoonActions.FileAdd)


        paths() shouldContainExactly listOf(
            dummyFiles[0].getProjectPath(),
            dummyFiles[1].getProjectPath()
        )
    }

    fun `test(FileRemove) - should add and remove file to the menu`() {
        paths() shouldHaveSize 0

        fixture.configureByFile(dummyFiles[0].relativeFilePath)
        performHarpoonAction(HarpoonActions.FileAdd)
        performHarpoonAction(HarpoonActions.FileRemove)

        paths().shouldBeEmpty()
    }

    fun `test(FileRemove) - should remove the first element form the list`() {
        paths() shouldHaveSize 0

        fixture.configureByFile(dummyFiles[0].relativeFilePath)
        performHarpoonAction(HarpoonActions.FileAdd)

        fixture.configureByFile(dummyFiles[1].relativeFilePath)
        performHarpoonAction(HarpoonActions.FileAdd)


        fixture.configureByFile(dummyFiles[0].relativeFilePath)
        performHarpoonAction(HarpoonActions.FileRemove)


        paths() shouldContainExactly listOf(
            dummyFiles[1].getProjectPath(),
        )
    }

    fun `test(FileToggle) - should toggle the same file in the menu`() {
        paths() shouldHaveSize 0

        fixture.configureByFile(dummyFiles[0].relativeFilePath)
        performHarpoonAction(HarpoonActions.FileToggle)
        paths() shouldContainExactly listOf(dummyFiles[0].getProjectPath())
    }

    fun `test(FileXReplace) - should replace each file with the QuickMenu`() {
        dummyFiles.forEach {
            it.configureFixture()
            performHarpoonAction(HarpoonActions.FileAdd)
        }

        val actions = arrayOf(
            HarpoonActions.File0Replace,
            HarpoonActions.File1Replace,
            HarpoonActions.File2Replace,
            HarpoonActions.File3Replace,
            HarpoonActions.File4Replace,
            HarpoonActions.File5Replace,
            HarpoonActions.File6Replace,
            HarpoonActions.File7Replace,
            HarpoonActions.File8Replace,
            HarpoonActions.File9Replace,
        )

        performHarpoonAction(HarpoonActions.QuickMenuOpen)

        paths().shouldNotBeEmpty()
        paths().shouldNotContainDuplicates()

        actions.forEachIndexed { index, action ->
            performHarpoonAction(action)
            val curPath = paths()[index]
            val menuPath = getMenuHelper().path

            curPath shouldBe menuPath
        }
    }
}
