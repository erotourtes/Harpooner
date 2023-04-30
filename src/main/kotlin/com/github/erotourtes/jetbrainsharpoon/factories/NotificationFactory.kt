package com.github.erotourtes.jetbrainsharpoon.factories

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

fun notify(message: String, type: NotificationType = NotificationType.ERROR, project: Project? = null) {
    val notification = Notification(
        "Harpoon",
        "Harpoon",
        message,
        type
    )
    notification.notify(project)
}