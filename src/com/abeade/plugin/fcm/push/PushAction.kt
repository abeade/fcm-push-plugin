package com.abeade.plugin.fcm.push

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder

class PushAction : AnAction() {

    override fun update(anActionEvent: AnActionEvent) {
        val project = anActionEvent.project
        val isValid = project != null
        anActionEvent.presentation.isEnabledAndVisible = isValid
    }

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val project = anActionEvent.project!!
        val authorization = SettingsManager().authorization
        if (authorization.isNullOrBlank()) {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, "FCM push sender")
        } else {
            val dialog = PushDialogWrapper(PropertiesComponent.getInstance())
            val result = dialog.showAndGet()
            if (result) {
                if (postToFcm(dialog.pushData!!, authorization)) {
                    showMessage(project, "Push notification sent", false)
                } else {
                    showMessage(project, "Error sending push notification", true)
                }
            }
        }
    }

    private fun postToFcm(pushData: PushData, authorization: String): Boolean {
        val httpClient = HttpClientBuilder.create().build()
        return try {
            val request = HttpPost("https://fcm.googleapis.com/fcm/send")
            val params = StringEntity("{\"to\":\"${pushData.firebaseId}\",\"data\":${pushData.data}}")
            request.addHeader("Content-Type", "application/json")
            request.addHeader("Authorization", "key=$authorization")
            request.entity = params
            val response = httpClient.execute(request)
            val result = String(response.entity.content.readBytes())
            showNotification(result, false)
            true
        } catch (ex: Exception) {
            showNotification(ex.toString(), true)
            false
        }
    }
}