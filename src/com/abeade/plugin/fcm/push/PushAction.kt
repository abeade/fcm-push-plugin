package com.abeade.plugin.fcm.push

import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.awt.RelativePoint
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
            Notifications.Bus.notify(Notification(
                "FCM push sender",
                "FCM push sender",
                response.toString(),
                NotificationType.INFORMATION
            ))
            true
        } catch (ex: Exception) {
            Notifications.Bus.notify(Notification(
                "FCM push sender",
                "FCM push sender",
                ex.toString(),
                NotificationType.ERROR
            ))
            false
        }
    }

    private fun showMessage(project: Project, message: String, isError: Boolean) {
        val statusBar = WindowManager.getInstance()
            ?.getStatusBar(project)
        JBPopupFactory.getInstance()
            ?.createHtmlTextBalloonBuilder(message, if (isError) MessageType.ERROR else MessageType.INFO, null)
            ?.setFadeoutTime(5000)
            ?.createBalloon()
            ?.show(RelativePoint.getCenterOf(statusBar?.component!!), Balloon.Position.above)
    }
}