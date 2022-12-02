package cn.bixin.sona.component.internal.audio.tencent

import android.text.TextUtils
import cn.bixin.sona.component.audio.AudioStream
import cn.bixin.sona.component.internal.audio.*
import cn.bixin.sona.util.SonaLogger
import com.tencent.trtc.TRTCCloud
import com.tencent.trtc.TRTCCloudDef

/**
 *
 * @Author luokun
 * @Date 2020-03-03
 */
class TencentStream(private val mTRTCCloud: TRTCCloud, private val audioComponent: TencentAudio) :
    IStream, StreamAdapter(),
    IStreamDelegate, IStreamFinder {

    private var proxy: IStream = MultiStream(this, this)

    /**
     * 是否推流成功
     */
    private var pushStreamSuccess: Boolean = false

    fun isPushStreamSuccess(): Boolean {
        return pushStreamSuccess
    }

    fun setAudioSession(audioSession: AudioSession) {
        if (proxy.sessionType() != audioSession) {
            proxy = if (audioSession == AudioSession.MIX) {
                MixStream(audioComponent, this)
            } else {
                MultiStream(this, this)
            }
        }
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
            val list = it.filter { stream -> TextUtils.equals(stream.streamId, streamId) }
            if (list.isNotEmpty()) {
                return list[0]
            }
        }
        return null
    }

    override fun findAudioStream(): List<AudioStream> {
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
        with(on) {
            if (this) {
                if (TextUtils.isEmpty(streamId)) {
                    mTRTCCloud.muteAllRemoteAudio(false)
                } else {
                    mTRTCCloud.muteRemoteAudio(streamId, false)
                }
            } else {
                if (TextUtils.isEmpty(streamId)) {
                    mTRTCCloud.muteAllRemoteAudio(true)
                } else {
                    mTRTCCloud.muteRemoteAudio(streamId, true)
                }
            }
        }

        SonaLogger.log(
            content = if (on) "拉流 streamId $streamId" else "停止拉流 streamId $streamId",
            code = AudioReportCode.TENCENT_PULL_STREAM_SUCCESS_CODE
        )

        return true
    }

    override fun speak(streamId: String, on: Boolean): Boolean {
        with(on) {
            if (this) {
                if (!pushStreamSuccess) {
                    mTRTCCloud.startLocalAudio(TRTCCloudDef.TRTC_AUDIO_QUALITY_MUSIC)
                    pushStreamSuccess = true

                    SonaLogger.log(
                        content = "推流 streamId $streamId",
                        code = AudioReportCode.TENCENT_PUSH_STREAM_SUCCESS_CODE
                    )
                }
            } else {
                if (pushStreamSuccess) {
                    mTRTCCloud.switchRole(TRTCCloudDef.TRTCRoleAudience)
                    mTRTCCloud.stopPublishing()
                    mTRTCCloud.stopLocalAudio()
                    pushStreamSuccess = false

                    SonaLogger.log(
                        content = "停止推流 streamId $streamId",
                        code = AudioReportCode.TENCENT_STOP_PUSH_STREAM_SUCCESS_CODE
                    )
                }
            }
        }
        return true
    }

    override fun silent(streamId: String, on: Boolean): Boolean {
        mTRTCCloud.setRemoteAudioVolume(streamId, if (on) 0 else 100)
        SonaLogger.log(
            content = if (on) "静音 streamId $streamId" else "取消静音 streamId $streamId",
            code = AudioReportCode.TENCENT_SET_VOLUME_SUCCESS_CODE
        )
        return true
    }

}