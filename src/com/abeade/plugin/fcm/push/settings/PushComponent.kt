package com.abeade.plugin.fcm.push.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import java.io.Serializable

@State(
    name = "ScreenGeneratorConfiguration",
    storages = [Storage(value = "screenGeneratorConfiguration.xml")])
class PushComponent : Serializable, PersistentStateComponent<PushComponent> {

    companion object {

        fun getInstance() = ServiceManager.getService(PushComponent::class.java)!!
    }

    var settings: PushSettings = PushSettings()

    override fun getState(): PushComponent = this

    override fun loadState(state: PushComponent) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
