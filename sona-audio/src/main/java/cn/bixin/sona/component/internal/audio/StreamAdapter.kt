package cn.bixin.sona.component.internal.audio

import cn.bixin.sona.component.audio.AudioStream

/**
 *
 * @Author luokun
 * @Date 2020-03-04
 */
open class StreamAdapter : IStreamProvider, IStreamAcquire {

    private var localStream: AudioStream? = null
    private var remoteStream: MutableList<AudioStream>? = null
    private var mixStream: AudioStream? = null

    override fun providerStream(type: SteamType, stream: Any?) {
        when (type) {
            SteamType.REMOTE -> remoteStream = stream as? MutableList<AudioStream>
            SteamType.MIX -> mixStream = stream as? AudioStream
            SteamType.LOCAL -> localStream = stream as? AudioStream
            SteamType.AREMOTE -> (stream as? MutableList<AudioStream>)?.let {
                if (remoteStream == null) {
                    remoteStream = mutableListOf()
                }
                remoteStream?.addAll(it)
            }
            SteamType.DREMOTE -> (stream as? MutableList<AudioStream>)?.let {
                if (remoteStream == null) {
                    remoteStream = mutableListOf()
                }
                remoteStream?.removeAll(it)
            }
        }

    }

    override fun acquireStream(type: SteamType): Any? {
        return when (type) {
            SteamType.REMOTE -> remoteStream
            SteamType.MIX -> mixStream
            SteamType.LOCAL -> localStream
            else -> null
        }
    }


}