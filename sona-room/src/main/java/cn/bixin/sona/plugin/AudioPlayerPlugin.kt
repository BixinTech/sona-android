package cn.bixin.sona.plugin

import cn.bixin.sona.component.audio.IAudioPlayer
import cn.bixin.sona.plugin.anotation.SonaPluginAnnotation
import cn.bixin.sona.plugin.config.AudioPlayerConfig
import cn.bixin.sona.plugin.entity.PluginEnum
import cn.bixin.sona.plugin.observer.AudioPlayerObserver

/**
 *
 * 音频背景音乐插件
 * 提供操作背景音乐的能力
 *
 * @Author luokun
 * @Date 2020-03-05
 */
@SonaPluginAnnotation(PluginEnum.APLAYER)
interface AudioPlayerPlugin : SonaPlugin<AudioPlayerConfig, AudioPlayerObserver>, IAudioPlayer {

    /**
     * 开始播放
     *
     * @param url 播放地址
     * @param startPosition 开始播放位置
     * @param pattern 播放模式
     */
    override fun play(url: String, startPosition: Long, pattern: IAudioPlayer.PlayPattern)

    /**
     * 开始播放
     *
     * @param url 播放地址
     * @param startPosition 开始播放位置
     * @param pattern 播放模式
     */
    override fun playRepeat(url: String, startPosition: Long, pattern: IAudioPlayer.PlayPattern)

    /**
     * 恢复
     */
    override fun resume()

    /**
     * 暂停
     */
    override fun pause()

    /**
     * 停止
     */
    override fun stop()

    /**
     * 拖动进度
     *
     * @param position 0~duration
     */
    override fun seek(position: Long)

    /**
     * 设置音量
     *
     * @param volume 0~100
     */
    override fun setVolume(volume: Int)

    /**
     * 获取总时长
     *
     * @return
     */
    override fun getDuration(): Long

    /**
     * 获取当前播放的位置
     */
    override fun getCurrentPosition(): Long

    /**
     * 获取播放状态
     *
     * @return
     */
    override fun getStatus(): IAudioPlayer.Status

}