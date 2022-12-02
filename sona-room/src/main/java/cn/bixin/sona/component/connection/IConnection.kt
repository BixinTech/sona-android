package cn.bixin.sona.component.connection;

import android.os.HandlerThread
import android.text.TextUtils
import cn.bixin.sona.api.ApiSubscriber
import cn.bixin.sona.api.SonaApi
import cn.bixin.sona.base.Sona
import cn.bixin.sona.base.pattern.Observable
import cn.bixin.sona.component.ComponentCallback
import cn.bixin.sona.component.ComponentMessage
import cn.bixin.sona.component.SonaComponent
import cn.bixin.sona.data.entity.SonaRoomData
import cn.bixin.sona.data.entity.UserData
import cn.bixin.sona.delegate.internal.PluginError
import cn.bixin.sona.driver.RoomDriver
import cn.bixin.sona.report.ReportCode
import cn.bixin.sona.util.SonaLogger
import com.alibaba.fastjson.JSONObject
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class IConnection(private val mRoomDriver: RoomDriver) : SonaComponent(), MessageCallback {

    companion object {
        private const val MAX_CACHE_SIZE = 600
        private const val CHECK_DURATION = 10 * 1000L
        private const val TIMEOUT_DURATION = 10 * 1000L
    }

    private var mDispatcher: MessageDispatcher? = null

    private var effective = false

    var connectionHandlerThread: HandlerThread? = null
    private var messageCacheHelper: MessageCacheHelper? = null
    private var uid: String? = ""
    private var isAck: Boolean = false // 标志长连消息是否需要ack
    private val compositeDisposable = CompositeDisposable()

    /**
     * 通过http接口发送消息
     */
    protected fun sendMessageByHttp(message: String, callback: ComponentCallback?) {
        val disposable: Disposable =
            SonaApi.sendMessage(message).subscribeWith(object : ApiSubscriber<Boolean?>() {
                override fun onSuccess(result: Boolean?) {
                    if (result == true) {
                        callback?.executeSuccess()
                    } else {
                        callback?.executeFailure(PluginError.SERVER_SEND_MSG_ERROR, "发送失败")
                    }
                    dispose()
                }

                override fun onFailure(e: Throwable?) {
                    callback?.executeFailure(
                        PluginError.SERVER_SEND_MSG_ERROR, if (e == null) "服务器错误" else e.message
                    )
                    dispose()
                }
            })
        compositeDisposable.add(disposable)
    }

    override fun provide(obj: Any) {
        mRoomDriver.provide(obj)
    }

    override fun <T : Any?> acquire(clz: Class<T>?): T? {
        return mRoomDriver.acquire(clz)
    }

    override fun <T : Any?> remove(clz: Class<T>?) {
        mRoomDriver.remove(clz)
    }

    override fun <T : Any?> observe(clz: Class<T>?): Observable<T> {
        return mRoomDriver.observe(clz)
    }

    override fun clear() {
        mRoomDriver.clear()
    }

    override fun dispatchMessage(roomMessage: ComponentMessage, message: Any?) {
        mRoomDriver.dispatchMessage(roomMessage, message);
    }

    override fun assembling() {
        effective = true
        val imInfo = acquire(SonaRoomData::class.java)?.imInfo
        val cacheSize = imInfo?.clientQueueSize ?: 0
        val expireTime = imInfo?.messageExpireTime ?: 0
        connectionHandlerThread = HandlerThread("SonaRoom-IConnection")
        connectionHandlerThread?.start()
        messageCacheHelper = MessageCacheHelper(
            if (cacheSize > 0) cacheSize else MAX_CACHE_SIZE,
            if (expireTime > 0) expireTime else CHECK_DURATION,
            if (expireTime > 0) expireTime else TIMEOUT_DURATION,
            connectionHandlerThread?.looper
        )
        uid = mRoomDriver.acquire(UserData::class.java)?.uid
        mDispatcher = createDispatcher()
    }

    override fun unAssembling() {
        effective = false
        messageCacheHelper?.release()
        mDispatcher?.quit()
        compositeDisposable.clear()
    }

    override fun setMessageAck(isAck: Boolean) {
        this.isAck = isAck
    }

    override fun onResponse(response: ConnectionMessage) {
        kotlin.runCatching {
            val message = JSONObject.parseObject(response.message)
            if (!filterMessage(message)) {
                dispatchMessage(
                    ComponentMessage.CONNECT_REV_MESSAGE, response
                )
            }
        }.onFailure {
            SonaLogger.log(content = "onResponse error ${it.message}")
            it.printStackTrace()
            dispatchMessage(ComponentMessage.CONNECT_REV_MESSAGE, response)
        }
    }

    private fun parseMessage(response: ConnectionMessage) {
        val message = JSONObject.parseObject(response.message)
        if (!filterMessage(message)) {
            dispatchMessage(
                ComponentMessage.CONNECT_REV_MESSAGE, response
            )
        }
    }

    /**
     * 消息缓存处理
     *
     * ack: 0 1
     */
    private fun filterMessage(message: JSONObject?): Boolean {
        message ?: return false
        val messageId = message.getString("messageId") // 命中ack
        if (isAck) {
            sendAckMessage(messageId)
        } // 消息幂等
        if (messageCacheHelper?.hasMessage(messageId) == true) {
            SonaLogger.log(
                content = "收到重复消息messageId = $messageId",
                code = ReportCode.RECEIVE_DUPLICATE_MESSAGE_CODE
            )
            return true
        } else {
            messageCacheHelper?.addMessage(messageId)
        }
        return false
    }

    protected fun onReceiveMessage(message: Any) {
        mDispatcher?.enqueue(message)
    }

    protected fun runUiThread(runnable: Runnable) {
        mRoomDriver.runUiThread(runnable)
    }

    /**
     * 发送消息
     */
    abstract fun sendMessage(
        message: String, callback: ComponentCallback?
    )

    /**
     * 发送消息
     */
    abstract fun sendMessage(
        message: String, isAck: Boolean, callback: ComponentCallback?
    )

    /**
     * 创建
     *
     * @return
     */
    protected abstract fun createDispatcher(): MessageDispatcher?

    /**
     * 获取聊天类型：聊天室、群聊
     *
     * @return
     */
    protected abstract fun getSessionType(): SessionTypeEnum

    private fun sendAckMessage(messageId: String?) {
        if (TextUtils.isEmpty(messageId)) return
        val roomId = mRoomDriver.acquire(SonaRoomData::class.java)?.roomId ?: ""
        val data = JSONObject()
        data["msgFormat"] = MessageItemEnum.ACK.value
        data["messageId"] = messageId
        data["roomId"] = roomId
        data["uid"] = Sona.getUid()
        sendMessage(data.toJSONString(), true, null)
    }
}
