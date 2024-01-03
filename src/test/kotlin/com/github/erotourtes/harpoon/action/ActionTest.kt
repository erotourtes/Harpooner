package com.github.erotourtes.harpoon.action

import com.github.erotourtes.harpoon.HarpoonTestCase
import com.github.erotourtes.harpoon.services.HarpoonService
import org.junit.jupiter.api.Test

class ActionTest : HarpoonTestCase() {
    @Test
    fun `add file action`() {
        val harpoonService = HarpoonService.getInstance(fixture.project)

        fixture.configureByFile("dummy0.txt")
        fixture.performEditorAction("HarpoonerAddFile")

        assertEquals(1, harpoonService.getPaths().size)

        fixture.configureByFile("dummy1.txt")
        fixture.performEditorAction("HarpoonerAddFile")

        fixture.configureByFile("dummy0.txt")
        fixture.performEditorAction("HarpoonerAddFile")

        assertEquals(2, harpoonService.getPaths().size)
    }
}