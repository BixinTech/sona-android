package com.yupaopao.mercury.library.chatroom

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.yupaopao.mercury.library.Common
import com.yupaopao.mercury.library.chatroom.model.ChatRoomMessageModel
import com.yupaopao.mercury.library.chatroom.model.ChatRoomSignalModel
import com.yupaopao.mercury.library.common.CommandEnum
import com.yupaopao.mercury.library.common.HeaderEnum
import com.yupaopao.mercury.library.core.log.MercuryLogger
import com.yupaopao.mercury.library.tunnel.*
import com.yupaopao.mercury.library.tunnel.model.MercuryMessage
import com.yupaopao.mercury.library.tunnel.model.TunnelStatus
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class ChatRoomInterface(val uid: String) : MessageController.MessageListener {

    companion object {
        val instance = HashMap<String, ChatRoomInterface>()
        fun register(roomId: String, chatRoomInterface: ChatRoomInterface) {
            instance[roomId] = chatRoomInterface
        }

        fun unRegister(roomId: String) {
            val obj = instance.remove(roomId)
            if (obj is ChatRoomInterface) {
                obj.onDestroy()
            }
        }

        fun get(roomId: String): ChatRoomInterface? {
            return instance[roomId]
        }
    }

    var onNetworkStatus: ((tunnelStatus: TunnelStatus) -> Unit)? = null
    var onSignal: ((model: ChatRoomSignalModel) -> Unit)? = null
    var onRoomMessage: ((model: ChatRoomMessageModel) -> Unit)? = null
    var tunnel: Tunnel? = null
    var isStarted = AtomicBoolean(false)
    var msgApi: ChatRoomMessageApi? = null
    var tunnelStatus: TunnelStatus = TunnelStatus.IDLE
    suspend fun connect(): MercuryResponse {
        if (isStarted.get()) {
            return MercuryResponse(0, "")
        }
        val result = isStarted.compareAndSet(false, true)
        if (result) {
            tunnel = Tunnel(Common.TUNNEL_CHAT_ROOM, uid)
            tunnel?.networkStatusCallback = {
                onNetworkStatus?.invoke(it)
                tunnelStatus = it
            }
            tunnel?.messageListener = this
            tunnel?.receiveMessageCallback = {

            }
            tunnel?.logCallback = {
                MercuryLogger.log(this, Common.TUNNEL_CHAT_ROOM, it)
            }
            msgApi = ChatRoomMessageApi(tunnel!!)
            return tunnel!!.connect()
        }
        return MercuryResponse(-1, "error state")
    }

    /**
     * identity 0 : 游客 ,  1: 普通用户
     */
    fun enter(
        roomId: String,
        identity: Int,
        callback: (code: Int, message: String) -> Unit
    ) {
        CoroutinePool.scope.launch {
            if (tunnelStatus != TunnelStatus.LOGIN) {
                val response = connect()
                if (response.code != 0) {
                    callback.invoke(response.code, response.message ?: "")
                    return@launch
                }
            }
            msgApi?.enter(roomId, uid, identity)?.let {
                callback.invoke(it.code, it.message ?: "")
            }
        }
    }

    fun exit(roomId: String, callback: (code: Int, message: String) -> Unit) {
        CoroutinePool.scope.launch {
            msgApi?.exit(roomId)?.let {
                callback.invoke(it.code, it.message ?: "")
            }
        }
    }

    fun onDestroy() {
        tunnel?.release()
    }


    fun sendMessage(model: ChatRoomMessageModel, callback: ((Int) -> Unit)?) {
        CoroutinePool.scope.launch {
            msgApi?.sendMessage(
                model.roomId ?: "", if (model.isAck) 1 else {
                    0
                }, model.body, callback != null
            )?.let {
                callback?.invoke(it.code)
            }
        }
    }

    fun appState(){
        CoroutinePool.scope.launch {
            tunnel?.appState()
        }
    }

    override fun onRequest(
        request: MercuryMessage,
        handler: MessageController.ServerRequestHandler?
    ) {
        //服务端需要响应的消息
        if (handler == null) {
            if (request.cmd == CommandEnum.CHATROOM_SEND.command) {//房间消息
                request.headers?.forEach {
                    if (it.type == HeaderEnum.CHATROOM.type) {
                        val jsonObject =
                            JSON.parse(String(it.data)) as JSONObject
                        val roomId = jsonObject.getString("room")
                        val ack = jsonObject.getInteger("ack") ?: 0
                        onRoomMessage?.invoke(
                            ChatRoomMessageModel(
                                request.requestData ?: "",
                                roomId,
                                ack == 1
                            )
                        )
                    }
                }

            } else if (request.cmd == CommandEnum.CHATROOM_SIGNAL.command) {//房间通知
                request.headers?.forEach {
                    if (it.type == HeaderEnum.CHATROOM.type) {
                        val jsonObject =
                            JSON.parse(String(it.data)) as JSONObject
                        val roomId = jsonObject.getString("room")
                        val signal = jsonObject.getIntValue("signal")
                        val uid = jsonObject.getString("uid")
                        onSignal?.invoke(ChatRoomSignalModel(request.requestData, roomId, uid, signal))
                    }
                }
            }
        }
    }

}