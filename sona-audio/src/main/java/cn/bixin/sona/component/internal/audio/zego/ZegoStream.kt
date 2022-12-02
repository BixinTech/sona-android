package cn.bixin.sona.component.internal.audio.zego

import android.text.TextUtils
import androidx.annotation.CallSuper
import cn.bixin.sona.component.audio.AudioStream
import cn.bixin.sona.component.internal.audio.*
import cn.bixin.sona.util.SonaLogger
import com.zego.zegoavkit2.ZegoStreamExtraPlayInfo
import com.zego.zegoliveroom.ZegoLiveRoom
import com.zego.zegoliveroom.constants.ZegoConstants

/**
 *
 * @Author luokun
 * @Date 2020-03-04
 */
class ZegoStream(private val zegoLiveRoom: ZegoLiveRoom) : IStream, StreamAdapter(),
    IStreamDelegate,
    IStreamFinder {

    private var proxy: IStream = MultiStream(this, this)

    var pushStreamSuccess = false

    /**
     * 设置模式
     *
     * @param audioSession
     */
    fun setAudioSession(audioSession: AudioSession) {
        if (proxy.sessionType() != audioSession) {
            proxy = if (audioSession == AudioSession.MIX) {
                MixStream(this, this)
            } else {
                MultiStream(this, this)
            }
        }
    }

    fun isPushStreamSuccess(): Boolean {
        return pushStreamSuccess
    }

    /**
     * 通过streamId查找对应的AudioStream
     *
     * @param streamId
     * @return
     */
    override fun findAudioStream(streamId: String?): AudioStream? {
        val multiStreams = acquireStream(SteamType.REMOTE) as? MutableList<AudioStream>
        multiStreams?.let {
            val list = it.filter { TextUtils.equals(it.streamId, streamId) }
            if (list.isNotEmpty()) {
                return list[0]
            }
        }
        return null
    }

    /**
     * 远端流信息
     */
    override fun findAudioStream(): List<AudioStream>? {
        val remoteStreams = acquireStream(SteamType.REMOTE) as? MutableList<AudioStream>
        val localStream = acquireStream(SteamType.LOCAL) as? AudioStream
        val streams = mutableListOf<AudioStream>()
        remoteStreams?.let {
            streams.addAll(it)
        }
        localStream?.let {
            streams.add(it)
        }
        return streams
    }

    /**
     * 恢复
     */
    @CallSuper
    fun resume() {
        //        zegoLiveRoom.resumeModule(ZegoConstants.ModuleType.AUDIO)
    }

    /**
     * 暂停
     */
    @CallSuper
    fun pause() {
        //        zegoLiveRoom.pauseModule(ZegoConstants.ModuleType.AUDIO)
    }

    override fun startPublishStream(streamId: String): Boolean {
        return proxy.startPublishStream(streamId)
    }

    override fun stopPublishStream(): Boolean {
        return proxy.stopPublishStream()
    }

    override fun startPullStream(): Boolean {
        return proxy.startPullStream()
    }

    override fun stopPullStream(): Boolean {
        return proxy.stopPullStream()
    }

    override fun startPullStream(streamId: String?): Boolean {
        return proxy.startPullStream(streamId)
    }

    override fun stopPullStream(streamId: String?): Boolean {
        return proxy.stopPullStream(streamId)
    }

    override fun silentStream(streamId: String?, on: Boolean): Boolean {
        return proxy.silentStream(streamId, on)
    }

    override fun sessionType(): AudioSession {
        return proxy.sessionType()
    }

    override fun play(streamId: String, on: Boolean): Boolean {
        return with(on) {
            val result: Boolean
            if (this) {
                if (sessionType() == AudioSession.MIX) {
                    val streamPlayInfo = ZegoStreamExtraPlayInfo()
                    streamPlayInfo.mode = ZegoStreamExtraPlayInfo.ZegoStreamResourceMode.CdnOnly
                    result = zegoLiveRoom.startPlayingStream(streamId, null, streamPlayInfo)
                } else {
                    result = zegoLiveRoom.startPlayingStream(streamId, null)
                }

                SonaLogger.log(
                    content = "拉流 streamId $streamId",
                    code = if (result) AudioReportCode.ZEGO_PULL_STREAM_SUCCESS_CODE else AudioReportCode.ZEGO_PULL_STREAM_FAIL_CODE
                )
                silent(streamId, false) // 每次拉流都把音量设置为100，避免通道复用导致没有声音
            } else {
                result = zegoLiveRoom.stopPlayingStream(streamId)
                SonaLogger.log(
                    content = "停止拉流 streamId $streamId",
                    code = if (result) AudioReportCode.ZEGO_STOP_PULL_STREAM_SUCCESS_CODE else AudioReportCode.ZEGO_STOP_PULL_STREAM_FAIL_CODE
                )
            }
            result
        }
    }

    override fun speak(streamId: String, on: Boolean): Boolean {
        return with(on) {
            var result = true
            if (this) {
                if (!pushStreamSuccess) {
                    zegoLiveRoom.enableCamera(false, ZegoConstants.PublishChannelIndex.MAIN)
                    result = zegoLiveRoom.startPublishing(
                        streamId,
                        streamId,
                        ZegoConstants.PublishFlag.MixStream
                    )
                    pushStreamSuccess = result

                    SonaLogger.log(
                        content = "推流 streamId $streamId",
                        code = if (result) AudioReportCode.ZEGO_PUSH_STREAM_SUCCESS_CODE else AudioReportCode.ZEGO_PUSH_STREAM_FAIL_CODE
                    )
                }
            } else {
                if (pushStreamSuccess) {
                    result = zegoLiveRoom.stopPublishing()
                    pushStreamSuccess = !result

                    SonaLogger.log(
                        content = "停止推流 streamId $streamId",
                        code = if (result) AudioReportCode.ZEGO_STOP_PUSH_STREAM_SUCCESS_CODE else AudioReportCode.ZEGO_STOP_PUSH_STREAM_FAIL_CODE
                    )
                }
            }
            result
        }
    }

    override fun silent(streamId: String, on: Boolean): Boolean {
        val result = if (on) {
            zegoLiveRoom.setPlayVolume(0, streamId)
        } else {
            zegoLiveRoom.setPlayVolume(100, streamId)
        }
        SonaLogger.log(
            content = if (on) "静音 streamId $streamId" else "取消静音 streamId $streamId",
            code = if (result) AudioReportCode.ZEGO_SET_VOLUME_SUCCESS_CODE else AudioReportCode.ZEGO_SET_VOLUME_FAIL_CODE
        )
        return result
    }

}