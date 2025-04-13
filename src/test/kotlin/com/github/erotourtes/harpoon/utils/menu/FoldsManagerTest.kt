package com.github.erotourtes.harpoon.utils.menu

import com.intellij.openapi.editor.*
import com.intellij.openapi.util.Key
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import org.mockito.Mockito.*

class FoldsManagerTest {
    private lateinit var foldingModel: FoldingModel
    private val projectInfo = ProjectInfo.from("/home/user/project/MyProject/.idea/Harpoon Menu")

    @BeforeEach
    fun setUp() {
        foldingModel = mock()

        `when`(foldingModel.runBatchFoldingOperation(any())).thenAnswer {
            it.getArgument(0, Runnable::class.java).run()
        }
        `when`(foldingModel.addFoldRegion(anyInt(), anyInt(), anyString())).thenAnswer {
            MockFoldRegion(it.getArgument(0), it.getArgument(1), it.getArgument(2))
        }
    }

    @Test
    fun `should add folds`() {
        val path = "/home/user/project/MyProject/src/main/kotlin/com/example/MyClass.kt"
        val foldsManager = FoldsManager(
            projectInfo,
            { true },
            { foldingModel },
            FoldsManager.Settings(true, 2)
        )

        `when`(foldingModel.allFoldRegions).thenReturn(arrayOf())
        foldsManager.updateFoldsAt(0, path)

        verify(foldingModel).addFoldRegion(0, 29, "MyProject/")
        verify(foldingModel).addFoldRegion(29, 49, ".../")
        verify(foldingModel, times(0)).removeFoldRegion(any())
    }

    @Test
    @DisplayName("Should add full path /../")
    fun updateFoldsAt2() {
        val path = "/home/user/project/MyProject/src/main/kotlin/com/example/MyClass.kt"
        val foldsManager = FoldsManager(
            projectInfo,
            { true },
            { foldingModel },
            FoldsManager.Settings(false, 3)
        )
        val folds = arrayOf(
            MockFoldRegion(0, 29, "MyProject/"),
            MockFoldRegion(29, 49, ".../"),
        )

        `when`(foldingModel.allFoldRegions).thenReturn(folds)
        foldsManager.updateFoldsAt(0, path)

        verify(foldingModel).addFoldRegion(0, 45, "/../")
        verify(foldingModel).removeFoldRegion(folds[0])
        verify(foldingModel).removeFoldRegion(folds[1])
    }

    @Test
    fun `should not do anything`() {
        val path = "src/main/kotlin/com/example/MyClass.kt"
        val foldsManager = FoldsManager(
            projectInfo,
            { true },
            { foldingModel },
            FoldsManager.Settings(false, 3)
        )
        val folds = arrayOf(
            MockFoldRegion(0, 16, ".../"),
        )

        `when`(foldingModel.allFoldRegions).thenReturn(folds)
        foldsManager.updateFoldsAt(0, path)

        verify(foldingModel, times(0)).addFoldRegion(anyInt(), anyInt(), anyString())
        verify(foldingModel, times(0)).removeFoldRegion(any())
    }

    @Test
    fun `should update folds`() {
        val path = "src/main/kotlin/com/example/MyClass.kt"
        val foldsManager = FoldsManager(
            projectInfo,
            { true },
            { foldingModel },
            FoldsManager.Settings(false, 3)
        )
        val folds = arrayOf(
            MockFoldRegion(0, 10, ".../"),
        )

        `when`(foldingModel.allFoldRegions).thenReturn(folds)
        foldsManager.updateFoldsAt(0, path)

        verify(foldingModel, times(1)).addFoldRegion(0, 16, ".../")
        verify(foldingModel, times(1)).removeFoldRegion(folds[0])
    }

    @Test
    fun `should not update folds if not in the editor`() {
        val path = "src/main/kotlin/com/example/MyClass.kt"
        val foldsManager = FoldsManager(
            projectInfo,
            isInRightEditor = { false },
            { foldingModel },
            FoldsManager.Settings(false, 3)
        )

        foldsManager.updateFoldsAt(0, path)

        verify(foldingModel, never()).addFoldRegion(anyInt(), anyInt(), anyString())
    }

    class MockFoldRegion(
        private val start: Int,
        private val end: Int,
        private val placeHolder: String,
        private var expanded: Boolean = false,
    ) : FoldRegion {
        override fun getStartOffset(): Int = start

        override fun getEndOffset(): Int = end

        override fun isExpanded(): Boolean = expanded

        override fun setExpanded(p0: Boolean) {
            expanded = p0
        }

        override fun getPlaceholderText(): String = placeHolder

        override fun <T : Any?> getUserData(p0: Key<T>): T = TODO("Not yet implemented")
        override fun <T : Any?> putUserData(p0: Key<T>, p1: T?): Unit = TODO("Not yet implemented")
        override fun getDocument(): Document = TODO("Not yet implemented")
        override fun isValid(): Boolean = TODO("Not yet implemented")
        override fun setGreedyToLeft(p0: Boolean): Unit = TODO("Not yet implemented")
        override fun setGreedyToRight(p0: Boolean): Unit = TODO("Not yet implemented")
        override fun isGreedyToRight(): Boolean = TODO("Not yet implemented")
        override fun isGreedyToLeft(): Boolean = TODO("Not yet implemented")
        override fun dispose(): Unit = TODO("Not yet implemented")
        override fun getEditor(): Editor = TODO("Not yet implemented")
        override fun getGroup(): FoldingGroup = TODO("Not yet implemented")
        override fun shouldNeverExpand(): Boolean = TODO("Not yet implemented")
    }
}