package com.abeade.plugin.fcm.push

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBList
import com.intellij.ui.layout.panel
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent

class DeviceDialogWrapper(private val devices: List<String>) : DialogWrapper(true) {

    init {
        init()
        title = "Select target device"
    }

    var selectedDevice: String = devices[0]
        private set

    override fun createCenterPanel(): JComponent? {
        val jbList = JBList(*devices.toTypedArray()).apply {
            selectedIndex = 0
            addListSelectionListener { selectedDevice = selectedValue }
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    if (e?.clickCount == 2) {
                        this@DeviceDialogWrapper.close(0, true)
                    }
                }
            })
        }
        return panel {
            row("Multiple devices are available via ADB.") { }
            row {  }
            row("Select device:") { }
            row {
                jbList(grow, grow)
            }
        }
    }
}
