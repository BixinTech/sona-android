package cn.bixin.sona.report.core

import cn.bixin.sona.report.SonaReportEvent

/**
 * 上报接口
 *
 * @Author luokun
 * @Date 2020/6/7
 */
interface IReport {

    fun report(event: SonaReportEvent)

    fun filter(type: Int): Boolean
}