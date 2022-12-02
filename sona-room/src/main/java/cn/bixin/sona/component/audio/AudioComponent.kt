package cn.bixin.sona.component.audio

import cn.bixin.sona.component.ComponentCallback
import cn.bixin.sona.component.SonaComponent
import cn.bixin.sona.data.AudioDeviceModeEnum


/**
 * 音频component
 *
 * @Author luokun
 * @Date 2020/3/25
 */

abstract class AudioComponent : SonaComponent() {

    /**
     * 开关麦克风
     *
     * @param on true 开麦克风 false 关闭麦克风
     * @param componentCallback
     */
    abstract fun switchMic(on: Boolean, componentCallback: ComponentCallback?)

    /**
     * 打开免提
     *
     * @param on true 打开免提  false 关闭免提
     * @param componentCallback
     */
    abstract fun switchHandsfree(on: Boolean, componentCallback: ComponentCallback?)

    /**
     * 静音
     *
     * @param streamId
     * @param on true 为静音，false为取消静音
     * @param componentCallback
     */
    abstract fun silent(streamId: String?, on: Boolean, componentCallback: ComponentCallback?)

    /**
     * 推流
     *
     * @param streamId
     * @param componentCallback
     */
    abstract fun pushStream(streamId: String?, componentCallback: ComponentCallback?)

    /**
     * 停止推流
     *
     * @param componentCallback
     */
    abstract fun stopPushStream(componentCallback: ComponentCallback?)

    /**
     * 拉单条流
     *
     * @param streamId
     * @param componentCallback
     */
    abstract fun pullStream(streamId: String?, componentCallback: ComponentCallback?)

    /**
     * 停止拉单条流
     *
     * @param streamId
     * @param componentCallback
     */
    abstract fun stopPullStream(streamId: String?, componentCallback: ComponentCallback?)

    /**
     * 拉所有流
     *
     * @param componentCallback
     */
    abstract fun pullStream(componentCallback: ComponentCallback?)

    /**
     * 停止拉所有流
     *
     * @param componentCallback
     */
    abstract fun stopPullStream(componentCallback: ComponentCallback?)

    /**
     * 获取背景音乐播放器
     *
     * @return
     */
    abstract fun getAudioPlayer(): IAudioPlayer?

    /**
     * 获取背景音乐播放器
     *
     * @return
     */
    abstract fun compareAndGet(index: IAudioPlayer.Index): IAudioPlayer?

    /**
     *
     * 当前房间内的流信息
     */
    abstract fun currentStream(): List<AudioStream>?

    /**
     * 切换拉流模式
     */
    abstract fun switchListen(realTime: Boolean, componentCallback: ComponentCallback?)

    /**
     * 设置音频设备模式
     */
    abstract fun setAudioDeviceMode(audioDeviceMode: AudioDeviceModeEnum)
}