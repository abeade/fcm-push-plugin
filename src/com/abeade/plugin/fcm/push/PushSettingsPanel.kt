package com.abeade.plugin.fcm.push

import com.intellij.ui.layout.LCFlags
import com.intellij.ui.layout.panel
import java.awt.BorderLayout
import java.text.NumberFormat
import javax.swing.JFormattedTextField
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.text.NumberFormatter

class PushSettingsPanel : JPanel() {

    private val preferenceKeyField: JTextField = JTextField()
    private val authorizationField: JTextField = JTextField()
    private val adbPortField: JFormattedTextField
    private val settingsManager: SettingsManager = SettingsManager()

    init {
        val format = NumberFormat.getInstance().apply {
            isGroupingUsed = false
        }
        val formatter = NumberFormatter(format).apply {
            valueClass = Integer::class.java
            minimum = 0
            maximum = 65535
            allowsInvalid = false
            commitsOnValidEdit = true
        }
        adbPortField = JFormattedTextField(formatter)
        layout = BorderLayout()
        createUI()
    }

    val isModified: Boolean
        get() = preferenceKeyField.text != settingsManager.preferenceKey ||
                authorizationField.text != settingsManager.authorization ||
                adbPortField.text != settingsManager.adbPort!!.toString()

    fun apply() {
        settingsManager.authorization = authorizationField.text
        settingsManager.preferenceKey = preferenceKeyField.text
        settingsManager.adbPort = Integer.parseInt(adbPortField.text)
    }

    fun reset() {
        preferenceKeyField.text = settingsManager.preferenceKey
        authorizationField.text = settingsManager.authorization
        adbPortField.text = settingsManager.adbPort!!.toString()
    }

    private fun createUI() {
        add(
            panel(LCFlags.fillX, title = "Android Components") {
                row("ADB Port") { adbPortField() }
                row("Shared preference Key") { preferenceKeyField() }
                row("") { label("Shared preference where the app has stored the Firebase Registration ID") }
                row("Authorization Key") { authorizationField() }
            }, BorderLayout.CENTER)
    }
}
