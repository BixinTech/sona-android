package cn.bixin.sona.component.internal.audio

import cn.bixin.sona.component.audio.AudioStream

/**
 * @Author luokun
 * @Date 2020/8/7
 */
interface IStreamFinder {

    /**
     * 查找音频信息
     */
    fun findAudioStream(streamId: String?): AudioStream?

    /**
     * 获取当前音频信息
     */
    fun findAudioStream(): List<AudioStream>?
}