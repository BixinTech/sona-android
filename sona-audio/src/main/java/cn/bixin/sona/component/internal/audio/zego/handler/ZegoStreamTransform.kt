package cn.bixin.sona.component.internal.audio.zego.handler

import android.text.TextUtils
import cn.bixin.sona.component.audio.AudioStream
import com.zego.zegoliveroom.entity.ZegoStreamInfo

object ZegoStreamTransform {

    /**
     * ZegoStreamInfo转化成AudioStream
     *
     * @param streams
     *
     * @return List<AudioStream>
     */
    @JvmStatic
    fun transform(streams: Array<ZegoStreamInfo>?): List<AudioStream> {
        val multiStreams: MutableList<AudioStream> = ArrayList()
        if (streams != null && streams.isNotEmpty()) {
            for (info in streams) {
                val streamId = info.streamID
                val userId = info.userID
                val userName = info.userName
                if (!TextUtils.isEmpty(streamId)) {
                    multiStreams.add(AudioStream(streamId, userId, userName))
                }
            }
        }
        return multiStreams
    }
}