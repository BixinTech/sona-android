package com.yupaopao.mercury.library.misc

import com.alibaba.fastjson.util.IOUtils
import java.io.*
import java.net.InetAddress
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.roundToInt


object Misc {
    fun exceptionToString(throwable: Throwable): String {
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        return sw.toString()
    }

    fun cpuInfo(): Map<String, String> {
        val map = HashMap<String, String>()
        var scanner:Scanner?=null
        try {
             scanner = Scanner(File("/proc/cpuinfo"))
            while (scanner.hasNextLine()) {
                val vals: Array<String> = scanner.nextLine().split(": ").toTypedArray()
                if (vals.size > 1) map[vals[0].trim { it <= ' ' }] = vals[1].trim { it <= ' ' }
            }

        } catch (e: Exception) {
        }finally {
            IOUtils.close(scanner)
        }
        return map
    }

    fun memInfo(): Int {
        var tm = 1000
        var reader:RandomAccessFile?=null
        try {
            reader = RandomAccessFile("/proc/meminfo", "r")
            val load = reader.readLine()
            val totalRam = load.split(" kB").toTypedArray()
            val trm = totalRam[0].split(" ").toTypedArray()
            tm = trm[trm.size - 1].toInt()
            tm = (tm / 1024.toFloat()).roundToInt()

        } catch (ex: IOException) {
            ex.printStackTrace()
        }finally {
            IOUtils.close(reader)
        }
        return tm
    }

    fun parseHost(host: String): ArrayList<String> {
        val ips = ArrayList<String>()
        try {
            val inetAddressArray = InetAddress.getAllByName(host)
            inetAddressArray?.let { inetAddressArray ->
                inetAddressArray.forEach {
                    ips.add(it.hostAddress)
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return ips
    }
}