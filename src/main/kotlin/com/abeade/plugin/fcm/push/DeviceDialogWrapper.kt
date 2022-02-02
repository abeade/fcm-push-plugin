package com.abeade.plugin.fcm.push

import com.abeade.plugin.fcm.push.ui.DeviceDialog
import com.intellij.openapi.ui.DialogWrapper
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

    override fun createCenterPanel(): JComponent {
        return DeviceDialog().apply {
            listDevices.apply {
                setListData(devices.toTypedArray())
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
        }.panelMain
    }
}
