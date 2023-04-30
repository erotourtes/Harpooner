package com.github.erotourtes.jetbrainsharpoon.factories

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType

fun notify(message: String, type: NotificationType = NotificationType.ERROR) {
    val notification = Notification(
        "Harpoon",
        "Harpoon",
        message,
        type
    )
    notification.notify(null)
}