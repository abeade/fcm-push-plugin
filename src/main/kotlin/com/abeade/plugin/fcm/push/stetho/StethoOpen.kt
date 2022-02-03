package com.abeade.plugin.fcm.push.stetho

import com.abeade.plugin.fcm.push.model.DEFAULT_ADB_PORT
import java.io.DataInputStream
import java.io.IOException
import java.io.OutputStream
import java.math.BigInteger
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.*

fun adbDevices(port: Int? = null): MutableList<String> {
    if (port == null) {
        throw HumanReadableException("Must specify a port when calling adbDevices")
    }
    val adb = AdbSmartSocketClient()
    adb.connect(port)
    try {
        adb.selectService("host:devices")
        val deviceNames = mutableListOf<String>()
        repeat(4) { adb.inStream.readByte() }
        val sc = Scanner(adb.inStream)
        while (sc.hasNextLine()) {
            val line = sc.nextLine()
            deviceNames.add(line.split('\t').first())
        }
        return deviceNames
    } catch (e: SelectServiceException) {
        throw HumanReadableException("Failure getting devices ${e.reason}")
    } finally {
        adb.disconnect()
    }
}

fun stethoOpen(device: String? = null, process: String? = null, port: Int? = null): AdbSmartSocketClient {
    val port = port ?: DEFAULT_ADB_PORT
    val adb = connectToDevice(device, port)
    val socketName = if (process == null) {
        findOnlyStethoSocket(device, port)
    } else {
        formatProcessAsStethoProcess(process)
    }
    try {
        adb.selectService("localabstract:$socketName")
    } catch (e: SelectServiceException) {
        throw HumanReadableException("Failure to target process $socketName: ${e.reason} (is it running?)")
    }
    return adb
}

fun connectToDevice(device: String? = null, port: Int? = null): AdbSmartSocketClient {
    if (port == null) {
        throw HumanReadableException("Must specify a port when calling connectToDevice")
    }
    val adb = AdbSmartSocketClient()
    adb.connect(port)

    try {
        if (device == null) {
            adb.selectService("host:transport-any")
        } else {
            adb.selectService("host:transport:$device")
        }
        return adb
    } catch (e: SelectServiceException) {
        throw HumanReadableException("Failure to target device $device ${e.reason}")
    }
}

fun findOnlyStethoSocket(device: String?, port: Int?): String? {
    val adb = connectToDevice(device, port)
    try {
        adb.selectService("shell:cat /proc/net/unix")
        var lastSocketName: String? = null
        val processNames = mutableListOf<String>()
        val sc = Scanner(adb.inStream)
        while (sc.hasNextLine()) {
            val line = sc.nextLine()
            val row = line.trimEnd().split(Regex("\\s+"))
            if (row.size < 8) {
                continue
            }
            val socketName = row[7]
            if (!socketName.startsWith("@stetho_")) {
                continue
            }
            // Filter out entries that are not server sockets
            if (row[3].toInt() != 10000 || row[5].toInt() != 1) {
                continue
            }
            lastSocketName = socketName.substring(1)
            processNames.add(parseProcessFromStethoSocket(socketName))
        }
        when {
            processNames.size > 1 -> throw MultipleStethoProcessesException(processNames)
            lastSocketName == null -> throw HumanReadableException("No stetho-enabled processes running")
            else -> return lastSocketName
        }
    } finally {
        adb.disconnect()
    }
}

fun parseProcessFromStethoSocket(socketName: String): String {
    val regex = "^@stetho_(.+)_devtools_remote$".toRegex()
    val match = regex.find(socketName) ?: throw Exception("Unexpected Stetho socket formatting: $socketName")
    return match.groupValues[1]
}

fun formatProcessAsStethoProcess(process: String) = "stetho_${process}_devtools_remote"

class AdbSmartSocketClient {

    private var connected: Boolean = false
    private lateinit var socket: Socket
    lateinit var outStream: OutputStream
    lateinit var inStream: DataInputStream

    fun connect(port: Int = DEFAULT_ADB_PORT) {
        if (!connected) {
            connected = true
            socket = Socket("127.0.0.1", port)
            socket.soTimeout = SOCKET_TIMEOUT
            outStream = socket.getOutputStream()
            inStream = DataInputStream(socket.getInputStream())
        }
    }

    fun disconnect() {
        if (connected) {
            connected = false
            inStream.close()
            outStream.close()
            socket.close()
        }
    }

    fun readInput(n: Int, tag: String): ByteArray {
        val buff = ByteArray(n)
        val readBytes = inStream.read(buff)
        if (readBytes != n) {
            throw IOException("Unexpected end of stream while reading $tag")
        }
        return buff
    }

    fun selectService(service: String) {
        val message = "%04x%s".format(service.length, service)
        val encoded = message.toByteArray(StandardCharsets.US_ASCII)
        outStream.write(encoded)
        val status = readInput(4, "status")
        when {
            status.contentEquals("OKAY".toByteArray()) -> {
                // All good...
            }
            status.contentEquals("FAIL".toByteArray()) -> {
                val size = readInput(4, "fail reason")
                val reasonLen = BigInteger(String(size, StandardCharsets.US_ASCII), 16).toInt()
                val reason = String(readInput(reasonLen, "fail reason lean"), StandardCharsets.US_ASCII)
                throw SelectServiceException(reason)
            }
            else -> throw Exception("Unrecognized status=$status")
        }
    }

    private companion object {
        const val SOCKET_TIMEOUT = 2000
    }
}
