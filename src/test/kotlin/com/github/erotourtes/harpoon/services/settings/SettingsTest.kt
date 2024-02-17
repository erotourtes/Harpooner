package com.github.erotourtes.harpoon.services.settings

import com.github.erotourtes.harpoon.HarpoonTestCase
import com.intellij.openapi.command.WriteCommandAction
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

            assertEquals(2, foldingModel.allFoldRegions.size)

            with(foldingModel.allFoldRegions[0]) {
                assertEquals(0, startOffset)
                assertEquals(25, endOffset) // 25 is  "/some/large/path/to/file/".length
                assertEquals(false, isExpanded)
            }

            with(foldingModel.allFoldRegions[1]) {
                assertEquals(31, startOffset)
                assertEquals(64, endOffset)
                assertEquals(false, isExpanded)
            }

            // Second part of changeSettings

            changeSettings {
                settings.numberOfSlashes = 3
            }
            performQuickMenuAction()

            assertEquals(2, foldingModel.allFoldRegions.size)

            with(foldingModel.allFoldRegions[0]) {
                assertEquals(0, startOffset)
                assertEquals(17, endOffset) // "/some/large/path/".length
                assertEquals(false, isExpanded)
            }

            with(foldingModel.allFoldRegions[1]) {
                assertEquals(31, startOffset)
                assertEquals(56, endOffset)
                assertEquals(false, isExpanded)
            }

        }
    }
}