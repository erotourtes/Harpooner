package com.github.erotourtes.harpoon.utils

import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.backend.navigation.NavigationRequest
import com.intellij.platform.ide.navigation.NavigationOptions
import com.intellij.platform.ide.navigation.NavigationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal suspend fun openFileInEditor(project: Project, file: VirtualFile): Unit = withContext(Dispatchers.Default) {
    val request = readAction {
        NavigationRequest.sourceNavigationRequest(project, file, -1)
    } ?: error("Cannot create a navigation request for ${file.path}")

    // Await navigation outside the read action. The explicit 262 overload is also retained in 263.
    NavigationService.getInstance(project).navigate(
        request,
        NavigationOptions.requestFocus().preserveCaret(true),
        null,
    )
}
