package com.yupaopao.mercury.library.chatroom

import android.os.Build
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.yupaopao.mercury.library.Common
import com.yupaopao.mercury.library.common.CommandEnum
import com.yupaopao.mercury.library.common.Header
import com.yupaopao.mercury.library.tunnel.MercuryException
import com.yupaopao.mercury.library.tunnel.MercuryResponse
import com.yupaopao.mercury.library.tunnel.Tunnel

class ChatRoomMessageApi(val tunnel: Tunnel) {

    suspend fun enter(roomId: String, uid: String, identity: Int): MercuryResponse {
        val header = Header().apply {
            type = 2
            data = JSONObject().apply {
                put("room", roomId)
                put("uid", uid)
                put("identity", identity)
            }.toString().toByteArray()
        }
        return tunnel.request(
            CommandEnum.CHATROOM_JOIN.command,
            null,
            headers = arrayListOf(header)
        )
    }

    suspend fun exit(roomId: String): MercuryResponse {
        val header = Header().apply {
            type = 2
            data = JSONObject().apply {
                put("room", roomId)
            }.toString().toByteArray()
        }
        return tunnel.request(
            CommandEnum.CHATROOM_LEAVE.command,
            null,
            headers = arrayListOf(header)
        )
    }

    suspend fun sendMessage(roomId: String, ack: Int, body: String?,twoWay:Boolean): MercuryResponse {
        val header = Header().apply {
            type = 2
            data = JSONObject().apply {
                put("room", roomId)
                put("ack", ack)
            }.toString().toByteArray()
        }
        return tunnel.request(
            CommandEnum.CHATROOM_SEND.command,
            body,
            headers = arrayListOf(header),
            twoWay = twoWay
        )
    }


    suspend fun sendSignal(roomId: String, uid: String, signal: Int) {
        val header = Header().apply {
            type = 2
            data = JSONObject().apply {
                put("room", roomId)
                put("uid", uid)
                put("signal", signal)
            }.toString().toByteArray()
        }
        tunnel.request(
            CommandEnum.CHATROOM_SIGNAL.command,
            null,
            headers = arrayListOf(header),
            twoWay=false
        )
    }
}