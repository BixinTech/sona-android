//package com.yupaopao.mercury.library.chatroom
//
//import com.alibaba.fastjson.JSON
//import com.alibaba.fastjson.JSONObject
//import com.yupaopao.mercury.library.chatroom.model.ChatRoomMessageModel
//import com.yupaopao.mercury.library.common.AccessMessage
//import com.yupaopao.mercury.library.core.Mercury
//
//object ChatRoomAccessMessageBuilder {
//
//    fun enterChatRoom(roomId: String, uid: String, appId: String, ext: String): AccessMessage {
//        val message = AccessMessage()
//        message.flag = Constant.MESSAGE_FLAG
//        message.command = CommandEnum.CLIENT_CHATROOM_JOIN.command
//        message.version = Constant.PROTOCOL_VERSION
//
//        message.extendHeaders = arrayListOf(
//            Header(NextHeaderEnum.AUTH, JSON.toJSONString(JSONObject().apply {
//                put("token", Mercury.currentUserInfo?.accessToke?:"")
//            }))
//        )
//        val body = JSON.toJSONString(JSONObject().apply {
//            put("roomid", roomId)
//            put("uid", uid)
//            put("appid", appId)
//            put("ext", ext)
//        })
//        message.body = body.toByteArray()
//
//        return message
//    }
//
//    fun exitChatRoom(roomId: String): AccessMessage {
//        val message = AccessMessage()
//        message.flag = Constant.MESSAGE_FLAG
//        message.command = CommandEnum.CLIENT_CHATROOM_LEAVE.command
//        message.version = Constant.PROTOCOL_VERSION
//
//        message.extendHeaders = arrayListOf()
//        val body = JSON.toJSONString(JSONObject().apply {
//            put("roomid", roomId)
//        })
//        message.body = body.toByteArray()
//
//        return message
//    }
//
//    fun roomMessage(model: ChatRoomMessageModel): AccessMessage {
//        val message = AccessMessage()
//        message.flag = Constant.MESSAGE_FLAG
//        message.command = CommandEnum.CLIENT_CHATROOM_SEND.command
//        message.version = Constant.PROTOCOL_VERSION
//
//        message.extendHeaders = arrayListOf(
//            Header(NextHeaderEnum.CHATROOM, JSON.toJSONString(JSONObject().apply {
//                put("roomid", model.roomId)
//                put("ack", if (model.isAck) 1 else 0)
//            }))
//        )
//        message.body = model.body
//
//        ChatRoom.tunnel?.let {
//            VersionPolicy.zipCheck(message, it)
//        }
//
//        return message
//    }
//
//    fun notificationMessage(model: ChatRoomMessageModel): AccessMessage {
//        val message = AccessMessage()
//        message.flag = Constant.MESSAGE_FLAG
//        message.command = CommandEnum.CLIENT_CHATROOM_SEND_SIGNAL.command
//        message.version = Constant.PROTOCOL_VERSION
//
//        message.extendHeaders = arrayListOf(
//            Header(NextHeaderEnum.CHATROOM, JSON.toJSONString(JSONObject().apply {
//                put("roomid", model.roomId)
//                put("ack", if (model.isAck) 1 else 0)
//            }))
//        )
//        message.body = model.body
//
//        ChatRoom.tunnel?.let {
//            VersionPolicy.zipCheck(message, it)
//        }
//
//        return message
//    }
//}