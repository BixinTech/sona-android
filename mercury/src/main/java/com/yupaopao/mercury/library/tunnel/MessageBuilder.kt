package com.yupaopao.mercury.library.tunnel

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.yupaopao.mercury.library.common.AccessMessage
import com.yupaopao.mercury.library.common.Header
import com.yupaopao.mercury.library.tunnel.util.MessageId
import java.util.concurrent.atomic.AtomicInteger

object MessageBuilder {


    private fun createAccessMessage(): AccessMessage {
        return AccessMessage().apply {
            version = 0
        }
    }

    fun request(cmd: Int, data: String?=null, header: List<Header>?=null,twoWay:Boolean): AccessMessage {
        val message = createAccessMessage()
        message.isReq = true
        message.isTwoWay = twoWay
        message.isHeartbeat = false
        message.id = MessageId.get()
        message.cmd = cmd
        message.headers = header
        message.body = data?.toByteArray()
        return message
    }

    fun response(request: AccessMessage,responseCode:Int, responseData: String?): AccessMessage {
        val message = createAccessMessage()
        message.isReq = false
        message.isTwoWay = false
        message.isHeartbeat = false
        message.id = request.id
        message.cmd = request.cmd
        message.body = JSONObject().apply {
            put("c",responseCode)
            put("d",responseData)
        }.toString().toByteArray()
        return message
    }



    fun ping():AccessMessage{
        val message = createAccessMessage()
        message.isReq = true
        message.isTwoWay = true
        message.isHeartbeat = true
        message.id = MessageId.get()
        return message
    }
    fun rePing():AccessMessage{
        val message = createAccessMessage()
        message.isReq = false
        message.isTwoWay = false
        message.isHeartbeat = true
        return message
    }
}