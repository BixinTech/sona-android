package cn.bixin.sona.component.internal.audio.tencent

import android.os.Bundle
import android.text.TextUtils
import cn.bixin.sona.base.Sona
import cn.bixin.sona.component.ComponentMessage
import cn.bixin.sona.component.audio.AudioStream
import cn.bixin.sona.component.internal.audio.*
import cn.bixin.sona.plugin.config.AudioConfig
import cn.bixin.sona.plugin.entity.SoundLevelInfoEntity
import cn.bixin.sona.report.SonaReport
import cn.bixin.sona.report.SonaReportEvent
import cn.bixin.sona.util.SonaConfigManager
import cn.bixin.sona.util.SonaLogger
import com.alibaba.fastjson.JSONObject
import com.tencent.rtmp.ITXLivePlayListener
import com.tencent.rtmp.TXLiveConstants
import com.tencent.rtmp.TXLivePlayConfig
import com.tencent.rtmp.TXLivePlayer

/**
 *
 * @Author luokun
 * @Date 2020-03-03
 */
class MixStream(
    private val audioComponent: TencentAudio,
    private val streamAcquire: IStreamAcquire
) : IStream {

    private var mixPlayer: TXLivePlayer? = null
    private var streamPulled = false
    private var audioComponentWrapper: AudioComponentWrapper? = null

    override fun startPublishStream(streamId: String): Boolean {
        return false
    }

    override fun stopPublishStream(): Boolean {
        return false
    }

    override fun startPullStream(): Boolean {
        val beginTime = System.currentTimeMillis()
        val mixStream = (streamAcquire.acquireStream(SteamType.MIX) as? AudioStream)?.streamId
        mixStream?.let {
            if (it.isNotEmpty()) {
                if (mixPlayer == null) {
                    mixPlayer = TXLivePlayer(Sona.getAppContext().applicationContext)
                    val playConfig = TXLivePlayConfig()
                    playConfig.connectRetryCount = 2
                    playConfig.isAutoAdjustCacheTime = true
                    playConfig.minAutoAdjustCacheTime = 1.5f
                    playConfig.maxAutoAdjustCacheTime = 4.5f
                    playConfig.isEnableMessage = true
                    mixPlayer?.enableHardwareDecode(false)
                    mixPlayer?.setConfig(playConfig)

                    mixPlayer?.setPlayListener(object : ITXLivePlayListener {
                        override fun onPlayEvent(event: Int, param: Bundle) {
                            if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT) {
                                mixPlayer?.stopPlay(true)
                                if (streamPulled) {
                                    mixPlayer?.startLivePlay(it, TXLivePlayer.PLAY_TYPE_LIVE_FLV)
                                }
                            } else if (event == TXLiveConstants.PLAY_EVT_GET_MESSAGE) {
                                handleMixSoundInfo(param)
                            }
                            if (event < 0 || event == 3001 || event == 3002 || event == 3003 || event == 3004) {
                                SonaLogger.log(
                                    content = "混流拉流事件",
                                    code = AudioReportCode.TENCENT_MIX_PLAY_EVENT_CODE,
                                    sdkCode = event
                                )
                                SonaConfigManager.getInstance().getConfig()
                            }
                            if (event == TXLiveConstants.PLAY_EVT_PLAY_BEGIN) {
                                val endTime = System.currentTimeMillis()
                                val ext: HashMap<String?, String?> = HashMap()
                                ext[ReportEvent.KEY_BEGIN_TIME] = beginTime.toString()
                                ext[ReportEvent.KEY_END_TIME] = endTime.toString()
                                ext[ReportEvent.KET_DURATION] = "${endTime - beginTime}"
                                val sonaReportEvent = SonaReportEvent.Builder()
                                    .setContent("pull stream $it cost time ${endTime - beginTime}")
                                    .setExt(ext)
                                    .setType(SonaReportEvent.LOG or SonaReportEvent.LOGAN)
                                    .build()
                                SonaReport.report(sonaReportEvent)
                            }
                        }

                        override fun onNetStatus(p0: Bundle?) {

                        }
                    })
                }

                if (!streamPulled) {
                    val result = mixPlayer?.startLivePlay(it, TXLivePlayer.PLAY_TYPE_LIVE_FLV)
                    if (result == 0) {
                        streamPulled = true
                    }

                    val reportCode = if (result == 0)
                        AudioReportCode.TENCENT_PULL_MIX_STREAM_SUCCESS_CODE
                    else
                        AudioReportCode.TENCENT_PULL_MIX_STREAM_FAIL_CODE
                    SonaLogger.log(
                        content = "拉混流 streamId $it",
                        code = reportCode,
                        sdkCode = result ?: 0
                    )

                    return result == 0
                } else {
                    return true
                }
            }
        }
        return false
    }

    override fun stopPullStream(): Boolean {
        mixPlayer?.let {
            if (streamPulled) {
                val result = it.stopPlay(true)
                if (result == 0) {
                    streamPulled = false
                }

                val reportCode = if (result == 0)
                    AudioReportCode.TENCENT_STOP_PULL_MIX_STREAM_SUCCESS_CODE
                else
                    AudioReportCode.TENCENT_STOP_PULL_MIX_STREAM_FAIL_CODE
                SonaLogger.log(
                    content = "停止拉流 streamId ".plus((streamAcquire.acquireStream(SteamType.MIX) as AudioStream?)?.streamId),
                    code = reportCode,
                    sdkCode = result
                )

                return result == 0
            }
        }
        return true
    }

    override fun startPullStream(streamId: String?): Boolean {
        val mixStream = (streamAcquire.acquireStream(SteamType.MIX) as? AudioStream)?.streamId
        if (!streamPulled && TextUtils.equals(streamId, mixStream)) {
            return startPullStream()
        }
        return false
    }

    override fun stopPullStream(streamId: String?): Boolean {
        val mixStream = (streamAcquire.acquireStream(SteamType.MIX) as? AudioStream)?.streamId
        if (streamPulled && TextUtils.equals(streamId, mixStream)) {
            return stopPullStream()
        }
        return true
    }

    override fun silentStream(streamId: String?, on: Boolean): Boolean {
        val mixStream = (streamAcquire.acquireStream(SteamType.MIX) as? AudioStream)?.streamId
        if (streamPulled && TextUtils.equals(streamId, mixStream)) {
            mixPlayer?.setMute(on)
            SonaLogger.log(
                content = if (on) "静音 streamId $streamId" else "取消静音 streamId $streamId",
                code = AudioReportCode.TENCENT_SET_VOLUME_SUCCESS_CODE
            )
            return true
        }
        return false
    }

    override fun sessionType(): AudioSession {
        return AudioSession.MIX
    }

    private fun handleMixSoundInfo(param: Bundle) {
        kotlin.runCatching {
            if (param.containsKey(TXLiveConstants.EVT_GET_MSG)) {
                val byteArray = param.getByteArray(TXLiveConstants.EVT_GET_MSG)
                byteArray?.let {
                    val msg = String(it)
                    if (TextUtils.isEmpty(msg)) {
                        return@runCatching
                    }
                    val jsonObject = JSONObject.parseObject(msg)
                    val regions = jsonObject.getJSONArray("regions")
                    if (regions.isNullOrEmpty()) {
                        return@runCatching
                    }
                    val audioConfig = audioComponentWrapper?.acquire(AudioConfig::class.java)
                    if (audioConfig == null || !audioConfig.soundCapture) return
                    val soundLevelInfoList = ArrayList<SoundLevelInfoEntity>()
                    for (item in regions) {
                        val userId = (item as? JSONObject)?.getString("uid") ?: ""
                        val volume = (item as? JSONObject)?.getIntValue("volume") ?: 0
                        if (!TextUtils.isEmpty(userId) && volume > 0) {
                            soundLevelInfoList.add(SoundLevelInfoEntity(userId, volume.toFloat()))
                        }
                    }
                    if (soundLevelInfoList.isNotEmpty()) {
                        audioComponent.dispatchMessage(
                            ComponentMessage.AUDIO_REV_SOUND_LEVEL_INFO,
                            soundLevelInfoList
                        )
                    }
                }
            }
        }.onFailure {
            it.printStackTrace()
        }
    }

}