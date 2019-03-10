package com.abeade.plugin.fcm.push.stetho

class MultipleStethoProcessesException(val reason: String, val processes: List<String>): Throwable()