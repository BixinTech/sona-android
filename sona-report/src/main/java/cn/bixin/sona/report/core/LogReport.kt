package cn.bixin.sona.report.core

import android.util.Log
import cn.bixin.sona.base.Sona
import cn.bixin.sona.report.SonaReportEvent

/**
 * 普通日志
 *
 * @Author luokun
 * @Date 2020/6/7
 */
class LogReport : MsgReport() {

    var print = Sona.showLog()

    override fun report(code: Int, msg: String) {
        if (print) {
            Log.e("SonaReport", msg)
        }
    }


    override fun filter(type: Int): Boolean {
        val r = SonaReportEvent.LOG and type
        return r != 0
    }
}