package com.abeade.plugin.fcm.push.settings

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import javax.swing.JComponent

class PushSettingsConfigurable(private val project: Project) : SearchableConfigurable {

    private var panel: PushSettingsWrapper? = null

    override fun getDisplayName(): String? = "FCM push sender"

    override fun getHelpTopic(): String? = "FCM push sender"

    override fun createComponent(): JComponent? {
        panel = PushSettingsWrapper(project)
        return panel!!.createPanel()
    }

    override fun isModified(): Boolean = panel?.isModified == true

    override fun getId(): String = "FCM push sender"

    override fun apply() {
        panel?.apply()
    }

    override fun reset() {
        panel?.reset()
    }
}
