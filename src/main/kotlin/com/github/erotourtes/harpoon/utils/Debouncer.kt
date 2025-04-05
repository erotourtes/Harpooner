package com.github.erotourtes.harpoon.utils

import com.intellij.openapi.Disposable
import com.intellij.util.Alarm

class Debouncer(
    disposable: Disposable,
    private val ms: Long
) {
    private val alarm = Alarm(disposable)

    fun debounce(action: () -> Unit) {
        alarm.cancelAllRequests()
        alarm.addRequest(action, ms)
    }
}