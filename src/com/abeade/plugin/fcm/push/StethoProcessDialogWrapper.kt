package com.abeade.plugin.fcm.push

import com.abeade.plugin.fcm.push.ui.StethoProcessDialog
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBList
import com.intellij.ui.layout.panel
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent

class StethoProcessDialogWrapper(private val processes: List<String>) : DialogWrapper(true) {

    init {
        init()
        title = "Select Stetho-enabled process"
    }

    var selectedProcess: String = processes[0]
        private set

    override fun createCenterPanel(): JComponent? {
        return StethoProcessDialog().apply {
            listProcesses.apply {
                setListData(processes.toTypedArray())
                selectedIndex = 0
                addListSelectionListener { selectedProcess = selectedValue }
                addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent?) {
                        if (e?.clickCount == 2) {
                            this@StethoProcessDialogWrapper.close(0, true)
                        }
                    }
                })
            }
        }.panelMain
    }
}
