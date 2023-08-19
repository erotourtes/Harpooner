package com.github.erotourtes.harpoon.utils

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

fun notify(message: String, type: NotificationType = NotificationType.ERROR, project: Project? = null) {
    val notification = Notification(
        PLUGIN_NAME,
        PLUGIN_NAME,
        message,
        type
    )
    notification.notify(project)
}