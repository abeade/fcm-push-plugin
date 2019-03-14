package com.abeade.plugin.fcm.push.settings

import com.abeade.plugin.fcm.push.utils.EMPTY
import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.project.Project

class SettingsManager(project: Project) {

    companion object {

        private const val SUBSYSTEM = "PushSystem"
        const val AUTHORIZATION_KEY = "#com.abeade.plugin.fcm.push.pushdialog.authorization"
    }

    private val pushComponent: PushComponent = PushComponent.getInstance(project)
    private val credentialAttributes: CredentialAttributes =
        CredentialAttributes(generateServiceName(
            SUBSYSTEM,
            AUTHORIZATION_KEY
        ))

    var authorization: String
        get() = PasswordSafe.instance.getPassword(credentialAttributes) ?: String.EMPTY
        set(value) {
            val credentials = Credentials(String.EMPTY, value)
            PasswordSafe.instance.set(credentialAttributes, credentials)
        }

    var preferenceKey: String
        get() = pushComponent.settings.preferenceKey
        set(value) { pushComponent.settings.preferenceKey = value }

    var adbPort: Int
        get() = pushComponent.settings.port
        set(value) { pushComponent.settings.port = value }

    val templates = pushComponent.settings.templates
}
