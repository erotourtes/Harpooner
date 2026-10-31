package com.github.erotourtes.harpoon.actions

import com.github.erotourtes.harpoon.HarpoonTestCase
import com.github.erotourtes.harpoon.MenuHelper
import com.github.erotourtes.harpoon.helpers.HarpoonActions
import com.github.erotourtes.harpoon.utils.menu.MenuFoldingPolicy
import com.github.erotourtes.harpoon.utils.menu.MenuFoldingState
import com.intellij.codeInsight.folding.CodeFoldingManager
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.service
import com.intellij.psi.PsiDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.vfs.VirtualFile
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldBeEmpty
import io.kotest.matchers.string.shouldNotBeEmpty

@Suppress("FunctionName")
class MenuActionTest : HarpoonTestCase() {
    lateinit var menu: MenuHelper

    override fun setUp() {
        super.setUp()
        project.service<MenuFoldingState>().loadState(MenuFoldingState.StoredState())
        dummyFiles[0].configureFixture()
        menu = getMenuHelper()
    }

    fun `test(MenuOpen) - should open menu in the current editor`() {
        curOpenedFilename shouldNotBe menu.name

        runHarpoonServiceAction { openMenu() }

        curOpenedFilename shouldBe menu.name
    }

    fun `test(MenuOpen) - should populate the menu before opening the editor`() {
        performHarpoonAction(HarpoonActions.FileAdd)
        var textWhenOpened: String? = null
        project.messageBus.connect(testRootDisposable).subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            object : FileEditorManagerListener {
                override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                    if (file == harpoonService.getMenVf()) {
                        textWhenOpened = FileDocumentManager.getInstance().getDocument(file)?.text
                    }
                }
            }
        )

        performHarpoonAction(HarpoonActions.QuickMenuOpen)

        textWhenOpened shouldBe dummyFiles[0].getProjectPath()
    }

    fun `test(MenuOpen) - should preserve manually expanded folds during an unchanged update`() {
        openMenuWithFoldableFile()
        val editor = FileEditorManager.getInstance(project).selectedTextEditor!!
        val foldingModel = editor.foldingModel
        foldingModel.allFoldRegions.toList().shouldNotBeEmpty()
        foldingModel.runBatchFoldingOperation {
            foldingModel.allFoldRegions.forEach { it.isExpanded = true }
        }
        val modificationStamp = editor.document.modificationStamp
        val bookmarkedFile = fixture.findFileInTempDir("src/main/kotlin/com/example/Folded.kt")!!

        runHarpoonServiceAction { replaceFile(0, bookmarkedFile) }

        editor.document.modificationStamp shouldBe modificationStamp
        foldingModel.allFoldRegions.all { it.isExpanded } shouldBe true
    }

    fun `test(MenuOpen) - should register default collapsed folds with the platform`() {
        openMenuWithFoldableFile()
        val editor = FileEditorManager.getInstance(project).selectedTextEditor!!
        val manager = CodeFoldingManager.getInstance(project)

        editor.foldingModel.allFoldRegions.toList().shouldNotBeEmpty()
        editor.foldingModel.allFoldRegions.all { !it.isExpanded } shouldBe true
        editor.foldingModel.allFoldRegions.all { manager.isCollapsedByDefault(it) == true } shouldBe true
    }

    fun `test(MenuOpen) - should not open menu once opened`() {
        curOpenedFilename shouldNotBe menu.name

        performHarpoonAction(HarpoonActions.QuickMenuOpen)
        curOpenedFilename shouldBe menu.name

        performHarpoonAction(HarpoonActions.QuickMenuOpen)
        curOpenedFilename shouldBe menu.name
    }

    fun `test(MenuClose) - should not open menu once opened`() {
        curOpenedFilename shouldNotBe menu.name

        performHarpoonAction(HarpoonActions.QuickMenuToggle)
        curOpenedFilename shouldBe menu.name

        performHarpoonAction(HarpoonActions.QuickMenuToggle)
        curOpenedFilename shouldNotBe menu.name
    }

    fun `test(MenuOpen) - should preserve expanded folds when opened again`() {
        openMenuWithFoldableFile()
        val foldingModel = FileEditorManager.getInstance(project).selectedTextEditor!!.foldingModel
        foldingModel.allFoldRegions.toList().shouldNotBeEmpty()
        foldingModel.runBatchFoldingOperation {
            foldingModel.allFoldRegions.forEach { it.isExpanded = true }
        }
        foldingModel.allFoldRegions.all { it.isExpanded } shouldBe true

        performHarpoonAction(HarpoonActions.QuickMenuOpen)

        foldingModel.allFoldRegions.toList().shouldNotBeEmpty()
        foldingModel.allFoldRegions.all { it.isExpanded } shouldBe true
    }

    private fun openMenuWithFoldableFile() {
        val file = fixture.addFileToProject("src/main/kotlin/com/example/Folded.kt", "").virtualFile
        fixture.configureFromExistingVirtualFile(file)
        performHarpoonAction(HarpoonActions.FileAdd)
        runHarpoonServiceAction { openMenu() }
        refreshMenuFolds()
    }

    private fun refreshMenuFolds() {
        // The lightweight editor manager skips normal editor loading. Run the real
        // asynchronous folding initialization, including its saved-state restoration.
        val editor = FileEditorManager.getInstance(project).selectedTextEditor!!
        MenuFoldingPolicy(project, testRootDisposable).attach(editor)
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        val update = runHarpoonServiceAction {
            readAction { CodeFoldingManager.getInstance(project).updateFoldRegionsAsync(editor, true) }
        }
        update?.run()
    }

    fun `test(MenuClear) - should not clear empty menu`() {
        val editor = curOpenedFilename
        paths().shouldBeEmpty()

        performHarpoonAction(HarpoonActions.QuickMenuClear)
        performHarpoonAction(HarpoonActions.File0Open)

        editor shouldBe curOpenedFilename
    }

    fun `test(MenuClear) - should clear the menu when it's closed`() {
        paths().shouldBeEmpty()
        menu.text.shouldBeEmpty()

        performHarpoonAction(HarpoonActions.FileAdd)
        paths() shouldContainExactly listOf(dummyFiles[0].getProjectPath())
        menu.text.shouldBeEmpty()

        performHarpoonAction(HarpoonActions.QuickMenuOpen)
        menu.text shouldBe dummyFiles[0].getProjectPath()

        performHarpoonAction(HarpoonActions.QuickMenuToggle)
        performHarpoonAction(HarpoonActions.QuickMenuClear)
        paths().shouldBeEmpty()
        menu.text.shouldNotBeEmpty()

        performHarpoonAction(HarpoonActions.QuickMenuOpen)
        menu.text.shouldBeEmpty()
    }

    fun `test(MenuClear) - should clear the menu while it's open`() {
        performHarpoonAction(HarpoonActions.FileAdd)
        paths().shouldNotBeEmpty()

        menu.text.shouldBeEmpty()
        performHarpoonAction(HarpoonActions.QuickMenuOpen)
        menu.text shouldBe dummyFiles[0].getProjectPath()

        performHarpoonAction(HarpoonActions.QuickMenuClear)
        paths().shouldBeEmpty()
        menu.text.shouldBeEmpty()
    }
}
