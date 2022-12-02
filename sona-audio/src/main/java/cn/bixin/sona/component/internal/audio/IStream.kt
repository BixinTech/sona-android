package cn.bixin.sona.component.internal.audio

/**
 *
 * @Author luokun
 * @Date 2020-03-03
 */
interface IStream {

    /**
     * 开始推流
     *
     * @return
     */
    fun startPublishStream(streamId: String): Boolean

    /**
     * 停止推流
     *
     * @return
     */
    fun stopPublishStream(): Boolean

    /**
     * 开始拉流
     *
     * @return
     */
    fun startPullStream(): Boolean

    /**
     * 停止拉流
     *
     * @return
     */
    fun stopPullStream(): Boolean

    /**
     * 拉单条流
     *
     * @param streamId
     * @return
     */

    fun startPullStream(streamId: String?): Boolean

    /**
     * 停止拉单条流
     *
     * @param streamId
     * @return
     */
    fun stopPullStream(streamId: String?): Boolean

    /**
     * 静音流
     *
     * @param streamId
     * @param on true 静音  false 取消静音
     * @return
     */
    fun silentStream(streamId: String?, on: Boolean): Boolean

    /**
     * 类型
     *
     * @return
     */
    fun sessionType(): AudioSession
}