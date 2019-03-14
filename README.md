![JetBrains IntelliJ Plugins](https://img.shields.io/jetbrains/plugin/v/12104-fcm-push-sender.svg?color=green&style=plastic)

# FCM push sender
Sends push notifications using FCM.<br>
Uses <a href="http://facebook.github.io/stetho/">Stetho</a>'s prefs dumpapp plugin to find the application Firebase Registration ID token in app shared preferences

# Installation
You can install the plugin from the [Plugin Repository](https://plugins.jetbrains.com/plugin/12104-fcm-push-sender).

Or you can download the latest binary from [releases](https://github.com/abeade/fcm-push-plugin/releases) on this repo

# Setup
You need to set the "Authorization Key" in plugin settings:

![Settings1](images/settings1.png)

# Usage
The plugin installs one menu entry under tools menu:

![Menu](images/menu.png)

And a toolbar button:

![Toolbar](images/toolbar.png)

When using this option the FCM push sender dialog will be shown:

![Dialog1](images/push1.png)

At this point the plugin tries to find the app Firebase Id in the configured shared preference key, if the id is found it will be shown, otherwise you can set it manually.

The "Remember" check allows to store the current data for next plugin executions.

# Extra configuration
Also you can create, import and export templates:

![Settings2](images/settings2.png)

Then you can use the templates in the send dialog:

![Dialog2](images/push2.png)

# Notes
One of the main features of this plugin is the ability of search automatically the Firebase Registration ID token in app shared preferences using stetho dumpapp plugin, however you can disable the stetho integration in plugin settings and set the ID manually.

When using stetho to find the Id, the plugin works with one single device connected on debug mode (ADB accessible), no multiple devices are allowed.

If the connected device has more than one stetho-enabled process running, the plugin will ask to select the target process:

![Popup](images/popup.png)