package com.github.erotourtes.harpoon.utils

import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBSlider

class CheckBoxDelegate(private val checkBox: JBCheckBox) {
    operator fun getValue(thisRef: Any?, property: Any?): Boolean = checkBox.isSelected
    operator fun setValue(thisRef: Any?, property: Any?, value: Boolean) = checkBox.setSelected(value)
}

class SliderDelegate(private val slider: JBSlider) {
    operator fun getValue(thisRef: Any?, property: Any?): Int = slider.value
    operator fun setValue(thisRef: Any?, property: Any?, value: Int) {
        slider.value = value
    }
}

data class ProjectInfo(val name: String, val path: String) {
    companion object {
        fun from(path: String?): ProjectInfo {
            val projectPath = path?.substring(0, path.lastIndexOf(IDEA_PROJECT_FOLDER)) ?: ""
            val name = projectPath.substring(projectNameIndex(projectPath))
            return ProjectInfo(name, projectPath)
        }

        private fun projectNameIndex(projectPath: String): Int {
            var lastIndex = -1
            for (index in projectPath.length - 2 downTo 0) {
                if (projectPath[index] != '/') continue
                lastIndex = index + 1
                break
            }

            return lastIndex
        }
    }
}
