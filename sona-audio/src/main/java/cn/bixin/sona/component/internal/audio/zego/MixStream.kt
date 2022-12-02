package cn.bixin.sona.component.internal.audio.zego

import android.text.TextUtils
import cn.bixin.sona.component.audio.AudioStream
import cn.bixin.sona.component.internal.audio.*

/**
 * 混流
 *
 * @Author luokun
 * @Date 2020-03-04
 */
class MixStream(private val delegate: IStreamDelegate, private val streamAcquire: IStreamAcquire) :
    IStream {

    private var streamPulled: Boolean = false

    override fun startPublishStream(streamId: String): Boolean {
        return false
    }

    override fun stopPublishStream(): Boolean {
        return false
    }

    override fun startPullStream(): Boolean {
        if (streamPulled) {
            return true
        }
        var mixStream = (streamAcquire.acquireStream(SteamType.MIX) as? AudioStream)?.streamId
        mixStream?.let {
            val result = delegate.play(it, true)
            if (result) {
                streamPulled = true
                return true
            }
        }
        return false
    }

    override fun stopPullStream(): Boolean {
        if (streamPulled) {
            var mixStream = (streamAcquire.acquireStream(SteamType.MIX) as? AudioStream)?.streamId
            mixStream?.let {
                val result = delegate.play(it, false)
                if (result) {
                    streamPulled = false
                }
                return result
            }

        }
        return true
    }

    override fun startPullStream(streamId: String?): Boolean {
        var mixStream = (streamAcquire.acquireStream(SteamType.MIX) as? AudioStream)?.streamId
        return if (!TextUtils.isEmpty(streamId) && TextUtils.equals(streamId, mixStream)) {
            delegate.play(streamId!!, true)
        } else false
    }

    override fun stopPullStream(streamId: String?): Boolean {
        var mixStream = (streamAcquire.acquireStream(SteamType.MIX) as? AudioStream)?.streamId
        if (streamPulled && !TextUtils.isEmpty(streamId) && TextUtils.equals(streamId, mixStream)) {
            val result = delegate.play(streamId!!, false)
            if (result) {
                streamPulled = false
            }
            return result
        }
        return true
    }

    override fun silentStream(streamId: String?, on: Boolean): Boolean {
        var mixStream = (streamAcquire.acquireStream(SteamType.MIX) as? AudioStream)?.streamId
        return if (streamPulled && !TextUtils.isEmpty(streamId) && TextUtils.equals(
                streamId,
                mixStream
            )
        ) {
            delegate.silent(streamId!!, on)
        } else false
    }

    override fun sessionType(): AudioSession {
        return AudioSession.MIX
    }
}