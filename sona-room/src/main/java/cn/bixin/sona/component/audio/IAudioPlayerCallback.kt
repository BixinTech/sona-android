package cn.bixin.sona.component.audio

/**
 *
 * @Author luokun
 * @Date 2020-03-05
 */
interface IAudioPlayerCallback {
    /**
     * 开始播放
     */
    fun onStart()

    /**
     * 播放暂停
     */
    fun onPause()

    /**
     * 恢复播放
     */
    fun onResume()

    /**
     * 播放停止
     */
    fun onStop()

    /**
     * 播放错误
     */
    fun onError(code: Int)

    /**
     * 播放结束
     */
    fun onComplete()

    /**
     * 进度回调
     */
    fun onProcess(process: Long)

    /**
     * 缓冲开始
     */
    fun onBufferBegin()

    /**
     * 缓冲结束
     */
    fun onBufferEnd()
}