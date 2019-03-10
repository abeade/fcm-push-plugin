package com.abeade.plugin.fcm.push

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.ide.util.PropertiesComponent

class SettingsManager {

    private companion object {

        private const val SUBSYSTEM = "PushSystem"
    }

    private val propertiesComponent: PropertiesComponent = PropertiesComponent.getInstance()
    private val credentialAttributes: CredentialAttributes =
        CredentialAttributes(generateServiceName(SUBSYSTEM, PushDialogWrapper.AUTHORIZATION_KEY))

    var authorization: String?
        get() = PasswordSafe.instance.getPassword(credentialAttributes)
        set(value) = propertiesComponent.setValue(PushDialogWrapper.PREFERENCE_KEY, value)

    var preferenceKey: String?
        get() {
            var preferenceKey = propertiesComponent.getValue(PushDialogWrapper.PREFERENCE_KEY)
            if (preferenceKey == null) {
                preferenceKey = PushDialogWrapper.DEFAULT_PREFERENCE
            }
            return preferenceKey
        }
        set(value) {
            val credentials = Credentials("", value)
            PasswordSafe.instance.set(credentialAttributes, credentials)
        }
}
