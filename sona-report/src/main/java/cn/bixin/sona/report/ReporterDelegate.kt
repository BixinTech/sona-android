package cn.bixin.sona.report

import android.os.Handler
import android.os.HandlerThread
import android.text.TextUtils
import cn.bixin.sona.report.core.IReport
import cn.bixin.sona.report.core.LogReport
import cn.bixin.sona.report.core.LoganReport
import cn.bixin.sona.report.core.MsgReport
import com.google.gson.Gson

/**
 * 上报-代理类
 *
 * @Author luokun
 * @Date 2020/6/7
 */
class ReporterDelegate private constructor() {

    companion object {
        var instance: ReporterDelegate? = null

        fun newInstance(): ReporterDelegate {
            return ReporterDelegate()
        }
    }

    private val attribute by lazy { HashMap<String, String>() }

    private val reporterList: MutableList<IReport> = mutableListOf(
        LogReport(), LoganReport()
    )

    var mReportHandler: Handler? = null

    init {
        instance = this
        val handlerThread = HandlerThread("sona-report")
        handlerThread.start()
        mReportHandler = Handler(handlerThread.looper)
    }

    /**
     * 增加属性
     *
     * @param key 属性key
     * @param value 属性value
     */
    fun addAttribute(key: String, value: String) {
        mReportHandler?.post {
            attribute[key] = value
        }
    }

    /**
     * 上报
     *
     * @param event
     */
    fun report(event: SonaReportEvent) {
        mReportHandler?.post {
            reportInternal(event)
        }
    }

    private fun reportInternal(event: SonaReportEvent) {
        try {
            event.productCode = attribute["productCode"] ?: ""
            event.roomId = attribute["roomId"] ?: ""
            event.uid = attribute["uid"] ?: ""
            event.snProductCode = attribute["snProductCode"] ?: ""
            event.supplier = attribute["supplier"] ?: ""

            val data = hashMapOf<String, Any?>()
            if (event.sdkCode != 0) {
                data["sdkCode"] = event.sdkCode
            }
            if (!TextUtils.isEmpty(event.content)) {
                data["message"] = event.content
            }
            if (!TextUtils.isEmpty(event.roomId)) {
                data["roomId"] = event.roomId
            }
            if (!TextUtils.isEmpty(event.uid)) {
                data["uid"] = event.uid
            }
            if (!TextUtils.isEmpty(event.reason)) {
                data["reason"] = event.reason
            }
            fillExt(event)
            if (event.ext != null) {
                data["ext"] = event.ext
            }
            data["mask"] = "SonaRoom"

            val jsonStr = Gson().toJson(data)
            reporterList.forEach { item ->
                if (item.filter(event.type)) {
                    if (item is MsgReport) {
                        item.report(event.code, jsonStr)
                    } else {
                        item.report(event)
                    }
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun fillExt(event: SonaReportEvent) {
        val params = HashMap<String?, String?>()
        params["snProductCode"] = event.snProductCode
        params["cloudProvider"] = event.supplier
        params["roomId"] = event.roomId
        params["sdkCode"] = "${event.sdkCode}"
        event.ext?.putAll(params) ?: let {
            event.ext = params
        }
    }
}