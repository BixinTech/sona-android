package cn.bixin.sona.demo

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.multidex.MultiDex
import cn.bixin.sona.base.Sona
import com.alibaba.fastjson.JSONObject
import com.dianping.logan.Logan
import com.dianping.logan.LoganConfig
import com.yupaopao.mercury.library.Common
import com.yupaopao.mercury.library.core.Config
import com.yupaopao.mercury.library.misc.Misc
import java.io.File
import java.util.*


class MainApplication : Application() {

    companion object {
        val TAG = "MainApplication"

       private lateinit var appContext: Application

        fun getContext(): Context {
            return appContext
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        Sona.init(this)
        Sona.openLog()
        initLogan()
        initMercury()
    }

    private fun initLogan() {
        val config = LoganConfig.Builder()
            .setCachePath(applicationContext.filesDir.absolutePath)
            .setPath(
                applicationContext.getExternalFilesDir(null)!!.absolutePath
                        + File.separator + "logan_v1"
            )
            .setEncryptKey16("0123456789012345".toByteArray())
            .setEncryptIV16("0123456789012345".toByteArray())
            .build()
        Logan.init(config)
    }

    private fun initMercury() {
        NetworkReceiver.register(this)
        Config.closeMercury = false
        Config.setIp(cn.bixin.sona.demo.constant.Config.MERCURY_IP, cn.bixin.sona.demo.constant.Config.MERCURY_PORT)
        Config.handShakeBody = JSONObject().apply {
            put("d", UUID.randomUUID().toString())
            put("p", 2)
            put("a", 30)
            put("sv", Build.VERSION.SDK_INT.toString())
            put("av", "4.4.2")
            put("u", "80872014")

            val cpuInfo = Misc.cpuInfo()
            val memInfo = Misc.memInfo()
            put("m", Build.MANUFACTURER + " " + Build.MODEL)
            put("e", JSONObject().apply {
                put("cpu", cpuInfo["Hardware"])
                put("memory", memInfo.toString() + "MB")
            })
        }

        Common.exceptionCallback = { type, exceptions ->
            exceptions.forEach {
                it.third.printStackTrace()
            }
            Log.e(TAG, type.toString() + ": " + exceptions.size.toString())
        }

        Common.logCallback = { type, log ->
            Log.e(TAG, "$type: $log")
        }
    }


}