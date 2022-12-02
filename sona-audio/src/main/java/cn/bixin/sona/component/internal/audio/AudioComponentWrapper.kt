package cn.bixin.sona.component.internal.audio

import android.text.TextUtils
import cn.bixin.sona.base.pattern.Observable
import cn.bixin.sona.component.ComponentCallback
import cn.bixin.sona.component.ComponentMessage
import cn.bixin.sona.component.audio.AudioError
import cn.bixin.sona.data.entity.RoomInfo
import cn.bixin.sona.data.entity.SonaRoomData

/**
 * 音频包装类
 *
 * @Author luokun
 * @Date 2020/3/25
 */
abstract class AudioComponentWrapper(private val target: cn.bixin.sona.component.audio.AudioComponent) :
    cn.bixin.sona.component.audio.AudioComponent() {

    /**
     * 是否自动拉取新增流
     */
    private var mAutoPullStream = false

    private var mEffect: Boolean = false

    override fun dispatchMessage(roomMessage: ComponentMessage, message: Any?) {
        target.dispatchMessage(roomMessage, message)
    }

    override fun <T> acquire(clz: Class<T>?): T? {
        return target.acquire(clz)
    }

    override fun provide(obj: Any) {
        target.provide(obj)
    }

    override fun <T> remove(clz: Class<T>?) {
        target.remove(clz)
    }

    override fun <T> observe(clz: Class<T>?): Observable<T>? {
        return target.observe(clz)
    }

    override fun clear() {
        target.clear()
    }

    override fun assembling() {
        mEffect = true
        val success = init()
        if (!success) {
            dispatchMessage(ComponentMessage.AUDIO_INIT_FAIL, "音频初始化失败")
            return
        }
        val sonaRoomData = acquire(SonaRoomData::class.java)
        val audioRoomId = getAudioRoomId()
        if (sonaRoomData != null && !TextUtils.isEmpty(audioRoomId) && sonaRoomData.streamInfo != null) {
            enter(audioRoomId!!, sonaRoomData.streamInfo, object : ComponentCallback {
                override fun executeSuccess() {
                    if (mEffect) {
                        dispatchMessage(ComponentMessage.AUDIO_INIT_SUCCESS, "")
                    }
                }

                override fun executeFailure(code: Int, reason: String) {
                    if (mEffect) {
                        dispatchMessage(ComponentMessage.AUDIO_INIT_FAIL, "音频初始化失败")
                        dispatchMessage(ComponentMessage.ERROR_MSG, AudioError.LOGIN_ROOM_ERROR)
                    }
                }
            })
        } else {
            dispatchMessage(ComponentMessage.AUDIO_INIT_FAIL, "音频初始化失败")
        }
    }

    override fun unAssembling() {
        mEffect = false
    }

    /**
     * 获取音频房间的roomId
     *
     * @return
     */
    open fun getAudioRoomId(): String? {
        val sonaRoomData = acquire(SonaRoomData::class.java)
        if (sonaRoomData?.streamInfo != null) {
            val streamConfig = sonaRoomData.streamInfo
            if (streamConfig.streamRoomId != null) {
                return streamConfig.streamRoomId!![streamConfig.supplier]
            }
        }
        return null
    }

    protected fun setAutoPullStream(auto: Boolean) {
        mAutoPullStream = auto
    }

    fun isAutoPullStream(): Boolean {
        return mAutoPullStream
    }


    /**
     * 初始化
     *
     * @return
     */
    protected abstract fun init(): Boolean

    /**
     * 进入
     *
     * @param roomId
     * @param streamConfig
     * @param componentCallback
     */
    protected abstract fun enter(
        roomId: String,
        streamConfig: RoomInfo.StreamConfig,
        componentCallback: ComponentCallback?
    )

    /**
     * 恢复
     */
    protected abstract fun resume()

    /**
     * 暂停
     */
    protected abstract fun pause()

}