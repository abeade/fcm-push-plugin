package com.abeade.plugin.fcm.push.stetho

import com.abeade.plugin.fcm.push.SettingsManager
import com.intellij.openapi.externalSystem.service.execution.NotSupportedException
import java.io.IOException
import java.nio.charset.StandardCharsets

class StethoPreferenceSearcher {

    private companion object {

        private const val PREF_SEPARATOR = " = "
        private val prefsAsUtf8 = "prefs".toByteArray(StandardCharsets.UTF_8)
        private val printAsUtf8 = "print".toByteArray(StandardCharsets.UTF_8)
        private val commands = listOf(prefsAsUtf8, printAsUtf8)
    }

    fun getSharedPreference(key: String, process: String?, port: Int?): String? {
        val result: String
        val struct = Struct()
        val adbSock = stethoOpen(null, process, port)
        adbSock.outStream.write("DUMP".toByteArray() + struct.pack("!i", 1))
        var enterFrame = "!".toByteArray() + struct.pack("!i", commands.size.toLong())
        for (command in commands) {
            enterFrame += struct.pack("!H", command.size.toLong())
            enterFrame += command
        }
        adbSock.outStream.write(enterFrame)
        result = readFrames(adbSock, struct)
        val shared = result.lines().firstOrNull { it.contains("$key$PREF_SEPARATOR") }
        return shared?.split(PREF_SEPARATOR)?.lastOrNull()
    }

    private fun readFrames(adbSock: AdbSmartSocketClient, struct: Struct): String {
        val stringBuilder = StringBuilder()
        loop@ while (true) {
            val code = adbSock.readInput(1, "code")
            val n = struct.unpack("!I", adbSock.readInput(4, "int4"))[0]
            when {
                code.contentEquals("1".toByteArray()) -> {
                    if (n > 0) {
                        stringBuilder.append(String(adbSock.readInput(n.toInt(), "stdout blob")))
                    }
                }
                code.contentEquals("2".toByteArray()) -> {
                    if (n > 0) {
                        System.err.write(adbSock.readInput(n.toInt(), "stderr blob"))
                        System.err.flush()
                    }
                }
                code.contentEquals("_".toByteArray()) -> {
                    throw NotSupportedException("Input stream not supported")
                }
                code.contentEquals("x".toByteArray()) -> {
                    //exitProcess(n.toInt())
                    break@loop
                }
                else -> {
                    throw IOException("Unexpected header: ${String(code)}")
                }
            }
        }
        return stringBuilder.toString()
    }
}