package com.github.erotourtes.harpoon

import com.github.erotourtes.harpoon.services.HarpoonService
import com.intellij.openapi.application.PathManager
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class HarpoonTestCase : BasePlatformTestCase() {
    protected lateinit var fixture: CodeInsightTestFixture

    @BeforeEach
    override fun setUp() {
        println("setUp")
        super.setUp()
        fixture = myFixture

//        val factory = IdeaTestFixtureFactory.getFixtureFactory()
//        fixture = createFixture(factory)
//        fixture.testDataPath = testDataPath
//        fixture.setUp()
    }

    @AfterEach
    override fun tearDown() {
        println("tearDown")

        val harpoonService = HarpoonService.getInstance(fixture.project)
        harpoonService.setPaths(emptyList())

        super.tearDown()
    }

    override fun getBasePath(): String = "/testData/"

    override fun getTestDataPath(): String = System.getProperty("user.dir") + basePath

//    protected open fun createFixture(factory: IdeaTestFixtureFactory): CodeInsightTestFixture {
//        val projectDescriptor = LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR
//        val fixture = factory.createLightFixtureBuilder(projectDescriptor, "HarpoonerTest").fixture
//        return factory.createCodeInsightFixture(
//            fixture,
//            LightTempDirTestFixtureImpl(true),
//        )
//    }
//    private val testDataPath: String
//        get() = "/testData/"
}