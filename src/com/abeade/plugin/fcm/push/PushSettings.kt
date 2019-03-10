package com.abeade.plugin.fcm.push

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class PushSettings : Configurable {

    private val panel = PushSettingsPanel()

    override fun getDisplayName(): String? = "FCM push sender"

    override fun getHelpTopic(): String? = "FCM push sender"

    override fun createComponent(): JComponent? = panel.createPanel()

    override fun isModified(): Boolean = panel.isModified

    override fun apply() {
        panel.apply()
    }

    override fun reset() {
        panel.reset()
    }

    override fun disposeUIResources() {
        // Empty
    }
}