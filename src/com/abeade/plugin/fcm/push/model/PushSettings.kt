package com.abeade.plugin.fcm.push.model

import com.abeade.plugin.fcm.push.utils.EMPTY
import java.io.Serializable

const val DEFAULT_ADB_PORT = 5037
const val DEFAULT_PREFERENCE_KEY = "gcm_token"

data class PushSettings(
    var port: Int = DEFAULT_ADB_PORT,
    var preferenceKey: String = DEFAULT_PREFERENCE_KEY,
    var templates: MutableList<PushTemplate> = mutableListOf()
) : Serializable

data class PushTemplate(
    var name: String = String.EMPTY,
    var data: String = String.EMPTY
) : Serializable {
    override fun toString() = name
}
