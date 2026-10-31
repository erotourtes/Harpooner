package com.github.erotourtes.harpoon.utils.menu

import com.intellij.openapi.editor.FoldRegion
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MenuFoldingDiagnosticsTest : BasePlatformTestCase() {
    fun `test records disposal during document updates without reading updating carets`() {
        val events = mutableListOf<Pair<String, Throwable?>>()
        myFixture.configureByText("folds.txt", "/src/main/kotlin/example/First.kt\nsecond line")
        val editor = myFixture.editor
        MenuFoldingDiagnostics(testRootDisposable) { message, trace -> events.add(message to trace) }
            .record(editor, "test start")
        editor.foldingModel.runBatchFoldingOperation {
            editor.foldingModel.addFoldRegion(0, 10, ".../")!!.isExpanded = false
        }
        events.clear()

        WriteCommandAction.runWriteCommandAction(project) { editor.document.setText("replacement") }

        assertTrue(events.any { " DISPOSED " in it.first && "carets=updating" in it.first })
    }

    fun `test captures expansion away from the caret with the caller stack`() {
        val events = mutableListOf<Pair<String, Throwable?>>()
        myFixture.configureByText("folds.txt", "/src/main/kotlin/example/First.kt\nsecond line")
        val editor = myFixture.editor
        editor.caretModel.moveToOffset(editor.document.getLineStartOffset(1))
        val diagnostics = MenuFoldingDiagnostics(testRootDisposable) { message, trace -> events.add(message to trace) }
        diagnostics.record(editor, "test start")
        lateinit var region: FoldRegion
        editor.foldingModel.runBatchFoldingOperation {
            region = editor.foldingModel.addFoldRegion(0, 10, ".../")!!
            region.isExpanded = false
        }
        events.clear()

        editor.foldingModel.runBatchFoldingOperation { region.isExpanded = true }

        val expansion = events.single { " EXPANDED " in it.first }
        assertTrue(expansion.first.contains("@line2"))
        assertNotNull(expansion.second)
        assertTrue(expansion.second!!.stackTrace.any { it.className == javaClass.name })
        assertFalse(events.any { " REMOVED " in it.first })
    }

    fun `test distinguishes removal of a collapsed fold from independent expansion`() {
        val events = mutableListOf<Pair<String, Throwable?>>()
        myFixture.configureByText("folds.txt", "/src/main/kotlin/example/First.kt\nsecond line")
        val editor = myFixture.editor
        editor.caretModel.moveToOffset(editor.document.getLineStartOffset(1))
        MenuFoldingDiagnostics(testRootDisposable) { message, trace -> events.add(message to trace) }
            .record(editor, "test start")
        lateinit var region: FoldRegion
        editor.foldingModel.runBatchFoldingOperation {
            region = editor.foldingModel.addFoldRegion(0, 10, ".../")!!
            region.isExpanded = false
        }
        events.clear()

        editor.foldingModel.runBatchFoldingOperation { editor.foldingModel.removeFoldRegion(region) }

        val expansion = events.single { " EXPANDED " in it.first }
        val removal = events.single { " REMOVED " in it.first }
        assertTrue(events.indexOf(expansion) < events.indexOf(removal))
        assertTrue(expansion.second!!.stackTrace.any { it.methodName == "removeFoldRegion" })
        val regionId = "fold=" + Integer.toHexString(System.identityHashCode(region))
        assertTrue(expansion.first.contains(regionId))
        assertTrue(removal.first.contains(regionId))
    }
}
