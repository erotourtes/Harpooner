package com.github.erotourtes.harpoon.actions

import com.github.erotourtes.harpoon.HarpoonTestCase
import org.junit.jupiter.api.Assertions

class ActionTest : HarpoonTestCase() {
    fun testAddFileAction() {
        fixtureAddDummyFile0()
        performAddFileAction()
        Assertions.assertEquals(1, harpoonService.getPaths().size)

        fixture.configureByFile("dummy1.txt")
        performAddFileAction()

        fixture.configureByFile("dummy0.txt")
        performAddFileAction()

        Assertions.assertEquals(2, harpoonService.getPaths().size)
        Assertions.assertEquals("/src/dummy0.txt", harpoonService.getPaths()[0])
        Assertions.assertEquals("/src/dummy1.txt", harpoonService.getPaths()[1])
    }
}
