package cn.bixin.sona.component.internal.audio

import cn.bixin.sona.annotation.BindSona
import cn.bixin.sona.component.ComponentCallback
import cn.bixin.sona.component.ComponentMessage
import cn.bixin.sona.component.audio.AudioStream
import cn.bixin.sona.component.audio.IAudioPlayer
import cn.bixin.sona.component.internal.audio.tencent.TencentAudio
import cn.bixin.sona.component.internal.audio.zego.ZegoAudio
import cn.bixin.sona.data.AudioDeviceModeEnum
import cn.bixin.sona.data.StreamSupplierEnum
import cn.bixin.sona.data.entity.SonaRoomData
import cn.bixin.sona.util.SonaLogger

/**
 * 音频component具体实现类
 *
 * @Author luokun
 * @Date 2020/3/25
 */
@BindSona
class AudioComponent : cn.bixin.sona.component.audio.AudioComponent() {

    private var proxy: AudioComponentWrapper? = null

    override fun assembling() {
        acquire(SonaRoomData::class.java)?.streamInfo?.supplier?.let {
            when (it) {
                StreamSupplierEnum.ZEGO.supplierName -> proxy = ZegoAudio(this)
                StreamSupplierEnum.TENCENT.supplierName -> proxy = TencentAudio(this)
                else -> {}
            }
        }
        if (proxy != null) {
            proxy?.assembling()
        } else {
            SonaLogger.log(
                content = "音频配置有问题",
                code = AudioReportCode.AUDIO_SETTING_ERROR_CODE
            )
            dispatchMessage(ComponentMessage.AUDIO_INIT_FAIL, "音频初始化失败")
        }
    }

    override fun compareAndGet(index: IAudioPlayer.Index): IAudioPlayer? {
        return proxy?.compareAndGet(index)
    }

    override fun getAudioPlayer(): IAudioPlayer? {
        return proxy?.getAudioPlayer()
    }

    override fun setAudioDeviceMode(audioDeviceMode: AudioDeviceModeEnum) {
        proxy?.setAudioDeviceMode(audioDeviceMode)
    }

    override fun pullStream(componentCallback: ComponentCallback?) {
        proxy?.pullStream(componentCallback)
    }

    override fun pullStream(streamId: String?, componentCallback: ComponentCallback?) {
        proxy?.pullStream(streamId, componentCallback)
    }

    override fun pushStream(streamId: String?, componentCallback: ComponentCallback?) {
        proxy?.pushStream(streamId, componentCallback)
    }

    override fun silent(streamId: String?, on: Boolean, componentCallback: ComponentCallback?) {
        proxy?.silent(streamId, on, componentCallback)
    }

    override fun stopPullStream(componentCallback: ComponentCallback?) {
        proxy?.stopPullStream(componentCallback)
    }

    override fun stopPullStream(streamId: String?, componentCallback: ComponentCallback?) {
        proxy?.stopPullStream(streamId, componentCallback)
    }

    override fun stopPushStream(componentCallback: ComponentCallback?) {
        proxy?.stopPushStream(componentCallback)
    }

    override fun switchHandsfree(on: Boolean, componentCallback: ComponentCallback?) {
        proxy?.switchHandsfree(on, componentCallback)
    }

    override fun switchListen(realTime: Boolean, componentCallback: ComponentCallback?) {
        proxy?.switchListen(realTime, componentCallback)
    }

    override fun switchMic(on: Boolean, componentCallback: ComponentCallback?) {
        proxy?.switchMic(on, componentCallback)
    }

    override fun currentStream(): List<AudioStream>? {
        return proxy?.currentStream()
    }

    override fun unAssembling() {
        proxy?.unAssembling()
    }
}