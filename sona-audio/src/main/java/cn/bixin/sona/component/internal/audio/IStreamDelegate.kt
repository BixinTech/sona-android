package cn.bixin.sona.component.internal.audio

/**
 *
 * @Author luokun
 * @Date 2020-03-04
 */
interface IStreamDelegate {

    /**
     * 拉流
     *
     * @param streamId 流id
     * @param on true 拉流 false 停止拉流
     */
    fun play(streamId: String = "", on: Boolean): Boolean

    /**
     * 推流
     *
     * @param streamId 流id
     * @param on true 推流 false 停止推流
     */
    fun speak(streamId: String = "", on: Boolean): Boolean

    /**
     * 静音流
     */
    fun silent(streamId: String, on: Boolean): Boolean

}