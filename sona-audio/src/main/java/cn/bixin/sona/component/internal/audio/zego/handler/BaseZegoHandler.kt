package cn.bixin.sona.component.internal.audio.zego.handler

import cn.bixin.sona.component.ComponentMessage
import cn.bixin.sona.component.internal.audio.IAudioComponentHandler
import cn.bixin.sona.component.internal.audio.zego.ZegoAudio
import cn.bixin.sona.component.internal.audio.zego.ZegoStream
import com.zego.zegoliveroom.ZegoLiveRoom


abstract class BaseZegoHandler(private val component: ZegoAudio) :
    IAudioComponentHandler<ZegoAudio> {

    override fun getComponent(): ZegoAudio {
        return component
    }

    override fun <T> acquire(clazz: Class<T>): T? {
        return component.acquire(clazz)
    }

    override fun dispatchMessage(roomMessage: ComponentMessage, message: Any?) {
        component.dispatchMessage(roomMessage, message)
    }

    fun getLiveRoom(): ZegoLiveRoom? {
        return component.liveRoom
    }

    fun getStreamHandler(): ZegoStream? {
        return component.streamHandler
    }

    fun getReportHandler(): ZegoReportHandler {
        return component.reportHandler
    }

    fun getStreamRetryHandler(): ZegoStreamRetryHandler {
        return component.streamRetryHandler
    }

    fun getSoundLevelHandler(): ZegoSoundLevelHandler {
        return component.soundLevelHandler
    }

    override fun assembling() {

    }

    override fun unAssembling() {

    }

}