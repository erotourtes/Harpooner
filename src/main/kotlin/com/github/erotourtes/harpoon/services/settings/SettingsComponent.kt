package com.github.erotourtes.harpoon.services.settings

import com.github.erotourtes.harpoon.utils.CheckBoxDelegate
import com.github.erotourtes.harpoon.utils.SliderDelegate
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBSlider
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel


/*
* TODO: come up with a better approach for adding new settings
*  To do it now you need to:
* 1. Add a new field to SettingsState
* 2. Update snapshot() method in SettingsState
* 3. Add a new UI field to SettingsComponent
* 4. Add a UI field to the FormBuilder in SettingsComponent
* 5. Add a new field to UIState
* 6. Update reset(), apply() and isModified() methods in the UIState
* */

class SettingsComponent {
    val panelUI: JPanel
    private val numberOfSlashesUI = JBSlider(1, 10, SettingsState.getInstance().numberOfSlashes)
    private val numberOfSlashesLabelUI = JBLabel("Number of visible words: ")
    private val showProjectPathUI = JBCheckBox("Show project path")
    private val showNotificationsUI = JBCheckBox("Show notifications")
    private val noteLabel =
        JBLabel("Note: The settings will be applied dynamically, so you should be able see to the changes immediately after applying it. ")

    val state = UIState()

    private fun createSlider(slider: JBSlider): JBSlider {
        val label = numberOfSlashesLabelUI
        val str = label.text
        label.text = "$str${slider.value}"

        slider.addChangeListener {
            label.text = "$str${slider.value}"
        }

        return slider
    }

    init {
        panelUI = FormBuilder.createFormBuilder()
            .addLabeledComponent(
                numberOfSlashesLabelUI,
                createSlider(numberOfSlashesUI),
                1,
                false
            )
            .addComponent(showProjectPathUI, 1)
            .addComponent(showNotificationsUI, 1)
            .addComponent(noteLabel, 1)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    fun getPreferredFocusedComponent(): JComponent = numberOfSlashesUI

    inner class UIState {
        private var numberOfSlashes: Int by SliderDelegate(numberOfSlashesUI)
        private var showProjectPath: Boolean by CheckBoxDelegate(showProjectPathUI)
        private var showNotifications: Boolean by CheckBoxDelegate(showNotificationsUI)

        fun reset(settings: SettingsState) {
            numberOfSlashes = settings.numberOfSlashes
            showProjectPath = settings.showProjectPath
            showNotifications = settings.showNotifications
        }

        fun apply(settings: SettingsState) {
            settings.numberOfSlashes = numberOfSlashes
            settings.showProjectPath = showProjectPath
            settings.showNotifications = showNotifications

            settings.notifyObservers(settings.snapshot())
        }

        fun isModified(state: SettingsState): Boolean =
            showProjectPath != state.showProjectPath ||
                    numberOfSlashes != state.numberOfSlashes ||
                    showNotifications != state.showNotifications
    }
}
