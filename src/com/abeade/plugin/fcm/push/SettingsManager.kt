package com.abeade.plugin.fcm.push

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.ide.util.PropertiesComponent

class SettingsManager {

    companion object {

        private const val SUBSYSTEM = "PushSystem"
        const val DEFAULT_ADB_PORT = 5037
        const val DEFAULT_PREFERENCE = "gcm_token"
        const val ADB_PORT_KEY = "#com.abeade.plugin.fcm.push.pushdialog.adbport"
        const val PREFERENCE_KEY = "#com.abeade.plugin.fcm.push.pushdialog.preferenceKey"
        const val AUTHORIZATION_KEY = "#com.abeade.plugin.fcm.push.pushdialog.authorization"
    }

    private val propertiesComponent: PropertiesComponent = PropertiesComponent.getInstance()
    private val credentialAttributes: CredentialAttributes =
        CredentialAttributes(generateServiceName(SUBSYSTEM, AUTHORIZATION_KEY))

    var authorization: String?
        get() = PasswordSafe.instance.getPassword(credentialAttributes) ?: String.EMPTY
        set(value) {
            val credentials = Credentials("", value)
            PasswordSafe.instance.set(credentialAttributes, credentials)
        }

    var preferenceKey: String?
        get() {
            var preferenceKey = propertiesComponent.getValue(PREFERENCE_KEY)
            if (preferenceKey == null) {
                preferenceKey = DEFAULT_PREFERENCE
            }
            return preferenceKey
        }
        set(value) = propertiesComponent.setValue(PREFERENCE_KEY, value)

    var adbPort: Int?
        get() = propertiesComponent.getInt(ADB_PORT_KEY, DEFAULT_ADB_PORT)
        set(value) = propertiesComponent.setValue(ADB_PORT_KEY, value ?: DEFAULT_ADB_PORT, DEFAULT_ADB_PORT)
}
