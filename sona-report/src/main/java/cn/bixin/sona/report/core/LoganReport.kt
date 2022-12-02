package cn.bixin.sona.report.core

import cn.bixin.sona.report.SonaReportEvent
import com.dianping.logan.Logan

/**
 * Logan 日志
 *
 * @Author luokun
 * @Date 2020/6/8
 */
class LoganReport : MsgReport() {

    override fun report(code: Int, msg: String) {
        Logan.w(msg, 1)
    }

    override fun filter(type: Int): Boolean {
        val r = SonaReportEvent.LOGAN and type
        return r != 0
    }
}