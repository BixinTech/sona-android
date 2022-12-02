package cn.bixin.sona.delegate

import cn.bixin.sona.component.ComponentMessage
import cn.bixin.sona.component.audio.IAudioPlayer
import cn.bixin.sona.component.audio.IAudioPlayerCallback
import cn.bixin.sona.delegate.internal.AudioDataTracker
import cn.bixin.sona.driver.ComponentType
import cn.bixin.sona.driver.RoomDriver
import cn.bixin.sona.plugin.AudioPlayerPlugin
import cn.bixin.sona.plugin.SonaPlugin
import cn.bixin.sona.plugin.config.AudioPlayerConfig
import cn.bixin.sona.plugin.entity.PluginEnum
import cn.bixin.sona.plugin.observer.AudioPlayerObserver

/**
 * 音频背景音乐播放器代理
 * @Author luokun
 * @Date 2020-03-05
 */
class AudioPlayerPluginDelegate(val roomDriver: RoomDriver) : AudioPlayerPlugin,
    SonaPluginDelegate() {

    override fun observe(observer: AudioPlayerObserver?) {
        if (roomDriver.componentCertified(ComponentType.AUDIO)) {
            roomDriver.audio?.getAudioPlayer()?.setCallback(object : IAudioPlayerCallback {
                override fun onStart() {
                    observer?.onStart()
                }

                override fun onPause() {
                    observer?.onPause()
                }

                override fun onResume() {
                    observer?.onResume()
                }

                override fun onStop() {
                    observer?.onStop()
                }

                override fun onError(code: Int) {
                    observer?.onError(code)
                }

                override fun onComplete() {
                    observer?.onComplete()
                }

                override fun onProcess(process: Long) {
                    observer?.onProcess(process)
                }

                override fun onBufferBegin() {
                    observer?.onBufferBegin()
                }

                override fun onBufferEnd() {
                    observer?.onBufferEnd()
                }
            })
            roomDriver.acquire(AudioDataTracker::class.java)?.apply {
                when (player) {
                    null -> {
                        player = Player()
                    }
                }
                player?.observer = observer
            }
        }
    }

    override fun config(config: AudioPlayerConfig?): SonaPlugin<*, *> {
        return this
    }

    override fun getVolume(): Int {
        if (roomDriver.componentCertified(ComponentType.AUDIO)) {
            return roomDriver.audio?.getAudioPlayer()?.getVolume() ?: 0
        }
        return 0
    }

    override fun getMaxVolume(): Int {
        if (roomDriver.componentCertified(ComponentType.AUDIO)) {
            return roomDriver.audio?.getAudioPlayer()?.getMaxVolume() ?: 0
        }
        return 0
    }

    override fun play(url: String, pattern: IAudioPlayer.PlayPattern) {
        if (roomDriver.componentCertified(ComponentType.AUDIO)) {
            roomDriver.audio?.getAudioPlayer()?.play(url, pattern)
            roomDriver.acquire(AudioDataTracker::class.java)?.apply {
                when (player) {
                    null -> {
                        player = Player()
                    }
                }
                player?.path = url
                player?.pattern = pattern
                player?.status = IAudioPlayer.Status.PLAY
            }
        }
    }

    override fun play(url: String, startPosition: Long, pattern: IAudioPlayer.PlayPattern) {
        if (roomDriver.componentCertified(ComponentType.AUDIO)) {
            roomDriver.audio?.getAudioPlayer()?.play(url, startPosition, pattern)
            roomDriver.acquire(AudioDataTracker::class.java)?.apply {
                when (player) {
                    null -> {
                        player = Player()
                    }
                }
                player?.path = url
                player?.pattern = pattern
                player?.status = IAudioPlayer.Status.PLAY
            }
        }
    }

    override fun playRepeat(url: String, pattern: IAudioPlayer.PlayPattern) {
        if (roomDriver.componentCertified(ComponentType.AUDIO)) {
            roomDriver.audio?.getAudioPlayer()?.playRepeat(url, pattern)
            roomDriver.acquire(AudioDataTracker::class.java)?.apply {
                when (player) {
                    null -> {
                        player = Player()
                    }
                }
                player?.path = url
                player?.pattern = pattern
                player?.status = IAudioPlayer.Status.PLAY
            }
        }
    }

    override fun playRepeat(url: String, startPosition: Long, pattern: IAudioPlayer.PlayPattern) {
        if (roomDriver.componentCertified(ComponentType.AUDIO)) {
            roomDriver.audio?.getAudioPlayer()?.playRepeat(url, startPosition, pattern)
            roomDriver.acquire(AudioDataTracker::class.java)?.apply {
                when (player) {
                    null -> {
                        player = Player()
                    }
                }
                player?.path = url
                player?.pattern = pattern
                player?.status = IAudioPlayer.Status.PLAY
            }
        }
    }

    override fun resume() {
        if (roomDriver.componentCertified(ComponentType.AUDIO)) {
            roomDriver.audio?.getAudioPlayer()?.resume()
            roomDriver.acquire(AudioDataTracker::class.java)?.player?.apply {
                status = IAudioPlayer.Status.PLAY
            }
        }
    }

    override fun pause() {
        if (roomDriver.componentCertified(ComponentType.AUDIO)) {
            roomDriver.audio?.getAudioPlayer()?.pause()
            roomDriver.acquire(AudioDataTracker::class.java)?.player?.apply {
                status = IAudioPlayer.Status.PAUSE
            }
        }
    }

    override fun stop() {
        if (roomDriver.componentCertified(ComponentType.AUDIO)) {
            roomDriver.audio?.getAudioPlayer()?.stop()
            roomDriver.acquire(AudioDataTracker::class.java)?.player?.apply {
                status = IAudioPlayer.Status.IDLE
            }
        }
    }

    override fun seek(position: Long) {
        if (roomDriver.componentCertified(ComponentType.AUDIO)) {
            roomDriver.audio?.getAudioPlayer()?.seek(position)
        }
    }

    override fun setVolume(volume: Int) {
        if (roomDriver.componentCertified(ComponentType.AUDIO)) {
            roomDriver.audio?.getAudioPlayer()?.setVolume(volume)
            roomDriver.acquire(AudioDataTracker::class.java)?.player?.apply {
                this.volume = volume
            }
        }
    }

    override fun getDuration(): Long {
        if (roomDriver.componentCertified(ComponentType.AUDIO)) {
            return roomDriver.audio?.getAudioPlayer()?.getDuration() ?: 0
        }
        return 0
    }

    override fun getCurrentPosition(): Long {
        if (roomDriver.componentCertified(ComponentType.AUDIO)) {
            return roomDriver.audio?.getAudioPlayer()?.getCurrentPosition() ?: 0
        }
        return 0
    }

    override fun getStatus(): IAudioPlayer.Status {
        if (roomDriver.componentCertified(ComponentType.AUDIO)) {
            return roomDriver.audio?.getAudioPlayer()?.getStatus() ?: IAudioPlayer.Status.IDLE
        }
        return IAudioPlayer.Status.IDLE
    }

    override fun setCallback(callback: IAudioPlayerCallback?) {
        throw IllegalStateException("不支持此接口")
    }

    override fun handleMessage(msgType: ComponentMessage?, message: Any?) {
        when (msgType) {
            ComponentMessage.AUDIO_CHANGE_START -> {
                roomDriver.acquire(AudioDataTracker::class.java)?.player?.apply {
                    position = getCurrentPosition()
                }
            }
            ComponentMessage.AUDIO_CHANGE_END -> {
                (message as AudioDataTracker)?.player?.apply {
                    if (status != IAudioPlayer.Status.IDLE) {
                        observer?.let {
                            observe(it)
                        }
                        path?.let {
                            play(it, pattern)
                            if (status == IAudioPlayer.Status.PAUSE) {
                                pause()
                            }
                            if (volume != -1) {
                                setVolume(volume)
                            }
                            if (position != -1L) {
                                seek(position)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun pluginType(): PluginEnum {
        return PluginEnum.APLAYER
    }

    override fun remove() {
        super.remove()
        roomDriver.acquire(AudioDataTracker::class.java)?.player?.observer = null
    }
}