package cn.bixin.sona.component.connection.mercury

import android.text.TextUtils
import cn.bixin.sona.component.ComponentCallback
import cn.bixin.sona.component.ComponentMessage
import cn.bixin.sona.component.connection.ConnectionError
import cn.bixin.sona.component.connection.SessionTypeEnum
import cn.bixin.sona.data.entity.SonaRoomData
import cn.bixin.sona.data.entity.UserData
import cn.bixin.sona.driver.RoomDriver
import cn.bixin.sona.plugin.entity.PluginEntity
import cn.bixin.sona.report.ReportCode
import cn.bixin.sona.util.SonaLogger
import com.yupaopao.mercury.library.chatroom.ChatRoomInterface
import com.yupaopao.mercury.library.chatroom.model.ChatRoomMessageModel
import com.yupaopao.mercury.library.tunnel.model.TunnelStatus

class ChatRoomMercuryConnection(roomDriver: RoomDriver) : MercuryConnection(roomDriver) {

    private var chatRoomInterface: ChatRoomInterface? = null

    @Volatile
    private var connectStatus = false

    private var identity = 1 // identity 0 : 游客 , 1: 普通用户

    override fun assembling() {
        super.assembling()
        if (!TextUtils.isEmpty((acquire(SonaRoomData::class.java)?.guestUid))) {
            identity = 0
            chatRoomInterface = ChatRoomInterface(acquire(SonaRoomData::class.java)?.guestUid ?: "")
        } else {
            identity = 1
            chatRoomInterface = ChatRoomInterface(acquire(UserData::class.java)?.uid ?: "")
        }
        chatRoomInterface?.onRoomMessage = { messageModel ->
            onReceiveMessage(
                MCMessage(
                    messageModel.roomId ?: "", messageModel.body ?: "", messageModel.isAck
                )
            )
        }
        chatRoomInterface?.onSignal = { messageModel ->
            // 0: 踢人,  1: 关闭房间
            if (messageModel.signal == 0) {
                val uid = acquire(UserData::class.java)?.uid
                val roomId = acquire(SonaRoomData::class.java)?.roomId
                val pluginEntity = PluginEntity(uid, roomId)
                dispatchMessage(ComponentMessage.USER_KICK, pluginEntity)
                SonaLogger.log(
                    content = "长连收到被踢",
                    reason = messageModel.body ?: "",
                    code = ReportCode.MERCURY_USER_KICK_CODE
                )
            }
        }

        chatRoomInterface?.onNetworkStatus = { tunnelStatus ->
            when (tunnelStatus) {
                TunnelStatus.DISCONNECT -> {
                    dispatchMessage(ComponentMessage.CONNECT_DISCONNECT, null)
                    SonaLogger.log(
                        content = "长连断开",
                        reason = "Tunnel DISCONNECT",
                        code = ReportCode.MERCURY_DISCONNECT_CODE
                    )
                }
                TunnelStatus.LOGIN -> {
                    SonaLogger.log(
                        content = "长连login",
                        code = ReportCode.MERCURY_LOGIN
                    )
                    runUiThread(Runnable {
                        // 需要手动重连
                        if (connectStatus) {
                            reconnect()
                        }
                    })
                }
            }
        }

        if (TextUtils.isEmpty(getImRoomId())) {
            SonaLogger.log(
                content = "长连进入聊天室失败",
                reason = "长连imRoomId不存在",
                code = ReportCode.MERCURY_ENTER_CHATROOM_FAIL_CODE
            )
            dispatchMessage(ComponentMessage.CONNECT_INIT_FAIL, "imRoomId不存在")
            return
        }
        ChatRoomInterface.register(getImRoomId(), chatRoomInterface!!)
        chatRoomInterface?.enter(getImRoomId(), identity) { code, message ->
            if (code == 0) {
                runUiThread(Runnable {
                    connectStatus = true
                    SonaLogger.log(
                        content = "长连进入聊天室成功",
                        code = ReportCode.MERCURY_ENTER_CHATROOM_SUCCESS_CODE
                    )
                    dispatchMessage(ComponentMessage.CONNECT_INIT_SUCCESS, null)
                })
            } else {
                runUiThread(Runnable {
                    connectStatus = true
                    var reason = "长链进入房间其他错误"
                    if (code == -1) {
                        reason = "长链进房间超时"
                    }
                    SonaLogger.log(
                        content = "长连进入聊天室失败",
                        reason = "$reason , code = ${code}, message = $message",
                        sdkCode = code,
                        code = ReportCode.MERCURY_ENTER_CHATROOM_FAIL_CODE
                    )
                    dispatchMessage(ComponentMessage.CONNECT_INIT_FAIL, message)
                    dispatchMessage(ComponentMessage.ERROR_MSG, ConnectionError.LOGIN_ROOM_ERROR)
                })
            }
        }
        SonaLogger.log(
            content = "长连开始进入聊天室",
            code = ReportCode.MERCURY_ENTER_CHATROOM_START_CODE
        )
    }

    override fun unAssembling() {
        super.unAssembling()
        chatRoomInterface?.exit(getImRoomId()) { code, message ->
        }
        ChatRoomInterface.unRegister(getImRoomId())
        chatRoomInterface?.onRoomMessage = null
        chatRoomInterface?.onNetworkStatus = null
        chatRoomInterface?.onSignal = null
        chatRoomInterface = null
        connectStatus = false
    }

    override fun sendMessage(message: String, callback: ComponentCallback?) {
        val mercury = acquire(SonaRoomData::class.java)?.imInfo?.imSendType == 2
        if (mercury) {
            sendMessage(message, false, callback)
        } else {
            sendMessageByHttp(message, callback)
        }
    }

    override fun sendMessage(message: String, isAck: Boolean, callback: ComponentCallback?) {
        val messageModel = ChatRoomMessageModel(message, getImRoomId(), isAck)
        chatRoomInterface?.sendMessage(messageModel) { code ->
            SonaLogger.print("sendMessage code = $code")
            if (code == 0) {
                callback?.executeSuccess()
            }
        }
    }

    override fun getSessionType(): SessionTypeEnum {
        return SessionTypeEnum.CHATROOM
    }

    private fun reconnect() {
        chatRoomInterface?.let {
            ChatRoomInterface.register(getImRoomId(), it)
            chatRoomInterface?.enter(getImRoomId(), identity) { code, message ->
                runUiThread(Runnable {
                    if (code == 0) {
                        connectStatus = true
                        dispatchMessage(ComponentMessage.CONNECT_INIT_SUCCESS, null)
                        dispatchMessage(ComponentMessage.CONNECT_RECONNECT, null)
                        SonaLogger.log(
                            content = "长连重连成功",
                            code = ReportCode.MERCURY_RECONNECT_CODE
                        )
                    } else {
                        connectStatus = true
                        reconnect()
                        SonaLogger.log(
                            content = "长连重连失败，继续重试",
                            code = ReportCode.MERCURY_RECONNECT_FAIL_CODE
                        )
                    }
                })
            }
            SonaLogger.log(
                content = "长连开始重连",
                code = ReportCode.MERCURY_RECONNECT_START_CODE
            )
        }
    }
}
