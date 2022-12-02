package cn.bixin.sona.component.connection.mercury

import android.os.HandlerThread
import android.text.TextUtils
import cn.bixin.sona.component.connection.MessageBridge
import cn.bixin.sona.component.connection.MessageCallback
import cn.bixin.sona.component.connection.MessageDispatcher
import cn.bixin.sona.util.SonaLogger

/**
 * 自建长连消息分发
 *
 * @Author luokun
 * @Date 2020/4/16
 */
class MercuryMessageDispatcher(
    roomId: String?,
    private val messageBridge: MessageBridge<MCMessage>,
    var messageCallback: MessageCallback,
    handlerThread: HandlerThread?
) : MessageDispatcher(roomId, messageCallback, handlerThread) {

    override fun dispatch(message: Any, callback: (String) -> Unit) {
        if (message is MCMessage) {
            if (messageBridge.filterMessage(message)) {
                dispatchMessage(message, callback)
            }
        }
    }

    fun dispatchMessage(message: MCMessage, callback: (String) -> Unit) {
        messageCallback.setMessageAck(message.isAck)
        val jsonString = message.message
        SonaLogger.print("mercury message[]: $jsonString")
        if (!TextUtils.isEmpty(jsonString)) {
            callback.invoke(jsonString)
        }
    }
}