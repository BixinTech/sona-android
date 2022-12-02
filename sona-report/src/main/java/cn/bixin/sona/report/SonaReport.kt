package cn.bixin.sona.report

/**
 * 上报
 *
 * @Author luokun
 * @Date 2020/6/8
 */
object SonaReport {

    /**
     * 增加属性
     *
     * @param key 属性key
     * @param value 属性value
     */
    fun addAttribute(key: String, value: String) {
        ReporterDelegate.instance?.addAttribute(key, value)
    }

    /**
     * 上报
     *
     * @param event
     */
    fun report(event: SonaReportEvent) {
        ReporterDelegate.instance?.report(event)
    }

    /**
     * 开启上报日志
     */
    fun start() {
        stop()
        ReporterDelegate.newInstance()
    }

    /**
     * 停止上报日志
     */
    fun stop() {
        ReporterDelegate.instance?.mReportHandler?.looper?.quitSafely()
        ReporterDelegate.instance = null
    }
}