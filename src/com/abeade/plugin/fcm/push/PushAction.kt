package com.abeade.plugin.fcm.push

import com.google.gson.Gson
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.ui.MessageType
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
            val dialog = PushDialogWrapper(PropertiesComponent.getInstance(), project)
            if (dialog.showAndGet()) {
                val result = postToFcm(dialog.pushData!!, authorization)
                if (result.success) {
                    showMessage(project, "Push notification sent", MessageType.INFO)
                } else {
                    val msg = result.message ?: String.EMPTY
                    showMessage(project, "Error sending push notification $msg", MessageType.ERROR)
                }
            }
        }
    }

    private fun postToFcm(pushData: PushData, authorization: String): FCMResult {
        val httpClient = HttpClientBuilder.create().build()
        return try {
            val request = HttpPost("https://fcm.googleapis.com/fcm/send")
            val params = StringEntity("{\"to\":\"${pushData.firebaseId}\",\"data\":${pushData.data}}")
            request.addHeader("Content-Type", "application/json")
            request.addHeader("Authorization", "key=$authorization")
            request.entity = params
            val response = httpClient.execute(request)
            val result = String(response.entity.content.readBytes())
            val fcmResponse = try {
                Gson().fromJson<FCMResponse>(result, FCMResponse::class.java)
            } catch (e: Exception) {
                null
            }
            if (fcmResponse != null && fcmResponse.failure > 0) {
                val error = fcmResponse.results.firstOrNull()?.error
                showNotification(error ?: result, NotificationType.ERROR)
                FCMResult(false, error)
            } else {
                val message = fcmResponse?.let { "${it.success} push notification sent" }
                showNotification(message ?: result, NotificationType.INFORMATION)
                FCMResult(true, message)
            }
        } catch (ex: Exception) {
            showNotification(ex.toString(), NotificationType.ERROR)
            FCMResult(false, null)
        }
    }

    private data class FCMResult(
        val success: Boolean,
        val message: String?
    )
}
