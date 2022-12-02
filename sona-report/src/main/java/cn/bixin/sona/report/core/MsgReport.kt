package cn.bixin.sona.report.core

import cn.bixin.sona.report.SonaReportEvent

/**
 * msg 日志
 *
 * @Author luokun
 * @Date 2020/6/8
 */
abstract class MsgReport : IReport {

    final override fun report(event: SonaReportEvent) {
        // do nothing
    }

    abstract fun report(code: Int, msg: String)
}