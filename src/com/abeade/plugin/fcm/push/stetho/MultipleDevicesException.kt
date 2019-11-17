package com.abeade.plugin.fcm.push.stetho

class MultipleDevicesException(reason: String, val devices: List<String>): HumanReadableException(reason)