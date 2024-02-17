package com.github.erotourtes.harpoon.action

import com.github.erotourtes.harpoon.HarpoonTestCase
import com.github.erotourtes.harpoon.services.HarpoonService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ActionTest : HarpoonTestCase() {
    @Test
    fun `add file action`() {
        val harpoonService = HarpoonService.getInstance(fixture.project)

        fixture.configureByFile("dummy0.txt")
        fixture.performEditorAction("HarpoonerAddFile")

        Assertions.assertEquals(1, harpoonService.getPaths().size)

        fixture.configureByFile("dummy1.txt")
        fixture.performEditorAction("HarpoonerAddFile")

        fixture.configureByFile("dummy0.txt")
        fixture.performEditorAction("HarpoonerAddFile")

        Assertions.assertEquals(2, harpoonService.getPaths().size)
        Assertions.assertEquals("/src/dummy0.txt", harpoonService.getPaths()[0])
        Assertions.assertEquals("/src/dummy1.txt", harpoonService.getPaths()[1])
    }
}