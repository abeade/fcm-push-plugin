package com.abeade.plugin.fcm.push.stetho

class MultipleDevicesException(val devices: List<String>): Throwable()
