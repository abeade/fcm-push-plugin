package com.abeade.plugin.fcm.push.settings

import com.intellij.openapi.options.SearchableConfigurable
import javax.swing.JComponent

class PushSettingsConfigurable : SearchableConfigurable {

    private val panel = PushSettingsPanel()

    override fun getDisplayName(): String? = "FCM push sender"

    override fun getHelpTopic(): String? = "FCM push sender"

    override fun createComponent(): JComponent? = panel

    override fun isModified(): Boolean = panel.isModified

    override fun getId(): String = "FCM push sender"

    override fun apply() {
        panel.apply()
    }

    override fun reset() {
        panel.reset()
    }
}