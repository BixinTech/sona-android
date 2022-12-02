package cn.bixin.sona.component.internal.audio.zego.handler

import cn.bixin.sona.component.audio.AudioStream
import cn.bixin.sona.component.internal.audio.AudioSession
import cn.bixin.sona.component.internal.audio.ReportEvent
import cn.bixin.sona.component.internal.audio.SteamType
import cn.bixin.sona.component.internal.audio.zego.ZegoAudio
import cn.bixin.sona.report.SonaReport
import cn.bixin.sona.report.SonaReportEvent

/**
 * 事件上报
 */
class ZegoReportHandler(component: ZegoAudio) : BaseZegoHandler(component) {

    private val pullStreamStartTimeMap: HashMap<String, Long> = HashMap() // 记录拉流开始时间

    override fun unAssembling() {
        pullStreamStartTimeMap.clear()
    }

    fun recordPullStreamTime(streamId: String) {
        pullStreamStartTimeMap[streamId] = System.currentTimeMillis()
    }

    /**
     * 上报拉流时长
     *
     * @param streamId
     */
    fun reportPullStreamCostTime(streamId: String) {
        val pullStreamStartTime = pullStreamStartTimeMap[streamId]
        if (pullStreamStartTime == null || getStreamHandler() == null) {
            return
        }
        val endTime = System.currentTimeMillis()
        val costTime = endTime - pullStreamStartTime
        if (costTime <= 0) return
        val ext = HashMap<String?, String?>()
        ext[ReportEvent.KEY_BEGIN_TIME] = pullStreamStartTime.toString()
        ext[ReportEvent.KEY_END_TIME] = endTime.toString()
        ext[ReportEvent.KET_DURATION] = costTime.toString()
        val sonaReportEvent = SonaReportEvent.Builder()
            .setContent("pull stream $streamId cost time $costTime")
            .setExt(ext)
            .setType(SonaReportEvent.LOG or SonaReportEvent.LOGAN)
            .build()
        SonaReport.report(sonaReportEvent)
    }

    /**
     * 记录拉流的开始时间
     */
    fun recordPullAllStreamTime() {
        getStreamHandler()?.let {
            if (it.sessionType() === AudioSession.MULTI) {
                val remoteStream = it.acquireStream(SteamType.REMOTE)
                if (remoteStream is List<*>) {
                    val remoteStreamList = remoteStream as? List<AudioStream>
                    remoteStreamList ?: return
                    for (audioStream in remoteStreamList) {
                        pullStreamStartTimeMap[audioStream.streamId] = System.currentTimeMillis()
                    }
                }
            } else {
                val mixStream = it.acquireStream(SteamType.MIX)
                if (mixStream is AudioStream) {
                    pullStreamStartTimeMap[mixStream.streamId] = System.currentTimeMillis()
                }
            }
        }
    }

    /**
     * 上报登录耗时
     */
    fun reportLoginCostTime(startLoginTime: Long) {
        val endTime = System.currentTimeMillis()
        val loginCostTime = endTime - startLoginTime
        if (loginCostTime <= 0) return
        val ext = HashMap<String?, String?>()
        ext[ReportEvent.KEY_BEGIN_TIME] = startLoginTime.toString()
        ext[ReportEvent.KEY_END_TIME] = endTime.toString()
        ext[ReportEvent.KET_DURATION] = loginCostTime.toString()
        val loginConsumeEvent = SonaReportEvent.Builder()
            .setContent("login zego room cost time $loginCostTime")
            .setExt(ext)
            .setType(SonaReportEvent.LOG or SonaReportEvent.LOGAN)
            .build()
        SonaReport.report(loginConsumeEvent)
    }

}