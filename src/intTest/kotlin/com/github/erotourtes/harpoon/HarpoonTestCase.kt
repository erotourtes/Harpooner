package com.github.erotourtes.harpoon

import com.github.erotourtes.harpoon.helpers.HarpoonActions
import com.github.erotourtes.harpoon.services.HarpoonService
import com.github.erotourtes.harpoon.settings.SettingsState
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.utils.vfs.getDocument

abstract class HarpoonTestCase : BasePlatformTestCase() {
    protected lateinit var fixture: CodeInsightTestFixture

    val app: Application
        get() = ApplicationManager.getApplication()

    val harpoonService: HarpoonService
        get() = HarpoonService.getInstance(fixture.project)

    val curOpenedFilename: String
        get() {
            val fileEditorManager = FileEditorManager.getInstance(project)
            return fileEditorManager.selectedEditor?.file?.name ?: ""
        }

    val curOpenedFilePath: String
        get() {
            val fileEditorManager = FileEditorManager.getInstance(project)
            val path = fileEditorManager.selectedEditor?.file?.path ?: ""
            return path
        }

    fun getMenuHelper(): MenuHelper {
        val menuHelper = MenuHelper(harpoonService, project)
        return menuHelper
    }

    // TODO: figure out how to deal with multiple states of settings throughout the tests
    private val settingsState by lazy { SettingsState.getInstance() }

    fun performHarpoonAction(action: HarpoonActions) {
        fixture.performEditorAction(action.actionName)
    }

    val dummyFiles: Array<DummyFile> = Array(10) {
        DummyFile(
            testDataPath,
            "dummy$it.txt",
            { file -> fixture.configureByFile(file) }
        )
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
            harpoonService.clearMenu()
        }

        super.tearDown()
    }

    override fun getBasePath(): String = "/src/intTest/resources/"

    override fun getTestDataPath(): String = System.getProperty("user.dir") + basePath
}

data class DummyFile(
    private val testDataPath: String,
    val relativeFilePath: String,
    private val _configureFixtureByFile: (String) -> Unit
) {
    fun getFulPath() = "$testDataPath$relativeFilePath"

    fun configureFixture() {
        _configureFixtureByFile(relativeFilePath)
    }

    fun getProjectPath(): String {
        val fullPath = "/src/$relativeFilePath"
        return fullPath
    }
}

class MenuHelper(
    private val harpoonService: HarpoonService,
    private val project: Project,
) {
    val name: String
        get() {
            val name = harpoonService.getMenVf().name
            return name
        }

    val path: String
        get() {
            val path = harpoonService.getMenVf().path
            return path
        }

    val text: String
        get() {
            val document = harpoonService
                .getMenVf()
                .getDocument()
            val text = document.text
            return text
        }

    fun closeMenuEditor() {
        val fileEditorManager = FileEditorManager.getInstance(project)
        fileEditorManager.closeFile(harpoonService.getMenVf())
    }
}