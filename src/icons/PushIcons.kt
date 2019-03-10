package icons

import com.intellij.openapi.util.IconLoader

import javax.swing.*

interface PushIcons {

    companion object {

        @JvmField
        val PUSH_ACTION_ICON = IconLoader.getIcon("/icons/fcmPushIcon.png")
    }
}
