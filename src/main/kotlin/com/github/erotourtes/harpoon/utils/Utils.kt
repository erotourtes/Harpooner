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