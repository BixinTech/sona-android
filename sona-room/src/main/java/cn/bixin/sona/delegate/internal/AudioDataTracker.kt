package cn.bixin.sona.delegate.internal

import cn.bixin.sona.component.audio.IAudioPlayer
import cn.bixin.sona.plugin.observer.AudioPlayerObserver


/**
 * 保存音频状态状态，避免过多的接口
 *
 * @Author luokun
 * @Date 2020/3/12
 */

class AudioDataTracker {

    /**
     * 是否正在推流
     */
    var isPublishing: Boolean = false

    /**
     * 是否拉流
     */
    var isPullStream: Boolean = false

    /**
     * 是否存于开麦状态，目前是自动开麦
     */
    var isMicOn: Boolean = true

    /**
     * 是否存在免提状态
     */
    var isHandsfree: Boolean = false

    /**
     * bgm 状态
     */
    var player: Player? = null

    inner class Player {
        var pattern: IAudioPlayer.PlayPattern = IAudioPlayer.PlayPattern.LOCAL
        var path: String? = null
        var status: IAudioPlayer.Status = IAudioPlayer.Status.IDLE
        var volume: Int = -1
        var position: Long = -1
        var observer: AudioPlayerObserver? = null
    }
}
