package com.github.erotourtes.harpoon.utils.menu

import com.github.erotourtes.harpoon.HarpoonTestCase
import com.github.erotourtes.harpoon.utils.TYPING_DEBOUNCE_MS
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import junit.framework.TestCase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class QuickMenuTest : HarpoonTestCase() {
    @Test
    fun `filter empty lines`() {
        performQuickMenuAction()

        app.invokeAndWait {
            WriteCommandAction.runWriteCommandAction(fixture.project) {
                menuDc.setText(
                    """
                        
                    $dummy0FullPath
                    
                    $dummy1FullPath
                    
                    $dummy0FullPath
                    
                """.trimIndent()
                )
            }

            menuCloseInEditor()
        }

        Assertions.assertEquals(listOf(dummy0FullPath, dummy1FullPath), harpoonService.getPaths())

        performOpenFileAction(0)
        Assertions.assertEquals(dummy0Filename, curOpenedFilename)

        performOpenFileAction(1)
        Assertions.assertEquals(dummy1Filename, curOpenedFilename)

        performOpenFileAction(2) // should not open anything new
        Assertions.assertEquals(dummy1Filename, curOpenedFilename)
    }


    @Test
    fun `remove file action`() {
        fixtureAddDummyFile0()
        performAddFileAction()
        fixtureAddDummyFile1()
        performAddFileAction()

        Assertions.assertEquals(2, harpoonService.getPaths().size)

        performQuickMenuAction()
        WriteCommandAction.runWriteCommandAction(fixture.project) {
            menuDc.setText("")
        }
        app.invokeAndWait {
            menuCloseInEditor()
        }

        Assertions.assertEquals(0, harpoonService.getPaths().size)
    }

    @Test
    fun `should update harpooner on request lost`() {
        fixtureAddDummyFile0()
        performAddFileAction()
        fixtureAddDummyFile1()
        performAddFileAction()
        TestCase.assertEquals(2, harpoonService.getPaths().size)

        app.invokeAndWait {
            performQuickMenuAction()
            WriteCommandAction.runWriteCommandAction(fixture.project) {
                menuDc.setText(
                    """
                $dummy1FullPath
                $dummy0FullPath
                """.trimIndent()
                )
            }
        }

        // change focus to another editor
        app.invokeAndWait {
            val fileEditorManager = FileEditorManager.getInstance(project)
            val editor = fileEditorManager.allEditors.filter { it.file.name == dummy1Filename }[0]
            fileEditorManager.openFile(editor.file, false)
        }

        // TODO: remove debouncer for testing
        Thread.sleep(TYPING_DEBOUNCE_MS * 2)

        val paths = harpoonService.getPaths()
        TestCase.assertEquals(dummy1FullPath, paths[0])
        TestCase.assertEquals(dummy0FullPath, paths[1])
    }
}