package com.github.erotourtes.harpoon.actions

import com.github.erotourtes.harpoon.HarpoonTestCase
import com.github.erotourtes.harpoon.helpers.HarpoonActions
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize

class FileActionTest : HarpoonTestCase() {
    fun `test(FileAdd) - should add file to the menu`() {
        harpoonService.getPaths() shouldHaveSize 0

        fixture.configureByFile(dummyFiles[0].relativeFilePath)
        performHarpoonAction(HarpoonActions.FileAdd)


        harpoonService.getPaths() shouldContainExactly listOf(
            dummyFiles[0].getProjectPath(),
        )
    }

    fun `test(FileAdd) - should not add duplicate files to the menu`() {
        harpoonService.getPaths() shouldHaveSize 0

        fixture.configureByFile(dummyFiles[0].relativeFilePath)
        performHarpoonAction(HarpoonActions.FileAdd)

        fixture.configureByFile(dummyFiles[1].relativeFilePath)
        performHarpoonAction(HarpoonActions.FileAdd)

        fixture.configureByFile(dummyFiles[0].relativeFilePath)
        performHarpoonAction(HarpoonActions.FileAdd)


        harpoonService.getPaths() shouldContainExactly listOf(
            dummyFiles[0].getProjectPath(),
            dummyFiles[1].getProjectPath()
        )
    }

    fun `test(FileRemove) - should add and remove file to the menu`() {
        harpoonService.getPaths() shouldHaveSize 0

        fixture.configureByFile(dummyFiles[0].relativeFilePath)
        performHarpoonAction(HarpoonActions.FileAdd)
        performHarpoonAction(HarpoonActions.FileRemove)

        harpoonService.getPaths().shouldBeEmpty()
    }

    fun `test(FileRemove) - should remove the first element form the list`() {
        harpoonService.getPaths() shouldHaveSize 0

        fixture.configureByFile(dummyFiles[0].relativeFilePath)
        performHarpoonAction(HarpoonActions.FileAdd)

        fixture.configureByFile(dummyFiles[1].relativeFilePath)
        performHarpoonAction(HarpoonActions.FileAdd)


        fixture.configureByFile(dummyFiles[0].relativeFilePath)
        performHarpoonAction(HarpoonActions.FileRemove)


        harpoonService.getPaths() shouldContainExactly listOf(
            dummyFiles[1].getProjectPath(),
        )
    }

    fun `test(FileTogggle) - should toggle the same file in the menu`() {
        harpoonService.getPaths() shouldHaveSize 0

        fixture.configureByFile(dummyFiles[0].relativeFilePath)
        performHarpoonAction(HarpoonActions.FileToggle)
        harpoonService.getPaths() shouldContainExactly listOf(dummyFiles[0].getProjectPath())
    }
}
