package com.abeade.plugin.fcm.push.stetho

import java.io.IOException
import java.nio.charset.StandardCharsets
import javax.naming.OperationNotSupportedException

class StethoPreferenceSearcher {

    private companion object {

        private const val PREF_SEPARATOR = " = "
        private val prefsAsUtf8 = "prefs".toByteArray(StandardCharsets.UTF_8)
        private val printAsUtf8 = "print".toByteArray(StandardCharsets.UTF_8)
        private val commonCommands = listOf(prefsAsUtf8, printAsUtf8)
    }

    fun getSharedPreference(file: String?, key: String, device: String?, process: String?, port: Int?): String? {
        val result: String
        val struct = Struct()
        val devices = adbDevices(port)
        if (device == null && devices.size > 1) {
            throw MultipleDevicesException(devices)
        }
        val adbSock = stethoOpen(device, process, port)
        adbSock.outStream.write("DUMP".toByteArray() + struct.pack("!i", 1))
        val commands = commonCommands.toMutableList()
        if (file != null) {
            commands.add(file.toByteArray(StandardCharsets.UTF_8))
            commands.add(key.toByteArray(StandardCharsets.UTF_8))
        }
        var enterFrame = "!".toByteArray() + struct.pack("!i", commands.size.toLong())
        for (command in commands) {
            enterFrame += struct.pack("!H", command.size.toLong())
            enterFrame += command
        }
        adbSock.outStream.write(enterFrame)
        result = readFrames(adbSock, struct)
        val sharedPrefs = result.lines().filter { it.contains("$key$PREF_SEPARATOR") }
        if (sharedPrefs.size > 1) {
            throw HumanReadableException("\"Multiple files contains $key shared preference:\n" +
                    sharedPrefs.fold("") { str, item ->  "$str\t$item\n" } +
                "Please specify the target file in plugin settings.")
        }
        return sharedPrefs.firstOrNull()?.split(PREF_SEPARATOR)?.lastOrNull()
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
                    throw OperationNotSupportedException("Input stream not supported")
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
