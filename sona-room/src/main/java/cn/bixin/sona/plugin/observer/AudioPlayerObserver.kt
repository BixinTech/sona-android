package cn.bixin.sona.plugin.observer

import cn.bixin.sona.component.audio.IAudioPlayerCallback

/**
 *
 * @Author luokun
 * @Date 2020-03-05
 */

interface AudioPlayerObserver : PluginObserver, IAudioPlayerCallback {

    /**
     * 开始播放
     */
    override fun onStart()

    /**
     * 播放暂停
     */
    override fun onPause()

    /**
     * 恢复播放
     */
    override fun onResume()

    /**
     * 播放停止
     */
    override fun onStop()

    /**
     * 播放错误
     */
    override fun onError(code: Int)

    /**
     * 播放结束
     */
    override fun onComplete()

    /**
     * 进度回调
     */
    override fun onProcess(process: Long)

    /**
     * 缓冲开始
     */
    override fun onBufferBegin()

    /**
     * 缓冲结束
     */
    override fun onBufferEnd()
}