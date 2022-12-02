package cn.bixin.sona.component.audio

/**
 *
 * @Author luokun
 * @Date 2020-02-10
 */
interface IAudioPlayer {

    enum class Status {
        IDLE, PLAY, PAUSE
    }

    enum class Index {
        NONE, ONE, TWO, THREE
    }

    enum class PlayPattern {
        LOCAL, // 本地播放模式，不会将音频混入推流中，只有调用端可以听到播放的声音
        REMOTE // 推流播放模式，会将音频混流推流中，调用端和拉流端都可以听到播放的声音
    }

    /**
     * 开始播放
     *
     * @param url 播放地址
     * @param pattern 播放模式
     */
    fun play(url: String, pattern: PlayPattern)

    /**
     * 开始播放
     *
     * @param url 播放地址
     * @param startPosition 开始播放位置
     * @param pattern 播放模式
     */
    fun play(url: String, startPosition: Long = 0, pattern: PlayPattern)

    /**
     * 开始播放，且重复播放
     *
     * @param url 播放地址
     * @param pattern 播放模式
     */
    fun playRepeat(url: String, pattern: PlayPattern)

    /**
     * 开始播放，且重复播放
     *
     * @param url 播放地址
     * @param startPosition 开始播放位置
     * @param pattern 播放模式
     */
    fun playRepeat(url: String, startPosition: Long = 0, pattern: PlayPattern)

    /**
     * 恢复
     */
    fun resume()

    /**
     * 暂停
     */
    fun pause()

    /**
     * 停止
     */
    fun stop()

    /**
     * 拖动进度
     *
     * @param position 0~duration
     */
    fun seek(position: Long)

    /**
     * 设置音量
     *
     * @param volume 0~100
     */
    fun setVolume(volume: Int)

    /**
     * 获取音量值
     */
    fun getVolume(): Int

    /**
     * 音量最大值
     */
    fun getMaxVolume(): Int

    /**
     * 获取总时长
     *
     * @return
     */
    fun getDuration(): Long

    /**
     * 获取当前播放的位置
     */
    fun getCurrentPosition(): Long

    /**
     * 获取播放状态
     *
     * @return
     */
    fun getStatus(): Status

    /**
     * 设置回调
     *
     * @param
     */
    fun setCallback(callback: IAudioPlayerCallback?)

}