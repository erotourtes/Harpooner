package com.github.erotourtes.harpoon.factories

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

fun notify(message: String, type: NotificationType = NotificationType.ERROR, project: Project? = null) {
    val notification = Notification(
        "Harpoon-JB",
        "Harpoon-JB",
        message,
        type
    )
    notification.notify(project)
}