package com.github.erotourtes.harpoon.action

import com.github.erotourtes.harpoon.HarpoonTestCase

class ActionTest : HarpoonTestCase() {
    fun `test add file action`() {
        fixture.configureByFile("dummy0.txt")
        fixture.performEditorAction("HarpoonerAddFile")

        assertEquals(1, harpoonService.getPaths().size)

        fixture.configureByFile("dummy1.txt")
        fixture.performEditorAction("HarpoonerAddFile")

        fixture.configureByFile("dummy0.txt")
        fixture.performEditorAction("HarpoonerAddFile")

        assertEquals(2, harpoonService.getPaths().size)
        assertEquals("/src/dummy0.txt", harpoonService.getPaths()[0])
        assertEquals("/src/dummy1.txt", harpoonService.getPaths()[1])
    }
}