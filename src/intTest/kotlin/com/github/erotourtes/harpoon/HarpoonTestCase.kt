package com.github.erotourtes.harpoon

import com.github.erotourtes.harpoon.services.HarpoonService
import com.github.erotourtes.harpoon.settings.SettingsState
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.FoldingModel
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture

abstract class HarpoonTestCase : BasePlatformTestCase() {
    protected lateinit var fixture: CodeInsightTestFixture

    val app: Application
        get() = ApplicationManager.getApplication()

    val harpoonService: HarpoonService
        get() = HarpoonService.getInstance(fixture.project)


//    val menuDc: Document
//        get() {
//            val menu = harpoonService.menuVF
//            return FileDocumentManager.getInstance().getDocument(menu) ?: throw Error("Can't read file")
//        }

    val curOpenedFilename: String
        get() {
            val fileEditorManager = FileEditorManager.getInstance(project)
            return fileEditorManager.selectedEditor?.file?.name ?: ""
        }

    val foldingModel: FoldingModel
        get() {
            return FileEditorManager.getInstance(project).selectedTextEditor?.foldingModel
                ?: throw Error("Can't get folding model")
        }

    // TODO: figure out how to deal with multiple states of settings throughout the tests
    private val settingsState by lazy { SettingsState.getInstance() }

    fun menuCloseInEditor() {
        val fileEditorManager = FileEditorManager.getInstance(project)
        fileEditorManager.closeFile(harpoonService.getMenVf())
    }

    fun performAddFileAction() {
        fixture.performEditorAction("HarpoonerAddFile")
    }

    fun performOpenFileAction(file: Int) {
        fixture.performEditorAction("HarpoonerOpenFile${file}")
    }

    fun performQuickMenuAction() {
        fixture.performEditorAction("HarpoonerQuickMenu")
    }

    val dummy0Filename = "dummy0.txt"
    val dummy0FullPath = "${testDataPath}${dummy0Filename}"
    fun fixtureAddDummyFile0() {
        fixture.configureByFile(dummy0Filename)
    }

    val dummy1Filename = "dummy1.txt"
    val dummy1FullPath = "${testDataPath}${dummy1Filename}"
    fun fixtureAddDummyFile1() {
        fixture.configureByFile(dummy1Filename)
    }

    fun changeSettings(action: SettingsState.() -> Unit) {
        val newSettings = settingsState.snapshot().apply {
            action(this)
        }

        settingsState.settings = newSettings.settings
        settingsState.notifyObservers(newSettings)
    }

    override fun setUp() {
        super.setUp()
        fixture = myFixture
    }

    override fun tearDown() {
        WriteCommandAction.runWriteCommandAction(fixture.project) {
            harpoonService.getMenVf().delete(this)
        }

        super.tearDown()
    }

    override fun getBasePath(): String = "/src/intTest/resources/"

    override fun getTestDataPath(): String = System.getProperty("user.dir") + basePath
}
