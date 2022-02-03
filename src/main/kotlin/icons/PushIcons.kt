package icons

import com.intellij.openapi.util.IconLoader

interface PushIcons {

    companion object {

        @JvmField
        val PUSH_ACTION_ICON = IconLoader.getIcon("/icons/fcmPushIcon.png", Companion::class.java)
    }
}
