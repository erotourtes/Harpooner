package com.github.erotourtes.harpoon

import com.github.erotourtes.harpoon.services.HarpoonService
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class HarpoonTestCase : BasePlatformTestCase() {
    protected lateinit var fixture: CodeInsightTestFixture

    val harpoonService: HarpoonService
        get() = HarpoonService.getInstance(fixture.project)

    val app: Application
        get() = ApplicationManager.getApplication()

    val menuDc: Document
        get() {
            val menu = harpoonService.menu.virtualFile
            return FileDocumentManager.getInstance().getDocument(menu) ?: throw Error("Can't read file")
        }

    val curOpenedFilename: String
        get() {
            val fileEditorManager = FileEditorManager.getInstance(project)
            println(fileEditorManager.selectedEditor?.file)
            return fileEditorManager.selectedEditor?.file?.name ?: ""
        }

    fun menuCloseInEditor() {
        val fileEditorManager = FileEditorManager.getInstance(project)
        fileEditorManager.closeFile(harpoonService.menu.virtualFile)
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

    @BeforeEach
    override fun setUp() {
        super.setUp()

        fixture = myFixture

        harpoonService.menu.reInitMenuFile(testDataPath.plus("MENU"))
        fixture.configureByFile("dummy0.txt")
    }

    @AfterEach
    override fun tearDown() {
        val harpoonService = HarpoonService.getInstance(fixture.project)
        harpoonService.setPaths(emptyList())

        super.tearDown()
    }

    override fun getBasePath(): String = "/testData/"

    override fun getTestDataPath(): String = System.getProperty("user.dir") + basePath
}