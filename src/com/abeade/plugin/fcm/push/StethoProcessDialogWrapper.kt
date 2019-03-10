package com.abeade.plugin.fcm.push

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBList
import com.intellij.ui.layout.panel
import javax.swing.JComponent
import javax.swing.JList

class StethoProcessDialogWrapper(private val processes: List<String>) :
    DialogWrapper(true) {

    init {
        init()
        title = "Select Stetho-enabled process"
    }

    var selectedProcess: String = processes[0]
        private set

    override fun createCenterPanel(): JComponent? {
        val jbList = JBList(*processes.toTypedArray()).apply {
            selectedIndex = 0
            addListSelectionListener { selectedProcess = selectedValue }
        }
        return panel {
            row("Multiple stetho-enabled processes available") { }
            row {  }
            row("Select process:") { }
            row {
                jbList(grow, grow)
            }
        }
    }
}
