package com.abeade.plugin.fcm.push

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.awt.RelativePoint

fun showMessage(project: Project, message: String, isError: Boolean) {
    val statusBar = WindowManager.getInstance()?.getStatusBar(project)
    JBPopupFactory.getInstance()
        ?.createHtmlTextBalloonBuilder(message, if (isError) MessageType.ERROR else MessageType.INFO, null)
        ?.setFadeoutTime(5000)
        ?.createBalloon()
        ?.show(RelativePoint.getCenterOf(statusBar?.component!!), Balloon.Position.above)
}

fun showNotification(message: String, isError: Boolean) {
    Notifications.Bus.notify(
        Notification(
            "FCM push sender",
            "FCM push sender",
            message,
            if (isError) NotificationType.ERROR else NotificationType.INFORMATION
        )
    )
}