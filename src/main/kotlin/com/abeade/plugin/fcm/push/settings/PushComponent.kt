package com.abeade.plugin.fcm.push.settings

import com.abeade.plugin.fcm.push.model.PushSettings
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import java.io.Serializable

@State(
    name = "FCMPushSenderConfiguration",
    storages = [Storage(value = "fcmPushSenderConfiguration.xml")])
class PushComponent : Serializable, PersistentStateComponent<PushComponent> {

    companion object {

        fun getInstance(project: Project) = project.service<PushComponent>()
    }

    var settings: PushSettings = PushSettings()

    override fun getState(): PushComponent = this

    override fun loadState(state: PushComponent) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
