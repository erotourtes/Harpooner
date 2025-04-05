package com.github.erotourtes.harpoon.utils

import com.github.erotourtes.harpoon.settings.SettingsState
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBSlider
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

fun notify(message: String, type: NotificationType = NotificationType.ERROR, project: Project? = null) {
    val settings = SettingsState.getInstance()
    if (!settings.showNotifications) return

    val notification = Notification(
        PLUGIN_NAME, PLUGIN_NAME, message, type
    )
    notification.notify(project)
}

class CheckBoxDelegate(private val checkBox: JBCheckBox) {
    operator fun getValue(thisRef: Any?, property: Any?): Boolean = checkBox.isSelected
    operator fun setValue(thisRef: Any?, property: Any?, value: Boolean) {
        checkBox.isSelected = value
    }
}

class SliderDelegate(private val slider: JBSlider) {
    operator fun getValue(thisRef: Any?, property: Any?): Int = slider.value
    operator fun setValue(thisRef: Any?, property: Any?, value: Int) {
        slider.value = value
    }
}

open class Observable<T> {
    @Transient
    private val observers = mutableListOf<(T) -> Unit>()

    /**
     * Adds an observer to the list of observers.
     * @return a function that removes the observer from the list.
     */
    fun addObserver(observer: (T) -> Unit): () -> Unit {
        observers.add(observer)
        return { observers.remove(observer) }
    }

    fun notifyObservers(value: T) {
        observers.forEach { it(value) }
    }
}