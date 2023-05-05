package com.github.erotourtes.harpoon.utils

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

fun notify(message: String, type: NotificationType = NotificationType.ERROR, project: Project? = null) {
    val notification = Notification(
        "Harpooner",
        "Harpooner",
        message,
        type
    )
    notification.notify(project)
}