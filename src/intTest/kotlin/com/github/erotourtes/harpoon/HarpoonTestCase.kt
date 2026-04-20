package com.github.erotourtes.harpoon

import com.github.erotourtes.harpoon.helpers.HarpoonActions
import com.github.erotourtes.harpoon.services.HarpoonService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.utils.vfs.getDocument
import kotlinx.coroutines.runBlocking

abstract class HarpoonTestCase : BasePlatformTestCase() {
    protected lateinit var fixture: CodeInsightTestFixture

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
        val menuHelper = MenuHelper(harpoonService)
        return menuHelper
    }

    fun performHarpoonAction(action: HarpoonActions) {
        fixture.performEditorAction(action.actionName)
        waitForHarpoonService()
        repeat(3) {
            PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
            waitForHarpoonService()
        }
    }

    val dummyFiles: Array<DummyFile> = Array(10) {
        DummyFile(
            testDataPath,
            "dummy$it.txt",
        ) { file -> fixture.configureByFile(file) }
    }

    override fun setUp() {
        super.setUp()
        fixture = myFixture
    }

    override fun tearDown() {
        runHarpoonServiceAction {
            clearMenu()
        }

        super.tearDown()
    }

    override fun getBasePath(): String = "/src/intTest/resources/"

    override fun getTestDataPath(): String = System.getProperty("user.dir") + basePath

    protected fun waitForHarpoonService() {
        runHarpoonServiceAction {
            awaitIdle()
        }
    }

    protected fun runHarpoonServiceAction(action: suspend HarpoonService.() -> Unit) {
        val future = ApplicationManager.getApplication().executeOnPooledThread {
            runBlocking {
                harpoonService.action()
            }
        }
        PlatformTestUtil.waitWithEventsDispatching("Waiting for Harpoon service", { future.isDone }, 10_000)
        future.get()
    }
}

data class DummyFile(
    private val testDataPath: String,
    val relativeFilePath: String,
    private val _configureFixtureByFile: (String) -> Unit
) {
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
            val document = harpoonService.getMenVf().getDocument()
            val text = document.text
            return text
        }

    fun updateText(text: String) {
        runWriteAction {
            val document = harpoonService.getMenVf().getDocument()
            document.setText(text)
        }
    }
}
