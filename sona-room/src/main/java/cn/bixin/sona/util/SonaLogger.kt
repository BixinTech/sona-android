package cn.bixin.sona.util

import android.util.Log
import cn.bixin.sona.base.Sona
import cn.bixin.sona.report.SonaReport
import cn.bixin.sona.report.SonaReportEvent

object SonaLogger {

    var print = Sona.showLog()

    @JvmStatic
    fun print(message: String?) {
        if (print) {
            Log.e(SonaConstant.TAG, message ?: "")
        }
    }

    @JvmStatic
    fun print(message: String?, e: Throwable?) {
        if (print) {
            Log.e(SonaConstant.TAG, message ?: "", e)
        }
    }

    @JvmStatic
    @JvmOverloads
    fun log(content: String, code: Int = 0, sdkCode: Int = 0, reason: String = "") {
        SonaReport.report(
            SonaReportEvent.Builder()
                .setCode(code)
                .setSdkCode(sdkCode)
                .setContent(content)
                .setReason(reason)
                .setType(SonaReportEvent.LOG or SonaReportEvent.LOGAN)
                .build()
        )
    }
}