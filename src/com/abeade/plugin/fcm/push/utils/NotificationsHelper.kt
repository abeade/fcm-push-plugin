package com.abeade.plugin.fcm.push.utils

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.awt.RelativePoint

private const val GROUP_DISPLAYED_ID = "FCM push sender"
private const val TITLE = "FCM push sender"

fun showMessage(project: Project, message: String, messageType: MessageType) {
    val statusBar = WindowManager.getInstance()?.getStatusBar(project)
    JBPopupFactory.getInstance()
        ?.createHtmlTextBalloonBuilder(message, messageType, null)
        ?.setFadeoutTime(5000)
        ?.createBalloon()
        ?.show(RelativePoint.getCenterOf(statusBar?.component!!), Balloon.Position.above)
}

fun showNotification(message: String, notificationType: NotificationType) {
    Notifications.Bus.notify(
        Notification(
            GROUP_DISPLAYED_ID,
            TITLE,
            message,
            notificationType
        )
    )
}
