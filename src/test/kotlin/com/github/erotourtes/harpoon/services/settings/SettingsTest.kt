package com.github.erotourtes.harpoon.services.settings

import com.github.erotourtes.harpoon.HarpoonTestCase
import com.intellij.openapi.command.WriteCommandAction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SettingsTest : HarpoonTestCase() {
    @Test
    fun `Test change of settings on the fly`() {
        performQuickMenuAction()

        app.invokeAndWait {
            WriteCommandAction.runWriteCommandAction(fixture.project) {
                menuDc.setText(
                    """
                        /some/large/path/to/file/1.txt
                        /some/another/large/path/to/file/2.txt
                    """.trimIndent()
                )
            }
            menuCloseInEditor()
            performQuickMenuAction()

            // First part of changeSettings

            changeSettings {
                settings.numberOfSlashes = 1
            }

            performQuickMenuAction()

            Assertions.assertEquals(2, foldingModel.allFoldRegions.size)

            with(foldingModel.allFoldRegions[0]) {
                Assertions.assertEquals(0, startOffset)
                Assertions.assertEquals(25, endOffset) // 25 is  "/some/large/path/to/file/".length
                Assertions.assertEquals(false, isExpanded)
            }

            with(foldingModel.allFoldRegions[1]) {
                Assertions.assertEquals(31, startOffset)
                Assertions.assertEquals(64, endOffset)
                Assertions.assertEquals(false, isExpanded)
            }

            // Second part of changeSettings

            changeSettings {
                settings.numberOfSlashes = 3
            }
            performQuickMenuAction()

            Assertions.assertEquals(2, foldingModel.allFoldRegions.size)

            with(foldingModel.allFoldRegions[0]) {
                Assertions.assertEquals(0, startOffset)
                Assertions.assertEquals(17, endOffset) // "/some/large/path/".length
                Assertions.assertEquals(false, isExpanded)
            }

            with(foldingModel.allFoldRegions[1]) {
                Assertions.assertEquals(31, startOffset)
                Assertions.assertEquals(56, endOffset)
                Assertions.assertEquals(false, isExpanded)
            }

        }
    }
}