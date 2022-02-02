package com.abeade.plugin.fcm.push.stetho

class MultipleStethoProcessesException(val processes: List<String>): Throwable()
