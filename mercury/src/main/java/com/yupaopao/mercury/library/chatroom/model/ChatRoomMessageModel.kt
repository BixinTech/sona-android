package com.yupaopao.mercury.library.chatroom.model;

import java.io.Serializable

data class ChatRoomMessageModel(
    val body: String?,
    val roomId: String?,
    val isAck: Boolean
):Serializable


data class ChatRoomSignalModel(
    val body: String?,
    val roomId: String?,
    val uid: String?,
    val signal: Int
):Serializable
