package com.github.erotourtes.harpoon.utils.menu

import com.github.erotourtes.harpoon.HarpoonTestCase
import com.intellij.openapi.command.WriteCommandAction

class QuickMenuTest : HarpoonTestCase() {
    fun `test filter empty lines`() {
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

        assertEquals(listOf(dummy0FullPath, dummy1FullPath), harpoonService.getPaths())

        performOpenFileAction(0)
        assertEquals(dummy0Filename, curOpenedFilename)

        performOpenFileAction(1)
        assertEquals(dummy1Filename, curOpenedFilename)

        performOpenFileAction(2) // should not open anything new
        assertEquals(dummy1Filename, curOpenedFilename)
    }


    fun `test remove file action`() {
        fixtureAddDummyFile0()
        performAddFileAction()
        fixtureAddDummyFile1()
        performAddFileAction()

        assertEquals(2, harpoonService.getPaths().size)

        performQuickMenuAction()
        runSyncWriteOperation { menuDc.setText("") }

        app.invokeAndWait { menuCloseInEditor() }

        assertEquals(0, harpoonService.getPaths().size)
    }

//    @Test
//    fun `should update harpooner on request lost`() {
//        fixtureAddDummyFile0()
//        performAddFileAction()
//        fixtureAddDummyFile1()
//        performAddFileAction()
//        assertEquals(2, harpoonService.getPaths().size)
//
//        app.invokeAndWait {
//            performQuickMenuAction()
//            runSyncWriteOperation {
//                menuDc.setText(
//                    """
//                $dummy1FullPath
//                $dummy0FullPath
//                """.trimIndent()
//                )
//            }
//
//            // change focus to another editor
//            val fileEditorManager = FileEditorManager.getInstance(project)
//            val editor = fileEditorManager.allEditors.filter { it.file.name == dummy1Filename }[0]
//            fileEditorManager.openFile(editor.file, false)
//        }
//
//        // TODO: remove debouncer for testing
//        Thread.sleep(TYPING_DEBOUNCE_MS * 2)
//
//        val paths = harpoonService.getPaths()
//        assertEquals(dummy1FullPath, paths[0])
//        assertEquals(dummy0FullPath, paths[1])
//    }
}