package com.github.erotourtes.harpoon.utils.menu

import com.github.erotourtes.harpoon.HarpoonTestCase
import com.github.erotourtes.harpoon.settings.SettingsState
import com.intellij.codeInsight.folding.CodeFoldingManager
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.util.xmlb.XmlSerializer

class MenuFoldingBuilderTest : HarpoonTestCase() {
    private lateinit var policy: MenuFoldingPolicy

    override fun setUp() {
        super.setUp()
        project.service<MenuFoldingState>().loadState(MenuFoldingState.StoredState())
        policy = MenuFoldingPolicy(project, testRootDisposable)
    }

    fun `test pending expanded state cannot reopen an initially collapsed caret line fold`() {
        configureMenu()
        val editor = fixture.editor
        editor.caretModel.moveToOffset(editor.document.textLength)
        val descriptor = MenuFoldingBuilder().buildFoldRegions(fixture.file, editor.document, false).single()
        lateinit var oldRegion: com.intellij.openapi.editor.FoldRegion
        editor.foldingModel.runBatchFoldingOperation {
            oldRegion = editor.foldingModel.addFoldRegion(
                descriptor.range.startOffset, descriptor.range.endOffset, descriptor.placeholderText!!)!!
        }
        CodeFoldingManager.getInstance(project).saveFoldingState(editor)
        editor.foldingModel.runBatchFoldingOperation { editor.foldingModel.removeFoldRegion(oldRegion) }

        initializeFolds(editor)

        assertTrue(editor.foldingModel.allFoldRegions.isNotEmpty())
        assertTrue(editor.foldingModel.allFoldRegions.all { !it.isExpanded })
    }

    fun `test initialization collapses new caret line folds before pending restoration`() {
        configureMenu()
        fixture.editor.caretModel.moveToOffset(fixture.editor.document.textLength)

        initializeFolds(fixture.editor)

        assertTrue(fixture.editor.foldingModel.allFoldRegions.isNotEmpty())
        assertTrue(fixture.editor.foldingModel.allFoldRegions.all { !it.isExpanded })
        initializeFolds(fixture.editor)
        assertTrue(fixture.editor.foldingModel.allFoldRegions.all { !it.isExpanded })
    }

    fun `test folding settings refresh without changing the menu document`() {
        val menu = QuickMenu(project, SettingsState(), testRootDisposable)
        runHarpoonServiceAction { menu.open(listOf("Header", "/src/main/kotlin/example/Folded.kt")) }
        val editor = FileEditorManager.getInstance(project).selectedTextEditor!!
        initializeFolds(editor)
        val oldRange = editor.foldingModel.allFoldRegions.single().endOffset
        val stamp = editor.document.modificationStamp
        val newSettings = SettingsState().apply { settings.numberOfSlashes = 2 }

        runHarpoonServiceAction { menu.updateSettings(newSettings) }
        initializeFolds(editor)

        assertEquals(stamp, editor.document.modificationStamp)
        assertTrue(oldRange != editor.foldingModel.allFoldRegions.single().endOffset)
    }

    fun `test leaves ordinary plain text files alone`() {
        val file = fixture.configureByText("ordinary.txt", "Header\n/src/main/kotlin/example/Folded.kt")

        assertTrue(MenuFoldingBuilder().buildFoldRegions(file, fixture.editor.document, false).isEmpty())
    }

    fun `test default collapsed folds survive asynchronous initialization`() {
        configureMenu()
        val editor = fixture.editor

        initializeFolds(editor)
        assertTrue(editor.foldingModel.allFoldRegions.isNotEmpty())
        assertTrue(editor.foldingModel.allFoldRegions.all { !it.isExpanded })
        assertTrue(editor.foldingModel.allFoldRegions.all {
            CodeFoldingManager.getInstance(project).isCollapsedByDefault(it) == true
        })

        // Repeat the platform update which previously overwrote our manual regions.
        initializeFolds(editor)

        assertTrue(editor.foldingModel.allFoldRegions.all { !it.isExpanded })
    }

    fun `test asynchronous initialization preserves manual expansion in another editor`() {
        configureMenu()
        val editor = fixture.editor
        initializeFolds(editor)
        editor.foldingModel.runBatchFoldingOperation {
            editor.foldingModel.allFoldRegions.forEach { it.isExpanded = true }
        }
        val saved = XmlSerializer.serialize(project.service<MenuFoldingState>().state)
        val restored = XmlSerializer.deserialize(saved, MenuFoldingState.StoredState::class.java)
        assertTrue(restored.expanded.isNotEmpty())
        project.service<MenuFoldingState>().loadState(restored)
        val otherEditor = EditorFactory.getInstance().createEditor(editor.document, project)
        try {
            initializeFolds(otherEditor)

            assertTrue(otherEditor.foldingModel.allFoldRegions.isNotEmpty())
            assertTrue(otherEditor.foldingModel.allFoldRegions.all { it.isExpanded })
        } finally {
            EditorFactory.getInstance().releaseEditor(otherEditor)
        }
    }

    private fun configureMenu() {
        val file = fixture.configureByText("menu.txt", "Header\n/src/main/kotlin/example/Folded.kt")
        file.virtualFile.putUserData(MenuFoldingBuilder.CONTEXT_KEY,
            MenuFoldingBuilder.Context(ProjectInfo("", ""), FoldsManager.Settings(false, 3)))
    }

    private fun initializeFolds(editor: Editor) {
        policy.attach(editor)
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        val update = runHarpoonServiceAction {
            readAction { CodeFoldingManager.getInstance(project).updateFoldRegionsAsync(editor, true) }
        }
        assertNotNull(update)
        update!!.run()
    }
}
