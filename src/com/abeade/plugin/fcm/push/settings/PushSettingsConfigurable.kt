package com.abeade.plugin.fcm.push.settings

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import javax.swing.JComponent

class PushSettingsConfigurable(project: Project) : SearchableConfigurable {

    private val panel = PushSettingsPanel(project)

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
