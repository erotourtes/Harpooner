package com.github.erotourtes.harpoon.actions

import com.github.erotourtes.harpoon.HarpoonTestCase
import com.github.erotourtes.harpoon.MenuHelper
import com.github.erotourtes.harpoon.helpers.HarpoonActions
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldBeEmpty

@Suppress("FunctionName")
class MenuActionTest : HarpoonTestCase() {
    lateinit var menu: MenuHelper

    override fun setUp() {
        super.setUp()
        dummyFiles[0].configureFixture()
        menu = getMenuHelper()
    }

    fun `test(MenuOpen) - should open menu in the current editor`() {
        curOpenedFilename shouldNotBe menu.name

        performHarpoonAction(HarpoonActions.QuickMenuOpen)

        curOpenedFilename shouldBe menu.name
    }

    fun `test(MenuOpen) - should not open menu once opened`() {
        curOpenedFilename shouldNotBe menu.name

        performHarpoonAction(HarpoonActions.QuickMenuOpen)
        curOpenedFilename shouldBe menu.name

        performHarpoonAction(HarpoonActions.QuickMenuOpen)
        curOpenedFilename shouldBe menu.name
    }

    fun `test(MenuClose) - should not open menu once opened`() {
        curOpenedFilename shouldNotBe menu.name

        performHarpoonAction(HarpoonActions.QuickMenuToggle)
        curOpenedFilename shouldBe menu.name

        performHarpoonAction(HarpoonActions.QuickMenuToggle)
        curOpenedFilename shouldNotBe menu.name
    }

    fun `test(MenuClear) - should not clear empty menu`() {
        val editor = curOpenedFilename
        harpoonService.getPaths().shouldBeEmpty()

        performHarpoonAction(HarpoonActions.QuickMenuClear)
        performHarpoonAction(HarpoonActions.File0Open)

        editor shouldBe curOpenedFilename
    }

    fun `test(MenuClear) - should clear the menu when it's closed`() {
        performHarpoonAction(HarpoonActions.FileAdd)
        harpoonService.getPaths().shouldNotBeEmpty()

        menu.text.shouldBeEmpty()
        performHarpoonAction(HarpoonActions.QuickMenuOpen)
        menu.text shouldBe dummyFiles[0].getProjectPath()

        performHarpoonAction(HarpoonActions.QuickMenuToggle)
        performHarpoonAction(HarpoonActions.QuickMenuClear)
        harpoonService.getPaths().shouldBeEmpty()
        menu.text.shouldBeEmpty()
    }

    fun `test(MenuClear) - should clear the menu while it's open`() {
        performHarpoonAction(HarpoonActions.FileAdd)
        harpoonService.getPaths().shouldNotBeEmpty()

        menu.text.shouldBeEmpty()
        performHarpoonAction(HarpoonActions.QuickMenuOpen)
        menu.text shouldBe dummyFiles[0].getProjectPath()

        performHarpoonAction(HarpoonActions.QuickMenuClear)
        harpoonService.getPaths().shouldBeEmpty()
        menu.text.shouldBeEmpty()
    }
}
