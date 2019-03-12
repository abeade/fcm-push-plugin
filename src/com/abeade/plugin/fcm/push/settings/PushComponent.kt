package com.abeade.plugin.fcm.push.settings

import com.abeade.plugin.fcm.push.model.PushSettings
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import java.io.Serializable

@State(
    name = "ScreenGeneratorConfiguration",
    storages = [Storage(value = "screenGeneratorConfiguration.xml")])
class PushComponent : Serializable, PersistentStateComponent<PushComponent> {

    companion object {

        fun getInstance(project: Project) = ServiceManager.getService(project, PushComponent::class.java)!!
    }

    var settings: PushSettings = PushSettings()

    override fun getState(): PushComponent = this

    override fun loadState(state: PushComponent) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
