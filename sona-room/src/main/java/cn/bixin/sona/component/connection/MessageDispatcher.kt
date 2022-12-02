package cn.bixin.sona.component.connection

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.text.TextUtils
import cn.bixin.sona.util.SonaLogger
import com.alibaba.fastjson.JSONObject

/**
 * 消息分发器
 * 消息插入、消息解析、消息分发
 * @Author luokun
 * @Date 2020/4/15
 */
abstract class MessageDispatcher(
    val roomId: String?,
    private val messageCallback: MessageCallback,
    handlerThread: HandlerThread?
) : Handler.Callback {

    companion object {
        /**
         * 消息类型：会话消息、系统消息
         */
        private const val SESSION_MESSAGE = 0
    }

    private var mConnectionHandler: Handler? = null

    init {
        handlerThread?.let {
            mConnectionHandler = Handler(it.looper, this)
        }
    }

    private val dispatcher: (String) -> Unit = { message ->
        if (!TextUtils.isEmpty(message)) {
            kotlin.runCatching {
                handleMessage(message, MessageGroupEnum.CUSTOM)
            }.onFailure {
                it.printStackTrace()
                SonaLogger.log(
                    content = "dispatcher message fail e = ${it.message}"
                )
            }
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.arg1) {
            SESSION_MESSAGE -> {
                dispatch(msg.obj, dispatcher)
                return true
            }
        }
        return false
    }

    fun enqueue(message: Any) {
        mConnectionHandler?.let {
            Message.obtain(it, SESSION_MESSAGE, message).sendToTarget()
        }
    }

    fun quit() {
        mConnectionHandler?.removeCallbacksAndMessages(null)
    }

    private fun handleMessage(message: String?, messageType: MessageGroupEnum) {
        message ?: return
        try {
            val data = JSONObject.parseObject(message)
            val type = data.getIntValue("msgType")
            when (val connectionMessageTypeEnum = MessageItemEnum.messageType(type)) {
                MessageItemEnum.TXT,
                MessageItemEnum.IMAGE,
                MessageItemEnum.EMOJI,
                MessageItemEnum.AUDIO,
                MessageItemEnum.VIDEO ->
                    dispatchMessage(MessageGroupEnum.BASIC, message, connectionMessageTypeEnum)
                MessageItemEnum.ENTER_ROOM,
                MessageItemEnum.LEAVE_ROOM,
                MessageItemEnum.CLOSE_ROOM,
                MessageItemEnum.ADMIN_SET_CANCEL,
                MessageItemEnum.BLACK_SET_CANCEL,
                MessageItemEnum.MUTE_SET_CANCEL,
                MessageItemEnum.KICK,
                MessageItemEnum.STREAM_SILENT_SET_CANCEL,
                MessageItemEnum.AUDIO_HOT_SWITCH ->
                    dispatchMessage(MessageGroupEnum.ADMIN, message, connectionMessageTypeEnum)
                else -> dispatchMessage(messageType, message, connectionMessageTypeEnum)
            }
        } catch (e: Exception) {
            SonaLogger.print("parse message fail", e)
        }
    }

    private fun dispatchMessage(
        messageGroupEnum: MessageGroupEnum,
        data: String?,
        messageItemEnum: MessageItemEnum
    ) {
        data?.let {
            messageCallback.onResponse(
                ConnectionMessage(
                    messageGroupEnum,
                    it,
                    messageItemEnum
                )
            )
        }
    }

    abstract fun dispatch(message: Any, callback: (String) -> Unit)
}