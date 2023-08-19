package com.github.erotourtes.harpoon.services.settings

import com.github.erotourtes.harpoon.utils.CheckBoxDelegate
import com.github.erotourtes.harpoon.utils.SliderDelegate
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBSlider
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel


class SettingsComponent {
    val panelUI: JPanel
    private val numberOfSlashesUI = JBSlider(1, 10, SettingsState.getInstance().numberOfSlashes)
    private val numberOfSlashesLabelUI = JBLabel("Number of visible words: ")
    private val showProjectPathUI = JBCheckBox("Show project path")

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
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    fun getPreferredFocusedComponent(): JComponent = numberOfSlashesUI

    inner class UIState {
        private var numberOfSlashes: Int by SliderDelegate(numberOfSlashesUI)
        private var showProjectPath: Boolean by CheckBoxDelegate(showProjectPathUI)

        fun reset(settings: SettingsState) {
            numberOfSlashes = settings.numberOfSlashes
            showProjectPath = settings.showProjectPath
        }

        fun apply(settings: SettingsState) {
            settings.numberOfSlashes = numberOfSlashes
            settings.showProjectPath = showProjectPath
        }

        fun isModified(state: SettingsState): Boolean =
            showProjectPath != state.showProjectPath ||
                    numberOfSlashes != state.numberOfSlashes
    }
}
