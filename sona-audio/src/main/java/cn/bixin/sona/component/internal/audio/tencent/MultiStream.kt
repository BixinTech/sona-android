package cn.bixin.sona.component.internal.audio.tencent

import android.text.TextUtils
import cn.bixin.sona.component.audio.AudioStream
import cn.bixin.sona.component.internal.audio.*

/**
 *
 * @Author luokun
 * @Date 2020-03-03
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
        return delegate.play("", true)
    }

    override fun stopPullStream(): Boolean {
        return delegate.play("", false)
    }

    override fun startPullStream(streamId: String?): Boolean {
        streamId?.let {
            return delegate.play(it, true)
        }
        return false
    }

    override fun stopPullStream(streamId: String?): Boolean {
        streamId?.let {
            return delegate.play(it, false)
        }
        return true
    }

    override fun silentStream(streamId: String?, on: Boolean): Boolean {
        val localStream = (streamAcquire.acquireStream(SteamType.LOCAL) as? AudioStream)?.streamId
        return if (!TextUtils.isEmpty(streamId) && !TextUtils.equals(streamId, localStream)) {
            delegate.silent(streamId!!, on)
        } else TextUtils.equals(streamId, localStream)
    }

    override fun sessionType(): AudioSession {
        return AudioSession.MULTI
    }

}