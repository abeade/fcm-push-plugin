package com.abeade.plugin.fcm.push

import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.awt.RelativePoint

class PushAction : AnAction() {

    override fun update(anActionEvent: AnActionEvent) {
        val project = anActionEvent.project
        val isValid = project != null
        anActionEvent.presentation.isEnabledAndVisible = isValid
    }

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val dialog = PushDialogWrapper(PropertiesComponent.getInstance())
        val result = dialog.showAndGet()
        if (result) {

        }
    }

    private fun showMessage(project: Project, message: String, isError: Boolean) {
        Notifications.Bus.notify(Notification(
            "FCM push sender",
            "FCM push sender",
            message,
            if (isError) NotificationType.ERROR else NotificationType.INFORMATION
        ))
        val statusBar = WindowManager.getInstance()
            ?.getStatusBar(project)
        JBPopupFactory.getInstance()
            ?.createHtmlTextBalloonBuilder(message, if (isError) MessageType.ERROR else MessageType.INFO, null)
            ?.setFadeoutTime(5000)
            ?.createBalloon()
            ?.show(RelativePoint.getCenterOf(statusBar?.component!!), Balloon.Position.above)
    }
}