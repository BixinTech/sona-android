package cn.bixin.sona.component.internal.audio.zego

import android.text.TextUtils
import cn.bixin.sona.component.audio.AudioStream
import cn.bixin.sona.component.internal.audio.*

/**
 *
 * @Author luokun
 * @Date 2020-03-04
 */
class MultiStream(
    private val delegate: IStreamDelegate,
    private val streamAcquire: IStreamAcquire
) :
    IStream {

    override fun startPublishStream(streamId: String): Boolean {
        return delegate.speak(streamId, true)
    }

    override fun stopPublishStream(): Boolean {
        val audioStream = streamAcquire.acquireStream(SteamType.LOCAL) as? AudioStream
        return delegate.speak(audioStream?.streamId ?: "", false)
    }

    override fun startPullStream(): Boolean {
        var result = true
        val multiStreams =
            streamAcquire.acquireStream(SteamType.REMOTE) as? MutableList<AudioStream>
        multiStreams?.let {
            if (it.isNotEmpty()) {
                it.forEach {
                    if (!startPullStream(it.streamId)) {
                        result = false
                    }
                }
            }
        }
        return result
    }

    override fun stopPullStream(): Boolean {
        var result = true
        val multiStreams =
            streamAcquire.acquireStream(SteamType.REMOTE) as? MutableList<AudioStream>
        multiStreams?.let {
            if (it.isNotEmpty()) {
                it.forEach {
                    if (!stopPullStream(it.streamId)) {
                        result = false
                    }
                }
            }
        }
        return result
    }

    override fun startPullStream(streamId: String?): Boolean {
        // 如果播放的流ID和自己的推流ID一致则返回
        var localStream = (streamAcquire.acquireStream(SteamType.LOCAL) as? AudioStream)?.streamId
        return if (TextUtils.isEmpty(streamId) || TextUtils.equals(streamId, localStream)) {
            true
        } else delegate.play(streamId!!, true)
    }

    override fun stopPullStream(streamId: String?): Boolean {
        return if (TextUtils.isEmpty(streamId)) {
            true
        } else delegate.play(streamId!!, false)
    }

    override fun silentStream(streamId: String?, on: Boolean): Boolean {
        var localStream = (streamAcquire.acquireStream(SteamType.LOCAL) as? AudioStream)?.streamId
        return if (!TextUtils.isEmpty(streamId) && !TextUtils.equals(streamId, localStream)) {
            delegate.silent(streamId!!, on)
        } else TextUtils.equals(streamId, localStream)
    }

    override fun sessionType(): AudioSession {
        return AudioSession.MULTI
    }

}