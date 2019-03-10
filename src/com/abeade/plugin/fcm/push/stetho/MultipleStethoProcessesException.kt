package com.abeade.plugin.fcm.push.stetho

class MultipleStethoProcessesException(reason: String, val processes: List<String>): HumanReadableException(reason)